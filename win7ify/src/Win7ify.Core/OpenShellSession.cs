namespace Boop.Win7ify.Core;

public sealed class OpenShellSession(string folder, IRegistryStore registry, IOpenShellHost host)
{
    public bool HasSession => File.Exists(Path.Combine(folder, "open-shell-session-v1.json"));
    public Task EnableAsync(Action<ShellEvent>? progress = null, bool launch = true) => throw new NotSupportedException("Open-Shell integration not implemented yet.");
    public Task UndoAsync(Action<ShellEvent>? progress = null) => throw new NotSupportedException("Open-Shell recovery not implemented yet.");
}
