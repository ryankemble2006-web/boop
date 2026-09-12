using Microsoft.Win32;

namespace Boop.Win7ify.Core;

public sealed class WindowsRegistryStore : IRegistryStore
{
    public RegistryStoredValue Read(string path, string name)
    {
        using var key = Registry.CurrentUser.OpenSubKey(path, writable: false);
        if (key is null || !key.GetValueNames().Contains(name, StringComparer.OrdinalIgnoreCase))
            return RegistryStoredValue.Missing;

        var kind = key.GetValueKind(name);
        var value = key.GetValue(name, null, RegistryValueOptions.DoNotExpandEnvironmentNames)
            ?? throw new InvalidDataException($"Registry value {path}\\{name} unexpectedly returned null.");
        return DecodeWindowsValue(value, kind);
    }

    public void Write(string path, string name, RegistryStoredValue value)
    {
        if (!value.Exists) throw new ArgumentException("Cannot write a missing registry value.", nameof(value));
        using var key = Registry.CurrentUser.CreateSubKey(path, writable: true)
            ?? throw new UnauthorizedAccessException($"Could not open HKCU\\{path} for writing.");
        var encoded = EncodeWindowsValue(value);
        key.SetValue(name, encoded.Data, encoded.Kind);
    }

    public void Delete(string path, string name)
    {
        using var key = Registry.CurrentUser.OpenSubKey(path, writable: true);
        key?.DeleteValue(name, throwOnMissingValue: false);
    }

    public void DeleteKeyIfEmpty(string path)
    {
        var slash = path.LastIndexOf('\\');
        if (slash <= 0 || slash == path.Length - 1) return;
        var parentPath = path[..slash];
        var childName = path[(slash + 1)..];

        try
        {
            using var parent = Registry.CurrentUser.OpenSubKey(parentPath, writable: true);
            if (parent is null) return;
            using var child = parent.OpenSubKey(childName, writable: false);
            if (child is null || child.SubKeyCount != 0 || child.ValueCount != 0) return;
            child.Close();
            parent.DeleteSubKey(childName, throwOnMissingSubKey: false);
        }
        catch (UnauthorizedAccessException)
        {
            // Value restoration already happened. Empty-key cleanup is best effort.
        }
    }

    public static RegistryStoredValue DecodeWindowsValue(object value, RegistryValueKind kind) => kind switch
    {
        RegistryValueKind.String => RegistryStoredValue.String((string)value),
        RegistryValueKind.ExpandString => RegistryStoredValue.ExpandString((string)value),
        RegistryValueKind.DWord => RegistryStoredValue.DWord(Convert.ToInt32(value)),
        RegistryValueKind.QWord => RegistryStoredValue.QWord(Convert.ToInt64(value)),
        RegistryValueKind.MultiString => RegistryStoredValue.MultiString((string[])value),
        RegistryValueKind.Binary => RegistryStoredValue.Binary((byte[])value),
        _ => throw new NotSupportedException($"Registry kind {kind} is not supported by Win7ify backup v1.")
    };

    public static (object Data, RegistryValueKind Kind) EncodeWindowsValue(RegistryStoredValue value)
    {
        if (!value.Exists || value.Kind is null)
            throw new ArgumentException("A missing registry value cannot be encoded.", nameof(value));

        return value.Kind.Value switch
        {
            RegistryDataKind.String => (value.Text ?? string.Empty, RegistryValueKind.String),
            RegistryDataKind.ExpandString => (value.Text ?? string.Empty, RegistryValueKind.ExpandString),
            RegistryDataKind.DWord => (checked((int)(value.Number ?? 0)), RegistryValueKind.DWord),
            RegistryDataKind.QWord => (value.Number ?? 0L, RegistryValueKind.QWord),
            RegistryDataKind.MultiString => (value.MultiText ?? Array.Empty<string>(), RegistryValueKind.MultiString),
            RegistryDataKind.Binary => (value.Bytes ?? Array.Empty<byte>(), RegistryValueKind.Binary),
            _ => throw new NotSupportedException($"Registry kind {value.Kind} is not supported by Win7ify backup v1.")
        };
    }
}