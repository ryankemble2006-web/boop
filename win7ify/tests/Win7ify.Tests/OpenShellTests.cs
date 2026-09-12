using Boop.Win7ify.Core;

namespace Boop.Win7ify.Tests;

internal static class OpenShellTests
{
    public static IEnumerable<(string Name, Action Run)> Cases => new (string, Action)[] {
        ("Open-Shell profile actually selects Windows 7 menu", Profile),
        ("Open-Shell rejects modified installer", RejectPayload),
        ("Open-Shell installs and activates new menu", () => Run(NewInstall)),
        ("Open-Shell repeated apply never reinstalls", () => Run(Repeat)),
        ("Open-Shell existing install is never uninstalled", () => Run(Existing)),
        ("Open-Shell owned install can be undone", () => Run(OwnedUndo)),
        ("Open-Shell install cancellation keeps recovery", () => Run(InstallFailure)),
        ("Open-Shell failed uninstall can be retried", () => Run(UninstallRetry)),
        ("Open-Shell damaged journal blocks installation", () => Run(CorruptState)),
        ("Open-Shell changed owned version is not removed", () => Run(ChangedVersion)),
        ("Open-Shell setup-only does not launch runtime", () => Run(SetupOnly)),
        ("Open-Shell undo without journal leaves existing menu", () => Run(NoOwnership))
    };
    private static void Check(bool value, string detail) { if (!value) throw new Exception(detail); }
    private static void Run(Func<Task> action) => action().GetAwaiter().GetResult();
    private static void Profile()
    {
        var p = OpenShellProfile.All;
        Check(p.Any(t => t.Name == "MenuStyle" && t.AppliedValue.Text == "Win7"), "Windows 7 menu is absent.");
        Check(p.Any(t => t.Name == "SkinW7" && t.AppliedValue.Text == "Windows Aero"), "Actual Aero menu skin is absent.");
        Check(p.Any(t => t.Name == "EnableStartButton" && t.AppliedValue.Number == 1), "Replacement Start button is absent.");
        Check(p.Any(t => t.Name == "ShowedStyle2"), "Initial settings wizard is not accounted for.");
        Check(!p.Any(t => t.Path.Contains("Policies", StringComparison.OrdinalIgnoreCase)), "Do not change policy.");
    }
    private static void RejectPayload()
    {
        using var stream = new MemoryStream(new byte[] { 77, 90, 0 });
        try { OpenShellPayload.Verify(stream); }
        catch (ShellProblem ex) { Check(ex.Code == "BOOP E210", "Hash failure needs the integrity code."); return; }
        throw new Exception("Modified installer was accepted.");
    }
    private sealed class Fixture : IDisposable
    {
        public readonly string Folder = Path.Combine(Path.GetTempPath(), "boop-shell-tests", Guid.NewGuid().ToString("N"));
        public readonly FakeRegistryStore Registry = new();
        public readonly Host Host = new();
        public OpenShellSession Session => new(Folder, Registry, Host);
        public void Dispose() { if (Directory.Exists(Folder)) Directory.Delete(Folder, true); }
    }
    private sealed class Host : IOpenShellHost
    {
        public ShellInfo Info = new(false);
        public int Installs, Uninstalls, Starts;
        public int InstallCode, UninstallCode;
        public ShellInfo Inspect() => Info;
        public void CheckEnvironment() { }
        public Task VerifyPayloadAsync() => Task.CompletedTask;
        public Task<int> InstallAsync() { Installs++; if (InstallCode == 0) Info = new(true, OpenShellPayload.Version, true); return Task.FromResult(InstallCode); }
        public Task StopAsync() { Info = Info with { Running = false }; return Task.CompletedTask; }
        public Task VerifyProfileAsync() => Task.CompletedTask;
        public Task StartAsync(bool openMenu) { Starts++; Info = Info with { Running = true }; return Task.CompletedTask; }
        public Task<int> UninstallAsync() { Uninstalls++; if (UninstallCode == 0) Info = new(false); return Task.FromResult(UninstallCode); }
    }
    private static async Task NewInstall()
    {
        using var f = new Fixture(); await f.Session.EnableAsync();
        Check(f.Host.Installs == 1 && f.Host.Info.Running, "Real installation/activation was not requested.");
        Check(f.Session.HasSession, "Ownership journal missing.");
        Check(f.Registry.Read(OpenShellProfile.Settings, "MenuStyle").Text == "Win7", "Menu is not configured.");
    }
    private static async Task Repeat()
    {
        using var f = new Fixture(); await f.Session.EnableAsync(); await f.Session.EnableAsync();
        Check(f.Host.Installs == 1, "Repeated Apply reinstalled the package.");
    }
    private static async Task Existing()
    {
        using var f = new Fixture(); f.Host.Info = new(true, OpenShellPayload.Version, false, true);
        f.Registry.Seed(OpenShellProfile.Settings, "MenuStyle", RegistryStoredValue.String("Classic2"));
        await f.Session.EnableAsync(); await f.Session.UndoAsync();
        Check(f.Host.Installs == 0 && f.Host.Uninstalls == 0 && f.Host.Info.Running, "Existing installation was not preserved.");
        Check(f.Registry.Read(OpenShellProfile.Settings, "MenuStyle").Text == "Classic2", "Existing preset was lost.");
    }
    private static async Task OwnedUndo()
    {
        using var f = new Fixture(); await f.Session.EnableAsync(); await f.Session.UndoAsync();
        Check(f.Host.Uninstalls == 1 && !f.Host.Info.Installed && !f.Session.HasSession, "Owned install was not removed.");
        Check(!f.Registry.Read(OpenShellProfile.Settings, "MenuStyle").Exists, "Originally absent setting was not restored.");
    }
    private static async Task InstallFailure()
    {
        using var f = new Fixture(); f.Host.InstallCode = 1602;
        try { await f.Session.EnableAsync(); throw new Exception("Cancelled install claimed success."); }
        catch (ShellProblem ex) { Check(ex.Code == "BOOP E221", "Installer cancellation needs an actionable code."); }
        Check(f.Session.HasSession && !f.Registry.Read(OpenShellProfile.Settings, "MenuStyle").Exists, "Cancellation lost recovery or changed profile.");
        f.Host.InstallCode = 0; await f.Session.EnableAsync();
        Check(f.Host.Info.Running, "Retry did not finish.");
    }
    private static async Task UninstallRetry()
    {
        using var f = new Fixture(); await f.Session.EnableAsync(); f.Host.UninstallCode = 1603;
        try { await f.Session.UndoAsync(); throw new Exception("Failed uninstall claimed success."); } catch (ShellProblem) { }
        Check(f.Session.HasSession, "Failed uninstall destroyed recovery.");
        f.Host.UninstallCode = 0; await f.Session.UndoAsync(); Check(!f.Session.HasSession, "Retry did not clear completed session.");
    }
    private static async Task CorruptState()
    {
        using var f = new Fixture(); Directory.CreateDirectory(f.Folder); File.WriteAllText(Path.Combine(f.Folder, "open-shell-session-v1.json"), "broken");
        try { await f.Session.EnableAsync(); throw new Exception("Corrupt journal accepted."); } catch (ShellProblem) { }
        Check(f.Host.Installs == 0, "Installer ran without a valid recovery journal.");
    }
    private static async Task ChangedVersion()
    {
        using var f = new Fixture(); await f.Session.EnableAsync(); f.Host.Info = f.Host.Info with { Version = "99.0.0" };
        try { await f.Session.UndoAsync(); throw new Exception("Unrelated updated version was accepted for uninstall."); } catch (ShellProblem) { }
        Check(f.Host.Uninstalls == 0 && f.Session.HasSession, "Updated install was removed or recovery lost.");
    }
    private static async Task SetupOnly()
    {
        using var f = new Fixture(); await f.Session.EnableAsync(launch: false);
        Check(f.Host.Installs == 1 && f.Host.Starts == 0, "Setup-only launched an interactive menu.");
    }
    private static async Task NoOwnership()
    {
        using var f = new Fixture(); f.Host.Info = new(true, OpenShellPayload.Version);
        await f.Session.UndoAsync(); Check(f.Host.Uninstalls == 0, "Undo removed somebody else's Open-Shell.");
    }
}
