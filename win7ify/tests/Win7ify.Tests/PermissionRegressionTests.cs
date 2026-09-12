using Boop.Win7ify.Core;
using System.Text.Json;

namespace Boop.Win7ify.Tests;

internal static class PermissionRegressionTests
{
    private const string PathName = @"Software\BOOP\Win7ifyUnitTest";
    public static void AlreadyCorrectDoesNotWrite()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["blocked"] = RegistryStoredValue.DWord(0);
        store.Deny = "blocked";
        service.Apply(new[] { "blocked" });
        Check(store.Writes == 0, "Already-correct settings must not be written.");
    }
    public static void BlockedSettingDoesNotAbandonOthers()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["blocked"] = RegistryStoredValue.DWord(1);
        store.Deny = "blocked";
        service.Apply(new[] { "blocked", "later" });
        Check(store.Read(PathName, "later") == RegistryStoredValue.DWord(9), "A blocked value abandoned the later setting.");
        Check(backup.HasBackup, "Original backup must survive partial apply.");
    }
    public static void RestoreSkipsUnchangedProtectedValue()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["blocked"] = RegistryStoredValue.DWord(0);
        backup.EnsureBackedUp(tweaks.Take(1));
        store.Deny = "blocked";
        backup.RestoreAll();
        Check(store.Writes == 0, "Restore must not rewrite unchanged protected values.");
    }
    public static void RestoreContinuesAfterBlockedValue()
    {
        var (store, backup, service, tweaks) = Fixture();
        store.Values["blocked"] = RegistryStoredValue.DWord(1);
        store.Values["later"] = RegistryStoredValue.DWord(2);
        backup.EnsureBackedUp(tweaks);
        store.Values["blocked"] = RegistryStoredValue.DWord(0);
        store.Values["later"] = RegistryStoredValue.DWord(9);
        store.Deny = "blocked";
        backup.RestoreAll();
        Check(store.Read(PathName, "later") == RegistryStoredValue.DWord(2), "Blocked restore abandoned a restorable value.");
        Check(backup.HasBackup, "A partial restore must keep its backup.");
    }
    public static void UnknownBackupVersionCannotWrite()
    {
        var (store, backup, service, tweaks) = Fixture();
        Directory.CreateDirectory(System.IO.Path.GetDirectoryName(backup.BackupPath)!);
        File.WriteAllText(backup.BackupPath, "{\"Version\":999,\"Entries\":[]}");
        try { service.Apply(new[] { "later" }); }
        catch (InvalidDataException) { Check(store.Writes == 0, "No writes with invalid backup."); return; }
        throw new Exception("Unknown backup version was silently accepted.");
    }
    internal static void Check(bool condition, string message)
    {
        if (!condition) throw new Exception(message);
    }
    internal static (FaultStore Store, BackupService Backup, Win7ifyService Service, TweakDefinition[] Tweaks) Fixture()
    {
        var store = new FaultStore();
        var path = System.IO.Path.Combine(System.IO.Path.GetTempPath(), "boop-win7ify-tests", Guid.NewGuid()+".json");
        var backup = new BackupService(store, path);
        var tweaks = new[] {
            new TweakDefinition("blocked", "Hide Widgets", PathName, "blocked", RegistryStoredValue.DWord(0), false),
            new TweakDefinition("later", "Later setting", PathName, "later", RegistryStoredValue.DWord(9), false)
        };
        return (store, backup, new Win7ifyService(store, backup, tweaks), tweaks);
    }
    internal sealed class FaultStore : IRegistryStore
    {
        public Dictionary<string, RegistryStoredValue> Values { get; } = new();
        public string? Deny { get; set; }
        public bool IgnoreWrites { get; set; }
        public int Writes { get; private set; }
        public RegistryStoredValue Read(string path, string name) => Values.GetValueOrDefault(name, RegistryStoredValue.Missing);
        public void Write(string path, string name, RegistryStoredValue value)
        {
            Writes++;
            if (name == Deny) throw new UnauthorizedAccessException("Attempted to perform an unauthorized operation.");
            if (!IgnoreWrites) Values[name] = value;
        }
        public void Delete(string path, string name)
        {
            if (name == Deny) throw new UnauthorizedAccessException("Delete denied.");
            Values.Remove(name);
        }
        public void DeleteKeyIfEmpty(string path) { }
    }
}
