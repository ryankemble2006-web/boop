package com.boop.alpha1;
import com.boop.bridge.DeezerHeartRules;
import java.io.*;
public final class Heart257Probe {
 static int checks; static void yes(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 public static void main(String[] args)throws Exception {
  yes(DeezerHeartRules.artistMatches("Waze & Odyssey","Waze & Odyssey, George Michael, Mary J. Blige, Tommy Theo"));
  yes(DeezerHeartRules.artistMatches("Waze & Odyssey","Waze & Odyssey"));
  yes(!DeezerHeartRules.artistMatches("Waze","Waze & Odyssey"));
  yes(!DeezerHeartRules.artistMatches("George Michael","Waze & Odyssey, George Michael"));
  yes(!DeezerHeartRules.artistMatches("Waze & Odyssey","Waze & Odyssey Tribute, Somebody"));
  yes(!DeezerHeartRules.artistMatches("","Somebody"));
  yes(!DeezerHeartRules.artistMatches("Artist","Artist, "));
  StringBuilder payload=new StringBuilder();for(int i=0;i<60000;i++)payload.append('A');
  java.util.List<String> uploads=DeezerHeartTransport.upload("/data/local/tmp/boop_test.jar.b64",payload.toString());
  StringBuilder received=new StringBuilder();
  for(String command:uploads){yes(command.length()<=2048);if(command.contains("printf '%s'")){int start=command.indexOf("'",command.indexOf("'%s'")+4);received.append(command.substring(start+1,command.indexOf("'",start+1)));}}
  yes(received.toString().equals(payload.toString()));
  int[] fallback={0},sent={0},closed={0};
  DeezerHeartTransport.Fallback old=()->{fallback[0]++;return "old";};
  DeezerHeartTransport.Connection live=new DeezerHeartTransport.Connection(){
   public String execute(String command)throws Exception{sent[0]++;return "OK";}
   public void close(){closed[0]++;}
  };
  yes("OK".equals(DeezerHeartTransport.execute(()->live,old,"toggle",()->true)));
  yes(sent[0]==1&&fallback[0]==0&&closed[0]==1);
  yes("old".equals(DeezerHeartTransport.execute(()->{throw new IOException("not trusted");},old,"toggle",()->true)));
  yes(fallback[0]==1);
  try {DeezerHeartTransport.execute(()->new DeezerHeartTransport.Connection(){
   public String execute(String c)throws Exception{sent[0]++;throw new IOException("lost after send");}
   public void close(){closed[0]++;}
  },old,"toggle",()->true);throw new AssertionError("lost receipt replayed");}catch(IOException expected){}
  yes(sent[0]==2&&fallback[0]==1&&closed[0]==2);
  try {DeezerHeartTransport.execute(()->live,old,"toggle",()->false);throw new AssertionError("cancel sent");}catch(IOException expected){}
  yes(sent[0]==2&&fallback[0]==1);
  System.out.println("Heart257: "+checks+" checks passed");
 }
}
