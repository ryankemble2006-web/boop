using Boop.Win7ify.Core;

namespace Boop.Win7ify;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();

        if (!OperatingSystem.IsWindows())
        {
            MessageBox.Show(
                "BOOP Win7ify only runs on Windows.",
                "BOOP Win7ify",
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
            return;
        }

        var backupPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "BOOP",
            "Win7ify",
            "backup-v1.json");

        var registry = new WindowsRegistryStore();
        var backup = new BackupService(registry, backupPath);
        var service = new Win7ifyService(registry, backup, TweakCatalog.All);

        Application.Run(new MainForm(service, backup));
    }
}