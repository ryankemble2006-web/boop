using Boop.Win7ify.Tests;
int failures = 0, total = 0;
foreach (var test in OpenShellTests.Cases)
{
    total++;
    try { test.Run(); Console.WriteLine($"PASS {test.Name}"); }
    catch (Exception ex) { failures++; Console.WriteLine($"FAIL {test.Name}: {ex.Message}"); }
}
Console.WriteLine($"TOTAL: {total} shell tests, {failures} failures.");
return failures == 0 ? 0 : 1;
