package com.boop.shared;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/** Ephemeral UI text: never persist it or include it in conversation requests. */
public final class DeezerScreen {
    private static final String PACKAGE="deezer.android.app";
    private final Document document;
    private DeezerScreen(Document document) { this.document=document; }
    public static DeezerScreen parse(String xml) throws IOException {
        if(xml==null || xml.length()>524288 || xml.contains("<!")) throw new IOException("Unsafe screen response");
        try {
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder=factory.newDocumentBuilder();
            builder.setEntityResolver((publicId,systemId)->{ throw new org.xml.sax.SAXException("External entity rejected"); });
            Document doc=builder.parse(new InputSource(new StringReader(xml)));
            if(!"hierarchy".equals(doc.getDocumentElement().getTagName())) throw new IOException("Not an Android screen");
            return new DeezerScreen(doc);
        } catch(Exception e) { throw new IOException("Screen unavailable",e); }
    }
    public boolean isDeezer() {
        boolean seen=false;
        NodeList nodes=document.getElementsByTagName("node");
        for(int i=0;i<nodes.getLength();i++) {
            String pkg=((Element)nodes.item(i)).getAttribute("package");
            if(PACKAGE.equals(pkg)) seen=true;
            else if(!pkg.isEmpty()) return false;
        }
        return seen;
    }
    public boolean hasText(String text) {
        if(!isDeezer()) return false;
        NodeList nodes=document.getElementsByTagName("node");
        for(int i=0;i<nodes.getLength();i++) if(normal(text).equals(normal(((Element)nodes.item(i)).getAttribute("text")))) return true;
        return false;
    }
    public boolean hasLyricsPanel() {
        if(!isDeezer()) return false;
        NodeList nodes=document.getElementsByTagName("node");
        for(int i=0;i<nodes.getLength();i++) {
            Element item=(Element)nodes.item(i);
            if(!"true".equals(item.getAttribute("scrollable"))) continue;
            NodeList descendants=item.getElementsByTagName("node");
            int lyricLines=0;
            for(int j=0;j<descendants.getLength();j++) {
                Element child=(Element)descendants.item(j);
                if("android.widget.TextView".equals(child.getAttribute("class"))
                        && !normal(child.getAttribute("text")).isEmpty()) lyricLines++;
            }
            if(lyricLines>=3) return true;
        }
        return false;
    }
    public Target lyricsTarget() { return target("Lyrics"); }
    public Target target(String text) {
        if(!isDeezer()) return null;
        Map<String,Target> matches=new LinkedHashMap<>();
        NodeList nodes=document.getElementsByTagName("node");
        for(int i=0;i<nodes.getLength();i++) {
            Element item=(Element)nodes.item(i);
            if(!normal(text).equals(normal(item.getAttribute("text")))
                    && !normal(text).equals(normal(item.getAttribute("content-desc")))) continue;
            Node parent=item;
            while(parent instanceof Element) {
                Element e=(Element)parent;
                if("true".equals(e.getAttribute("clickable")) && "true".equals(e.getAttribute("enabled"))) {
                    String bounds=e.getAttribute("bounds");
                    boolean focused="true".equals(e.getAttribute("focused"));
                    // Compose can expose nested wrappers for the same button; focus belongs
                    // to the outer wrapper. Only identical bounds may share that focus.
                    Node wrapper=e.getParentNode();
                    while(wrapper instanceof Element && bounds.equals(((Element)wrapper).getAttribute("bounds"))) {
                        focused|="true".equals(((Element)wrapper).getAttribute("focused"));
                        wrapper=wrapper.getParentNode();
                    }
                    Target candidate=Target.from(bounds, focused);
                    if(candidate!=null) matches.put(e.getAttribute("bounds"),candidate);
                    break;
                }
                parent=parent.getParentNode();
            }
        }
        return matches.size()==1 ? matches.values().iterator().next() : null;
    }
    private static String normal(String text) { return text.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT); }
    public static final class Target {
        public final int x,y;
        public final boolean focused;
        private Target(int x,int y,boolean focused) { this.x=x; this.y=y; this.focused=focused; }
        private static Target from(String bounds, boolean focused) {
            Matcher m=Pattern.compile("\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]").matcher(bounds);
            if(!m.matches()) return null;
            try {
                int x1=Integer.parseInt(m.group(1)),y1=Integer.parseInt(m.group(2)),x2=Integer.parseInt(m.group(3)),y2=Integer.parseInt(m.group(4));
                if(x2<=x1 || y2<=y1 || x2>16384 || y2>16384) return null;
                return new Target((x1+x2)/2,(y1+y2)/2,focused);
            } catch(NumberFormatException e) { return null; }
        }
    }
}
