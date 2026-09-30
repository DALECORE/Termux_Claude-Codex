package com.termux.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/** Lightweight launcher surface hosted inside TermuxActivity. */
public final class DaleLauncherView extends View {
    public interface Listener {
        void onTerminal();
        void onSettings();
    }

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private final RectF terminal = new RectF();
    private final RectF settings = new RectF();

    public DaleLauncherView(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        stroke.setStyle(Paint.Style.STROKE);
        setFocusable(true);
        setContentDescription("DALE launcher");
    }

    @Override protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        canvas.drawColor(Color.rgb(3, 6, 12));
        fill.setShader(new android.graphics.LinearGradient(0, 0, w, h,
            Color.rgb(7, 17, 30), Color.rgb(2, 4, 9),
            android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, fill);
        fill.setShader(null);

        fill.setColor(Color.WHITE);
        fill.setTypeface(Typeface.DEFAULT_BOLD);
        fill.setTextSize(24);
        canvas.drawText("DALE", 24, 48, fill);
        fill.setColor(Color.rgb(99, 230, 255));
        fill.setTypeface(Typeface.DEFAULT);
        fill.setTextSize(10);
        canvas.drawText("LOCAL • AGENT • WORKSPACE", 24, 68, fill);

        float cx = w / 2f;
        float cy = h * .34f;
        float radius = Math.min(w, h) * .12f;
        fill.setShader(new android.graphics.RadialGradient(cx - radius * .25f,
            cy - radius * .25f, radius,
            new int[]{Color.rgb(120, 245, 255), Color.rgb(12, 60, 78), Color.rgb(3, 8, 14)},
            null, android.graphics.Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, radius, fill);
        fill.setShader(null);
        stroke.setColor(Color.rgb(99, 230, 255));
        stroke.setStrokeWidth(2f);
        canvas.drawCircle(cx, cy, radius * 1.3f, stroke);
        fill.setTextAlign(Paint.Align.CENTER);
        fill.setTypeface(Typeface.DEFAULT_BOLD);
        fill.setTextSize(15);
        fill.setColor(Color.WHITE);
        canvas.drawText("DALE CORE", cx, cy + 5, fill);
        fill.setTextAlign(Paint.Align.LEFT);

        terminal.set(w * .12f, h * .58f, w * .88f, h * .70f);
        settings.set(w * .12f, h * .74f, w * .88f, h * .86f);
        drawCard(canvas, terminal, "TERMINAL", "open the existing Termux session", true);
        drawCard(canvas, settings, "SETTINGS", "configure Termux and DALE", false);

        fill.setTextAlign(Paint.Align.CENTER);
        fill.setTypeface(Typeface.DEFAULT);
        fill.setTextSize(9);
        fill.setColor(Color.rgb(145, 160, 175));
        canvas.drawText("TAP TERMINAL TO CONTINUE", w / 2f, h - 28, fill);
        fill.setTextAlign(Paint.Align.LEFT);
    }

    private void drawCard(Canvas canvas, RectF rect, String title, String subtitle, boolean active) {
        fill.setColor(Color.argb(active ? 235 : 190, 8, 15, 25));
        canvas.drawRoundRect(rect, 16, 16, fill);
        stroke.setColor(active ? Color.rgb(99, 230, 255) : Color.argb(100, 140, 160, 180));
        stroke.setStrokeWidth(active ? 2.2f : 1f);
        canvas.drawRoundRect(rect, 16, 16, stroke);
        fill.setColor(active ? Color.rgb(99, 230, 255) : Color.rgb(190, 200, 210));
        fill.setTypeface(Typeface.DEFAULT_BOLD);
        fill.setTextSize(13);
        canvas.drawText(title, rect.left + 18, rect.top +  thirty(), fill);
        fill.setTypeface(Typeface.DEFAULT);
        fill.setTextSize(9);
        fill.setColor(Color.rgb(135, 150, 165));
        canvas.drawText(subtitle, rect.left + 18, rect.top + 58, fill);
    }

    private float thirty() { return 30f; }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        if (terminal.contains(event.getX(), event.getY())) {
            listener.onTerminal();
        } else if (settings.contains(event.getX(), event.getY())) {
            listener.onSettings();
        }
        return true;
    }

    public static void attach(ViewGroup host, Listener listener) {
        DaleLauncherView view = new DaleLauncherView(host.getContext(), listener);
        host.addView(view, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
