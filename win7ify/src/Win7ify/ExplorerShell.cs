using System.Diagnostics;

namespace Boop.Win7ify;

internal static class ExplorerShell
{
    public static void Restart()
    {
        foreach (var process in Process.GetProcessesByName("explorer"))
        {
            try
            {
                process.Kill();
                process.WaitForExit(3000);
            }
            catch
            {
                // Explorer may already be exiting or Windows may deny one instance.
            }
            finally
            {
                process.Dispose();
            }
        }

        Thread.Sleep(250);
        if (Process.GetProcessesByName("explorer").Length == 0)
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = "explorer.exe",
                UseShellExecute = true
            });
        }
    }
}