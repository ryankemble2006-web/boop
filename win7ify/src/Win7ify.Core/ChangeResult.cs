using System.Security;

namespace Boop.Win7ify.Core;

public enum ChangeStatus { Changed, AlreadyCorrect, Blocked, Failed }

public sealed record ChangeResult(string Label, string Path, string Name, ChangeStatus Status, string Detail)
{
    public bool Succeeded => Status is ChangeStatus.Changed or ChangeStatus.AlreadyCorrect;
}

internal static class RegistryChange
{
    public static ChangeResult Execute(IRegistryStore store, string label, string path, string name,
        RegistryStoredValue wanted)
    {
        try
        {
            if (Matches(store.Read(path, name), wanted))
                return new(label, path, name, ChangeStatus.AlreadyCorrect, "Already correct; left alone.");
            if (wanted.Exists) store.Write(path, name, wanted);
            else store.Delete(path, name);
            if (!Matches(store.Read(path, name), wanted))
                return new(label, path, name, ChangeStatus.Failed, "Windows did not keep this value. Original backup retained.");
            return new(label, path, name, ChangeStatus.Changed, "Saved and read back successfully.");
        }
        catch (Exception ex) when (ex is UnauthorizedAccessException or SecurityException)
        {
            var help = name.Equals("TaskbarDa", StringComparison.OrdinalIgnoreCase)
                ? "Windows protects Widgets. Use Taskbar settings instead; no permission changes were attempted."
                : "Windows denied this setting. No permission changes were attempted.";
            return new(label, path, name, ChangeStatus.Blocked, help);
        }
        catch (Exception ex) when (ex is IOException or NotSupportedException or ArgumentException)
        {
            return new(label, path, name, ChangeStatus.Failed, ex.Message);
        }
    }

    public static bool Matches(RegistryStoredValue a, RegistryStoredValue b)
    {
        if (a.Exists != b.Exists) return false;
        if (!a.Exists) return true;
        if (a.Kind != b.Kind) return false;
        return a.Kind switch
        {
            RegistryDataKind.String or RegistryDataKind.ExpandString => a.Text == b.Text,
            RegistryDataKind.DWord or RegistryDataKind.QWord => a.Number == b.Number,
            RegistryDataKind.MultiString => a.MultiText is not null && b.MultiText is not null && a.MultiText.SequenceEqual(b.MultiText),
            RegistryDataKind.Binary => a.Bytes is not null && b.Bytes is not null && a.Bytes.SequenceEqual(b.Bytes),
            _ => false
        };
    }
}
