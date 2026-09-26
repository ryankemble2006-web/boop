from pathlib import Path
import hashlib,json,os,re,shutil,subprocess,zipfile

tools=Path(os.environ['ANDROID_HOME'])/'build-tools/36.0.0'
apk=Path('boop-build/BOOP-Alpha1/notification-sender/build/outputs/apk/debug/notification-sender-debug.apk')
cert=subprocess.check_output([str(tools/'apksigner'),'verify','--print-certs',str(apk)],text=True)
expected=Path('shield-overlay/signing/boop-dev-cert-sha256.txt').read_text().strip().lower()
assert re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-fA-F]+)',cert).group(1).lower()==expected
badging=subprocess.check_output([str(tools/'aapt'),'dump','badging',str(apk)],text=True)
assert "package: name='com.boop.notificationlab'" in badging and "versionCode='2'" in badging
assert "com.boop.alpha1.NotificationSenderActivity" in badging
with zipfile.ZipFile(apk) as z:
    assert z.testzip() is None
    assert not any(n.startswith('assets/') for n in z.namelist()), 'Sender must not own or redraw BOOP art'
out=Path('split-artifact');out.mkdir(exist_ok=True)
name='BOOP-Test-Sender-v2.apk';shutil.copyfile(apk,out/name)
(out/'notification-sender-receipt.json').write_text(json.dumps(dict(
    file=name,sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),bytes=apk.stat().st_size,
    signerSha256=expected,package='com.boop.notificationlab',versionCode=2),indent=2)+'\n')
print('PASS notification sender package, permanent signer, ZIP and no duplicated artwork')
