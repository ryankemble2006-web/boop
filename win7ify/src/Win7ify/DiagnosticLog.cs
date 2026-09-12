using Boop.Win7ify.Core;
using System.Runtime.InteropServices;
using System.Text;

namespace Boop.Win7ify;

internal static class AppPaths
{
    public static string Folder => Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "BOOP", "Win7ify");
    public static string LogFolder => Path.Combine(Folder, "logs");
}
internal sealed class DiagnosticLog
{
    private readonly object _gate = new();
    private readonly StringBuilder _text = new();
    public string FilePath { get; } = Path.Combine(AppPaths.LogFolder, $"Win7ify-0.2.0-{DateTime.Now:yyyyMMdd-HHmmss}-{Environment.ProcessId}.log");
    public event Action<ShellEvent>? Updated;
    public DiagnosticLog()
    {
        _text.AppendLine("BOOP Win7ify 0.2.0 diagnostic report");
        _text.AppendLine("Windows: " + Environment.OSVersion.VersionString + "; architecture: " + RuntimeInformation.OSArchitecture);
        _text.AppendLine("Open-Shell payload: " + OpenShellPayload.Version + "; SHA256: " + OpenShellPayload.Sha256);
        _text.AppendLine("Checksums are not code signatures. No security or permissions bypasses.");
        _text.AppendLine("Reports stay local. Review before sharing. No microphone, cloud calls or telemetry.");
    }
    public void Add(ShellEvent entry)
    {
        lock (_gate)
        {
            _text.AppendLine($"[{DateTimeOffset.Now:O}] {entry.Code}: {entry.Message}");
            if (!string.IsNullOrWhiteSpace(entry.Detail)) _text.AppendLine(entry.Detail);
            try
            {
                Directory.CreateDirectory(AppPaths.LogFolder);
                File.AppendAllText(FilePath, $"[{DateTimeOffset.Now:O}] {entry.Code}: {entry.Message}{Environment.NewLine}{entry.Detail}{Environment.NewLine}");
            }
            catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
            { _text.AppendLine("BOOP E290: Disk log unavailable. Use COPY REPORT. " + ex.Message); }
        }
        Updated?.Invoke(entry);
    }
    public void Failure(Exception ex)
    {
        var code = (ex as ShellProblem)?.Code ?? "BOOP E299";
        Add(new(code, ex.Message, ex.ToString()));
    }
    public string Report()
    {
        lock (_gate)
        {
            var text = _text.ToString();
            var profile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            return string.IsNullOrEmpty(profile) ? text : text.Replace(profile, "%USERPROFILE%", StringComparison.OrdinalIgnoreCase);
        }
    }
}
