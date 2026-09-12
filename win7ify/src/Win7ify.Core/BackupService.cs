using System.Text.Json;

namespace Boop.Win7ify.Core;

public sealed class BackupService
{
    private readonly IRegistryStore _registry;
    private readonly string _backupPath;
    private static readonly JsonSerializerOptions JsonOptions = new() { WriteIndented = true };

    public BackupService(IRegistryStore registry, string backupPath)
    {
        _registry = registry;
        _backupPath = backupPath;
    }

    public string BackupPath => _backupPath;
    public bool HasBackup => File.Exists(_backupPath);

    public void EnsureBackedUp(IEnumerable<TweakDefinition> tweaks)
    {
        var document = Load() ?? new BackupDocument();
        var changed = false;

        foreach (var tweak in tweaks)
        {
            if (document.Entries.Any(entry => SameValue(entry, tweak.Path, tweak.Name)))
                continue;

            document.Entries.Add(new BackupEntry(tweak.Path, tweak.Name, _registry.Read(tweak.Path, tweak.Name)));
            changed = true;
        }

        if (!changed) return;
        var directory = Path.GetDirectoryName(_backupPath);
        if (!string.IsNullOrWhiteSpace(directory)) Directory.CreateDirectory(directory);
        File.WriteAllText(_backupPath, JsonSerializer.Serialize(document, JsonOptions));
    }

    public int RestoreAll()
    {
        var document = Load();
        if (document is null) return 0;

        foreach (var entry in document.Entries)
        {
            if (entry.Value.Exists)
                _registry.Write(entry.Path, entry.Name, entry.Value);
            else
            {
                _registry.Delete(entry.Path, entry.Name);
                _registry.DeleteKeyIfEmpty(entry.Path);
            }
        }

        File.Delete(_backupPath);
        return document.Entries.Count;
    }

    private BackupDocument? Load()
    {
        if (!File.Exists(_backupPath)) return null;
        return JsonSerializer.Deserialize<BackupDocument>(File.ReadAllText(_backupPath), JsonOptions)
            ?? throw new InvalidDataException("Win7ify backup file is empty or invalid.");
    }

    private static bool SameValue(BackupEntry entry, string path, string name) =>
        string.Equals(entry.Path, path, StringComparison.OrdinalIgnoreCase) &&
        string.Equals(entry.Name, name, StringComparison.OrdinalIgnoreCase);

    public sealed class BackupDocument
    {
        public int Version { get; set; } = 1;
        public DateTime CreatedUtc { get; set; } = DateTime.UtcNow;
        public List<BackupEntry> Entries { get; set; } = new();
    }

    public sealed record BackupEntry(string Path, string Name, RegistryStoredValue Value);
}