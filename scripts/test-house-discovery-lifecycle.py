"""Execute the real dialog method against a minimal Android window-lifetime fake.

This is a nonvisual lifecycle regression, not an Android rendering test.
"""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
source = (root / 'source/MainActivity.java').read_text()
start = source.index('    private void showConnectPrompt(')
end = source.index('    private void handleAuthIntent(', start)
method = source[start:end]
harness = r'''
public class DiscoveryLifecycleCheck {
 boolean finishing, destroyed, connectPromptShowing, connected;
 int shown;
 boolean isFinishing() { return finishing; }
 boolean isDestroyed() { return destroyed; }
 Store tokenStore = new Store();
 class Store { boolean hasConnection() { return connected; } }
 static class Discovery { void stop() {} }
 Discovery discovery = new Discovery();
 static class Auth { String begin(String s) { return s; } }
 Auth haAuth = new Auth();
 static class Uri { static String parse(String s) { return s; } }
 static class Intent { static String ACTION_VIEW = "view"; Intent(String a, String b) {} }
 void startActivity(Intent intent) {}
 void speak(String text) {}
 interface Click { void run(Object dialog, int which); }
 interface Cancel { void run(Object dialog); }
 static class AlertDialog {
  static class Builder {
   final DiscoveryLifecycleCheck owner;
   Builder(DiscoveryLifecycleCheck owner) { this.owner = owner; }
   Builder setTitle(String s) { return this; }
   Builder setMessage(String s) { return this; }
   Builder setPositiveButton(String s, Click c) { return this; }
   Builder setNegativeButton(String s, Click c) { return this; }
   Builder setOnCancelListener(Cancel c) { return this; }
   void show() {
    if (owner.finishing || owner.destroyed) throw new AssertionError("Dialog used a closed activity window");
    owner.shown++;
   }
  }
 }
 METHOD
 public static void main(String[] args) {
  for (int state = 0; state < 4; state++) {
   DiscoveryLifecycleCheck activity = new DiscoveryLifecycleCheck();
   activity.finishing = (state & 1) != 0;
   activity.destroyed = (state & 2) != 0;
   activity.showConnectPrompt("Test house", "http://example.invalid");
   if (activity.shown != (state == 0 ? 1 : 0)) throw new AssertionError("Unexpected prompt count");
   activity.showConnectPrompt("Duplicate", "http://example.invalid");
   if (activity.shown > 1) throw new AssertionError("Duplicate prompt");
  }
  DiscoveryLifecycleCheck connected = new DiscoveryLifecycleCheck();
  connected.connected = true;
  connected.showConnectPrompt("Already connected", "http://example.invalid");
  if (connected.shown != 0) throw new AssertionError("Prompt for an existing connection");
  System.out.println("Discovery lifecycle: active, finishing, destroyed, duplicate and connected cases passed");
 }
}
'''.replace('METHOD', method)
java_home = Path(os.environ.get('JAVA_HOME', ''))
javac = shutil.which('javac') or str(java_home / 'bin/javac')
java = shutil.which('java') or str(java_home / 'bin/java')
with tempfile.TemporaryDirectory() as folder:
    path = Path(folder) / 'DiscoveryLifecycleCheck.java'
    path.write_text(harness)
    subprocess.run([javac, '-d', folder, str(path)], check=True)
    subprocess.run([java, '-cp', folder, 'DiscoveryLifecycleCheck'], check=True)
