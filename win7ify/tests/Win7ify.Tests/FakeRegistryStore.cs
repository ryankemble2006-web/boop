using Boop.Win7ify.Core;

namespace Boop.Win7ify.Tests;

internal sealed class FakeRegistryStore : IRegistryStore
{
    private readonly Dictionary<(string Path, string Name), RegistryStoredValue> _values = new();

    public RegistryStoredValue Read(string path, string name) =>
        _values.TryGetValue((path, name), out var value)
            ? value
            : RegistryStoredValue.Missing;

    public void Write(string path, string name, RegistryStoredValue value)
    {
        if (!value.Exists) throw new ArgumentException("Cannot write a missing value.", nameof(value));
        _values[(path, name)] = value;
    }

    public void Delete(string path, string name) => _values.Remove((path, name));

    public void DeleteKeyIfEmpty(string path) { }

    public void Seed(string path, string name, RegistryStoredValue value) => Write(path, name, value);
}