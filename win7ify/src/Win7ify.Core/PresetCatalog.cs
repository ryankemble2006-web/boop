namespace Boop.Win7ify.Core;

public static class PresetCatalog
{
    public static IReadOnlyList<string> Windows7ish { get; } =
        TweakCatalog.All
            .Where(tweak => !tweak.Experimental)
            .Select(tweak => tweak.Id)
            .ToArray();
}