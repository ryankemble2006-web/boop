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
    public const string Settings = @"Software\OpenShell\StartMenu\Settings";
    public static IReadOnlyList<TweakDefinition> All { get; } = Array.Empty<TweakDefinition>();
}
public static class OpenShellPayload
{
    public const string Version = "4.4.198";
    public const long Size = 9924608;
    public const string Sha256 = "a4d2d4459de55b5e962ba2a14f7bb794170511649138173dfa72949837b48c3f";
    public static void Verify(Stream data) { }
}
