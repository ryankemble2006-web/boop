"""Executable first-run/identity policies and package-level split integration."""
from pathlib import Path
import json
import re
import subprocess
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
A = '{http://schemas.android.com/apk/res/android}'


def java_test(files, harness):
    with tempfile.TemporaryDirectory() as folder:
        root = Path(folder)
        inputs = []
        for name, content in files.items():
            path = root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content)
            inputs.append(str(path))
        subprocess.run(['javac', '-d', folder, *inputs], check=True)
        subprocess.run(['java', '-cp', folder, harness], check=True)


def test_runtime_body_and_setup_execute_the_production_policies():
    files = {p: (ROOT / p).read_text() for p in (
        'source/BoopAppIdentity.java', 'source/BoopSetupState.java', 'unified/BoopDeviceProfile.java')}
    files['android/content/SharedPreferences.java'] = '''package android.content;
public interface SharedPreferences {
 boolean getBoolean(String k,boolean def);
 Editor edit();
 interface Editor { Editor putBoolean(String k,boolean v); Editor putString(String k,String v); Editor remove(String k); void apply(); boolean commit(); }
}'''
    files['android/content/Context.java'] = '''package android.content;
public class Context {
 public static final int MODE_PRIVATE=0;
 public final String pkg; public int writes, reads; public boolean saved, fail;
 public Context(String p){pkg=p;}
 public String getPackageName(){return pkg;}
 public SharedPreferences getSharedPreferences(String name,int mode){
  if(!"boop_app_setup_v1".equals(name)) throw new AssertionError("Runtime consulted legacy profile: "+name);
  return new SharedPreferences(){
   public boolean getBoolean(String k,boolean d){reads++;return saved;}
   public Editor edit(){return new Editor(){
    boolean pending;
    public Editor putBoolean(String k,boolean v){pending=v;return this;}
    public Editor putString(String k,String v){throw new AssertionError();}
    public Editor remove(String k){throw new AssertionError();}
    public void apply(){throw new AssertionError("Unverified async setup completion");}
    public boolean commit(){writes++;if(fail)return false;saved=pending;return true;}
   };}
  };
 }
}'''
    files['com/boop/alpha1/SetupHarness.java'] = '''package com.boop.alpha1;
import android.content.Context;
public class SetupHarness {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  for(String pkg:new String[]{"com.boop.alpha1","com.boop.shieldoverlay"}){
   Context c=new Context(pkg);
   check(BoopDeviceProfile.resolve(c)==(pkg.endsWith("shieldoverlay")?BoopDeviceProfile.Mode.SHIELD:BoopDeviceProfile.Mode.WALL),"wrong shell body");
   for(int i=0;i<5;i++)check(!BoopSetupState.complete(c),"first launch/reentry skipped setup");
   check(c.writes==0,"reads auto-completed onboarding");
   c.fail=true;check(!BoopSetupState.completeFromUser(c),"failed persistence claimed success");
   check(!BoopSetupState.complete(c),"failed save advanced setup");
   c.fail=false;check(BoopSetupState.completeFromUser(c),"user Continue not persisted");
   check(BoopSetupState.complete(c),"user completion not observed");
   check(!BoopSetupState.complete(new Context(pkg)),"fresh install inherited prior setup");
  }
  System.out.println(checks+" production body/first-setup assertions passed");
 }
}'''
    java_test(files, 'com.boop.alpha1.SetupHarness')


def test_only_user_continue_can_mark_setup_complete():
    profile = (ROOT / 'unified/BoopProfileActivity.java').read_text()
    entry = (ROOT / 'unified/UnifiedEntryActivity.java').read_text()
    assert profile.count('BoopSetupState.completeFromUser(this)') == 1
    assert '"Continue"' in profile and 'this::finishSetupFromUser' in profile
    create = profile.split('onCreate(Bundle state)', 1)[1].split('private void homeSetup()', 1)[0]
    assert 'completeFromUser' not in create
    assert 'completeFromUser' not in entry
    assert entry.index('!BoopSetupState.complete(this)') < entry.index('BoopNotificationStartupGate.resolve')
    assert 'grantRuntimePermission' not in profile + entry
    assert 'setHomeActivity' not in profile + entry
    assert 'RoleManager.ROLE_HOME' in profile


def test_legacy_johnny_authority_has_one_owner_and_guard_is_not_weakened():
    wall = ET.parse(ROOT / 'split/wall/AndroidManifest.xml').getroot()
    shield = ET.parse(ROOT / 'split/shield/AndroidManifest.xml').getroot()
    assert not wall.findall('.//provider')
    provider = shield.find('.//provider')
    assert provider is not None
    assert provider.get(A + 'name') == 'com.boop.alpha1.JohnnyStateProvider'
    assert set(provider.get(A + 'authorities').split(';')) == {
        'com.boop.alpha1.johnny_states', 'com.boop.shieldoverlay.johnny_states'}
    assert provider.get(A + 'grantUriPermissions') == 'false'
    source = (ROOT / 'unified/JohnnyStateProvider.java').read_text()
    assert source.index('enforceCaller();') < source.index('Binder.clearCallingIdentity()')
    assert 'JohnnyStatePolicy.trusted' in source
    assert 'names.length != 1' in source
    assert 'Only the fixed snapshot is supported' in source
    diff = subprocess.check_output(['git','diff','--name-only','9d57019d9370dbe3f47061b6e8b0ce8ed5134715','HEAD','--','unified/JohnnyStateProvider.java','unified/JohnnyStatePolicy.java'],cwd=ROOT,text=True)
    assert not diff.strip(), 'Johnny caller/auth guard changed'


def test_materialized_shared_components_assets_and_home_filters():
    root = ROOT / 'boop-build/BOOP-Alpha1'
    common = ET.parse(root / 'assistant-lib/src/main/AndroidManifest.xml').getroot()
    assert common.find('application').get(A + 'name') == 'com.boop.alpha1.UnifiedApplication'
    for component in common.findall('.//activity') + common.findall('.//service') + common.findall('.//provider'):
        assert not component.get(A + 'name').startswith('.')
    assert not common.findall('.//provider')
    for body in ('wall', 'shield'):
        manifest = ET.parse(root / (body+'-app/src/main/AndroidManifest.xml')).getroot()
        activities = manifest.findall('.//activity')
        assert len(activities) == 1
        assert activities[0].get(A + 'name') == 'com.boop.alpha1.UnifiedEntryActivity'
        categories = {n.get(A + 'name') for n in manifest.findall('.//category')}
        assert 'android.intent.category.HOME' in categories
        assert 'android.intent.category.LAUNCHER' in categories
        assert ('android.intent.category.LEANBACK_LAUNCHER' in categories) == (body == 'shield')
    report = json.loads((ROOT / 'split-materialization.json').read_text())
    assert report['shared_java_asset_files'] > 100
    assert report['changed_shared_inputs'] == ['launcher-lib/src/main/java/com/boop/launcher/AppRepository.java']
    for path, sha in report['source_sha256_before'].items():
        if 'Voice' in path or 'Speech' in path or '/assets/' in path:
            assert report['source_sha256_after'][path] == sha
    settings = (root/'settings.gradle').read_text()
    assert 'include(":app")' not in settings
    assert all(module in settings for module in (':assistant-lib', ':wall-app', ':shield-app'))
