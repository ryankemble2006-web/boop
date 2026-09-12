namespace Boop.Win7ify.Core;

public interface IRegistryStore
{
    RegistryStoredValue Read(string path, string name);
    void Write(string path, string name, RegistryStoredValue value);
    void Delete(string path, string name);
    void DeleteKeyIfEmpty(string path);
}