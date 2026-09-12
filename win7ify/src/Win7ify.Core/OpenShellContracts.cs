using System.Security.Cryptography;

namespace Boop.Win7ify.Core;

public sealed record ShellInfo(bool Installed, string Version = "", bool OwnedLocation = false, bool Running = false);
public sealed record ShellEvent(string Code, string Message, string Detail = "");
public sealed class ShellProblem(string code, string message, Exception? inner = null) : Exception(message, inner)
{
    public string Code { get; } = code;
}
public interface IOpenShellHost
{
    ShellInfo Inspect();
    void CheckEnvironment();
    Task VerifyPayloadAsync();
    Task<int> InstallAsync();
    Task StopAsync();
    Task VerifyProfileAsync();
    Task StartAsync(bool openMenu);
    Task<int> UninstallAsync();
}
public static class OpenShellProfile
{
    public const string Root = @"Software\OpenShell\StartMenu";
    public const string Settings = Root + @"\Settings";
    public static IReadOnlyList<TweakDefinition> All { get; } = Array.AsReadOnly(new[] {
        Setting("MenuStyle", RegistryStoredValue.String("Win7")),
        Setting("SkinW7", RegistryStoredValue.String("Windows Aero")),
        Setting("SkinVariationW7", RegistryStoredValue.String("")),
        Setting("EnableStartButton", RegistryStoredValue.DWord(1)),
        Setting("StartButtonType", RegistryStoredValue.String("AeroButton")),
        Setting("MouseClick", RegistryStoredValue.String("ClassicMenu")),
        Setting("ShiftClick", RegistryStoredValue.String("WindowsMenu")),
        Setting("WinKey", RegistryStoredValue.String("ClassicMenu")),
        Setting("ShiftWin", RegistryStoredValue.String("WindowsMenu")),
        new TweakDefinition("openshell.first-run", "Skip the already configured style picker", Root, "ShowedStyle2", RegistryStoredValue.DWord(1)),
        new TweakDefinition("openshell.taskbar-left", "Align Start on the left", @"Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced", "TaskbarAl", RegistryStoredValue.DWord(0))
    });
    private static TweakDefinition Setting(string name, RegistryStoredValue value) =>
        new("openshell." + name, name, Settings, name, value);
}
public static class OpenShellPayload
{
    public const string Version = "4.4.198";
    public const long Size = 9924608;
    public const string Sha256 = "a4d2d4459de55b5e962ba2a14f7bb794170511649138173dfa72949837b48c3f";
    public static void Verify(Stream data)
    {
        if (!data.CanRead || !data.CanSeek || data.Length != Size)
            throw new ShellProblem("BOOP E210", "The bundled installer is incomplete. Nothing was installed.");
        data.Position = 0;
        var actual = Convert.ToHexString(SHA256.HashData(data)).ToLowerInvariant();
        if (!StringComparer.Ordinal.Equals(actual, Sha256))
            throw new ShellProblem("BOOP E210", "The bundled installer failed its integrity check. Nothing was installed.");
        data.Position = 0;
    }
}
