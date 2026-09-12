"""Run the real pure-Java Startup Manager behavior suites; no device writes or visuals."""
from pathlib import Path
import os, re, shutil, subprocess, tempfile
root=Path(__file__).resolve().parents[1]
workflow=(root/'.github/workflows/build-boop-unified.yml').read_text()
block=workflow.split('javac -d work/startup-prevention-tests',1)[1].split('java -cp work/startup-prevention-tests',1)[0]
files=re.findall(r'(?:unified|source-test)/[^\s\\]+\.java',block)
tests=root/'unified/shield-home/src/test/java/com/boop/shieldhome'
files=list(dict.fromkeys(files+[p.relative_to(root).as_posix() for p in tests.glob('*RegressionTest.java')]))
names=['StartupPreventionPolicyTest','StartupPreventionRecordTest','StartupCleanupPolicyTest']
names+=['com.boop.shieldhome.'+Path(f).stem for f in files if '/src/test/' in f]
def tool(name):
    return shutil.which(name) or str(Path(os.environ['JAVA_HOME'])/'bin'/(name+('.exe' if os.name=='nt' else '')))
with tempfile.TemporaryDirectory() as out:
    subprocess.run([tool('javac'),'-encoding','UTF-8','-d',out,*[str(root/f) for f in files]],check=True)
    for name in names:
        subprocess.run([tool('java'),'-cp',out,name],check=True)
print(f'Startup Manager: {len(names)} suites passed')
