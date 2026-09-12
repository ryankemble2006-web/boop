using Boop.Win7ify.Core;
using System.Reflection;

namespace Boop.Win7ify;

internal sealed class MainForm : Form
{
    private static readonly Color Cyan = Color.FromArgb(0, 224, 255);
    private static readonly Color PanelBlack = Color.FromArgb(16, 16, 18);

    private readonly Win7ifyService _service;
    private readonly BackupService _backup;
    private readonly List<ToggleBinding> _toggles = new();
    private readonly TextBox _log;
    private readonly Button _applyButton;
    private readonly Button _restoreButton;
    private readonly Image? _eyesImage;

    public MainForm(Win7ifyService service, BackupService backup)
    {
        _service = service;
        _backup = backup;

        Text = "BOOP // Win7ify";
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(780, 720);
        Size = new Size(900, 900);
        BackColor = Color.Black;
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 11f);
        AutoScaleMode = AutoScaleMode.Dpi;

        var root = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            FlowDirection = FlowDirection.TopDown,
            WrapContents = false,
            AutoScroll = true,
            Padding = new Padding(26),
            BackColor = Color.Black
        };

        _eyesImage = LoadEyes();
        if (_eyesImage is not null)
        {
            root.Controls.Add(new PictureBox
            {
                Image = _eyesImage,
                SizeMode = PictureBoxSizeMode.Zoom,
                Size = new Size(360, 180),
                Margin = new Padding(0, 0, 0, 4)
            });
        }

        root.Controls.Add(MakeLabel("BOOP // WIN7IFY", 28f, FontStyle.Bold, Cyan));
        root.Controls.Add(MakeLabel("Put the familiar furniture back without replacing Windows.", 12f, FontStyle.Regular, Color.White));
        root.Controls.Add(MakeLabel(
            $"{Environment.OSVersion.VersionString}\r\nOriginal settings are saved before Win7ify touches them.",
            10f,
            FontStyle.Regular,
            Color.Silver));

        var options = new FlowLayoutPanel
        {
            FlowDirection = FlowDirection.TopDown,
            WrapContents = false,
            AutoSize = true,
            BackColor = PanelBlack,
            Padding = new Padding(16),
            Margin = new Padding(0, 18, 0, 14),
            MinimumSize = new Size(790, 0)
        };

        AddToggle(options, "Start and taskbar buttons on the left", "taskbar.left");
        AddToggle(options, "Ask Windows for separate taskbar buttons with labels", "taskbar.nevercombine");
        AddToggle(options, "Hide Search from the taskbar", "taskbar.search.hide");
        AddToggle(options, "Hide Task View", "taskbar.taskview.hide");
        AddToggle(options, "Hide Widgets", "taskbar.widgets.hide");
        AddToggle(options, "Restore the far-right Show Desktop corner", "taskbar.showdesktop");
        AddToggle(options, "Open File Explorer to This PC", "explorer.thispc");
        AddToggle(options, "Show the classic desktop icons",
            "desktop.computer", "desktop.userfiles", "desktop.network", "desktop.controlpanel", "desktop.recyclebin");
        AddToggle(options, "EXPERIMENTAL: ask for the old full right-click menu", false, "context.classic.experimental");
        root.Controls.Add(options);

        _applyButton = MakeButton("MAKE WINDOWS 7-ISH", true);
        _restoreButton = MakeButton("PUT WINDOWS 11 BACK", false);
        _applyButton.Click += (_, _) => ApplySelected();
        _restoreButton.Click += (_, _) => RestoreWindows();

        var buttons = new FlowLayoutPanel
        {
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            WrapContents = true,
            BackColor = Color.Black,
            Margin = new Padding(0, 0, 0, 14)
        };
        buttons.Controls.Add(_applyButton);
        buttons.Controls.Add(_restoreButton);
        root.Controls.Add(buttons);

        root.Controls.Add(MakeLabel(
            "Windows 11 updates can ignore some old shell settings. Win7ify reports what it actually writes, not what Windows merely promises to draw.",
            9.5f,
            FontStyle.Regular,
            Color.Silver));

        _log = new TextBox
        {
            Multiline = true,
            ReadOnly = true,
            ScrollBars = ScrollBars.Vertical,
            Width = 790,
            Height = 150,
            BackColor = PanelBlack,
            ForeColor = Cyan,
            BorderStyle = BorderStyle.FixedSingle,
            Font = new Font("Consolas", 9.5f),
            Margin = new Padding(0, 12, 0, 0)
        };
        root.Controls.Add(_log);
        root.Controls.Add(MakeLabel("No system DLL patches. No hidden installer. One big undo button.", 9f, FontStyle.Regular, Color.Gray));

        Controls.Add(root);
        FormClosed += (_, _) => _eyesImage?.Dispose();

        WriteLog(_backup.HasBackup
            ? "A Win7ify backup already exists. The restore button will use that original baseline."
            : "Ready. Nothing has been changed by this copy of Win7ify yet.");
    }

    private void AddToggle(Control parent, string text, params string[] ids) =>
        AddToggle(parent, text, ids.All(id => PresetCatalog.Windows7ish.Contains(id)), ids);

    private void AddToggle(Control parent, string text, bool defaultChecked, params string[] ids)
    {
        var box = new CheckBox
        {
            Text = text,
            Checked = defaultChecked,
            AutoSize = true,
            ForeColor = text.StartsWith("EXPERIMENTAL", StringComparison.Ordinal) ? Color.Gold : Color.White,
            Font = new Font("Segoe UI", 11f, FontStyle.Bold),
            Margin = new Padding(4, 6, 4, 6),
            Padding = new Padding(4)
        };
        _toggles.Add(new ToggleBinding(box, ids));
        parent.Controls.Add(box);
    }

    private void ApplySelected()
    {
        var ids = _toggles
            .Where(binding => binding.Box.Checked)
            .SelectMany(binding => binding.TweakIds)
            .Distinct(StringComparer.OrdinalIgnoreCase)
            .ToArray();

        if (ids.Length == 0)
        {
            WriteLog("Nothing selected, so BOOP left Windows alone.");
            return;
        }

        SetBusy(true);
        try
        {
            var applied = _service.Apply(ids);
            WriteLog($"Saved the original values at {_backup.BackupPath}");
            foreach (var tweak in applied)
                WriteLog($"Wrote: {tweak.Label}");

            WriteLog("Restarting Explorer so Windows can reread the shell settings...");
            ExplorerShell.Restart();
            WriteLog("Explorer restarted. Any ignored legacy setting is a Windows limitation, not reported as success here.");
        }
        catch (Exception ex)
        {
            WriteLog($"Stopped safely: {ex.Message}");
            MessageBox.Show(ex.Message, "BOOP Win7ify", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
        finally
        {
            SetBusy(false);
        }
    }

    private void RestoreWindows()
    {
        if (!_backup.HasBackup)
        {
            WriteLog("There is no Win7ify backup to restore yet.");
            return;
        }

        SetBusy(true);
        try
        {
            var count = _service.RestoreAll();
            WriteLog($"Restored {count} original registry values.");
            WriteLog("Restarting Explorer...");
            ExplorerShell.Restart();
            WriteLog("Original saved Windows settings restored. The backup was removed after the successful restore.");
        }
        catch (Exception ex)
        {
            WriteLog($"Restore stopped: {ex.Message}");
            WriteLog("The backup was kept so you can try Restore again.");
            MessageBox.Show(ex.Message, "BOOP Win7ify", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
        finally
        {
            SetBusy(false);
        }
    }

    private void SetBusy(bool busy)
    {
        _applyButton.Enabled = !busy;
        _restoreButton.Enabled = !busy;
        UseWaitCursor = busy;
    }

    private void WriteLog(string message) =>
        _log.AppendText($"[{DateTime.Now:HH:mm:ss}] {message}{Environment.NewLine}");

    private static Label MakeLabel(string text, float size, FontStyle style, Color color) => new()
    {
        Text = text,
        AutoSize = true,
        MaximumSize = new Size(790, 0),
        ForeColor = color,
        Font = new Font("Segoe UI", size, style),
        Margin = new Padding(0, 2, 0, 6)
    };

    private static Button MakeButton(string text, bool primary)
    {
        var button = new Button
        {
            Text = text,
            Width = primary ? 360 : 330,
            Height = 62,
            FlatStyle = FlatStyle.Flat,
            Font = new Font("Segoe UI", 12f, FontStyle.Bold),
            BackColor = primary ? Cyan : Color.Black,
            ForeColor = primary ? Color.Black : Cyan,
            Margin = new Padding(0, 4, 18, 4)
        };
        button.FlatAppearance.BorderColor = Cyan;
        button.FlatAppearance.BorderSize = 2;
        return button;
    }

    private static Image? LoadEyes()
    {
        var assembly = Assembly.GetExecutingAssembly();
        const string resource = "Boop.Win7ify.Assets.boopApprovedEyes.png";
        using var stream = assembly.GetManifestResourceStream(resource);
        if (stream is null) return null;
        using var source = Image.FromStream(stream);
        return new Bitmap(source);
    }

    private sealed record ToggleBinding(CheckBox Box, string[] TweakIds);
}