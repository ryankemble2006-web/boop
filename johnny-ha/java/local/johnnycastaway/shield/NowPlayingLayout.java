package local.johnnycastaway.shield;
/** Pure geometry for keeping the media card inside Johnny's 4:3 picture. */
public final class NowPlayingLayout {
 public static final int CARD_WIDTH_1080=516;
 public static final int CARD_HEIGHT_1080=172;
 public static final int MARGIN_1080=48;
 private NowPlayingLayout(){ }
 public static int[] content43(int width,int height){
  if(width<=0||height<=0)return new int[]{0,0,0,0};
  int contentWidth=Math.min(width,Math.round(height*4f/3f));
  int contentHeight=Math.min(height,Math.round(width*3f/4f));
  int left=(width-contentWidth)/2;
  int top=(height-contentHeight)/2;
  return new int[]{left,top,left+contentWidth,top+contentHeight};
 }
 public static int[] cardRect(int width,int height){
  if(width<=0||height<=0)return new int[]{0,0,0,0};
  float scale=Math.min(width/1920f,height/1080f);
  int cardWidth=Math.round(CARD_WIDTH_1080*scale);
  int cardHeight=Math.round(CARD_HEIGHT_1080*scale);
  int margin=Math.round(MARGIN_1080*scale);
  int[] content=content43(width,height);
  int right=content[2]-margin;
  int top=content[1]+margin;
  return new int[]{right-cardWidth,top,right,top+cardHeight};
 }
}
