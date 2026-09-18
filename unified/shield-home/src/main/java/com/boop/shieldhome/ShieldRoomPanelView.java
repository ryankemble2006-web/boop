package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boop.shieldoverlay.EntityCard;
import com.boop.shieldoverlay.RoomPanelController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Native BOOP charcoal panel. Entity identity, focus and scroll survive live state changes. */
public final class ShieldRoomPanelView extends LinearLayout {
    public interface Toggle { void onToggle(long generation, String entityId); }
    private final TextView title, status, empty, notice;
    private final HorizontalScrollView scroller;
    private final LinearLayout row;
    private final Map<String, Tile> tiles = new LinkedHashMap<>();
    private final ShieldHomeStore orderStore;
    private RoomPanelController.State state;
    private String focusedId;
    private Runnable exitUp;
    private Toggle toggle;
    private FavouriteGrabSession grabSession;
    private Tile grabbedTile;

    public ShieldRoomPanelView(Context context) {
        super(context);
        orderStore = new ShieldHomeStore(context);
        setOrientation(VERTICAL);
        setPadding(dp(18), dp(14), dp(18), dp(14));
        setFocusable(false);
        GradientDrawable background = FocusChrome.filled(context, Color.rgb(16,16,16), 14, false);
        background.setStroke(dp(1), Color.rgb(48,48,48));
        setBackground(background);
        LinearLayout heading = new LinearLayout(context);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        title = text("Home", 22, Color.WHITE);
        title.setSingleLine(true); title.setEllipsize(TextUtils.TruncateAt.END);
        heading.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        status = text("", 13, Color.LTGRAY);
        status.setPadding(dp(12), 0, 0, 0);
        status.setSingleLine(true);
        heading.addView(status, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        LayoutParams header = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        header.bottomMargin = dp(10);
        addView(heading, header);
        empty = text("", 16, Color.LTGRAY);
        empty.setMaxLines(3);
        addView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        scroller = new HorizontalScrollView(context);
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.setFocusable(false);
        scroller.setFillViewport(true);
        scroller.setClipChildren(false);
        scroller.setClipToPadding(false);
        scroller.setPadding(dp(3), dp(3), dp(3), dp(3));
        row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setClipChildren(false);
        row.setClipToPadding(false);
        scroller.addView(row, new HorizontalScrollView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        addView(scroller, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        notice = text("", 13, Color.LTGRAY);
        notice.setMaxLines(2);
        addView(notice, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        scroller.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> {
            if (r-l != or-ol) resizeTiles();
        });
    }

    public void setActions(Toggle toggle, Runnable exitUp) { this.toggle = toggle; this.exitUp = exitUp; }

    public void bind(RoomPanelController.State next) {
        boolean heldFocus = hasFocus();
        int oldIndex = row.indexOfChild(row.findFocus());
        String previousRoom = state == null || state.room == null ? null : state.room.id();
        String nextRoom = next == null || next.room == null ? null : next.room.id();
        boolean roomChanged = !java.util.Objects.equals(previousRoom, nextRoom);
        if (roomChanged) cancelGrab();
        state = next;
        title.setText(nextRoom == null ? "Home" : next.room.name());

        List<EntityCard> cards = next == null ? List.of() : next.cards;
        List<EntityCard> roomCards = new ArrayList<>();
        List<String> availableIds = new ArrayList<>();
        for (EntityCard card : cards) {
            if (!inRoom(card)) continue;
            roomCards.add(card);
            availableIds.add(card.entityId());
        }

        List<String> orderedIds = grabSession == null
                ? orderStore.loadRoomControlOrder(nextRoom, availableIds)
                : ShieldHomeStore.reconcileOrder(grabSession.current(), availableIds);
        Map<String, EntityCard> cardsById = new LinkedHashMap<>();
        for (EntityCard card : roomCards) cardsById.put(card.entityId(), card);
        List<EntityCard> orderedCards = new ArrayList<>();
        for (String id : orderedIds) {
            EntityCard card = cardsById.get(id);
            if (card != null) orderedCards.add(card);
        }

        boolean rebuild = roomChanged || !orderedIds.equals(new ArrayList<>(tiles.keySet()));
        if (roomChanged) { tiles.clear(); focusedId = null; }
        if (rebuild) row.removeAllViews();
        Map<String, Tile> updated = new LinkedHashMap<>();
        for (EntityCard card : orderedCards) {
            Tile tile = tiles.get(card.entityId());
            if (tile == null) tile = new Tile(card.entityId());
            tile.bind(card);
            updated.put(card.entityId(), tile);
            if (rebuild) row.addView(tile, new LayoutParams(dp(190), LayoutParams.MATCH_PARENT));
        }
        tiles.clear(); tiles.putAll(updated);
        if (grabSession != null) {
            grabbedTile = tiles.get(grabSession.grabbedComponent());
            if (grabbedTile == null) grabSession = null;
            else grabbedTile.setGrabbed(true);
        }
        boolean hasCards = !tiles.isEmpty();
        empty.setVisibility(hasCards ? GONE : VISIBLE);
        scroller.setVisibility(hasCards ? VISIBLE : GONE);
        String phase = next == null ? "" : next.phase.name();
        boolean live = "LIVE".equals(phase);
        status.setText(live ? tiles.size() + (tiles.size()==1 ? " device" : " devices")
                : "CONNECTING".equals(phase) ? "Connecting" : "NO_ROOM".equals(phase) ? "Room not set"
                : "AUTH_REQUIRED".equals(phase) ? "Reconnect" : "OFFLINE".equals(phase) ? "Offline" : "");
        status.setTextColor(live ? FocusChrome.accentColor(getContext()) : Color.LTGRAY);
        String message = next == null ? "Connecting to Home Assistant…"
                : next.room == null ? "Use ‘Set this device room’ in BOOP device and room settings."
                : next.message != null ? next.message
                : next.phase == RoomPanelController.Phase.CONNECTING ? "Connecting to Home Assistant…"
                : "No supported lights, switches or fans are assigned to this room yet.";
        empty.setText(message);
        notice.setText(next == null || next.message == null ? "" : next.message);
        notice.setVisibility(hasCards && next.message != null ? VISIBLE : GONE);
        if (rebuild) {
            resizeTiles();
            if (roomChanged) scroller.scrollTo(0, 0);
            if (heldFocus) {
                Tile previous = tiles.get(focusedId);
                if (previous != null) previous.requestFocus();
                else if (row.getChildCount() > 0) row.getChildAt(Math.max(0,Math.min(oldIndex,row.getChildCount()-1))).requestFocus();
                else if (exitUp != null) exitUp.run();
            }
        }
    }

    private void beginGrab(Tile tile) {
        if (tile == null || state == null || state.room == null || !tiles.containsKey(tile.entityId)) return;
        if (grabbedTile != null && grabbedTile != tile) grabbedTile.setGrabbed(false);
        grabSession = FavouriteGrabSession.begin(new ArrayList<>(tiles.keySet()), tile.entityId);
        grabbedTile = tile;
        focusedId = tile.entityId;
        tile.setGrabbed(true);
        tile.requestFocus();
    }

    private void moveGrab(int delta) {
        if (grabSession == null || grabbedTile == null) return;
        if (grabSession.move(delta)) reorderTiles(grabSession.current());
    }

    private void commitGrab() {
        if (grabSession == null) return;
        String roomId = state == null || state.room == null ? null : state.room.id();
        orderStore.saveRoomControlOrder(roomId, grabSession.commit());
        if (grabbedTile != null) grabbedTile.setGrabbed(false);
        grabSession = null;
        grabbedTile = null;
    }

    private void cancelGrab() {
        if (grabbedTile != null) grabbedTile.setGrabbed(false);
        grabSession = null;
        grabbedTile = null;
    }

    private void reorderTiles(List<String> preferred) {
        List<String> order = ShieldHomeStore.reconcileOrder(preferred, new ArrayList<>(tiles.keySet()));
        Map<String, Tile> existing = new LinkedHashMap<>(tiles);
        row.removeAllViews();
        tiles.clear();
        for (String id : order) {
            Tile tile = existing.get(id);
            if (tile == null) continue;
            tiles.put(id, tile);
            row.addView(tile, new LayoutParams(dp(190), LayoutParams.MATCH_PARENT));
        }
        resizeTiles();
        if (grabbedTile != null) {
            grabbedTile.setGrabbed(true);
            grabbedTile.requestFocus();
            grabbedTile.post(() -> {
                if (grabbedTile != null && grabbedTile.hasFocus())
                    grabbedTile.requestRectangleOnScreen(
                            new Rect(0, 0, grabbedTile.getWidth(), grabbedTile.getHeight()), true);
            });
        }
    }

    private boolean inRoom(EntityCard card) {
        return card != null && state != null && state.room != null && state.room.id().equals(card.areaId());
    }
    public boolean focusControls() {
        if (getVisibility()!=VISIBLE || tiles.isEmpty()) return false;
        Tile previous = tiles.get(focusedId);
        return (previous == null ? row.getChildAt(0) : previous).requestFocus();
    }
    private void resizeTiles() {
        int width = scroller.getWidth() - scroller.getPaddingLeft() - scroller.getPaddingRight();
        if (width <= 0) return;
        int size = RoomPanelLayout.tileWidth(width, row.getChildCount(), dp(12), dp(150));
        for (int i=0; i<row.getChildCount(); i++) {
            View tile = row.getChildAt(i);
            LayoutParams p=(LayoutParams)tile.getLayoutParams();
            int gap=i+1==row.getChildCount()?0:dp(12);
            if (p.width!=size || p.rightMargin!=gap) { p.width=size; p.rightMargin=gap; tile.setLayoutParams(p); }
        }
        View focus=row.findFocus();
        if (focus!=null) focus.post(() -> {
            if (focus.hasFocus()) focus.requestRectangleOnScreen(new Rect(0,0,focus.getWidth(),focus.getHeight()),true);
        });
    }

    private final class Tile extends LinearLayout {
        final String entityId;
        final TextView name, value;
        final DeviceIcon icon;
        boolean grabbed;
        Tile(String entityId) {
            super(ShieldRoomPanelView.this.getContext());
            this.entityId=entityId;
            setId(View.generateViewId());
            setFocusable(true); setClickable(true); setDefaultFocusHighlightEnabled(false);
            setOrientation(HORIZONTAL); setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(14),dp(8),dp(14),dp(8));
            setBackground(chrome(false));
            icon=new DeviceIcon(getContext());
            LayoutParams art=new LayoutParams(dp(28),dp(28)); art.rightMargin=dp(12);
            addView(icon,art);
            LinearLayout labels=new LinearLayout(getContext()); labels.setOrientation(VERTICAL);
            name=text("",17,Color.WHITE); name.setSingleLine(true); name.setEllipsize(TextUtils.TruncateAt.END);
            value=text("",14,Color.LTGRAY); value.setSingleLine(true);
            labels.addView(name); labels.addView(value);
            addView(labels,new LayoutParams(0,LayoutParams.WRAP_CONTENT,1f));
            setOnFocusChangeListener((v,focused) -> {
                if (focused) focusedId=entityId;
                refreshEmphasis();
            });
            setOnLongClickListener(v -> {
                beginGrab(this);
                return true;
            });
            setOnClickListener(v -> {
                if (grabSession != null && grabbedTile == this) {
                    commitGrab();
                    return;
                }
                if (toggle==null || state==null || !state.actionable()) return;
                for (EntityCard card:state.cards) if (entityId.equals(card.entityId()) && inRoom(card)
                        && ("on".equals(card.state())||"off".equals(card.state()))) {
                    toggle.onToggle(state.generation,entityId); return;
                }
            });
            setOnKeyListener((v,key,event) -> {
                if (grabSession != null && grabbedTile == this) {
                    if (key==KeyEvent.KEYCODE_DPAD_LEFT || key==KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (event.getAction()==KeyEvent.ACTION_DOWN)
                            moveGrab(key==KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1);
                        return true;
                    }
                    if (key==KeyEvent.KEYCODE_DPAD_CENTER || key==KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) commitGrab();
                        return true;
                    }
                    if (key==KeyEvent.KEYCODE_DPAD_UP || key==KeyEvent.KEYCODE_DPAD_DOWN) return true;
                }
                if (key==KeyEvent.KEYCODE_DPAD_LEFT || key==KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (event.getAction()==KeyEvent.ACTION_DOWN) {
                        int i=row.indexOfChild(v)+(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);
                        if(i>=0 && i<row.getChildCount()) row.getChildAt(i).requestFocus();
                    }
                    return true;
                }
                if(key==KeyEvent.KEYCODE_DPAD_UP || key==KeyEvent.KEYCODE_DPAD_DOWN) {
                    if(key==KeyEvent.KEYCODE_DPAD_UP && event.getAction()==KeyEvent.ACTION_DOWN && exitUp!=null) exitUp.run();
                    return true;
                }
                return false;
            });
        }
        void setGrabbed(boolean value) {
            grabbed = value;
            refreshEmphasis();
        }
        void refreshEmphasis() {
            boolean focused = hasFocus();
            setBackground(chrome(focused));
            name.setSelected(focused);
            animate().cancel();
            float scale = grabbed ? 1.10f : 1f;
            animate().scaleX(scale).scaleY(scale)
                    .translationZ(grabbed ? dp(10) : 0f)
                    .setDuration(TvAppCardView.FOCUS_DURATION_MS).start();
        }
        void bind(EntityCard card) {
            name.setText(card.displayName());
            boolean live=state.phase==RoomPanelController.Phase.LIVE;
            boolean available="on".equals(card.state())||"off".equals(card.state());
            String label=!live?"Offline":state.pending(entityId)?"Updating…":!available?"Unavailable":"on".equals(card.state())?"On":"Off";
            value.setText(label);
            int accent=FocusChrome.accentColor(getContext());
            value.setTextColor("On".equals(label)?accent:Color.LTGRAY);
            icon.kind=semanticIcon(card); icon.color=live&&available?accent:Color.GRAY; icon.invalidate();
            setContentDescription(card.displayName()+", "+label);
            // Focus remains available while pending or offline. Only the action is gated.
            setAlpha(live&&available?1f:0.60f);
        }
        GradientDrawable chrome(boolean focused) { return FocusChrome.filled(getContext(),Color.rgb(42,42,42),10,focused); }
    }

    private static String semanticIcon(EntityCard card) {
        String name = ((card.displayName() == null ? "" : card.displayName()) + " "
                + (card.deviceName() == null ? "" : card.deviceName()) + " "
                + card.entityId()).toLowerCase(java.util.Locale.ROOT);
        if (name.contains("subwoofer") || name.matches(".*\\bsub\\b.*")
                || name.contains("sub_") || name.contains("_sub")) return "subwoofer";
        if (name.matches(".*\\bfan\\b.*") || name.contains("_fan") || name.contains("fan_")) return "fan";
        return card.domain();
    }

    /** Small vector symbols avoid missing emoji fonts and never animate independently of BOOP. */
    private static final class DeviceIcon extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        String kind="switch"; int color;
        DeviceIcon(Context c) { super(c); setFocusable(false); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save(); canvas.scale(getWidth()/28f,getHeight()/28f);
            paint.setColor(color); paint.setStrokeWidth(2f); paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND);
            if ("light".equals(kind)) {
                canvas.drawArc(new RectF(7,3,21,18),145,250,false,paint);
                canvas.drawLine(8.3f,14.5f,11,21,paint); canvas.drawLine(19.7f,14.5f,17,21,paint);
                canvas.drawLine(11,21,17,21,paint); canvas.drawLine(12,25,16,25,paint);
            } else if ("fan".equals(kind)) {
                canvas.drawCircle(14,14,2,paint);
                for(int i=0;i<3;i++) {
                    canvas.save();canvas.rotate(i*120,14,14);
                    canvas.drawOval(new RectF(11,2,18,10),paint);canvas.restore();
                }
            } else if ("subwoofer".equals(kind)) {
                canvas.drawRoundRect(new RectF(5,2,23,26),2.5f,2.5f,paint);
                canvas.drawCircle(14,9,2.4f,paint);
                canvas.drawCircle(14,18.5f,5.2f,paint);
                canvas.drawCircle(14,18.5f,1.2f,paint);
            } else {
                canvas.drawArc(new RectF(5,5,23,24),-55,290,false,paint);
                canvas.drawLine(14,2,14,13,paint);
            }
            canvas.restore();
        }
    }
    private TextView text(String label,int size,int color) {
        TextView v=new TextView(getContext());v.setText(label);v.setTextColor(color);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);v.setGravity(Gravity.CENTER_VERTICAL);return v;
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
}
