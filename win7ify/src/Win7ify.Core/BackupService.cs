using System.Text.Json;

namespace Boop.Win7ify.Core;

public sealed class BackupService
{
    private readonly IRegistryStore _registry;
    private readonly string _backupPath;
    private readonly IReadOnlyList<TweakDefinition>? _allowed;
    private static readonly JsonSerializerOptions JsonOptions = new() { WriteIndented = true };

    public BackupService(IRegistryStore registry, string backupPath, IReadOnlyList<TweakDefinition>? allowed = null)
    {
        _registry = registry;
        _backupPath = backupPath;
        _allowed = allowed;
    }
    public string BackupPath => _backupPath;
    public bool HasBackup => File.Exists(_backupPath);

    public void EnsureBackedUp(IEnumerable<TweakDefinition> tweaks)
    {
        var document = Load() ?? new BackupDocument();
        var changed = false;
        foreach (var tweak in tweaks)
        {
            if (document.Entries.Any(e => SameValue(e, tweak.Path, tweak.Name))) continue;
            document.Entries.Add(new BackupEntry(tweak.Path, tweak.Name, _registry.Read(tweak.Path, tweak.Name)));
            changed = true;
        }
        Validate(document);
        if (changed) SaveAtomically(document);
    }

    public IReadOnlyList<ChangeResult> RestoreAll(Action<ChangeResult>? progress = null, bool keepBackup = false)
    {
        var document = Load();
        if (document is null) return Array.Empty<ChangeResult>();
        var results = new List<ChangeResult>();
        foreach (var entry in document.Entries)
        {
            var label = _allowed?.FirstOrDefault(t => SameValue(entry, t.Path, t.Name))?.Label ?? entry.Name;
            var result = RegistryChange.Execute(_registry, label, entry.Path, entry.Name, entry.Value);
            results.Add(result);
            progress?.Invoke(result);
        }
        // Multi-stage recovery can keep the baseline until its installer removal also succeeds.
        if (!keepBackup && results.All(r => r.Succeeded)) File.Delete(_backupPath);
        return results;
    }

    private void SaveAtomically(BackupDocument document)
    {
        var directory = Path.GetDirectoryName(Path.GetFullPath(_backupPath))!;
        Directory.CreateDirectory(directory);
        var temp = Path.Combine(directory, $".backup-{Guid.NewGuid():N}.tmp");
        try
        {
            using (var stream = new FileStream(temp, FileMode.CreateNew, FileAccess.Write, FileShare.None,
                4096, FileOptions.WriteThrough))
            {
                JsonSerializer.Serialize(stream, document, JsonOptions);
                stream.Flush(flushToDisk: true);
            }
            if (File.Exists(_backupPath)) File.Replace(temp, _backupPath, null);
            else File.Move(temp, _backupPath);
        }
        finally
        {
            if (File.Exists(temp)) File.Delete(temp);
        }
    }

    private BackupDocument? Load()
    {
        if (!File.Exists(_backupPath)) return null;
        if (new FileInfo(_backupPath).Length > 2_000_000)
            throw new InvalidDataException("The backup is unexpectedly large. Kept untouched; no settings written.");
        BackupDocument document;
        try
        {
            document = JsonSerializer.Deserialize<BackupDocument>(File.ReadAllText(_backupPath), JsonOptions)
                ?? throw new InvalidDataException("The backup is empty. Kept untouched; no settings written.");
        }
        catch (JsonException ex)
        {
            throw new InvalidDataException("The backup could not be read. Kept untouched; no settings written.", ex);
        }
        Validate(document);
        return document;
    }

    private void Validate(BackupDocument document)
    {
        if (document.Version != 1 || document.Entries is null || document.Entries.Count > 100)
            throw new InvalidDataException("This backup format is not supported. Kept untouched; no settings written.");
        var seen = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        foreach (var entry in document.Entries)
        {
            if (entry is null || string.IsNullOrWhiteSpace(entry.Path) || entry.Name is null || entry.Value is null ||
                !seen.Add(entry.Path + "\0" + entry.Name))
                throw new InvalidDataException("The backup contains an invalid or duplicate entry. Nothing written.");
            if (_allowed is not null && !_allowed.Any(t => SameValue(entry, t.Path, t.Name)))
                throw new InvalidDataException("The backup names a setting outside Win7ify. Nothing written.");
            if (entry.Value.Exists)
            {
                if (entry.Value.Kind is null || !Enum.IsDefined(entry.Value.Kind.Value))
                    throw new InvalidDataException("The backup has an unknown value type. Nothing written.");
                if (entry.Value.Kind is RegistryDataKind.DWord && (entry.Value.Number is null || entry.Value.Number < int.MinValue || entry.Value.Number > int.MaxValue))
                    throw new InvalidDataException("The backup has an invalid number. Nothing written.");
                if (entry.Value.Kind is RegistryDataKind.QWord && entry.Value.Number is null ||
                    entry.Value.Kind is RegistryDataKind.String or RegistryDataKind.ExpandString && entry.Value.Text is null ||
                    entry.Value.Kind is RegistryDataKind.Binary && entry.Value.Bytes is null ||
                    entry.Value.Kind is RegistryDataKind.MultiString && (entry.Value.MultiText is null || entry.Value.MultiText.Any(s => s is null)))
                    throw new InvalidDataException("The backup has incomplete data. Nothing written.");
            }
        }
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
