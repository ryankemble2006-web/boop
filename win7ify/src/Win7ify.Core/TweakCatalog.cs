namespace Boop.Win7ify.Core;

public static class TweakCatalog
{
    private const string Advanced = @"Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced";
    private const string Search = @"Software\Microsoft\Windows\CurrentVersion\Search";
    private const string DesktopIcons = @"Software\Microsoft\Windows\CurrentVersion\Explorer\HideDesktopIcons\NewStartPanel";
    private const string ClassicMenu = @"Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}\InprocServer32";

    public static IReadOnlyList<TweakDefinition> All { get; } = new TweakDefinition[]
    {
        new("taskbar.left", "Put Start and taskbar buttons on the left", Advanced, "TaskbarAl", RegistryStoredValue.DWord(0)),
        new("taskbar.nevercombine", "Ask Windows for uncombined taskbar buttons with labels", Advanced, "TaskbarGlomLevel", RegistryStoredValue.DWord(2)),
        new("taskbar.search.hide", "Hide Search from the taskbar", Search, "SearchboxTaskbarMode", RegistryStoredValue.DWord(0)),
        new("taskbar.taskview.hide", "Hide Task View", Advanced, "ShowTaskViewButton", RegistryStoredValue.DWord(0)),
        new("taskbar.widgets.hide", "Hide Widgets", Advanced, "TaskbarDa", RegistryStoredValue.DWord(0)),
        new("taskbar.showdesktop", "Restore the far-right Show Desktop corner", Advanced, "TaskbarSd", RegistryStoredValue.DWord(1)),
        new("explorer.thispc", "Open File Explorer to This PC", Advanced, "LaunchTo", RegistryStoredValue.DWord(1)),
        new("desktop.computer", "Show Computer on the desktop", DesktopIcons, "{20D04FE0-3AEA-1069-A2D8-08002B30309D}", RegistryStoredValue.DWord(0)),
        new("desktop.userfiles", "Show User Files on the desktop", DesktopIcons, "{59031A47-3F72-44A7-89C5-5595FE6B30EE}", RegistryStoredValue.DWord(0)),
        new("desktop.network", "Show Network on the desktop", DesktopIcons, "{F02C1A0D-BE21-4350-88B0-7367FC96EF3C}", RegistryStoredValue.DWord(0)),
        new("desktop.controlpanel", "Show Control Panel on the desktop", DesktopIcons, "{5399E694-6CE5-4D6C-8FCE-1D8870FDCBA0}", RegistryStoredValue.DWord(0)),
        new("desktop.recyclebin", "Show Recycle Bin on the desktop", DesktopIcons, "{645FF040-5081-101B-9F08-00AA002F954E}", RegistryStoredValue.DWord(0)),
        new("context.classic.experimental", "Experimental: classic full right-click menu", ClassicMenu, "", RegistryStoredValue.String(""), true)
    };
}