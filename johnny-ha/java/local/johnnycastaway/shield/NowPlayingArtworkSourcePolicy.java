package local.johnnycastaway.shield;
import java.net.URI;
/** Pure allow-list for artwork URI sources. */
public final class NowPlayingArtworkSourcePolicy {
 public enum Kind { LOCAL, REMOTE_HTTPS, UNSUPPORTED }
 private NowPlayingArtworkSourcePolicy(){ }
 public static Kind kind(String rawUri){
  if(rawUri==null||rawUri.trim().isEmpty())return Kind.UNSUPPORTED;
  String scheme;
  try{scheme=URI.create(rawUri.trim()).getScheme();}catch(IllegalArgumentException bad){return Kind.UNSUPPORTED;}
  if(scheme==null)return Kind.UNSUPPORTED;
  if("content".equalsIgnoreCase(scheme)||"file".equalsIgnoreCase(scheme)||"android.resource".equalsIgnoreCase(scheme))return Kind.LOCAL;
  if("https".equalsIgnoreCase(scheme))return Kind.REMOTE_HTTPS;
  return Kind.UNSUPPORTED;
 }
}
