using Boop.Win7ify.Core;
using Boop.Win7ify.Tests;

var tests = new (string Name, Action Run)[]
{
    ("apply backs up original before writing", ApplyBacksUpOriginal),
    ("second apply keeps first baseline", SecondApplyKeepsBaseline),
    ("restore removes originally missing value", RestoreRemovesOriginallyMissing),
    ("restore returns original value and kind", RestoreReturnsOriginalValue),
    ("catalog has expected safe scope", CatalogAndConversionTests.CatalogHasExpectedSafeScope),
    ("default preset excludes experimental tweak", CatalogAndConversionTests.DefaultPresetExcludesExperimentalTweak),
    ("Windows DWORD conversion round-trips", CatalogAndConversionTests.WindowsDwordConversionRoundTrips),
    ("Windows expand string preserves kind", CatalogAndConversionTests.WindowsExpandStringPreservesKind),
    ("Windows binary conversion preserves bytes", CatalogAndConversionTests.WindowsBinaryConversionPreservesBytes)
};

var failures = 0;
foreach (var test in tests)
{
    try
    {
        test.Run();
        Console.WriteLine($"PASS {test.Name}");
    }
    catch (Exception ex)
    {
        failures++;
        Console.Error.WriteLine($"FAIL {test.Name}: {ex.Message}");
    }
}

return failures == 0 ? 0 : 1;

static void ApplyBacksUpOriginal()
{
    const string path = @"Software\BOOP\Test";
    var original = RegistryStoredValue.DWord(1);
    var applied = RegistryStoredValue.DWord(0);
    var (store, backup, service, tweak) = Fixture(path, "Example", applied);
    store.Seed(path, "Example", original);

    service.Apply(new[] { tweak.Id });
    AssertEqual(applied, store.Read(path, "Example"), "applied registry value");

    backup.RestoreAll();
    AssertEqual(original, store.Read(path, "Example"), "restored original value");
}

static void SecondApplyKeepsBaseline()
{
    const string path = @"Software\BOOP\Test";
    var original = RegistryStoredValue.DWord(7);
    var (store, backup, service, tweak) = Fixture(path, "Repeat", RegistryStoredValue.DWord(2));
    store.Seed(path, "Repeat", original);

    service.Apply(new[] { tweak.Id });
    store.Write(path, "Repeat", RegistryStoredValue.DWord(99));
    service.Apply(new[] { tweak.Id });
    backup.RestoreAll();

    AssertEqual(original, store.Read(path, "Repeat"), "first baseline survives second apply");
}

static void RestoreRemovesOriginallyMissing()
{
    const string path = @"Software\BOOP\Test";
    var (store, backup, service, tweak) = Fixture(path, "Created", RegistryStoredValue.String("hello"));

    service.Apply(new[] { tweak.Id });
    AssertTrue(store.Read(path, "Created").Exists, "value exists after apply");
    backup.RestoreAll();
    AssertTrue(!store.Read(path, "Created").Exists, "value removed after restore");
}

static void RestoreReturnsOriginalValue()
{
    const string path = @"Software\BOOP\Test";
    var original = RegistryStoredValue.ExpandString(@"%USERPROFILE%\Thing");
    var (store, backup, service, tweak) = Fixture(path, "Typed", RegistryStoredValue.String("changed"));
    store.Seed(path, "Typed", original);

    service.Apply(new[] { tweak.Id });
    backup.RestoreAll();
    AssertEqual(original, store.Read(path, "Typed"), "kind and data restored exactly");
}

static (FakeRegistryStore Store, BackupService Backup, Win7ifyService Service, TweakDefinition Tweak)
    Fixture(string path, string name, RegistryStoredValue applied)
{
    var store = new FakeRegistryStore();
    var backupPath = Path.Combine(Path.GetTempPath(), "boop-win7ify-tests", Guid.NewGuid() + ".json");
    var backup = new BackupService(store, backupPath);
    var tweak = new TweakDefinition("test", "Test", path, name, applied, false);
    var service = new Win7ifyService(store, backup, new[] { tweak });
    return (store, backup, service, tweak);
}

static void AssertEqual<T>(T expected, T actual, string message)
{
    if (!EqualityComparer<T>.Default.Equals(expected, actual))
        throw new Exception($"{message}: expected {expected}, got {actual}");
}

static void AssertTrue(bool condition, string message)
{
    if (!condition) throw new Exception(message);
}