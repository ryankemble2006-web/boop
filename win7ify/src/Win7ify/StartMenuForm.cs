using Boop.Win7ify.Core;
using System.Diagnostics;
using System.Reflection;

namespace Boop.Win7ify;

internal sealed class StartMenuForm : Form
{
    private static readonly Color Cyan = Color.FromArgb(0, 224, 255);
    private readonly OpenShellSession _session;
    private readonly WindowsOpenShellHost _host;
    private readonly DiagnosticLog _log;
    private readonly Win7ifyService _desktop;
    private readonly BackupService _desktopBackup;
    private readonly Label _stage;
    private readonly TextBox _details;
    private readonly List<Button> _actions = new();
    private readonly Image _eyes;
    private bool _busy;
    public StartMenuForm(OpenShellSession session, WindowsOpenShellHost host, DiagnosticLog log,
        Win7ifyService desktop, BackupService desktopBackup)
    {
        _session = session; _host = host; _log = log; _desktop = desktop; _desktopBackup = desktopBackup;
        AutoScaleDimensions = new SizeF(96, 96); AutoScaleMode = AutoScaleMode.Dpi;
        Font = new Font("Segoe UI", 15f);
        Text = "BOOP // Win7ify 0.2.0";
        BackColor = Color.Black; ForeColor = Color.White;
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(800, 680); ClientSize = new Size(1180, 900);
        WindowState = FormWindowState.Maximized;
        var root = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 1, RowCount = 6, Padding = new Padding(24) };
        root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 130));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 180));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 70));
        var header = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 2, RowCount = 1 };
        header.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100)); header.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 180));
        header.Controls.Add(new Label { Text = "BOOP // WIN7IFY 0.2.0\r\nA real Windows 7-style Start menu.", Font = new Font("Segoe UI", 23f, FontStyle.Bold), ForeColor = Cyan, Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft });
        using (var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("Boop.Win7ify.Assets.boopApprovedEyes.png")
            ?? throw new InvalidDataException("The approved BOOP eyes are missing."))
        using (var image = Image.FromStream(stream)) _eyes = new Bitmap(image);
        header.Controls.Add(new PictureBox { Image = _eyes, SizeMode = PictureBoxSizeMode.Zoom, Dock = DockStyle.Fill }, 1, 0);
        root.Controls.Add(header, 0, 0);
        root.Controls.Add(new Label {
            Text = "Includes official Open-Shell 4.4.198: Start menu, search and an Aero menu skin.\r\nNo full-desktop Aero or replacement taskbar. Windows may ask permission to install.\r\nYour existing menu preferences are saved first. Shift-click Start opens the Windows menu.",
            Dock = DockStyle.Fill, ForeColor = Color.White, TextAlign = ContentAlignment.MiddleLeft
        }, 0, 1);
        _stage = new Label { Text = "READY\r\nOpening this app changes nothing.", Dock = DockStyle.Fill, Font = new Font("Segoe UI", 22f, FontStyle.Bold), Padding = new Padding(14), BackColor = Color.FromArgb(20, 30, 38), ForeColor = Cyan };
        root.Controls.Add(_stage, 0, 2);
        var buttons = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 2, RowCount = 2 };
        buttons.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50)); buttons.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));
        buttons.RowStyles.Add(new RowStyle(SizeType.Percent, 50)); buttons.RowStyles.Add(new RowStyle(SizeType.Percent, 50));
        ActionButton(buttons, 0, 0, "INSTALL WINDOWS 7 START MENU", async () => await _session.EnableAsync(_log.Add), true);
        ActionButton(buttons, 1, 0, "PUT WINDOWS 11 BACK", UndoAsync);
        ActionButton(buttons, 0, 1, "OPEN WINDOWS 7 MENU", async () => { await _host.VerifyProfileAsync(); await _host.StartAsync(true); _log.Add(new("BOOP OK", "The Start-menu window is open.")); });
        ActionButton(buttons, 1, 1, "DESKTOP SETTINGS", () => { using var settings = new MainForm(_desktop, _desktopBackup); settings.ShowDialog(this); return Task.CompletedTask; });
        root.Controls.Add(buttons, 0, 3);
        _details = new TextBox { Multiline = true, ReadOnly = true, ScrollBars = ScrollBars.Vertical, Dock = DockStyle.Fill, Font = new Font("Consolas", 12f), BackColor = Color.FromArgb(20, 22, 26), ForeColor = Color.White, Text = _log.Report() };
        root.Controls.Add(_details, 0, 4);
        var tools = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 5, RowCount = 1 };
        for (var i = 0; i < 5; i++) tools.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 20));
        ToolButton(tools, 0, "CHECK / REPORT", Check);
        ToolButton(tools, 1, "COPY REPORT", () => Clipboard.SetText(_log.Report()));
        ToolButton(tools, 2, "SAVE REPORT", SaveReport);
        ToolButton(tools, 3, "OPEN LOGS", () => { Directory.CreateDirectory(AppPaths.LogFolder); Process.Start(new ProcessStartInfo(AppPaths.LogFolder) { UseShellExecute = true }); });
        ToolButton(tools, 4, "CREDITS", Credits);
        root.Controls.Add(tools, 0, 5);
        Controls.Add(root);
        _log.Updated += ShowEvent;
        FormClosing += (_, e) => { if (_busy) { e.Cancel = true; _stage.Text = "PLEASE WAIT\r\nThe current operation is still finishing."; } };
        FormClosed += (_, _) => { _log.Updated -= ShowEvent; _eyes.Dispose(); };
    }
    private async Task UndoAsync()
    {
        await _session.UndoAsync(_log.Add);
        if (_desktopBackup.HasBackup)
        {
            var results = await Task.Run(() => _desktop.RestoreAll());
            foreach (var item in results) _log.Add(new("BOOP S270", item.Label, item.Detail));
            if (results.Any(r => !r.Succeeded)) throw new ShellProblem("BOOP E270", "The menu was undone, but some older desktop settings remain blocked. The original backup is kept.");
        }
        _log.Add(new("BOOP OK", "Saved Windows settings are back. Explorer was not force-restarted."));
    }
    private static Button MakeButton(string text, bool primary = false) => new() {
        Text = text, Dock = DockStyle.Fill, Margin = new Padding(5), MinimumSize = new Size(0, 48),
        FlatStyle = FlatStyle.Flat, BackColor = primary ? Cyan : Color.FromArgb(32, 40, 48),
        ForeColor = primary ? Color.Black : Color.White, Font = new Font("Segoe UI", 16f, FontStyle.Bold)
    };
    private void ActionButton(TableLayoutPanel panel, int column, int row, string text, Func<Task> action, bool primary = false)
    {
        var button = MakeButton(text, primary); button.FlatAppearance.BorderColor = Cyan; _actions.Add(button);
        button.Click += async (_, _) => {
            if (_busy) return;
            _busy = true; foreach (var item in _actions) item.Enabled = false;
            try { await action(); }
            catch (Exception ex) { _log.Failure(ex); }
            finally { _busy = false; foreach (var item in _actions) item.Enabled = true; }
        };
        panel.Controls.Add(button, column, row);
    }
    private void ToolButton(TableLayoutPanel panel, int column, string text, Action action)
    {
        var button = MakeButton(text); button.Font = new Font("Segoe UI", 12f, FontStyle.Bold);
        button.Click += (_, _) => { try { action(); } catch (Exception ex) { _log.Failure(ex); } };
        panel.Controls.Add(button, column, 0);
    }
    private void Check()
    {
        var info = _host.Inspect();
        _log.Add(new("BOOP REPORT", info.Installed ? "Open-Shell is installed. See the report for its state." : "No Open-Shell Start menu is installed yet.",
            $"Installed={info.Installed}; version={info.Version}; BOOP location={info.OwnedLocation}; current-session running={info.Running}\r\nRecovery journal={_session.HasSession}; desktop backup={_desktopBackup.HasBackup}; taskbar available={WindowsOpenShellHost.HasDesktopTaskbar}"));
    }
    private void ShowEvent(ShellEvent entry)
    {
        if (IsDisposed) return;
        if (InvokeRequired) { BeginInvoke(new Action(() => ShowEvent(entry))); return; }
        var failed = entry.Code.StartsWith("BOOP E", StringComparison.Ordinal);
        _stage.BackColor = failed ? Color.FromArgb(92, 20, 24) : Color.FromArgb(20, 30, 38);
        _stage.ForeColor = failed ? Color.White : Cyan;
        _stage.Text = entry.Code + "\r\n" + entry.Message;
        _details.Text = _log.Report(); _details.SelectionStart = _details.TextLength; _details.ScrollToCaret();
    }
    private void SaveReport()
    {
        using var dialog = new SaveFileDialog { Filter = "Text report|*.txt", FileName = "BOOP-Win7ify-report.txt" };
        if (dialog.ShowDialog(this) == DialogResult.OK) File.WriteAllText(dialog.FileName, _log.Report());
    }
    private void Credits()
    {
        using var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("Boop.Win7ify.ThirdPartyNotices");
        using var reader = new StreamReader(stream ?? throw new InvalidDataException("Credits are missing."));
        using var form = new Form { Text = "BOOP Win7ify: third-party notices", Size = new Size(900, 700), StartPosition = FormStartPosition.CenterParent };
        form.Controls.Add(new TextBox { Text = reader.ReadToEnd(), ReadOnly = true, Multiline = true, ScrollBars = ScrollBars.Vertical, Dock = DockStyle.Fill, Font = new Font("Segoe UI", 13f) });
        form.ShowDialog(this);
    }
}
