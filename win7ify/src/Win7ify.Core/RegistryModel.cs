namespace Boop.Win7ify.Core;

public enum RegistryDataKind
{
    String,
    ExpandString,
    DWord,
    QWord,
    MultiString,
    Binary
}

public sealed record RegistryStoredValue
{
    public bool Exists { get; init; }
    public RegistryDataKind? Kind { get; init; }
    public string? Text { get; init; }
    public long? Number { get; init; }
    public string[]? MultiText { get; init; }
    public byte[]? Bytes { get; init; }

    public static RegistryStoredValue Missing { get; } = new() { Exists = false };

    public static RegistryStoredValue String(string value) =>
        new() { Exists = true, Kind = RegistryDataKind.String, Text = value };

    public static RegistryStoredValue ExpandString(string value) =>
        new() { Exists = true, Kind = RegistryDataKind.ExpandString, Text = value };

    public static RegistryStoredValue DWord(int value) =>
        new() { Exists = true, Kind = RegistryDataKind.DWord, Number = value };

    public static RegistryStoredValue QWord(long value) =>
        new() { Exists = true, Kind = RegistryDataKind.QWord, Number = value };

    public static RegistryStoredValue MultiString(params string[] values) =>
        new() { Exists = true, Kind = RegistryDataKind.MultiString, MultiText = values.ToArray() };

    public static RegistryStoredValue Binary(params byte[] values) =>
        new() { Exists = true, Kind = RegistryDataKind.Binary, Bytes = values.ToArray() };
}