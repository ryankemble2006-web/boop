package com.boop.shieldhome;

/** Resolution-independent layout arithmetic, measured in the caller's pixel coordinates. */
public final class RoomPanelLayout {
    private RoomPanelLayout() { }
    public static final class Bounds {
        public final int top, width, height, right;
        Bounds(int top, int width, int height, int right) {
            this.top = top; this.width = width; this.height = height; this.right = right;
        }
    }
    public static Bounds calculate(int stageWidth, int stageHeight, int contentBottom,
            int gap, int reservation, int minimumHeight) {
        int width = Math.max(0, stageWidth), height = Math.max(0, stageHeight);
        int top = (int) Math.min(height, Math.max(0L, (long) contentBottom + Math.max(0, gap)));
        int right = Math.min(width, Math.max(0, reservation));
        int available = height - top;
        return new Bounds(top, width - right,
                available >= minimumHeight && width > right ? available : 0, right);
    }
    public static int tileWidth(int viewport, int count, int gap, int minimumWidth) {
        if (viewport <= 0 || count <= 0) return 0;
        int visible = Math.min(4, count);
        int width = Math.max(1, (viewport - Math.max(0, gap) * (visible - 1)) / visible);
        return count == 1 ? viewport : Math.max(Math.max(1, minimumWidth), width);
    }
}
