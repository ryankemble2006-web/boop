using System.Diagnostics;
using System.Runtime.InteropServices;

namespace Boop.Win7ify;

internal static class ExplorerShell
{
    [DllImport("user32.dll")] private static extern IntPtr GetShellWindow();
    [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr window, out uint processId);

    public static void Restart()
    {
        var window = GetShellWindow();
        if (window == IntPtr.Zero) throw new InvalidOperationException("No desktop shell found. Nothing was closed.");
        GetWindowThreadProcessId(window, out var pid);
        using var current = Process.GetCurrentProcess();
        using var shell = Process.GetProcessById(checked((int)pid));
        if (shell.SessionId != current.SessionId || !shell.ProcessName.Equals("explorer", StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("The desktop is not a normal Explorer in this session. Nothing was closed.");
        // Called only after an explicit warning and confirmation. Never kill every explorer process.
        shell.Kill();
        if (!shell.WaitForExit(5000)) throw new IOException("Explorer did not stop. No extra processes were closed.");
        Thread.Sleep(1500);
        if (GetShellWindow() == IntPtr.Zero)
        {
            using var started = Process.Start(new ProcessStartInfo(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "explorer.exe")) { UseShellExecute = true });
        }
        for (var i = 0; i < 30; i++)
        {
            if (GetShellWindow() != IntPtr.Zero) return;
            Thread.Sleep(250);
        }
        throw new IOException("Windows has not reported its desktop back yet. Use Task Manager > Run new task > explorer.exe if it stays missing.");
    }
}
