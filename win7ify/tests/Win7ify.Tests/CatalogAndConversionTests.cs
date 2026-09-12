using Boop.Win7ify.Core;
using Microsoft.Win32;

namespace Boop.Win7ify.Tests;

internal static class CatalogAndConversionTests
{
    public static void CatalogHasExpectedSafeScope()
    {
        var all = TweakCatalog.All;
        var ids = all.Select(tweak => tweak.Id).ToHashSet(StringComparer.OrdinalIgnoreCase);
        var expected = new[]
        {
            "taskbar.left", "taskbar.nevercombine", "taskbar.search.hide",
            "taskbar.taskview.hide", "taskbar.widgets.hide", "taskbar.showdesktop",
            "explorer.thispc", "desktop.computer", "desktop.userfiles", "desktop.network",
            "desktop.controlpanel", "desktop.recyclebin", "context.classic.experimental"
        };

        if (all.Count != expected.Length) throw new Exception($"expected {expected.Length} tweaks, got {all.Count}");
        foreach (var id in expected)
            if (!ids.Contains(id)) throw new Exception($"missing tweak {id}");

        var experimental = all.Single(tweak => tweak.Id == "context.classic.experimental");
        if (!experimental.Experimental) throw new Exception("classic context menu must remain experimental");
    }

    public static void WindowsDwordConversionRoundTrips()
    {
        var stored = WindowsRegistryStore.DecodeWindowsValue(42, RegistryValueKind.DWord);
        if (stored != RegistryStoredValue.DWord(42)) throw new Exception("DWORD decode mismatch");

        var encoded = WindowsRegistryStore.EncodeWindowsValue(stored);
        if (encoded.Kind != RegistryValueKind.DWord || !Equals(encoded.Data, 42))
            throw new Exception("DWORD encode mismatch");
    }

    public static void WindowsExpandStringPreservesKind()
    {
        var stored = WindowsRegistryStore.DecodeWindowsValue(@"%USERPROFILE%\Thing", RegistryValueKind.ExpandString);
        if (stored.Kind != RegistryDataKind.ExpandString || stored.Text != @"%USERPROFILE%\Thing")
            throw new Exception("expand-string decode mismatch");
    }

    public static void WindowsBinaryConversionPreservesBytes()
    {
        var stored = WindowsRegistryStore.DecodeWindowsValue(new byte[] { 1, 3, 7 }, RegistryValueKind.Binary);
        if (stored.Bytes is null || !stored.Bytes.SequenceEqual(new byte[] { 1, 3, 7 }))
            throw new Exception("binary decode mismatch");
    }
}