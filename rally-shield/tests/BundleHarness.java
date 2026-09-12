package com.boop.rally;
import java.io.*;
public final class BundleHarness {
    public static void main(String[] args) throws Exception {
        File valid=new File(args[0]), invalid=new File(args[1]);
        GameBundle.validate(valid, GameSpec.CLASSIC);
        try { GameBundle.validate(invalid,GameSpec.CLASSIC); throw new AssertionError("Unsafe bundle accepted"); }
        catch(IOException expected) { }
        try { GameBundle.validate(valid,GameSpec.CHAMPIONSHIP); throw new AssertionError("Wrong game accepted"); }
        catch(IOException expected) { }
        System.out.println("Bundle policy: valid pack accepted; traversal and wrong game rejected");
    }
}
