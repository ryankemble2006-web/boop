package com.boop.lab;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class LabPacket {
    public static final String MAGIC="BOOPLAB1";
    public final String kind,deviceId,deviceName,requestId,testType,title,text;
    private LabPacket(String kind,String deviceId,String deviceName,String requestId,
                      String testType,String title,String text){
        this.kind=kind;this.deviceId=deviceId;this.deviceName=deviceName;
        this.requestId=requestId;this.testType=testType;this.title=title;this.text=text;
    }
    public static LabPacket discover(String id,String name){return new LabPacket("DISCOVER",id,name,"","","","");}
    public static LabPacket here(String id,String name){return new LabPacket("HERE",id,name,"","","","");}
    public static LabPacket notify(String requestId,String type,String title,String text){return new LabPacket("NOTIFY","","",requestId,type,title,text);}
    private static String b(String s){return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));}
    private static String u(String s){return new String(Base64.getUrlDecoder().decode(s),StandardCharsets.UTF_8);}
    public String encode(){
        if("DISCOVER".equals(kind)||"HERE".equals(kind))return MAGIC+"|"+kind+"|"+b(deviceId)+"|"+b(deviceName);
        if("NOTIFY".equals(kind))return MAGIC+"|NOTIFY|"+b(requestId)+"|"+b(testType)+"|"+b(title)+"|"+b(text);
        throw new IllegalStateException("Unknown kind");
    }
    public static LabPacket parse(String raw){
        try{
            String[] p=raw.split("\\|",-1);if(p.length<4||!MAGIC.equals(p[0]))throw new IllegalArgumentException("Bad packet");
            if(("DISCOVER".equals(p[1])||"HERE".equals(p[1]))&&p.length==4)return new LabPacket(p[1],u(p[2]),u(p[3]),"","","","");
            if("NOTIFY".equals(p[1])&&p.length==6)return new LabPacket("NOTIFY","","",u(p[2]),u(p[3]),u(p[4]),u(p[5]));
            throw new IllegalArgumentException("Bad packet");
        }catch(RuntimeException e){if(e instanceof IllegalArgumentException)throw (IllegalArgumentException)e;throw new IllegalArgumentException("Bad packet",e);}
    }
}
