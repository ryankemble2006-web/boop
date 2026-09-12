namespace Boop.Win7ify.Core;

public sealed class Win7ifyService
{
    private readonly IRegistryStore _registry;
    private readonly BackupService _backup;
    private readonly IReadOnlyList<TweakDefinition> _tweaks;

    public Win7ifyService(IRegistryStore registry, BackupService backup, IReadOnlyList<TweakDefinition> tweaks)
    {
        _registry = registry;
        _backup = backup;
        _tweaks = tweaks;
    }

    public IReadOnlyList<ChangeResult> Apply(IEnumerable<string> tweakIds, Action<ChangeResult>? progress = null)
    {
        var requested = new HashSet<string>(tweakIds, StringComparer.OrdinalIgnoreCase);
        var known = new HashSet<string>(_tweaks.Select(t => t.Id), StringComparer.OrdinalIgnoreCase);
        var unknown = requested.Where(id => !known.Contains(id)).ToArray();
        if (unknown.Length > 0)
            throw new ArgumentException($"Unknown Win7ify tweak: {string.Join(", ", unknown)}", nameof(tweakIds));
        var chosen = _tweaks.Where(t => requested.Contains(t.Id)).ToArray();
        if (chosen.Length == 0) return Array.Empty<ChangeResult>();

        // This must finish before ANY registry write, including a partially applied batch.
        _backup.EnsureBackedUp(chosen);
        var results = new List<ChangeResult>();
        foreach (var tweak in chosen)
        {
            var result = RegistryChange.Execute(_registry, tweak.Label, tweak.Path, tweak.Name, tweak.AppliedValue);
            results.Add(result);
            progress?.Invoke(result);
        }
        return results;
    }

    public IReadOnlyList<ChangeResult> RestoreAll(Action<ChangeResult>? progress = null) => _backup.RestoreAll(progress);
}
