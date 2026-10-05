package com.boop.shieldhome;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.boop.shieldoverlay.AreaInfo;
import com.boop.shieldoverlay.RoomPanelSession;

/** Home room selection uses BOOP's saved room and the existing HA credentials. */
final class ShieldRoomPickerDialog {
    interface Rooms {
        AreaInfo selectedRoom();
        void selectRoom(AreaInfo room);
        Runnable loadRooms(com.boop.shieldoverlay.HomeAssistantRepository.AreasCallback callback);
    }
    static AlertDialog show(Context context, RoomPanelSession session) {
        return show(context, new Rooms() {
            public AreaInfo selectedRoom() { return session.selectedRoom(); }
            public void selectRoom(AreaInfo room) { session.selectRoom(room); }
            public Runnable loadRooms(com.boop.shieldoverlay.HomeAssistantRepository.AreasCallback callback) {
                return session.loadRooms(callback);
            }
        });
    }
    static AlertDialog show(Context context, Rooms session) {
        LinearLayout rows = new LinearLayout(context);
        rows.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(16 * context.getResources().getDisplayMetrics().density);
        rows.setPadding(padding, padding, padding, padding);
        ScrollView scroll = new ScrollView(context);
        scroll.addView(rows);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Choose room")
                .setView(scroll).setNegativeButton("Cancel", null).create();
        Runnable[] cancel = { () -> { } };
        Runnable[] load = new Runnable[1];
        load[0] = () -> {
            cancel[0].run();
            rows.removeAllViews();
            TextView loading = label(context, "Loading rooms…", padding);
            rows.addView(loading);
            cancel[0] = session.loadRooms((areas, error) -> {
                if (!dialog.isShowing()) return;
                rows.removeAllViews();
                if (error != null || areas == null || areas.isEmpty()) {
                    rows.addView(label(context, error == null ? "No rooms found." : error, padding));
                    TextView retry = action(context, "Try again", padding);
                    retry.setOnClickListener(v -> load[0].run());
                    rows.addView(retry); retry.requestFocus();
                    return;
                }
                AreaInfo selected = session.selectedRoom();
                TextView focus = null;
                for (AreaInfo area : areas) {
                    TextView item = action(context, area.name(), padding);
                    item.setContentDescription(area.name() + (selected != null && area.id().equals(selected.id()) ? ", current room" : ""));
                    item.setOnClickListener(v -> { session.selectRoom(area); dialog.dismiss(); });
                    rows.addView(item, new LinearLayout.LayoutParams(-1, -2));
                    if (focus == null || selected != null && area.id().equals(selected.id())) focus = item;
                }
                if (focus != null) focus.requestFocus();
            });
        };
        dialog.setOnDismissListener(ignored -> cancel[0].run());
        dialog.show();
        load[0].run();
        return dialog;
    }
    private static TextView label(Context context, String text, int padding) {
        TextView view = new TextView(context);
        view.setText(text); view.setTextSize(20); view.setTextColor(Color.WHITE);
        view.setPadding(padding, padding, padding, padding);
        return view;
    }
    private static TextView action(Context context, String text, int padding) {
        TextView view = label(context, text, padding);
        view.setFocusable(true); view.setClickable(true); view.setDefaultFocusHighlightEnabled(false);
        view.setBackground(FocusChrome.filled(context, Color.rgb(34,34,34), 8, false));
        view.setOnFocusChangeListener((v, focused) -> {
            view.setTextColor(focused ? FocusChrome.accentColor(context) : Color.WHITE);
            view.setBackground(FocusChrome.filled(context, Color.rgb(34,34,34), 8, focused));
        });
        return view;
    }
}
