package com.boop.lab;

import java.net.*;
import java.nio.charset.StandardCharsets;

public final class LabLanClient {
    private LabLanClient(){}
    public static final class Peer {public final String id,name;public final InetAddress address;Peer(String id,String name,InetAddress address){this.id=id;this.name=name;this.address=address;}}
    public static Peer discover(String senderId,String senderName)throws Exception{
        try(DatagramSocket socket=new DatagramSocket()){
            socket.setBroadcast(true);socket.setSoTimeout(1800);
            byte[] out=LabPacket.discover(senderId,senderName).encode().getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(out,out.length,InetAddress.getByName("255.255.255.255"),LabNotificationReceiverService.PORT));
            byte[] buf=new byte[4096];long until=System.currentTimeMillis()+1800;
            while(System.currentTimeMillis()<until){
                try{
                    DatagramPacket in=new DatagramPacket(buf,buf.length);socket.receive(in);
                    LabPacket p=LabPacket.parse(new String(in.getData(),in.getOffset(),in.getLength(),StandardCharsets.UTF_8));
                    if("HERE".equals(p.kind)&&!senderId.equals(p.deviceId))return new Peer(p.deviceId,p.deviceName,in.getAddress());
                }catch(SocketTimeoutException timeout){break;}catch(IllegalArgumentException ignored){}
            }
            return null;
        }
    }
    public static void send(Peer peer,String type,String title,String text)throws Exception{
        String request=Long.toHexString(System.nanoTime());
        byte[] out=LabPacket.notify(request,type,title,text).encode().getBytes(StandardCharsets.UTF_8);
        try(DatagramSocket socket=new DatagramSocket()){
            socket.send(new DatagramPacket(out,out.length,peer.address,LabNotificationReceiverService.PORT));
        }
    }
}
