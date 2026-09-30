package com.termux.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;

import com.caverock.androidsvg.SVG;

import java.io.IOException;

public final class DaleLauncherActivity extends Activity {
    private static final String SVG_ASSET = "dale/nethunter-original.svg";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(3, 6, 12));
        getWindow().setNavigationBarColor(Color.rgb(3, 6, 12));
        setContentView(new DaleHome());
    }

    private void open(String id) {
        try {
            switch (id) {
                case "terminal":
                case "nethunter":
                    startActivity(new Intent(this, TermuxActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    break;

                case "lanie": {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:8080"));
                    if (i.resolveActivity(getPackageManager()) != null) {
                        startActivity(i);
                    } else {
                        showDialog("LANIE", "No activity can open http://127.0.0.1:8080.");
                    }
                    break;
                }

                case "workspace":
                    startActivityForResult(
                        new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
                        44);
                    break;

                case "settings":
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                    break;

                case "code":
                    openOptional("com.termux.app.CodeChatActivity", "Code workspace is not included in this PickleHik3 source.");
                    break;

                default:
                    showDialog("DALE", "Unknown capability: " + id);
                    break;
            }
        } catch (Exception e) {
            showDialog("DALE", "Could not open " + id + ": " + e.getMessage());
        }
    }

    private void openOptional(String className, String missingMessage) {
        try {
            Class<?> type = Class.forName(className);
            Intent intent = new Intent(this, type);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            showDialog("DALE", missingMessage);
        }
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private final class DaleHome extends View {
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String[] ids = {
            "terminal", "nethunter", "lanie", "workspace", "code", "settings"
        };
        private final String[] names = {
            "TERMINAL", "NETHUNTER", "LANIE", "WORKSPACE", "CODE", "SETTINGS"
        };
        private final String[] desc = {
            "PickleHik3 PTY / panes",
            "original NetHunter design",
            "local AI",
            "files + projects",
            "agent workspace",
            "Android"
        };
        private final RectF[] cards = new RectF[ids.length];
        private final SVG netHunterSvg;
        private int selected = 0;
        private float downX;
        private float downY;

        DaleHome() {
            super(DaleLauncherActivity.this);
            stroke.setStyle(Paint.Style.STROKE);
            setFocusable(true);
            setContentDescription("DALE launcher");

            SVG loaded = null;
            try {
                loaded = SVG.getFromAsset(getAssets(), SVG_ASSET);
            } catch (SVG.SVGParseException | IOException ignored) {
                // The launcher still works without the optional artwork.
            }
            netHunterSvg = loaded;
        }

        @Override
        protected void onDraw(Canvas c) {
            final int w = getWidth();
            final int h = getHeight();

            c.drawColor(Color.rgb(3, 6, 12));

            fill.setShader(new LinearGradient(
                0, 0, w, h,
                Color.rgb(7, 17, 30),
                Color.rgb(2, 4, 9),
                Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, fill);
            fill.setShader(null);

            // Subtle perspective grid.
            stroke.setColor(Color.argb(30, 99, 230, 255));
            stroke.setStrokeWidth(1f);
            for (int i = -8; i <= 8; i++) {
                float x = w / 2f + i * (w / 10f);
                c.drawLine(x, h * .46f, w / 2f + (x - w / 2f) * 2.3f, h, stroke);
            }
            for (int i = 0; i < 6; i++) {
                float y = h * .56f + i * i * 14f;
                c.drawLine(0, y, w, y, stroke);
            }

            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(dp(22));
            text.setColor(Color.WHITE);
            c.drawText("DALE", dp(20), dp(38), text);

            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(9));
            text.setColor(Color.rgb(99, 230, 255));
            c.drawText("LOCAL • AGENT • WORKSPACE", dp(20), dp(55), text);

            float cx = w / 2f;
            float cy = dp(108);
            float r = Math.min(w, h) * .085f;

            fill.setShader(new android.graphics.RadialGradient(
                cx - r * .25f, cy - r * .25f, r,
                new int[]{
                    Color.rgb(120, 245, 255),
                    Color.rgb(12, 60, 78),
                    Color.rgb(3, 8, 14)
                },
                null,
                Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, r, fill);
            fill.setShader(null);

            stroke.setColor(Color.argb(190, 99, 230, 255));
            stroke.setStrokeWidth(dp(2));
            c.drawCircle(cx, cy, r * 1.30f, stroke);

            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(dp(13));
            text.setColor(Color.WHITE);
            c.drawText("DALE CORE", cx, cy + dp(4), text);

            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(7));
            text.setColor(Color.rgb(130, 215, 235));
            c.drawText("CONTROL PLANE", cx, cy + r + dp(19), text);

            float top = dp(182);
            float gap = dp(10);
            float margin = dp(16);
            float cardW = (w - margin * 2f - gap) / 2f;
            float cardH = Math.max(dp(82), Math.min(dp(102), (h - top - dp(100) - gap * 2f) / 3f));

            for (int i = 0; i < cards.length; i++) {
                int col = i % 2;
                int row = i / 2;
                float left = margin + col * (cardW + gap);
                float y = top + row * (cardH + gap);
                cards[i] = new RectF(left, y, left + cardW, y + cardH);

                boolean active = i == selected;
                fill.setColor(Color.argb(active ? 235 : 185, 8, 15, 25));
                c.drawRoundRect(cards[i], dp(14), dp(14), fill);

                stroke.setColor(active
                    ? Color.rgb(99, 230, 255)
                    : Color.argb(88, 130, 160, 175));
                stroke.setStrokeWidth(active ? dp(2) : dp(1));
                c.drawRoundRect(cards[i], dp(14), dp(14), stroke);

                text.setTextAlign(Paint.Align.LEFT);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                text.setTextSize(dp(11));
                text.setColor(active
                    ? Color.rgb(99, 230, 255)
                    : Color.rgb(205, 215, 225));
                c.drawText(names[i], cards[i].left + dp(12), cards[i].top + dp(25), text);

                text.setTypeface(Typeface.DEFAULT);
                text.setTextSize(dp(8));
                text.setColor(Color.rgb(132, 150, 164));
                c.drawText(desc[i], cards[i].left + dp(12), cards[i].top + dp(45), text);

                fill.setColor(active
                    ? Color.rgb(99, 230, 255)
                    : Color.rgb(65, 90, 105));
                c.drawCircle(
                    cards[i].left + dp(13),
                    cards[i].bottom - dp(13),
                    dp(3),
                    fill);

                if ("nethunter".equals(ids[i])) {
                    drawNetHunter(c, cards[i]);
                }
            }

            fill.setColor(Color.argb(225, 4, 7, 13));
            c.drawRect(0, h - dp(62), w, h, fill);

            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(dp(10));
            text.setColor(Color.rgb(99, 230, 255));
            c.drawText(names[selected], w / 2f, h - dp(36), text);

            text.setTypeface(Typeface.DEFAULT);
            text.setTextSize(dp(8));
            text.setColor(Color.rgb(145, 160, 175));
            c.drawText("TAP TO OPEN • SWIPE", w / 2f, h - dp(18), text);
        }

        private void drawNetHunter(Canvas c, RectF card) {
            float maxH = card.height() - dp(16);
            float maxW = dp(34);
            float aspect = 290f / 620f;
            float hh = Math.min(maxH, maxW / aspect);
            float ww = hh * aspect;
            RectF viewport = new RectF(
                card.right - ww - dp(10),
                card.top + (card.height() - hh) / 2f,
                card.right - dp(10),
                card.top + (card.height() - hh) / 2f + hh);

            fill.setColor(Color.argb(245, 240, 243, 246));
            c.drawRoundRect(
                new RectF(viewport.left - dp(4), viewport.top - dp(3),
                    viewport.right + dp(4), viewport.bottom + dp(3)),
                dp(5), dp(5), fill);

            if (netHunterSvg != null) {
                netHunterSvg.renderToCanvas(c, viewport);
            } else {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                text.setTextSize(dp(9));
                text.setColor(Color.DKGRAY);
                c.drawText("NH", viewport.centerX(), viewport.centerY() + dp(3), text);
            }
        }

        private int dp(float v) {
            return Math.round(v * getResources().getDisplayMetrics().density);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    return true;

                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - downX;
                    if (Math.abs(dx) > dp(48)) {
                        selected = Math.floorMod(
                            selected + (dx > 0 ? -1 : 1),
                            ids.length);
                        invalidate();
                        return true;
                    }

                    int hit = -1;
                    for (int i = 0; i < cards.length; i++) {
                        if (cards[i] != null && cards[i].contains(event.getX(), event.getY())) {
                            hit = i;
                            break;
                        }
                    }

                    if (hit >= 0) {
                        selected = hit;
                        invalidate();
                        open(ids[hit]);
                    } else if (event.getY() > getHeight() - dp(74)) {
                        open(ids[selected]);
                    }
                    return true;

                default:
                    return true;
            }
        }
    }
}
