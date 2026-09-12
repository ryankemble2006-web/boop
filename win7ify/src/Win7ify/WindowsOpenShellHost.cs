using Boop.Win7ify.Core;
using Microsoft.Win32;
using System.ComponentModel;
using System.Diagnostics;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Xml;
using System.Xml.Linq;

namespace Boop.Win7ify;

/// <summary>Only this host invokes the official, checksum-pinned Open-Shell installer.</summary>
internal sealed class WindowsOpenShellHost : IOpenShellHost, IDisposable
{
    private readonly DiagnosticLog _log;
    private string? _payloadFolder, _installer;
    private FileStream? _payloadLock;
    public WindowsOpenShellHost(DiagnosticLog log) => _log = log;
    public static string OwnedFolder => Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "BOOP Win7ify Open-Shell");
    private static string ProgramFiles => Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);

    public ShellInfo Inspect()
    {
        using var machine = RegistryKey.OpenBaseKey(RegistryHive.LocalMachine, RegistryView.Registry64);
        using var key = machine.OpenSubKey(@"Software\OpenShell\OpenShell");
        var folder = key?.GetValue("Path", null, RegistryValueOptions.DoNotExpandEnvironmentNames) as string;
        if (string.IsNullOrWhiteSpace(folder)) return new(false);
        folder = ValidateFolder(folder);
        var exe = Path.Combine(folder, "StartMenu.exe");
        if (!File.Exists(exe)) throw new ShellProblem("BOOP E213", "Open-Shell is registered but its Start-menu component is missing. The existing installation was left alone.");
        var version = FileVersionInfo.GetVersionInfo(exe);
        var text = $"{version.FileMajorPart}.{version.FileMinorPart}.{version.FileBuildPart}";
        return new(true, text, SameFolder(folder, OwnedFolder), Running(exe));
    }
    private static string ValidateFolder(string path)
    {
        var full = Path.GetFullPath(path).TrimEnd(Path.DirectorySeparatorChar);
        if (!full.StartsWith(ProgramFiles.TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase))
            throw new ShellProblem("BOOP E213", "The existing menu is outside the normal protected Program Files folder. BOOP will not launch or replace it.");
        for (var dir = new DirectoryInfo(full); dir is not null && !SameFolder(dir.FullName, ProgramFiles); dir = dir.Parent)
            if (dir.Exists && (dir.Attributes & FileAttributes.ReparsePoint) != 0)
                throw new ShellProblem("BOOP E213", "The menu folder is redirected. It was left alone.");
        return full;
    }
    private static bool SameFolder(string a, string b) => string.Equals(Path.TrimEndingDirectorySeparator(a), Path.TrimEndingDirectorySeparator(b), StringComparison.OrdinalIgnoreCase);
    private string MenuExe()
    {
        var info = Inspect();
        if (!info.Installed || info.Version != OpenShellPayload.Version)
            throw new ShellProblem("BOOP E212", "The supported Open-Shell menu is not installed. Use INSTALL WINDOWS 7 START MENU first.");
        using var machine = RegistryKey.OpenBaseKey(RegistryHive.LocalMachine, RegistryView.Registry64);
        using var key = machine.OpenSubKey(@"Software\OpenShell\OpenShell");
        return Path.Combine(ValidateFolder((string)key!.GetValue("Path")!), "StartMenu.exe");
    }
    public void CheckEnvironment()
    {
        if (!OperatingSystem.IsWindowsVersionAtLeast(10, 0, 22000) || RuntimeInformation.OSArchitecture != Architecture.X64)
            throw new ShellProblem("BOOP E200", "This build is for 64-bit Intel/AMD Windows 11. This computer was left unchanged.");
    }
    public async Task VerifyPayloadAsync()
    {
        if (_payloadLock is not null) { OpenShellPayload.Verify(_payloadLock); return; }
        using var resource = Assembly.GetExecutingAssembly().GetManifestResourceStream("Boop.Win7ify.OpenShellInstaller")
            ?? throw new ShellProblem("BOOP E210", "The official installer is missing from this build. Nothing was installed.");
        OpenShellPayload.Verify(resource);
        _payloadFolder = Path.Combine(AppPaths.Folder, "payload", Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(_payloadFolder);
        _installer = Path.Combine(_payloadFolder, "OpenShellSetup_4_4_198.exe");
        await using (var output = new FileStream(_installer, FileMode.CreateNew, FileAccess.Write, FileShare.None))
            await resource.CopyToAsync(output);
        _payloadLock = new FileStream(_installer, FileMode.Open, FileAccess.Read, FileShare.Read);
        OpenShellPayload.Verify(_payloadLock);
        _log.Add(new("BOOP S211", "Official installer checksum verified.", OpenShellPayload.Sha256 + " (checksum provenance, not an Authenticode signature)"));
    }
    public async Task<int> InstallAsync()
    {
        if (Inspect().Installed) throw new ShellProblem("BOOP E213", "An installation already exists. BOOP will not silently replace it.");
        await VerifyPayloadAsync();
        var log = Path.Combine(AppPaths.LogFolder, "OpenShell-install-" + DateTime.Now.ToString("yyyyMMdd-HHmmss") + ".log");
        var args = $"/qn ADDLOCAL=StartMenu NOSTART=1 REBOOT=ReallySuppress /norestart APPLICATIONFOLDER=\"{OwnedFolder}\" /L*v \"{log}\"";
        return await InstallerAsync(args);
    }
    public async Task<int> UninstallAsync()
    {
        var info = Inspect();
        if (!info.Installed) return 0;
        if (!info.OwnedLocation || info.Version != OpenShellPayload.Version)
            throw new ShellProblem("BOOP E252", "This is not the exact installation BOOP owns. It was not removed.");
        foreach (var process in Process.GetProcessesByName("StartMenu"))
        {
            using (process)
                if (process.SessionId != Process.GetCurrentProcess().SessionId)
                    throw new ShellProblem("BOOP E253", "Another signed-in session has a Start menu running. Nothing was uninstalled.");
        }
        await VerifyPayloadAsync();
        var log = Path.Combine(AppPaths.LogFolder, "OpenShell-remove-" + DateTime.Now.ToString("yyyyMMdd-HHmmss") + ".log");
        return await InstallerAsync($"/x \"%MSI%\" /qn /norestart REBOOT=ReallySuppress /L*v \"{log}\"");
    }
    private async Task<int> InstallerAsync(string args)
    {
        Directory.CreateDirectory(AppPaths.LogFolder);
        _log.Add(new("BOOP S220", "Starting the official installer using normal Windows permission handling."));
        try
        {
            using var process = Process.Start(new ProcessStartInfo {
                FileName = _installer!, Arguments = args, WorkingDirectory = _payloadFolder!,
                UseShellExecute = true, Verb = "runas"
            }) ?? throw new ShellProblem("BOOP E225", "Windows did not start the installer.");
            using var timeout = new CancellationTokenSource(TimeSpan.FromMinutes(5));
            try { await process.WaitForExitAsync(timeout.Token); }
            catch (OperationCanceledException ex)
            { throw new ShellProblem("BOOP E226", "Windows Installer is taking longer than expected. It was NOT killed. Let it finish before retrying; your recovery journal is kept.", ex); }
            _log.Add(new("BOOP S222", "Installer finished.", "Exit code: " + process.ExitCode));
            return process.ExitCode;
        }
        catch (Win32Exception ex) when (ex.NativeErrorCode == 1223)
        { throw new ShellProblem("BOOP E221", "Windows permission was cancelled. Nothing was forced; retry when ready.", ex); }
        catch (Win32Exception ex)
        { throw new ShellProblem("BOOP E225", "Windows could not launch the installer. Security settings were not changed.", ex); }
    }
    public async Task StopAsync()
    {
        var info = Inspect();
        if (!info.Installed || !info.Running) return;
        var exe = MenuExe();
        // Upstream MSG_EXIT is ignored while CanShowMenu() is false. Dismiss the
        // active menu first, then send the official exit command. Never kill Explorer.
        for (var request = 0; request < 3 && Running(exe); request++)
        {
            var menu = FindWindow("OpenShell.CMenuContainer", null);
            var shell = GetShellWindow();
            if (menu != IntPtr.Zero && shell != IntPtr.Zero)
            {
                GetWindowThreadProcessId(menu, out var menuPid);
                GetWindowThreadProcessId(shell, out var shellPid);
                if (menuPid == shellPid)
                {
                    _log.Add(new("BOOP S241", "Closing the open menu before its official shutdown."));
                    PostMessage(menu, 0x0010, IntPtr.Zero, IntPtr.Zero);
                    for (var wait = 0; wait < 20 && IsWindowVisible(menu); wait++) await Task.Delay(100);
                }
            }
            await RunToExitAsync(exe, "-exit", 15);
            for (var wait = 0; wait < 40 && Running(exe); wait++) await Task.Delay(250);
        }
        if (Running(exe))
            throw new ShellProblem("BOOP E241", "The current Start menu did not close cleanly. BOOP did not force-kill Explorer. Recovery remains available.");
        _log.Add(new("BOOP S244", "The Start-menu process finished its normal shutdown."));
    }
    public async Task VerifyProfileAsync()
    {
        var folder = Path.GetDirectoryName(MenuExe())!;
        if (!File.Exists(Path.Combine(folder, "Skins", "Windows Aero.skin7")))
            throw new ShellProblem("BOOP E232", "The Windows 7 menu skin is missing. Undo remains available.");
        var export = Path.Combine(AppPaths.Folder, "effective-menu-" + Guid.NewGuid().ToString("N") + ".xml");
        try
        {
            var code = await RunToExitAsync(MenuExe(), $"-backup \"{export}\"", 30);
            if (code != 0 || !File.Exists(export)) throw new ShellProblem("BOOP E233", "Open-Shell could not report its effective menu settings.");
            var xmlSettings = new XmlReaderSettings { DtdProcessing = DtdProcessing.Prohibit, XmlResolver = null, MaxCharactersInDocument = 2_000_000 };
            using var reader = XmlReader.Create(export, xmlSettings);
            var doc = XDocument.Load(reader);
            if (doc.Root?.Name != "Settings" || (string?)doc.Root.Attribute("component") != "StartMenu")
                throw new ShellProblem("BOOP E233", "Open-Shell returned an unexpected settings report.");
            var expected = new Dictionary<string, string> {
                ["MenuStyle"] = "Win7", ["SkinW7"] = "Windows Aero", ["EnableStartButton"] = "1",
                ["StartButtonType"] = "AeroButton", ["MouseClick"] = "ClassicMenu", ["WinKey"] = "ClassicMenu",
                ["ShiftClick"] = "WindowsMenu", ["ShiftWin"] = "WindowsMenu"
            };
            foreach (var item in expected)
                if (!string.Equals((string?)doc.Root.Element(item.Key)?.Attribute("value"), item.Value, StringComparison.OrdinalIgnoreCase))
                    throw new ShellProblem("BOOP E234", "Open-Shell did not accept " + item.Key + ". A policy or incompatible setting may be in control. Undo remains available.");
            _log.Add(new("BOOP S235", "Open-Shell itself confirmed the Windows 7 menu settings and installed skin."));
        }
        finally { if (File.Exists(export)) File.Delete(export); }
    }
    public async Task StartAsync(bool openMenu)
    {
        var exe = MenuExe();
        var wasRunning = Running(exe);
        using var started = Process.Start(new ProcessStartInfo(exe, openMenu ? "-open" : "") { UseShellExecute = false, WorkingDirectory = Path.GetDirectoryName(exe)! })
            ?? throw new ShellProblem("BOOP E240", "The installed menu could not start.");
        if (wasRunning)
        {
            // A second -open is a command sender, not the resident menu process.
            // Let it finish forwarding before reporting readiness or beginning Undo.
            using var commandTimeout = new CancellationTokenSource(TimeSpan.FromSeconds(15));
            try { await started.WaitForExitAsync(commandTimeout.Token); }
            catch (OperationCanceledException ex)
            { throw new ShellProblem("BOOP E243", "The existing menu did not finish receiving its command. Check the report before retrying.", ex); }
        }
        for (var attempt = 0; attempt < 100; attempt++)
        {
            if (Running(exe) && (!openMenu || MenuWindowVisible()))
            { _log.Add(new("BOOP S242", openMenu ? "The Start-menu window is open." : "The previous Start-menu process is running.")); return; }
            await Task.Delay(200);
        }
        throw new ShellProblem("BOOP E240", "The files are installed, but the menu window did not open. Your Windows build may need a sign-out or a compatibility fix. No success was assumed; Undo remains available.");
    }
    private static bool Running(string exe)
    {
        foreach (var process in Process.GetProcessesByName("StartMenu"))
        {
            using (process)
            {
                try
                {
                    if (process.SessionId == Process.GetCurrentProcess().SessionId &&
                        string.Equals(process.MainModule?.FileName, exe, StringComparison.OrdinalIgnoreCase)) return true;
                }
                catch (Exception ex) when (ex is Win32Exception or InvalidOperationException) { }
            }
        }
        return false;
    }
    public static bool HasDesktopTaskbar => FindWindow("Shell_TrayWnd", null) != IntPtr.Zero;
    public static bool MenuWindowVisible()
    {
        var window = FindWindow("OpenShell.CMenuContainer", null);
        return window != IntPtr.Zero && IsWindowVisible(window);
    }
    private static async Task<int> RunToExitAsync(string exe, string args, int seconds)
    {
        using var process = Process.Start(new ProcessStartInfo(exe, args) { UseShellExecute = false, WorkingDirectory = Path.GetDirectoryName(exe)! })
            ?? throw new ShellProblem("BOOP E240", "The installed menu command could not start.");
        using var timeout = new CancellationTokenSource(TimeSpan.FromSeconds(seconds));
        try { await process.WaitForExitAsync(timeout.Token); }
        catch (OperationCanceledException ex) { throw new ShellProblem("BOOP E243", "A Start-menu command did not finish. It was not force-killed. Check the diagnostic log.", ex); }
        return process.ExitCode;
    }
    public void Dispose()
    {
        _payloadLock?.Dispose();
        if (_payloadFolder is null) return;
        try { if (_installer is not null) File.Delete(_installer); Directory.Delete(_payloadFolder); }
        catch (IOException) { }
        catch (UnauthorizedAccessException) { }
    }
    [DllImport("user32.dll")] private static extern IntPtr GetShellWindow();
    [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr window, out uint processId);
    [DllImport("user32.dll")] [return: MarshalAs(UnmanagedType.Bool)] private static extern bool PostMessage(IntPtr window, uint message, IntPtr wParam, IntPtr lParam);
    [DllImport("user32.dll", CharSet = CharSet.Unicode)] private static extern IntPtr FindWindow(string className, string? windowName);
    [DllImport("user32.dll")] [return: MarshalAs(UnmanagedType.Bool)] private static extern bool IsWindowVisible(IntPtr window);
}
