package com.boop.shieldturbo.power;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import javax.crypto.Cipher;

/** Nonvisual protocol tests. No Android settings or real devices are changed. */
public final class AdbWireSelfTest {
    private static void check(boolean value,String why) { if(!value) throw new AssertionError(why); }
    private static void rejects(byte[] bytes) throws Exception {
        try { AdbWire.read(new ByteArrayInputStream(bytes));throw new AssertionError("Invalid packet accepted"); }
        catch(IOException expected) { }
    }
    public static void runAll() throws Exception {
        byte[] packet=AdbWire.encode(AdbWire.CNXN,0x01000000,4096,new byte[]{65,0});
        check(packet.length==26,"Frame size");
        ByteBuffer b=ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN);
        check(b.getInt()==AdbWire.CNXN && b.getInt()==0x01000000 && b.getInt()==4096 && b.getInt()==2 && b.getInt()==65 && b.getInt()==~AdbWire.CNXN,"Header/checksum");
        AdbWire.Packet read=AdbWire.read(new ByteArrayInputStream(packet) {
            @Override public synchronized int read(byte[] v,int off,int len) { return super.read(v,off,Math.min(1,len)); }
        });
        check(Arrays.equals(read.payload,new byte[]{65,0}),"Fragmented read");
        byte[] corrupt=packet.clone();corrupt[24]++;rejects(corrupt);
        corrupt=packet.clone();corrupt[20]++;rejects(corrupt);
        corrupt=packet.clone();ByteBuffer.wrap(corrupt).order(ByteOrder.LITTLE_ENDIAN).putInt(12,-1);rejects(corrupt);
        corrupt=packet.clone();ByteBuffer.wrap(corrupt).order(ByteOrder.LITTLE_ENDIAN).putInt(12,AdbWire.MAX_PACKET+1);rejects(corrupt);
        rejects(Arrays.copyOf(packet,25));
        AdbWire.Result result=AdbWire.parseResult("example\n__marker__7\n","__marker__");
        check(result.exitCode==7 && result.output.equals("example"),"Nonzero shell status");
        try { AdbWire.parseResult("success without receipt","__marker__");throw new AssertionError("Missing receipt accepted"); }
        catch(IOException expected) { }
        File dir=Files.createTempDirectory("turbo-adb-test").toFile();File file=new File(dir,"identity");
        KeyPair key=AdbWire.identity(file),again=AdbWire.identity(file);
        check(Arrays.equals(key.getPrivate().getEncoded(),again.getPrivate().getEncoded()),"Identity must survive reopening");
        byte[] token=new byte[20];new SecureRandom().nextBytes(token);
        Cipher rsa=Cipher.getInstance("RSA/ECB/NoPadding");rsa.init(Cipher.DECRYPT_MODE,key.getPublic());
        byte[] padded=rsa.doFinal(AdbWire.sign(key,token));
        check(padded.length==256 && padded[0]==0 && padded[1]==1 && Arrays.equals(token,Arrays.copyOfRange(padded,236,256)),"ADB raw-token RSA signature");
        String pub=new String(AdbWire.publicPayload(key),StandardCharsets.UTF_8);
        byte[] struct=Base64.getDecoder().decode(pub.split(" ")[0]);
        check(struct.length==524 && ByteBuffer.wrap(struct).order(ByteOrder.LITTLE_ENDIAN).getInt()==64 && pub.endsWith("SHIELD_TURBO@localhost\0"),"ADB public-key format");
        protocolRoundTrip(key,token);
        check(file.delete() && dir.delete(),"Private fixture cleanup");
    }
    private static void protocolRoundTrip(KeyPair key,byte[] token) throws Exception {
        ExecutorService worker=Executors.newSingleThreadExecutor();
        try(ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
            Future<?> fixture=worker.submit(() -> {
                try(Socket socket=server.accept()) {
                    socket.setSoTimeout(5000);InputStream in=socket.getInputStream();OutputStream out=socket.getOutputStream();
                    check(AdbWire.read(in).command==AdbWire.CNXN,"Connect packet");
                    out.write(AdbWire.encode(AdbWire.AUTH,1,0,token));out.flush();
                    AdbWire.Packet signature=AdbWire.read(in);
                    check(signature.command==AdbWire.AUTH && signature.arg0==2 && signature.payload.length==256,"Authentication signature");
                    out.write(AdbWire.encode(AdbWire.AUTH,1,0,token));out.flush();
                    AdbWire.Packet publicKey=AdbWire.read(in);
                    check(publicKey.command==AdbWire.AUTH && publicKey.arg0==3,"Approval path");
                    out.write(AdbWire.encode(AdbWire.CNXN,0x01000000,4096,"device::\0".getBytes(StandardCharsets.UTF_8)));out.flush();
                    AdbWire.Packet open=AdbWire.read(in);String command=new String(open.payload,StandardCharsets.UTF_8);
                    check(command.startsWith("shell:(id -u)"),"Requested command");
                    Matcher marker=Pattern.compile("__TURBO_RC_[0-9a-f]+__").matcher(command);check(marker.find(),"Unique receipt marker");
                    out.write(AdbWire.encode(AdbWire.OKAY,42,open.arg0,new byte[0]));
                    out.write(AdbWire.encode(AdbWire.WRTE,42,open.arg0,"20".getBytes(StandardCharsets.UTF_8)));out.flush();
                    check(AdbWire.read(in).command==AdbWire.OKAY,"Flow-control acknowledgement");
                    out.write(AdbWire.encode(AdbWire.WRTE,42,open.arg0,("00\n\n"+marker.group()+"0\n").getBytes(StandardCharsets.UTF_8)));out.flush();
                    check(AdbWire.read(in).command==AdbWire.OKAY,"Final data acknowledged before close");
                    out.write(AdbWire.encode(AdbWire.CLSE,42,open.arg0,new byte[0]));out.flush();
                    check(AdbWire.read(in).command==AdbWire.CLSE,"Stream closes cleanly");
                } catch(Exception e) { throw new RuntimeException(e); }
            });
            final boolean[] notice={false};
            try(AdbWire client=new AdbWire()) {
                client.connect(server.getLocalPort(),key,5000,() -> notice[0]=true);
                AdbWire.Result result=client.execute("id -u",5000);
                check(notice[0] && result.exitCode==0 && result.output.equals("2000"),"Complete authenticated command result");
            }
            fixture.get(6,TimeUnit.SECONDS);
        } finally { worker.shutdownNow(); }
    }
    public static void main(String[] args) throws Exception { runAll();System.out.println("PASS: frame integrity, bounds, fragmented reads, RSA, persistent identity, authorisation, stream flow control and exit receipts"); }
}
