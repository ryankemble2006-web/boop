package local.johnnycastaway.shield;
import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.HashMap;
import java.util.Map;
/** User-enabled read-only MediaSession authority plus media-art fallback. */
public final class JohnnyMusicListener extends NotificationListenerService {
 private static final Map<String,Bitmap> ARTWORK=new HashMap<>();
 public static synchronized Bitmap artworkFor(String packageName){return packageName==null?null:ARTWORK.get(packageName);}
 private static synchronized void putArtwork(String packageName,Bitmap bitmap){if(packageName!=null&&bitmap!=null)ARTWORK.put(packageName,bitmap);}
 private static synchronized void removeArtwork(String packageName){if(packageName!=null)ARTWORK.remove(packageName);}
 @Override public void onListenerConnected(){super.onListenerConnected();StatusBarNotification[] active=getActiveNotifications();if(active!=null)for(StatusBarNotification sbn:active)onNotificationPosted(sbn);}
 @Override public void onNotificationPosted(StatusBarNotification sbn){
  if(!isMedia(sbn))return;Bitmap art=artworkFrom(sbn.getNotification());if(art!=null)putArtwork(sbn.getPackageName(),art);
 }
 @Override public void onNotificationRemoved(StatusBarNotification sbn){if(isMedia(sbn))removeArtwork(sbn.getPackageName());}
 private static boolean isMedia(StatusBarNotification sbn){
  if(sbn==null||sbn.getNotification()==null)return false;Notification n=sbn.getNotification();
  return (n.extras!=null&&n.extras.containsKey(Notification.EXTRA_MEDIA_SESSION))||Notification.CATEGORY_TRANSPORT.equals(n.category);
 }
 private Bitmap artworkFrom(Notification n){
  if(n==null)return null;Bitmap bitmap=bitmapFrom(n.getLargeIcon());if(bitmap!=null)return bitmap;
  @SuppressWarnings("deprecation") Bitmap legacy=n.largeIcon;if(legacy!=null)return legacy;
  if(n.extras==null)return null;
  for(String key:new String[]{Notification.EXTRA_LARGE_ICON_BIG,Notification.EXTRA_LARGE_ICON,Notification.EXTRA_PICTURE,Notification.EXTRA_PICTURE_ICON}){
   bitmap=bitmapFrom(n.extras.get(key));if(bitmap!=null)return bitmap;
  }
  return null;
 }
 private Bitmap bitmapFrom(Object value){
  if(value instanceof Bitmap)return (Bitmap)value;
  if(value instanceof Icon){try{return bitmapFrom(((Icon)value).loadDrawable(this));}catch(RuntimeException unavailable){return null;}}
  if(value instanceof Drawable)return bitmapFrom((Drawable)value);
  return null;
 }
 private Bitmap bitmapFrom(Drawable drawable){
  if(drawable==null)return null;
  if(drawable instanceof BitmapDrawable){Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();if(bitmap!=null)return bitmap;}
  int width=drawable.getIntrinsicWidth(),height=drawable.getIntrinsicHeight();
  if(width<=0)width=512;if(height<=0)height=512;width=Math.min(width,1024);height=Math.min(height,1024);
  try{Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);drawable.setBounds(0,0,width,height);drawable.draw(canvas);return bitmap;}
  catch(RuntimeException unavailable){return null;}
 }
}
