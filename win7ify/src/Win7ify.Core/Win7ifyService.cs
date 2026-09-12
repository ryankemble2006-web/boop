namespace Boop.Win7ify.Core;

public sealed class Win7ifyService
{
    private readonly IRegistryStore _registry;
    private readonly BackupService _backup;
    private readonly IReadOnlyList<TweakDefinition> _tweaks;

    public Win7ifyService(
        IRegistryStore registry,
        BackupService backup,
        IReadOnlyList<TweakDefinition> tweaks)
    {
        _registry = registry;
        _backup = backup;
        _tweaks = tweaks;
    }

    public IReadOnlyList<TweakDefinition> Apply(IEnumerable<string> tweakIds)
    {
        var requested = new HashSet<string>(tweakIds, StringComparer.OrdinalIgnoreCase);
        var chosen = _tweaks.Where(tweak => requested.Contains(tweak.Id)).ToArray();

        var known = new HashSet<string>(_tweaks.Select(tweak => tweak.Id), StringComparer.OrdinalIgnoreCase);
        var unknown = requested.Where(id => !known.Contains(id)).ToArray();
        if (unknown.Length > 0)
            throw new ArgumentException($"Unknown Win7ify tweak: {string.Join(", ", unknown)}", nameof(tweakIds));

        if (chosen.Length == 0) return chosen;

        _backup.EnsureBackedUp(chosen);
        foreach (var tweak in chosen)
            _registry.Write(tweak.Path, tweak.Name, tweak.AppliedValue);

        return chosen;
    }

    public int RestoreAll() => _backup.RestoreAll();
}