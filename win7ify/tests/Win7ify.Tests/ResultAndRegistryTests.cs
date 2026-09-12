using Boop.Win7ify.Core;
using Microsoft.Win32;
using System.Text.Json;
using static Boop.Win7ify.Tests.PermissionRegressionTests;

namespace Boop.Win7ify.Tests;

internal static class ResultAndRegistryTests
{
    public static void BlockedResultNamesExactSetting()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["blocked"] = RegistryStoredValue.DWord(1);
        store.Deny = "blocked";
        var report = service.Apply(new[] { "blocked", "later" });
        Check(report.Count == 2, "Every selected setting needs an outcome.");
        Check(report[0].Status == ChangeStatus.Blocked && report[0].Name == "blocked" && report[0].Label == "Hide Widgets", "Wrong blocked result.");
        Check(report[1].Status == ChangeStatus.Changed, "Later setting must report saved.");
    }
    public static void UnchangedIsNotReportedAsWritten()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["later"] = RegistryStoredValue.DWord(9);
        var report = service.Apply(new[] { "later" });
        Check(report.Single().Status == ChangeStatus.AlreadyCorrect && store.Writes == 0, "No-op was reported as a write.");
    }
    public static void IgnoredWriteIsNotSuccess()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.IgnoreWrites = true;
        var report = service.Apply(new[] { "later" });
        Check(report.Single().Status == ChangeStatus.Failed, "Write without matching readback claimed success.");
        Check(backup.HasBackup, "Unverified operation lost its baseline.");
    }
    public static void RetryRestoreKeepsFirstBaseline()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["blocked"] = RegistryStoredValue.DWord(1);
        store.Values["later"] = RegistryStoredValue.DWord(2);
        service.Apply(new[] { "blocked", "later" });
        var baseline = File.ReadAllText(backup.BackupPath);
        store.Deny = "blocked";
        var first = service.RestoreAll();
        Check(first.Any(r => r.Status == ChangeStatus.Blocked), "Partial failure missing.");
        Check(File.ReadAllText(backup.BackupPath) == baseline, "Partial restore overwrote the original backup.");
        store.Deny = null;
        var second = service.RestoreAll();
        Check(second.All(r => r.Succeeded) && !backup.HasBackup, "Completed retry failed.");
        Check(store.Values["blocked"] == RegistryStoredValue.DWord(1), "Original value was lost.");
    }
    public static void InvalidBackupPathCannotEscapeAllowlist()
    {
        var (store, oldBackup, service, tweaks) = Fixture();
        var backup = new BackupService(store, oldBackup.BackupPath, tweaks);
        Directory.CreateDirectory(Path.GetDirectoryName(backup.BackupPath)!);
        var doc = new BackupService.BackupDocument();
        doc.Entries.Add(new BackupService.BackupEntry(@"Software\Unrelated", "bad", RegistryStoredValue.DWord(1)));
        File.WriteAllText(backup.BackupPath, JsonSerializer.Serialize(doc));
        try { backup.RestoreAll(); }
        catch (InvalidDataException) { Check(store.Writes == 0 && backup.HasBackup, "Invalid backup was not kept safely."); return; }
        throw new Exception("Backup escaped the allowed setting list.");
    }
    public static void CorruptBackupStopsBeforeWrite()
    {
        var (store, backup, service, tweaks) = Fixture();
        Directory.CreateDirectory(Path.GetDirectoryName(backup.BackupPath)!);
        File.WriteAllText(backup.BackupPath, "{incomplete");
        try { service.Apply(new[] { "later" }); }
        catch (InvalidDataException) { Check(store.Writes == 0 && File.ReadAllText(backup.BackupPath) == "{incomplete", "Corrupt backup was overwritten."); return; }
        throw new Exception("Corrupt backup was ignored.");
    }
    public static void RealWindowsRegistryRoundTrip()
    {
        // A disposable test subtree only. Never use the desktop tweak catalogue here.
        var id = Guid.NewGuid().ToString("N");
        var path = @"Software\BOOP\Win7ifySelfTests\" + id;
        var file = Path.Combine(Path.GetTempPath(), "boop-win7ify-tests", id + ".json");
        var store = new WindowsRegistryStore();
        var originals = new[] { RegistryStoredValue.DWord(17), RegistryStoredValue.ExpandString(@"%USERPROFILE%\test"),
            RegistryStoredValue.Binary(1, 2, 250), RegistryStoredValue.MultiString("a", "b"),
            RegistryStoredValue.QWord(12345678901234), RegistryStoredValue.String("text"), RegistryStoredValue.Missing };
        var tweaks = originals.Select((v, i) => new TweakDefinition(i.ToString(), "Scratch " + i, path, "value" + i, RegistryStoredValue.String("changed"), false)).ToArray();
        try
        {
            for (var i = 0; i < originals.Length; i++) if (originals[i].Exists) store.Write(path, "value" + i, originals[i]);
            var backup = new BackupService(store, file, tweaks);
            var service = new Win7ifyService(store, backup, tweaks);
            Check(service.Apply(tweaks.Select(t => t.Id)).All(r => r.Status == ChangeStatus.Changed), "Real adapter apply failed.");
            Check(service.RestoreAll().All(r => r.Succeeded), "Real adapter restore failed.");
            for (var i = 0; i < originals.Length; i++)
                Check(JsonSerializer.Serialize(store.Read(path, "value" + i)) == JsonSerializer.Serialize(originals[i]), "Real typed restore mismatch at " + i);
            Check(!backup.HasBackup, "Successful restore left active backup.");
        }
        finally
        {
            Registry.CurrentUser.DeleteSubKeyTree(path, throwOnMissingSubKey: false);
            if (File.Exists(file)) File.Delete(file);
        }
    }
}
