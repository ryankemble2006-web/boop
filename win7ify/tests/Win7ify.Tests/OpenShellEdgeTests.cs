using Boop.Win7ify.Core;

namespace Boop.Win7ify.Tests;

internal static class OpenShellEdgeTests
{
    public static IEnumerable<(string Name, Action Run)> Cases => new (string, Action)[] {
        ("missing profile cannot stop an existing menu", () => MissingOrCorrupt(false).GetAwaiter().GetResult()),
        ("corrupt profile cannot stop an existing menu", () => MissingOrCorrupt(true).GetAwaiter().GetResult()),
        ("correct size but modified payload is rejected", TamperedPayload),
        ("blocked profile keeps recovery and does not launch", () => BlockedProfile().GetAwaiter().GetResult()),
        ("failed uninstall retains original profile through retry", () => RetainedProfile().GetAwaiter().GetResult()),
        ("interrupted owned installation is recovered without reinstall", () => InterruptedInstall().GetAwaiter().GetResult()),
        ("pending journal cannot claim another install location", () => OtherLocation().GetAwaiter().GetResult()),
        ("repeated configuration preserves pre-existing menu style", () => RepeatPreservesOriginal().GetAwaiter().GetResult())
    };
    private static void Check(bool condition, string message) { if (!condition) throw new Exception(message); }
    private sealed class TestRegistry : IRegistryStore
    {
        public readonly FakeRegistryStore Inner = new();
        public bool BlockMenuStyle;
        public RegistryStoredValue Read(string p, string n) => Inner.Read(p, n);
        public void Write(string p, string n, RegistryStoredValue v) {
            if (BlockMenuStyle && n == "MenuStyle") throw new UnauthorizedAccessException("protected fixture");
            Inner.Write(p, n, v);
        }
        public void Delete(string p, string n) => Inner.Delete(p, n);
        public void DeleteKeyIfEmpty(string p) { }
    }
    private sealed class TestHost : IOpenShellHost
    {
        public ShellInfo Info = new(false);
        public int Stops, Installs, Starts, Removes, InstallCode, RemoveCode;
        public ShellInfo Inspect() => Info;
        public void CheckEnvironment() { }
        public Task VerifyPayloadAsync() => Task.CompletedTask;
        public Task<int> InstallAsync() { Installs++; if (InstallCode == 0) Info = new(true, OpenShellPayload.Version, true); return Task.FromResult(InstallCode); }
        public Task StopAsync() { Stops++; Info = Info with { Running = false }; return Task.CompletedTask; }
        public Task VerifyProfileAsync() => Task.CompletedTask;
        public Task StartAsync(bool openMenu) { Starts++; Info = Info with { Running = true }; return Task.CompletedTask; }
        public Task<int> UninstallAsync() { Removes++; if (RemoveCode == 0) Info = new(false); return Task.FromResult(RemoveCode); }
    }
    private sealed class Fixture : IDisposable
    {
        public readonly string Folder = Path.Combine(Path.GetTempPath(), "boop-shell-edges", Guid.NewGuid().ToString("N"));
        public readonly TestRegistry Registry = new();
        public readonly TestHost Host = new();
        public string Profile => Path.Combine(Folder, "open-shell-profile-v1.json");
        public OpenShellSession Session => new(Folder, Registry, Host);
        public void Dispose() { if (Directory.Exists(Folder)) Directory.Delete(Folder, true); }
    }
    private static async Task MissingOrCorrupt(bool corrupt)
    {
        using var f = new Fixture(); f.Host.Info = new(true, OpenShellPayload.Version, false, true);
        await f.Session.EnableAsync(); var stopped = f.Host.Stops;
        if (corrupt) File.WriteAllText(f.Profile, "broken"); else File.Delete(f.Profile);
        try { await f.Session.UndoAsync(); throw new Exception("Invalid backup was accepted."); }
        catch (Exception ex) when (ex is ShellProblem or InvalidDataException) { }
        Check(f.Host.Stops == stopped && f.Host.Info.Running, "Existing menu was stopped before its recovery data was validated.");
        Check(f.Host.Removes == 0 && f.Session.HasSession, "Recovery was lost or the installation removed.");
    }
    private static void TamperedPayload()
    {
        using var bytes = new MemoryStream(new byte[(int)OpenShellPayload.Size]);
        try { OpenShellPayload.Verify(bytes); throw new Exception("Hash check accepted modified payload."); }
        catch (ShellProblem ex) { Check(ex.Code == "BOOP E210", "Wrong integrity code."); }
    }
    private static async Task BlockedProfile()
    {
        using var f = new Fixture(); f.Registry.BlockMenuStyle = true;
        try { await f.Session.EnableAsync(); throw new Exception("Blocked settings were reported active."); }
        catch (ShellProblem ex) { Check(ex.Code == "BOOP E230", "Blocked profile lacked a useful code."); }
        Check(f.Host.Starts == 0 && f.Session.HasSession && File.Exists(f.Profile), "Blocked Apply lost recovery or opened an unconfigured menu.");
        f.Registry.BlockMenuStyle = false; await f.Session.UndoAsync(); Check(f.Host.Removes == 1, "Blocked installation could not be undone.");
    }
    private static async Task RetainedProfile()
    {
        using var f = new Fixture(); f.Registry.Inner.Seed(OpenShellProfile.Settings, "MenuStyle", RegistryStoredValue.String("Classic2"));
        await f.Session.EnableAsync(); var original = File.ReadAllBytes(f.Profile); f.Host.RemoveCode = 1603;
        try { await f.Session.UndoAsync(); throw new Exception("Uninstall failure was ignored."); } catch (ShellProblem) { }
        Check(File.ReadAllBytes(f.Profile).SequenceEqual(original), "Original profile was deleted or replaced after incomplete removal.");
        f.Registry.Inner.Seed(OpenShellProfile.Settings, "MenuStyle", RegistryStoredValue.String("Win7"));
        f.Host.RemoveCode = 0; await f.Session.UndoAsync();
        Check(f.Registry.Read(OpenShellProfile.Settings, "MenuStyle").Text == "Classic2", "Retry did not preserve the first baseline.");
    }
    private static async Task InterruptedInstall()
    {
        using var f = new Fixture(); f.Host.InstallCode = 1602;
        try { await f.Session.EnableAsync(); } catch (ShellProblem) { }
        f.Host.Info = new(true, OpenShellPayload.Version, true);
        await f.Session.EnableAsync(); Check(f.Host.Installs == 1, "Recovered installation was needlessly reinstalled.");
        await f.Session.UndoAsync(); Check(f.Host.Removes == 1, "Recovered owned installation could not be removed.");
    }
    private static async Task OtherLocation()
    {
        using var f = new Fixture(); f.Host.InstallCode = 1602;
        try { await f.Session.EnableAsync(); } catch (ShellProblem) { }
        f.Host.Info = new(true, OpenShellPayload.Version, false);
        try { await f.Session.EnableAsync(); throw new Exception("Another installation was claimed."); } catch (ShellProblem) { }
        try { await f.Session.UndoAsync(); throw new Exception("Another installation was accepted for removal."); } catch (ShellProblem) { }
        Check(f.Host.Removes == 0 && f.Session.HasSession, "Another installation was removed or recovery was discarded.");
    }
    private static async Task RepeatPreservesOriginal()
    {
        using var f = new Fixture(); f.Host.Info = new(true, OpenShellPayload.Version, false);
        f.Registry.Inner.Seed(OpenShellProfile.Settings, "MenuStyle", RegistryStoredValue.String("Classic2"));
        await f.Session.EnableAsync(); f.Registry.Inner.Seed(OpenShellProfile.Settings, "MenuStyle", RegistryStoredValue.String("Classic1"));
        await f.Session.EnableAsync(); await f.Session.UndoAsync();
        Check(f.Registry.Read(OpenShellProfile.Settings, "MenuStyle").Text == "Classic2" && f.Host.Removes == 0, "The original existing-menu baseline was lost.");
    }
}
