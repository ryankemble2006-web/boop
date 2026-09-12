namespace Boop.Win7ify.Core;

public sealed record TweakDefinition(
    string Id,
    string Label,
    string Path,
    string Name,
    RegistryStoredValue AppliedValue,
    bool Experimental = false);