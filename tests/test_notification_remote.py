"""Exercise the actual lab socket boundary, including rejected and stale requests."""
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

HARNESS = r'''
package com.boop.alpha1;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public class RemoteHarness {
  static void check(boolean yes, String label) { if(!yes) throw new AssertionError(label); }
  static String raw(int port, String line) throws Exception {
    try(Socket s=new Socket("127.0.0.1",port)) {
      s.setSoTimeout(4000);
      s.getOutputStream().write(line.getBytes(StandardCharsets.US_ASCII));
      s.getOutputStream().flush();
      return new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
    }
  }
  public static void main(String[] args) throws Exception {
    BoopPreviewSendGate gate=new BoopPreviewSendGate();
    gate.select();
    BoopPreviewSendGate.Ticket old=gate.begin("PING");
    gate.select();
    check(!gate.finish(old,true),"old phone acknowledgement ignored");
    check(!gate.paired(),"old phone cannot pair newly selected phone");
    BoopPreviewSendGate.Ticket current=gate.begin("PING");
    check(gate.finish(current,true) && gate.paired(),"current endpoint pairs");
    BoopPreviewSendGate.Ticket preview=gate.begin("GMAIL");
    check(gate.queueStopIfBusy(),"stop queues during in-flight delivery");
    check(gate.finish(preview,true) && gate.takeStop(),"queued stop delivered after acknowledgement");
    check(!gate.takeStop(),"stop consumed exactly once");
    AtomicInteger calls=new AtomicInteger();
    List<String> delivered=Collections.synchronizedList(new ArrayList<>());
    try(BoopPreviewServer server=new BoopPreviewServer("123456", c->{calls.incrementAndGet();delivered.add(c);return true;})) {
      int port=server.port();
      check("ERROR auth".equals(raw(port,"000000\tGMAIL\n")),"wrong PIN rejected");
      check("ERROR command".equals(raw(port,"123456\tSHELL\n")),"unknown command rejected");
      check("ERROR request".equals(raw(port,"123456\tGMAIL\textra\n")),"extra fields rejected");
      check("ERROR request".equals(raw(port,"x".repeat(140)+"\n")),"oversized input rejected");
      check(calls.get()==0,"rejected messages never reach renderer");
      check("OK PING".equals(BoopPreviewClient.send("127.0.0.1",port,"123456","PING")),"pair handshake");
      check(calls.get()==0,"PING does not animate");
      String[] cases={"FACEBOOK","WHATSAPP","GMAIL","X","YOUTUBE","MESSENGER","INSTAGRAM","DISCORD","SPOTIFY","REDDIT","LOCKED","BUNDLE","STOP"};
      for(String c:cases) check(("OK "+c).equals(BoopPreviewClient.send("127.0.0.1",port,"123456",c)),"accepted "+c);
      check(delivered.equals(Arrays.asList(cases)),"exactly once and in order");
      // A client stalled mid-line must not keep the listening socket alive after close.
      Socket stalled=new Socket("127.0.0.1",port);
      Thread.sleep(80);
      server.close();
      check(!server.isRunning(),"closed state");
      try { BoopPreviewClient.send("127.0.0.1",port,"123456","GMAIL"); throw new AssertionError("closed server accepted request"); }
      catch(IOException expected) { }
      stalled.close();
    }
    try(BoopPreviewServer unavailable=new BoopPreviewServer("654321", c->false)) {
      check("ERROR inactive".equals(BoopPreviewClient.send("127.0.0.1",unavailable.port(),"654321","GMAIL")),"no false delivery acknowledgement");
    }
    for(String pin:new String[]{"", "12345", "1234567", "abcdef", "12345\n"}) {
      try { new BoopPreviewServer(pin,c->true); throw new AssertionError("invalid PIN accepted"); }
      catch(IllegalArgumentException expected) { }
    }
    BoopNotificationPresentation locked=BoopDevNotificationPreview.presentation(BoopDevMenuModel.Action.NOTIFICATION_LOCKED,1);
    check(locked.cards().size()==3,"locked count");
    for(BoopNotificationEnvelope c:locked.cards()) check(c.title()==null && c.text()==null,"locked preview redacted");
    for(String c:BoopPreviewProtocol.SCENARIOS) {
      BoopNotificationPresentation p=BoopDevNotificationPreview.presentation(BoopDevMenuModel.Action.valueOf("NOTIFICATION_"+c),1);
      check(!p.cards().isEmpty(),"every remote command has a real production preview");
    }
    System.out.println("PASS real socket authentication, 12 production previews, stop, bounds, teardown, redaction");
  }
}
'''

class RemoteTests(unittest.TestCase):
    def test_remote_delivery_is_bounded_authenticated_and_uses_real_previews(self):
        names = ['BoopPreviewProtocol','BoopPreviewServer','BoopPreviewClient','BoopPreviewSendGate',
                 'BoopDevNotificationPreview','BoopDevMenuModel','BoopNotificationPresentation',
                 'BoopNotificationEnvelope','BoopNotificationSurface']
        files=[ROOT/'source'/f'{n}.java' for n in names]
        self.assertTrue(all(p.is_file() for p in files), 'Remote preview transport is not implemented')
        with tempfile.TemporaryDirectory() as tmp:
            harness=Path(tmp)/'RemoteHarness.java'
            harness.write_text(HARNESS)
            subprocess.run(['javac','-d',tmp,*map(str,files),str(harness)],check=True)
            subprocess.run(['java','-cp',tmp,'com.boop.alpha1.RemoteHarness'],check=True,timeout=20)

if __name__=='__main__': unittest.main()
