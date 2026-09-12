using Boop.Win7ify.Core;
using System.Text.Json;

namespace Boop.Win7ify;

internal static class Program
{
    [STAThread]
    private static int Main(string[] args)
    {
        ApplicationConfiguration.Initialize();
        var log = new DiagnosticLog();
        using var host = new WindowsOpenShellHost(log);
        var registry = new WindowsRegistryStore();
        var session = new OpenShellSession(AppPaths.Folder, registry, host);
        if (args.Length > 0) return CommandAsync(args, session, host, log).GetAwaiter().GetResult();
        using var single = new Mutex(true, @"Local\BOOP.Win7ify", out var first);
        if (!first) { MessageBox.Show("BOOP Win7ify is already open. Use that window first.", "BOOP Win7ify"); return 1; }
        Application.ThreadException += (_, e) => log.Failure(e.Exception);
        AppDomain.CurrentDomain.UnhandledException += (_, e) => {
            if (e.ExceptionObject is Exception ex) log.Failure(ex);
        };
        try
        {
            var backup = new BackupService(registry, Path.Combine(AppPaths.Folder, "backup-v1.json"), TweakCatalog.All);
            var desktop = new Win7ifyService(registry, backup, TweakCatalog.All);
            Application.Run(new StartMenuForm(session, host, log, desktop, backup));
            return 0;
        }
        catch (Exception ex)
        {
            log.Failure(ex);
            MessageBox.Show("BOOP E299\r\n" + ex.Message + "\r\nDetails: " + log.FilePath, "BOOP Win7ify could not open", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            return 1;
        }
    }
    private static async Task<int> CommandAsync(string[] args, OpenShellSession session, WindowsOpenShellHost host, DiagnosticLog log)
    {
        var exit = 0;
        var command = args.Length == 1 ? args[0] : "invalid";
        try
        {
            switch (command)
            {
                case "--setup-start-menu": await session.EnableAsync(log.Add, launch: false); break;
                case "--install-start-menu": await session.EnableAsync(log.Add); break;
                case "--activate-start-menu": await host.VerifyProfileAsync(); await host.StartAsync(true); break;
                case "--undo-start-menu": await session.UndoAsync(log.Add); break;
                case "--verify-payload": await host.VerifyPayloadAsync(); break;
                case "--diagnose": log.Add(new("BOOP REPORT", "Read-only menu diagnostics", JsonSerializer.Serialize(host.Inspect()))); break;
                case "--diagnostics-selftest": throw new ShellProblem("BOOP E298", "Diagnostic test message. No settings were changed.");
                default: throw new ShellProblem("BOOP E202", "Unknown command. No setup action was started.");
            }
        }
        catch (Exception ex) { exit = 1; log.Failure(ex); }
        Directory.CreateDirectory(AppPaths.Folder);
        File.WriteAllText(Path.Combine(AppPaths.Folder, "last-command.txt"), log.Report());
        File.WriteAllText(Path.Combine(AppPaths.Folder, "last-command.json"), JsonSerializer.Serialize(new {
            Version = "0.2.0", Command = command, ExitCode = exit, FinishedUtc = DateTimeOffset.UtcNow,
            ProcessId = Environment.ProcessId, Report = log.Report()
        }, new JsonSerializerOptions { WriteIndented = true }));
        return exit;
    }
}
