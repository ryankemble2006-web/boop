from pathlib import Path
import os, shutil, subprocess, tempfile
root=Path(__file__).resolve().parents[1]
src=root/'shield-launcher/overrides/java/com/boop/shieldhome/LocalMediaClosePolicy.java'
assert src.is_file(), "Standalone local media close is not implemented"
def tool(n): return shutil.which(n) or str(Path(os.environ['JAVA_HOME'])/'bin'/(n+('.exe' if os.name=='nt' else '')))
with tempfile.TemporaryDirectory() as out:
 subprocess.run([tool('javac'),'-encoding','UTF-8','-d',out,str(src),str(root/'shield-launcher/tests/LocalMediaClosePolicyTest.java')],check=True)
 subprocess.run([tool('java'),'-cp',out,'com.boop.shieldhome.LocalMediaClosePolicyTest'],check=True)
