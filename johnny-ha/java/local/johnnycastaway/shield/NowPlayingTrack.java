package local.johnnycastaway.shield;
public final class NowPlayingTrack {
 public final String packageName,title,artist,album;
 public NowPlayingTrack(String packageName,String title,String artist,String album){
  this.packageName=clean(packageName);this.title=clean(title);this.artist=clean(artist);this.album=clean(album);
 }
 public boolean complete(){return !title.isEmpty()&&!artist.isEmpty()&&!album.isEmpty();}
 public boolean sameAs(NowPlayingTrack other){
  return other!=null&&packageName.equals(other.packageName)&&title.equals(other.title)&&artist.equals(other.artist)&&album.equals(other.album);
 }
 private static String clean(String value){return value==null?"":value.trim();}
}