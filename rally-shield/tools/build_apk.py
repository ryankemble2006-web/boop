"""Reproducible Linux Android build. No game data or signing keys are read."""
from pathlib import Path
import hashlib
import os
import shutil
import subprocess
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
REVISION = 'a4a0bab7f8931433588f2fcad9045c85b277373d'
NDK_VERSION = '26.3.11579264'

def run(*args, cwd=None):
    subprocess.run([str(a) for a in args], cwd=cwd, check=True)

def main():
    sdk = Path(os.environ['ANDROID_HOME'])
    ndk = sdk / 'ndk' / NDK_VERSION
    tools = sdk / 'build-tools' / '36.0.0'
    android = sdk / 'platforms' / 'android-36' / 'android.jar'
    build = ROOT / 'build'
    build.mkdir(exist_ok=True)
    vendor = ROOT / 'vendor' / 'dosbox-pure'
    if not vendor.exists():
        vendor.parent.mkdir(exist_ok=True)
        run('git', 'clone', '--depth', '1', '--branch', '1.0-preview6', 'https://github.com/schellingb/dosbox-pure.git', vendor)
    head = subprocess.check_output(['git', '-C', str(vendor), 'rev-parse', 'HEAD'], text=True).strip()
    if head != REVISION:
        raise RuntimeError('Unexpected emulator revision')
    run(ndk/'ndk-build', 'APP_ABI=arm64-v8a', 'APP_PLATFORM=android-26', '-j2', cwd=vendor/'jni')
    libdir = vendor / 'libs' / 'arm64-v8a'
    core = libdir/'libretro.so'
    if not core.is_file():
        raise RuntimeError('Native DOSBox Pure output missing')
    native = build/'native'; native.mkdir(exist_ok=True)
    clang = ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android26-clang++'
    run(clang, '-std=c++17', '-O2', '-fPIC', '-shared', ROOT/'native/host.cpp',
        '-I'+str(vendor/'libretro-common/include'), '-L'+str(libdir), '-lretro',
        '-landroid', '-laaudio', '-llog', '-static-libstdc++', '-Wl,-z,max-page-size=16384',
        '-o', native/'libbooprally.so')
    generated=build/'generated'; classes=build/'classes'; dex=build/'dex'
    for d in (generated,classes,dex):
        if d.exists(): shutil.rmtree(d)
        d.mkdir()
    run(tools/'aapt2','compile','--dir',ROOT/'res','-o',build/'resources.zip')
    run(tools/'aapt2','link','-o',build/'base.apk','-I',android,'--manifest',ROOT/'AndroidManifest.xml',
        '--java',generated,'--min-sdk-version','26','--target-sdk-version','36',build/'resources.zip')
    java_sources=sorted((ROOT/'java').rglob('*.java'))+sorted(generated.rglob('*.java'))
    run('javac','-encoding','UTF-8','-source','8','-target','8','-cp',android,'-d',classes,*java_sources)
    run(tools/'d8','--lib',android,'--min-api','26','--output',dex,*sorted(classes.rglob('*.class')))
    with zipfile.ZipFile(build/'base.apk','a',compression=zipfile.ZIP_DEFLATED) as archive:
        for f in dex.glob('*.dex'): archive.write(f,f.name)
        archive.write(core,'lib/arm64-v8a/libretro.so')
        archive.write(native/'libbooprally.so','lib/arm64-v8a/libbooprally.so')
        for f in ('LICENSE','NOTICE.txt'): archive.write(ROOT/f,'assets/licenses/'+f)
        for f in ('LICENSE','DOSBOX-AUTHORS','DOSBOX-THANKS'):
            archive.write(vendor/f,'assets/licenses/dosbox-pure/'+f)
    run(tools/'zipalign','-P','16','-f','4',build/'base.apk',build/'BOOP-Rally-unsigned.apk')
    # Supply the actual complete corresponding source, excluding build products/private files.
    with tarfile.open(build/'corresponding-source.tar.gz','w:gz') as tar:
        for f in sorted(ROOT.rglob('*')):
            if not f.is_file(): continue
            rel=f.relative_to(ROOT)
            if any(x in rel.parts for x in ('build','vendor','release','games','__pycache__','.git')): continue
            tar.add(f,arcname='rally-shield/'+str(rel))
        for name in subprocess.check_output(['git','-C',str(vendor),'ls-files'],text=True).splitlines():
            f=vendor/name
            if f.is_file():tar.add(f,arcname='rally-shield/vendor/dosbox-pure/'+name)
    print('Unsigned APK built with pinned ARM64 emulator; signing and verification remain separate.')

if __name__=='__main__':main()
