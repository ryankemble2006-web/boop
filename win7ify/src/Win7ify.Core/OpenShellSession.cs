using System.Text.Json;

namespace Boop.Win7ify.Core;

/// <summary>A retryable, per-user journal. It never takes ownership of an existing installation.</summary>
public sealed class OpenShellSession
{
    private readonly string _folder;
    private readonly IRegistryStore _registry;
    private readonly IOpenShellHost _host;
    private string Journal => Path.Combine(_folder, "open-shell-session-v1.json");
    private string Profile => Path.Combine(_folder, "open-shell-profile-v1.json");
    private static readonly JsonSerializerOptions Json = new() { WriteIndented = true };
    public OpenShellSession(string folder, IRegistryStore registry, IOpenShellHost host)
    { _folder = folder; _registry = registry; _host = host; }
    public bool HasSession => File.Exists(Journal);

    public async Task EnableAsync(Action<ShellEvent>? progress = null, bool launch = true)
    {
        using var gate = Lock();
        _host.CheckEnvironment();
        var state = Load();
        var info = _host.Inspect();
        if (state is not null && state.Phase is "restoring" or "restored" or "done")
            throw new ShellProblem("BOOP E216", "Finish PUT WINDOWS 11 BACK before starting a new makeover.");
        if (info.Installed && info.Version != OpenShellPayload.Version)
            throw new ShellProblem("BOOP E212", "A different Open-Shell version is installed. It has been left alone.");
        progress?.Invoke(new("BOOP S210", "Checking the official bundled installer..."));
        await _host.VerifyPayloadAsync();
        if (state is null)
        {
            if (File.Exists(Profile))
                throw new ShellProblem("BOOP E214", "An earlier profile backup has no ownership journal. Kept untouched for recovery.");
            state = new JournalState { Preexisting = info.Installed, WasRunning = info.Running };
            Save(state);
        }
        var backup = new BackupService(_registry, Profile, OpenShellProfile.All);
        if (state.SnapshotReady && !backup.HasBackup)
            throw new ShellProblem("BOOP E214", "The original menu backup is missing. Nothing else was changed.");
        backup.EnsureBackedUp(OpenShellProfile.All);
        state.SnapshotReady = true;
        Save(state);
        if (state.Preexisting && !info.Installed)
            throw new ShellProblem("BOOP E215", "The original Open-Shell installation is no longer present. Use Undo to restore saved preferences.");
        if (!state.Preexisting)
        {
            if (info.Installed && (!info.OwnedLocation || (!state.InstallPending && !state.Owned)))
                throw new ShellProblem("BOOP E215", "Another Open-Shell installation appeared. It has been left alone.");
            if (!info.Installed)
            {
                state.InstallPending = true;
                Save(state);
                progress?.Invoke(new("BOOP S220", "Installing the Start-menu component. Windows may ask permission..."));
                var code = await _host.InstallAsync();
                if (code is not 0 and not 3010)
                    throw new ShellProblem("BOOP E221", $"Installation did not finish (Windows code {code}). Your backup is kept; retry is available.");
                info = _host.Inspect();
                if (!info.Installed || !info.OwnedLocation || info.Version != OpenShellPayload.Version)
                    throw new ShellProblem("BOOP E222", "The installer ended, but the expected menu files were not found. Recovery information is kept.");
                state.Owned = true;
                state.InstallPending = false;
                Save(state);
                if (code == 3010)
                    throw new ShellProblem("BOOP E224", "Windows needs a restart to finish installation. BOOP did not restart the computer. Retry after your next restart.");
            }
            else
            {
                state.Owned = true;
                state.InstallPending = false;
                Save(state);
            }
        }
        await _host.StopAsync();
        progress?.Invoke(new("BOOP S230", "Applying the Windows 7 menu and Aero menu skin..."));
        var results = new Win7ifyService(_registry, backup, OpenShellProfile.All)
            .Apply(OpenShellProfile.All.Select(t => t.Id));
        foreach (var result in results)
            progress?.Invoke(new("BOOP S231", result.Label, result.Status + ": " + result.Detail));
        var failed = results.Where(r => !r.Succeeded).ToArray();
        if (failed.Length > 0)
            throw new ShellProblem("BOOP E230", "Windows blocked menu preferences: " + string.Join(", ", failed.Select(r => r.Label)) + ". Undo remains available.");
        await _host.VerifyProfileAsync();
        state.Phase = "configured";
        Save(state);
        if (launch)
        {
            progress?.Invoke(new("BOOP S240", "Starting the menu and checking its window..."));
            await _host.StartAsync(openMenu: true);
            state.Phase = "active";
            Save(state);
        }
        progress?.Invoke(new("BOOP OK", launch ? "The Windows 7-style Start menu is running." : "Start-menu installation and effective settings verified. Runtime not started."));
    }

    public async Task UndoAsync(Action<ShellEvent>? progress = null)
    {
        using var gate = Lock();
        var state = Load();
        if (state is null) { progress?.Invoke(new("BOOP OK", "No BOOP menu installation to undo. Existing software was left alone.")); return; }
        if (state.Phase == "done") { Cleanup(); return; }
        if (!state.SnapshotReady)
        {
            // Setup never reached any installer, process-stop or settings-write stage.
            state.Phase = "done"; Save(state); Cleanup();
            progress?.Invoke(new("BOOP OK", "The unfinished preparation was cleared. No menu had been changed."));
            return;
        }
        // Validate the whole existing baseline BEFORE stopping a working menu.
        // With an empty selection EnsureBackedUp only loads/validates; it writes nothing.
        var backup = new BackupService(_registry, Profile, OpenShellProfile.All);
        if (!backup.HasBackup)
            throw new ShellProblem("BOOP E214", "The original menu backup is missing. The running menu and installation were left alone.");
        try { backup.EnsureBackedUp(Array.Empty<TweakDefinition>()); }
        catch (IOException ex)
        { throw new ShellProblem("BOOP E214", "The original menu backup could not be validated. The running menu was left alone; recovery data is kept.", ex); }
        var info = _host.Inspect();
        var owned = !state.Preexisting && (state.Owned || state.InstallPending);
        if (owned && info.Installed && (!info.OwnedLocation || info.Version != OpenShellPayload.Version))
            throw new ShellProblem("BOOP E252", "Open-Shell has changed since installation. BOOP will not remove a different version or somebody else's installation.");
        if (info.Installed) await _host.StopAsync();
        state.Phase = "restoring";
        Save(state);
        var results = backup.RestoreAll(keepBackup: true);
        foreach (var result in results)
            progress?.Invoke(new("BOOP S250", result.Label, result.Status + ": " + result.Detail));
        if (results.Any(r => !r.Succeeded))
            throw new ShellProblem("BOOP E251", "Some original menu preferences could not be restored. The complete backup is kept for retry.");
        state.Phase = "restored";
        Save(state);
        if (owned && info.Installed)
        {
            progress?.Invoke(new("BOOP S260", "Removing only the Open-Shell installation BOOP created..."));
            await _host.VerifyPayloadAsync();
            var code = await _host.UninstallAsync();
            if (code is not 0 and not 3010 || _host.Inspect().Installed)
                throw new ShellProblem("BOOP E261", $"Removal has not completed (Windows code {code}). Recovery is kept; you can retry Undo.");
        }
        else if (state.Preexisting && state.WasRunning && info.Installed)
            await _host.StartAsync(openMenu: false);
        state.Phase = "done";
        Save(state);
        Cleanup();
        progress?.Invoke(new("BOOP OK", "Original menu preferences restored. Existing third-party installations were preserved."));
    }

    private FileStream Lock()
    {
        Directory.CreateDirectory(_folder);
        try { return new FileStream(Path.Combine(_folder, "open-shell-operation.lock"), FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None); }
        catch (IOException ex) { throw new ShellProblem("BOOP E201", "Another menu operation is still running. Nothing new was started.", ex); }
    }
    private JournalState? Load()
    {
        if (!File.Exists(Journal)) return null;
        try
        {
            if (new FileInfo(Journal).Length > 16384) throw new InvalidDataException();
            var state = JsonSerializer.Deserialize<JournalState>(File.ReadAllText(Journal), Json);
            if (state is null || state.Schema != 1 || state.Release != OpenShellPayload.Version ||
                state.Phase is not ("prepared" or "configured" or "active" or "restoring" or "restored" or "done") ||
                state.Preexisting && (state.Owned || state.InstallPending) ||
                !state.SnapshotReady && (state.Owned || state.InstallPending || state.Phase is "active" or "configured"))
                throw new InvalidDataException();
            return state;
        }
        catch (Exception ex) when (ex is JsonException or IOException)
        { throw new ShellProblem("BOOP E214", "The menu recovery journal could not be read. It was kept untouched; no installation was started.", ex); }
    }
    private void Save(JournalState state)
    {
        var temp = Path.Combine(_folder, ".shell-" + Guid.NewGuid().ToString("N") + ".tmp");
        try
        {
            using (var stream = new FileStream(temp, FileMode.CreateNew, FileAccess.Write, FileShare.None, 4096, FileOptions.WriteThrough))
            { JsonSerializer.Serialize(stream, state, Json); stream.Flush(true); }
            if (File.Exists(Journal)) File.Replace(temp, Journal, null); else File.Move(temp, Journal);
        }
        finally { if (File.Exists(temp)) File.Delete(temp); }
    }
    private void Cleanup() { if (File.Exists(Profile)) File.Delete(Profile); File.Delete(Journal); }
    public sealed class JournalState
    {
        public int Schema { get; set; } = 1;
        public string Release { get; set; } = OpenShellPayload.Version;
        public string Phase { get; set; } = "prepared";
        public bool Preexisting { get; set; }
        public bool WasRunning { get; set; }
        public bool SnapshotReady { get; set; }
        public bool InstallPending { get; set; }
        public bool Owned { get; set; }
    }
}
