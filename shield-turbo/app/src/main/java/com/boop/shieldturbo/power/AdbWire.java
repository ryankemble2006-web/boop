package com.boop.shieldturbo.power;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.*;
import javax.crypto.Cipher;

/** A bounded, single-stream ADB client. Its only destination is this device's loopback. */
public final class AdbWire implements Closeable {
    public static final int CNXN=0x4e584e43, AUTH=0x48545541, OPEN=0x4e45504f,
            OKAY=0x59414b4f, WRTE=0x45545257, CLSE=0x45534c43;
    public static final int MAX_PACKET=1024*1024, MAX_OUTPUT=256*1024;
    private final Socket socket = new Socket();
    private InputStream input;
    private OutputStream output;
    private long deadline;
    private int streamId;

    /** Used when a background caller is allowed to use only an already-trusted ADB key. */
    public static final class AdbApprovalRequiredException extends IOException {
        public AdbApprovalRequiredException(String message) { super(message); }
    }

    public static final class Packet {
        public final int command, arg0, arg1;
        public final byte[] payload;
        Packet(int c,int a,int b,byte[] p) { command=c;arg0=a;arg1=b;payload=p; }
    }
    public static final class Result {
        public final String output;
        public final int exitCode;
        Result(String o,int c) { output=o;exitCode=c; }
    }
    public static byte[] encode(int command,int arg0,int arg1,byte[] payload) {
        if(payload.length>MAX_PACKET) throw new IllegalArgumentException("ADB packet too large");
        int sum=0; for(byte b:payload) sum+=b&255;
        ByteBuffer out=ByteBuffer.allocate(24+payload.length).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(command).putInt(arg0).putInt(arg1).putInt(payload.length).putInt(sum)
                .putInt(~command).put(payload);
        return out.array();
    }
    public static Packet read(InputStream in) throws IOException {
        DataInputStream data=new DataInputStream(in);
        byte[] header=new byte[24]; data.readFully(header);
        ByteBuffer b=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int command=b.getInt(),arg0=b.getInt(),arg1=b.getInt(),len=b.getInt(),sum=b.getInt(),magic=b.getInt();
        if(magic!=~command || len<0 || len>MAX_PACKET) throw new IOException("Invalid ADB header");
        byte[] payload=new byte[len]; data.readFully(payload);
        int actual=0; for(byte value:payload) actual+=value&255;
        if(actual!=sum) throw new IOException("Invalid ADB checksum");
        return new Packet(command,arg0,arg1,payload);
    }
    private void limit(int timeoutMs) { deadline=System.nanoTime()+timeoutMs*1_000_000L; }
    private Packet receive() throws IOException {
        if(Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Cancelled");
        long remaining=(deadline-System.nanoTime())/1_000_000L;
        if(remaining<=0) throw new SocketTimeoutException("ADB timed out");
        socket.setSoTimeout((int)Math.min(remaining,Integer.MAX_VALUE));
        return read(input);
    }
    private void send(int c,int a,int b,byte[] bytes) throws IOException {
        output.write(encode(c,a,b,bytes)); output.flush();
    }
    public void connect(int port,KeyPair identity,int timeoutMs,Runnable approvalRequired) throws Exception {
        connect(port,identity,timeoutMs,approvalRequired,true);
    }
    public void connect(int port,KeyPair identity,int timeoutMs,Runnable approvalRequired,boolean allowNewApproval) throws Exception {
        if(port<1 || port>65535) throw new IllegalArgumentException("Invalid local ADB port");
        // No hostname, network scan, user-supplied IP or off-device address is accepted.
        socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127,0,0,1}),port),2000);
        socket.setTcpNoDelay(true);
        input=socket.getInputStream(); output=socket.getOutputStream(); limit(timeoutMs);
        // Negotiate the original protocol so payload checksums remain mandatory.
        send(CNXN,0x01000000,4096,"host::\0".getBytes(StandardCharsets.UTF_8));
        boolean signatureSent=false, publicSent=false;
        for(int i=0;i<8;i++) {
            Packet p=receive();
            if(p.command==CNXN) return;
            if(p.command!=AUTH || p.arg0!=1 || p.payload.length!=20) throw new IOException("Unsupported ADB handshake");
            if(!signatureSent) { send(AUTH,2,0,sign(identity,p.payload)); signatureSent=true; }
            else if(!publicSent) {
                if(!allowNewApproval) throw new AdbApprovalRequiredException("ADB key is not already trusted; open SHIELD TURBO and enable ADB TURBO interactively");
                send(AUTH,3,0,publicPayload(identity)); publicSent=true; approvalRequired.run();
            } else throw new IOException("Debugging approval was not accepted");
        }
        throw new IOException("ADB handshake did not finish");
    }
    public Result execute(String command,int timeoutMs) throws IOException {
        if(command.indexOf('\0')>=0 || command.length()>2048) throw new IllegalArgumentException("Invalid command");
        String marker="__TURBO_RC_"+UUID.randomUUID().toString().replace("-","")+"__";
        String script="("+command+"); result=$?; printf '\\n"+marker+"%s\\n' \"$result\"";
        int local=++streamId, remote=0; boolean opened=false;
        limit(timeoutMs);
        send(OPEN,local,0,("shell:"+script+"\0").getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        for(int count=0;count<8192;count++) {
            Packet p=receive();
            if(p.arg1!=local) throw new IOException("Unexpected ADB stream");
            if(p.command==OKAY) { remote=p.arg0;opened=true; }
            else if(p.command==WRTE) {
                remote=p.arg0;opened=true;
                if(bytes.size()+p.payload.length>MAX_OUTPUT) throw new IOException("Diagnostic output exceeded its limit");
                bytes.write(p.payload);send(OKAY,local,remote,new byte[0]);
            } else if(p.command==CLSE) {
                if(!opened) throw new IOException("Shield rejected the ADB service");
                send(CLSE,local,p.arg0,new byte[0]);
                return parseResult(bytes.toString("UTF-8"),marker);
            } else throw new IOException("Unexpected ADB response");
        }
        throw new IOException("Too many ADB packets");
    }
    public static Result parseResult(String text,String marker) throws IOException {
        int pos=text.lastIndexOf("\n"+marker);
        if(pos<0) throw new IOException("Connection closed without a command result");
        String status=text.substring(pos+marker.length()+1).trim();
        if(!status.matches("[0-9]{1,3}")) throw new IOException("Invalid command result");
        int code=Integer.parseInt(status);
        if(code>255) throw new IOException("Invalid command status");
        return new Result(text.substring(0,pos).trim(),code);
    }
    public static synchronized KeyPair identity(File file) throws Exception {
        KeyFactory factory=KeyFactory.getInstance("RSA");
        if(file.exists()) {
            try(DataInputStream in=new DataInputStream(new FileInputStream(file))) {
                int size=in.readInt();if(size<128 || size>8192) throw new IOException("Invalid private ADB identity");
                byte[] priv=new byte[size];in.readFully(priv);
                int publicSize=in.readInt();if(publicSize<128 || publicSize>8192) throw new IOException("Invalid public ADB identity");
                byte[] pub=new byte[publicSize];in.readFully(pub);
                KeyPair pair=new KeyPair(factory.generatePublic(new X509EncodedKeySpec(pub)),factory.generatePrivate(new PKCS8EncodedKeySpec(priv)));
                if(((RSAPublicKey)pair.getPublic()).getModulus().bitLength()!=2048) throw new IOException("Invalid ADB key size");
                return pair;
            }
        }
        KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        KeyPair pair=generator.generateKeyPair();
        File temporary=new File(file.getParentFile(),file.getName()+".new");
        try {
            try(FileOutputStream out=new FileOutputStream(temporary);DataOutputStream data=new DataOutputStream(out)) {
                byte[] priv=pair.getPrivate().getEncoded(),pub=pair.getPublic().getEncoded();
                data.writeInt(priv.length);data.write(priv);data.writeInt(pub.length);data.write(pub);
                data.flush();out.getFD().sync();
            }
            if(!temporary.renameTo(file)) throw new IOException("Could not save private ADB identity");
            return pair;
        } finally { if(temporary.exists()) temporary.delete(); }
    }
    public static byte[] sign(KeyPair pair,byte[] token) throws Exception {
        if(token.length!=20) throw new IllegalArgumentException("ADB token must be 20 bytes");
        byte[] info={0x30,0x21,0x30,0x09,0x06,0x05,0x2b,0x0e,0x03,0x02,0x1a,0x05,0x00,0x04,0x14};
        byte[] padded=new byte[256];Arrays.fill(padded,(byte)255);padded[0]=0;padded[1]=1;
        int start=256-info.length-token.length;padded[start-1]=0;
        System.arraycopy(info,0,padded,start,info.length);System.arraycopy(token,0,padded,236,20);
        Cipher rsa=Cipher.getInstance("RSA/ECB/NoPadding");rsa.init(Cipher.ENCRYPT_MODE,pair.getPrivate());
        return rsa.doFinal(padded);
    }
    public static byte[] publicPayload(KeyPair pair) {
        RSAPublicKey key=(RSAPublicKey)pair.getPublic();BigInteger n=key.getModulus();
        if(n.bitLength()!=2048) throw new IllegalArgumentException("ADB requires a 2048-bit key");
        BigInteger word=BigInteger.ONE.shiftLeft(32);
        ByteBuffer data=ByteBuffer.allocate(524).order(ByteOrder.LITTLE_ENDIAN);
        data.putInt(64).putInt(n.mod(word).modInverse(word).negate().intValue());
        for(int i=0;i<64;i++) data.putInt(n.shiftRight(i*32).intValue());
        BigInteger rr=BigInteger.ONE.shiftLeft(4096).mod(n);
        for(int i=0;i<64;i++) data.putInt(rr.shiftRight(i*32).intValue());
        data.putInt(key.getPublicExponent().intValue());
        return (Base64.getEncoder().encodeToString(data.array())+" SHIELD_TURBO@localhost\0").getBytes(StandardCharsets.UTF_8);
    }
    @Override public void close() throws IOException { socket.close(); }
}
