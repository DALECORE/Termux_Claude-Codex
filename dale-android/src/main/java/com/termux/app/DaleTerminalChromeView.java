package com.termux.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.termux.terminal.TerminalSession;

/**
 * DALE/MusePool terminal chrome.
 *
 * This is deliberately a surface over the real Termux TerminalView. It provides the
 * reference project's glass-dock interaction model and session controls without replacing
 * the underlying PTY/session engine.
 */
public final class DaleTerminalChromeView extends View {

    private static final int CYAN = Color.rgb(99, 230, 255);
    private static final int TEXT = Color.rgb(220, 232, 240);
    private static final int DIM = Color.rgb(125, 145, 158);
    private static final int BG = Color.rgb(5, 10, 18);

    private final TermuxActivity activity;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF[] buttons = new RectF[6];
    private final String[] labels = {"NEW", "SESS", "NEXT", "PALETTE", "GUI", "HIDE"};
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean pressed;
    private int pressedIndex = -1;
    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            invalidate();
            handler.postDelayed(this, 1000);
        }
    };

    public DaleTerminalChromeView(Context context, TermuxActivity activity) {
        super(context);
        this.activity = activity;
        stroke.setStyle(Paint.Style.STROKE);
        setFocusable(false);
        setClickable(false);
        setWillNotDraw(false);
        handler.post(refresh);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        fill.setShader(new LinearGradient(
            0, 0, 0, h,
            Color.argb(236, 7, 16, 28),
            Color.argb(220, 2, 7, 13),
            Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(0, 0, w, h), dp(18), dp(18), fill);
        fill.setShader(null);

        stroke.setColor(Color.argb(90, 130, 220, 240));
        stroke.setStrokeWidth(dp(1));
        canvas.drawRoundRect(
            new RectF(dp(1), dp(1), w - dp(1), h - dp(1)),
            dp(18), dp(18), stroke);

        // Specular glass strip.
        fill.setShader(new LinearGradient(
            0, 0, w, 0,
            Color.TRANSPARENT,
            Color.argb(70, 99, 230, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, dp(2), fill);
        fill.setShader(null);

        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextSize(dp(10));
        text.setColor(CYAN);
        text.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("MUSEPOOL", dp(14), dp(16), text);

        text.setTypeface(Typeface.DEFAULT);
        text.setTextSize(dp(8));
        text.setColor(DIM);
        canvas.drawText(statusText(), dp(14), dp(30), text);

        float left = dp(116);
        float gap = dp(6);
        float bw = Math.max(dp(46), (w - left - dp(10) - gap * 5) / 6f);
        float top = dp(6);
        float bh = dp(38);

        for (int i = 0; i < 6; i++) {
            float x = left + i * (bw + gap);
            buttons[i] = new RectF(x, top, x + bw, top + bh);

            boolean active = i == pressedIndex;
            fill.setColor(active
                ? Color.argb(100, 99, 230, 255)
                : Color.argb(95, 14, 28, 40));
            canvas.drawRoundRect(buttons[i], dp(10), dp(10), fill);

            stroke.setColor(active
                ? CYAN
                : Color.argb(80, 150, 180, 195));
            stroke.setStrokeWidth(active ? dp(1.6f) : dp(1));
            canvas.drawRoundRect(buttons[i], dp(10), dp(10), stroke);

            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(i == 3 ? 7 : 8));
            text.setColor(active ? Color.WHITE : TEXT);
            canvas.drawText(
                labels[i],
                buttons[i].centerX(),
                buttons[i].centerY() + dp(3),
                text);
        }
    }

    private String statusText() {
        TerminalSession session = activity.getCurrentSession();
        int count = 0;

        if (activity.getTermuxService() != null)
            count = activity.getTermuxService().getTermuxSessionsSize();

        String name = session == null || session.mSessionName == null
            ? "Kali"
            : session.mSessionName;

        return "KALI • " + name + " • " + count + " SESSION" + (count == 1 ? "" : "S");
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressed = true;
            pressedIndex = hit(event.getX(), event.getY());
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            pressed = false;
            pressedIndex = -1;
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int hit = hit(event.getX(), event.getY());
            pressed = false;
            pressedIndex = -1;
            invalidate();

            if (hit >= 0)
                activate(hit);

            return true;
        }

        return true;
    }

    private int hit(float x, float y) {
        for (int i = 0; i < buttons.length; i++) {
            RectF b = buttons[i];
            if (b != null && b.contains(x, y))
                return i;
        }
        return -1;
    }

    private void activate(int index) {
        switch (index) {
            case 0:
                activity.getTermuxTerminalSessionClient().addNewSession(false, null);
                break;

            case 1:
                activity.getDrawer().openDrawer(Gravity.LEFT);
                break;

            case 2:
                activity.getTermuxTerminalSessionClient().switchToSession(true);
                break;

            case 3:
                showPalette();
                break;

            case 4:
                startGui();
                break;

            case 5:
                hideChrome();
                break;

            default:
                break;
        }
    }

    private void showPalette() {
        final String[] actions = {
            "New Kali session",
            "Open session browser",
            "Next session",
            "Start XFCE / VNC",
            "Hide MusePool bar"
        };

        new AlertDialog.Builder(activity)
            .setTitle("MusePool command palette")
            .setItems(actions, (dialog, which) -> {
                switch (which) {
                    case 0:
                        activity.getTermuxTerminalSessionClient().addNewSession(false, null);
                        break;
                    case 1:
                        activity.getDrawer().openDrawer(Gravity.LEFT);
                        break;
                    case 2:
                        activity.getTermuxTerminalSessionClient().switchToSession(true);
                        break;
                    case 3:
                        startGui();
                        break;
                    case 4:
                        hideChrome();
                        break;
                    default:
                        break;
                }
            })
            .show();
    }

    private void startGui() {
        new Thread(() -> {
            String result;
            try {
                Process p = new ProcessBuilder(
                    "su", "-c", "/usr/local/bin/musepool-gui start")
                    .redirectErrorStream(true)
                    .start();

                byte[] data = new byte[8192];
                int n = p.getInputStream().read(data);
                int rc = p.waitFor();

                result = rc == 0
                    ? "GUI started on DISPLAY :1 / VNC 5901"
                    : "GUI controller returned exit " + rc
                        + "\n" + new String(data, 0, Math.max(0, n));
            } catch (Exception e) {
                result = "GUI start failed: " + e.getMessage();
            }

            final String message = result;
            activity.runOnUiThread(() ->
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show());

        }).start();
    }

    private void hideChrome() {
        setVisibility(GONE);
    }

    public static DaleTerminalChromeView attach(
        TermuxActivity activity,
        ViewGroup host) {

        DaleTerminalChromeView view =
            new DaleTerminalChromeView(activity, activity);

        int height = view.dp(50);

        FrameLayout.LayoutParams lp =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                Gravity.BOTTOM);

        lp.leftMargin = view.dp(8);
        lp.rightMargin = view.dp(8);
        lp.bottomMargin = view.dp(8);

        host.addView(view, lp);
        view.bringToFront();

        return view;
    }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(refresh);
        super.onDetachedFromWindow();
    }
}
