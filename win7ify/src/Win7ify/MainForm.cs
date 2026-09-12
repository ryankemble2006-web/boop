using Boop.Win7ify.Core;
using System.Diagnostics;
using System.Reflection;

namespace Boop.Win7ify;

internal sealed class MainForm : Form
{
    private static readonly Color Cyan = Color.FromArgb(0, 224, 255);
    private static readonly Color PanelBlack = Color.FromArgb(20, 22, 26);
    private readonly Win7ifyService _service;
    private readonly BackupService _backup;
    private readonly List<(CheckBox Box, string[] Ids)> _toggles = new();
    private readonly TextBox _log;
    private readonly Label _status;
    private readonly Button _apply, _restore, _refresh, _settings;
    private readonly Image _eyes;
    private readonly string _logPath;
    private bool _busy;

    public MainForm(Win7ifyService service, BackupService backup)
    {
        _service = service;
        _backup = backup;
        _logPath = Path.Combine(Path.GetDirectoryName(backup.BackupPath)!, "logs", $"Win7ify-{DateTime.Now:yyyyMMdd-HHmmss}-{Environment.ProcessId}.log");
        AutoScaleDimensions = new SizeF(96, 96);
        AutoScaleMode = AutoScaleMode.Dpi;
        Font = new Font("Segoe UI", 13f);
        Text = "BOOP // Win7ify 0.1.1";
        BackColor = Color.Black;
        ForeColor = Color.White;
        MinimumSize = new Size(720, 660);
        ClientSize = new Size(1080, 850);
        StartPosition = FormStartPosition.CenterScreen;
        WindowState = FormWindowState.Maximized;

        var root = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 1, RowCount = 5, Padding = new Padding(22), BackColor = Color.Black };
        root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 120));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 136));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 58));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 160));

        var header = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 2, RowCount = 1 };
        header.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        header.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 200));
        header.Controls.Add(new Label {
            Text = "BOOP // WIN7IFY 0.1.1\r\nFamiliar Windows habits. Your original settings kept safe.",
            Dock = DockStyle.Fill, ForeColor = Cyan, Font = new Font("Segoe UI", 19f, FontStyle.Bold),
            TextAlign = ContentAlignment.MiddleLeft
        }, 0, 0);
        _eyes = LoadEyes();
        header.Controls.Add(new PictureBox { Image = _eyes, SizeMode = PictureBoxSizeMode.Zoom, Dock = DockStyle.Fill }, 1, 0);
        root.Controls.Add(header, 0, 0);

        var scroll = new Panel { Dock = DockStyle.Fill, AutoScroll = true, BackColor = PanelBlack };
        var choices = new TableLayoutPanel { Dock = DockStyle.Top, AutoSize = true, ColumnCount = 1, Padding = new Padding(12) };
        choices.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        AddToggle(choices, "Start and taskbar buttons on the left", "taskbar.left");
        AddToggle(choices, "Separate taskbar buttons with labels", "taskbar.nevercombine");
        AddToggle(choices, "Hide Search from the taskbar", "taskbar.search.hide");
        AddToggle(choices, "Hide Task View", "taskbar.taskview.hide");
        AddToggle(choices, "Hide Widgets (Windows may protect this)", "taskbar.widgets.hide");
        AddToggle(choices, "Restore the far-right Show Desktop corner", "taskbar.showdesktop");
        AddToggle(choices, "Open File Explorer to This PC", "explorer.thispc");
        AddToggle(choices, "Show Computer, user files, Network, Control Panel and Recycle Bin",
            "desktop.computer", "desktop.userfiles", "desktop.network", "desktop.controlpanel", "desktop.recyclebin");
        AddToggle(choices, "EXPERIMENTAL: old full right-click menu", "context.classic.experimental");
        scroll.Controls.Add(choices);
        root.Controls.Add(scroll, 0, 1);

        _apply = MakeButton("MAKE WINDOWS 7-ISH", true);
        _restore = MakeButton("PUT WINDOWS 11 BACK", false);
        _refresh = MakeButton("REFRESH DESKTOP...", false);
        _settings = MakeButton("TASKBAR SETTINGS", false);
        _apply.Click += async (_, _) => await RunChanges(restore: false);
        _restore.Click += async (_, _) => await RunChanges(restore: true);
        _refresh.Click += async (_, _) => await RefreshDesktop();
        _settings.Click += (_, _) => OpenTaskbarSettings();
        var buttons = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 2, RowCount = 2, Padding = new Padding(0, 8, 0, 0) };
        buttons.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));
        buttons.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));
        buttons.RowStyles.Add(new RowStyle(SizeType.Percent, 50));
        buttons.RowStyles.Add(new RowStyle(SizeType.Percent, 50));
        buttons.Controls.Add(_apply, 0, 0);
        buttons.Controls.Add(_restore, 1, 0);
        buttons.Controls.Add(_refresh, 0, 1);
        buttons.Controls.Add(_settings, 1, 1);
        root.Controls.Add(buttons, 0, 2);

        _status = new Label { Dock = DockStyle.Fill, ForeColor = Color.White, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 12f, FontStyle.Bold) };
        root.Controls.Add(_status, 0, 3);
        _log = new TextBox { Dock = DockStyle.Fill, Multiline = true, ReadOnly = true, ScrollBars = ScrollBars.Vertical,
            BackColor = PanelBlack, ForeColor = Cyan, BorderStyle = BorderStyle.FixedSingle, Font = new Font("Consolas", 11f) };
        root.Controls.Add(_log, 0, 4);
        Controls.Add(root);
        FormClosed += (_, _) => _eyes.Dispose();
        FormClosing += (_, e) => { if (_busy) { e.Cancel = true; _status.Text = "Please let this operation finish before closing."; } };
        _status.Text = _backup.HasBackup ? "Original backup found. Undo will use that same baseline." : "Ready. Nothing is changed just by opening this window.";
        // Startup is read-only. Logs are created only after a user-requested operation.
        _log.Text = $"Windows build: {Environment.OSVersion.Version}\r\n" +
            "This is a settings makeover, not a Windows 7 Start-menu replacement.\r\n" +
            "Already-correct settings are left alone. Blocked settings are named, not forced.\r\n" +
            "The desktop is refreshed only when you press REFRESH DESKTOP.\r\n";
    }

    private void AddToggle(TableLayoutPanel parent, string text, params string[] ids)
    {
        var experimental = ids.Contains("context.classic.experimental");
        var box = new CheckBox { Text = text, Checked = ids.All(PresetCatalog.Windows7ish.Contains), Dock = DockStyle.Fill,
            AutoSize = false, Height = 54, MinimumSize = new Size(0, 54), Padding = new Padding(10, 4, 10, 4),
            ForeColor = experimental ? Color.Gold : Color.White, BackColor = PanelBlack,
            Font = new Font("Segoe UI", 13f, FontStyle.Bold), Margin = new Padding(2, 3, 2, 3),
            AccessibleDescription = text + ". Click anywhere in this row, or use Space, to select." };
        parent.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        parent.Controls.Add(box, 0, parent.RowCount++);
        _toggles.Add((box, ids));
    }

    private async Task RunChanges(bool restore)
    {
        if (_busy) return;
        if (restore && !_backup.HasBackup) { _status.Text = "There are no BOOP changes to undo yet."; return; }
        var ids = _toggles.Where(t => t.Box.Checked).SelectMany(t => t.Ids).Distinct().ToArray();
        if (!restore && ids.Length == 0) { _status.Text = "Nothing selected. Windows was left alone."; return; }
        if (!restore && ids.Contains("context.classic.experimental") &&
            MessageBox.Show(this, "The old right-click menu is an unsupported, experimental compatibility tweak. Include it?", "BOOP Win7ify",
                MessageBoxButtons.YesNo, MessageBoxIcon.Warning, MessageBoxDefaultButton.Button2) != DialogResult.Yes) return;
        SetBusy(true);
        _status.Text = restore ? "Putting the saved settings back..." : "Saving your originals, then applying the selected settings...";
        WriteLog(restore ? "UNDO requested." : $"APPLY requested: {ids.Length} settings.");
        IProgress<ChangeResult> progress = new Progress<ChangeResult>(ShowResult);
        try
        {
            var results = await Task.Run(() => restore ? _service.RestoreAll(progress.Report) : _service.Apply(ids, progress.Report));
            var changed = results.Count(r => r.Status == ChangeStatus.Changed);
            var unchanged = results.Count(r => r.Status == ChangeStatus.AlreadyCorrect);
            var blocked = results.Count(r => r.Status == ChangeStatus.Blocked);
            var failed = results.Count(r => r.Status == ChangeStatus.Failed);
            _status.Text = $"{changed} changed | {unchanged} already correct | {blocked} blocked | {failed} not verified.";
            WriteLog(_status.Text);
            if (restore) WriteLog(_backup.HasBackup ? "Original backup kept. Some settings still need attention; Undo can be retried." : "All saved values are back. The completed backup has been removed.");
            else WriteLog($"Original backup kept at {_backup.BackupPath}");
            if (changed > 0) WriteLog("Press REFRESH DESKTOP when file transfers are finished, or sign out later, to let Windows reread the changes.");
            WriteLog("A saved registry value does not prove a visible Windows change. No shell replacement is installed.");
        }
        catch (Exception ex)
        {
            _status.Text = "Could not finish. Check the details below; do not delete your backup.";
            WriteLog($"{ex.GetType().Name}: {ex.Message}");
        }
        finally { SetBusy(false); }
    }

    private void ShowResult(ChangeResult result)
    {
        var state = result.Status switch { ChangeStatus.Changed => "SAVED", ChangeStatus.AlreadyCorrect => "ALREADY CORRECT", ChangeStatus.Blocked => "BLOCKED", _ => "NOT VERIFIED" };
        WriteLog($"{state}: {result.Label}. {result.Detail}");
        if (!result.Succeeded) WriteLog($"Setting: HKCU\\{result.Path} | {result.Name}");
    }

    private async Task RefreshDesktop()
    {
        if (_busy) return;
        if (MessageBox.Show(this, "Finish all file copying and moving first.\r\n\r\nThis restarts your Windows desktop and taskbar, and may close File Explorer windows. Other applications should stay open.\r\n\r\nRefresh now?",
            "Refresh desktop", MessageBoxButtons.YesNo, MessageBoxIcon.Warning, MessageBoxDefaultButton.Button2) != DialogResult.Yes) return;
        SetBusy(true);
        _status.Text = "Refreshing the Windows desktop...";
        try { await Task.Run(ExplorerShell.Restart); _status.Text = "Desktop restarted. Check which changes Windows displayed."; WriteLog(_status.Text); }
        catch (Exception ex) { _status.Text = "Desktop refresh did not finish. Sign out later to reload the settings."; WriteLog(ex.Message); }
        finally { SetBusy(false); }
    }

    private void OpenTaskbarSettings()
    {
        try { Process.Start(new ProcessStartInfo("ms-settings:taskbar") { UseShellExecute = true }); }
        catch (Exception ex) { _status.Text = "Open Windows Settings > Personalisation > Taskbar."; WriteLog(ex.Message); }
    }

    private void SetBusy(bool busy)
    {
        _busy = busy;
        _apply.Enabled = _restore.Enabled = _refresh.Enabled = _settings.Enabled = !busy;
        foreach (var t in _toggles) t.Box.Enabled = !busy;
        UseWaitCursor = busy;
    }
    private void WriteLog(string message)
    {
        var line = $"[{DateTime.Now:HH:mm:ss}] {message}{Environment.NewLine}";
        _log.AppendText(line);
        try { Directory.CreateDirectory(Path.GetDirectoryName(_logPath)!); File.AppendAllText(_logPath, line); }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException) { _log.AppendText("Could not save the log file. Details remain in this window.\r\n"); }
    }
    private static Button MakeButton(string text, bool primary)
    {
        var button = new Button { Text = text, Dock = DockStyle.Fill, FlatStyle = FlatStyle.Flat,
            UseVisualStyleBackColor = false, Font = new Font("Segoe UI", 13f, FontStyle.Bold),
            BackColor = primary ? Cyan : Color.FromArgb(36, 43, 50), ForeColor = primary ? Color.Black : Color.White,
            Margin = new Padding(5), MinimumSize = new Size(0, 48) };
        button.FlatAppearance.BorderColor = Cyan;
        button.FlatAppearance.BorderSize = 2;
        return button;
    }
    private static Image LoadEyes()
    {
        using var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("Boop.Win7ify.Assets.boopApprovedEyes.png")
            ?? throw new InvalidDataException("The approved BOOP eyes were not included in this build.");
        using var source = Image.FromStream(stream);
        return new Bitmap(source);
    }
}
