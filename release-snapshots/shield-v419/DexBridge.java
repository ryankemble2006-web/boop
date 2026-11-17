import java.io.*;
import java.nio.file.*;
import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.writer.builder.DexBuilder;
import com.android.tools.smali.dexlib2.writer.io.FileDataStore;
import com.android.tools.smali.baksmali.*;
import brut.androlib.src.SmaliBuilder;
public final class DexBridge {
 public static void main(String[] args) throws Exception {
  File input=new File(args[1]),output=new File(args[2]);
  if (args[0].equals("decode")) {
   MultiDexContainer container=DexFileFactory.loadDexContainer(input,new Opcodes(29,-1));
   Object name=container.getDexEntryNames().get(0);
   BaksmaliOptions options=new BaksmaliOptions(); options.localsDirective=true; options.debugInfo=true;
   Baksmali.disassembleDexFile(container.getEntry((String)name).getDexFile(),output,2,options);
  } else if(args[0].equals("compile")) {
   DexBuilder dex=new DexBuilder(new Opcodes(29,-1));
   SmaliBuilder builder=new SmaliBuilder(input,29);
   try (java.util.stream.Stream<Path> files=Files.walk(input.toPath())) {
    for(Path file:(Iterable<Path>)files.filter(p->p.toString().endsWith(".smali"))::iterator)
     builder.buildFile(input.toPath().relativize(file).toString().replace(File.separatorChar,'/'),dex);
   }
   dex.writeTo(new FileDataStore(output));
  } else throw new IllegalArgumentException("Unknown command");
 }
}

