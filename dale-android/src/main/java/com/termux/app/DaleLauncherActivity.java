package com.termux.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import java.util.Locale;

public final class DaleLauncherActivity extends Activity {
    private DaleHome home;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(3, 6, 12));
        getWindow().setNavigationBarColor(Color.rgb(3, 6, 12));
        home = new DaleHome();
        setContentView(home);
    }

    private void open(String id) {
        try {
            if ("terminal".equals(id)) {
                Intent i = new Intent(this, TermuxActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } else if ("code".equals(id)) {
                startActivity(new Intent(this, CodeChatActivity.class));
            } else if ("workspace".equals(id)) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(i, 44);
            } else if ("settings".equals(id)) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } else if ("lanie".equals(id)) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:8080")));
            } else {
                new AlertDialog.Builder(this)
                    .setTitle(id.toUpperCase(Locale.US))
                    .setMessage("DALE capability: " + id
                        + "\n\nThe shell is implemented; this capability is not falsely reported as connected to a backend.")
                    .setPositiveButton("OK", null).show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                .setTitle("DALE")
                .setMessage("Could not open " + id + ": " + e.getMessage())
                .setPositiveButton("OK", null).show();
        }
    }

    private final class DaleHome extends View {
        final Paint p = new Paint(3), s = new Paint(3);
        final String[] ids = {"workspace","terminal","code","lanie","rag","voice","vision","git","settings"};
        final String[] names = {"WORKSPACE","TERMINAL","CODE","LANIE","RAG","VOICE","VISION","GIT","SETTINGS"};
        final String[] desc = {"files + projects","shell runtime","agent editor","local AI",
                "retrieval","whisper","camera","repositories","android"};
        float downX, downY, spin = 0;
        int selected = 0;

        DaleHome() {
            super(DaleLauncherActivity.this);
            s.setStyle(Paint.Style.STROKE);
            setFocusable(true);
        }

        @Override protected void onDraw(Canvas c) {
            int w = getWidth(), h = getHeight();
            c.drawColor(Color.rgb(3, 6, 12));

            p.setShader(new LinearGradient(0, 0, w, h,
                    Color.rgb(7, 17, 30), Color.rgb(2, 4, 9), Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);

            s.setColor(Color.argb(34, 99, 230, 255));
            s.setStrokeWidth(1);
            for (int i = -10; i < 12; i++) {
                float x = w / 2f + i * 68 + spin * .15f;
                c.drawLine(x, h * .54f, w / 2f + (x - w / 2f) * 2.7f, h, s);
            }
            for (int i = 0; i < 7; i++) {
                float y = h * .56f + i * i * 13;
                c.drawLine(0, y, w, y, s);
            }

            p.setColor(Color.WHITE);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(22);
            c.drawText("DALE", 24, 42, p);

            p.setColor(Color.rgb(99, 230, 255));
            p.setTextSize(10);
            c.drawText("LOCAL • AGENT • WORKSPACE", 24, 61, p);

            p.setTextAlign(Paint.Align.RIGHT);
            p.setColor(Color.rgb(150, 170, 185));
            p.setTextSize(9);
            c.drawText("DALE CORE / 3D SHELL", w - 20, 40, p);
            p.setTextAlign(Paint.Align.LEFT);

            float cx = w / 2f, cy = h * .40f, r = Math.min(w, h) * .105f;
            p.setShader(new RadialGradient(cx - r * .25f, cy - r * .25f, r,
                    new int[]{Color.rgb(120,245,255), Color.rgb(12,60,78), Color.rgb(3,8,14)},
                    null, Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, r, p);
            p.setShader(null);

            s.setColor(Color.argb(180, 99, 230, 255));
            s.setStrokeWidth(2);
            c.drawCircle(cx, cy, r * 1.3f, s);

            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(Color.WHITE);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(15);
            c.drawText("DALE CORE", cx, cy + 5, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(8);
            p.setColor(Color.rgb(130,215,235));
            c.drawText("CONTROL PLANE", cx, cy + r + 20, p);

            float orbit = Math.min(w, h) * .34f;
            for (int i = 0; i < ids.length; i++) {
                double a = Math.toRadians(i * 40 - 160 + spin * .05f);
                float x = cx + (float)Math.cos(a) * orbit;
                float y = cy + (float)Math.sin(a) * orbit * .72f;
                float depth = (float)Math.cos(a);
                float scale = .70f + .30f * ((depth + 1) / 2f) + (i == selected ? .08f : 0);
                float cw = 112 * scale, ch = 66 * scale;
                RectF rr = new RectF(x-cw/2, y-ch/2, x+cw/2, y+ch/2);

                p.setColor(Color.argb(i == selected ? 235 : 185, 8, 15, 25));
                c.drawRoundRect(rr, 12, 12, p);
                s.setColor(i == selected ? Color.rgb(99,230,255) : Color.argb(85,110,150,170));
                s.setStrokeWidth(i == selected ? 2.2f : 1);
                c.drawRoundRect(rr, 12, 12, s);

                p.setTextAlign(Paint.Align.LEFT);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(10 * scale);
                p.setColor(i == selected ? Color.rgb(99,230,255) : Color.rgb(180,195,208));
                c.drawText(names[i], rr.left + 10 * scale, rr.top + 22 * scale, p);

                p.setTypeface(Typeface.DEFAULT);
                p.setTextSize(7.5f * scale);
                p.setColor(Color.rgb(120,138,153));
                c.drawText(desc[i], rr.left + 10 * scale, rr.top + 39 * scale, p);

                p.setColor(i == selected ? Color.rgb(99,230,255) : Color.rgb(65,90,105));
                c.drawCircle(rr.left + 12 * scale, rr.bottom - 11 * scale, 3 * scale, p);
            }

            p.setColor(Color.argb(225, 4, 7, 13));
            c.drawRect(0, h - 82, w, h, p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(11);
            p.setColor(Color.rgb(99,230,255));
            c.drawText(names[selected], w/2f, h-52, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(9);
            p.setColor(Color.rgb(145,160,175));
            c.drawText(desc[selected] + " • TAP TO OPEN • SWIPE", w/2f, h-33, p);
            p.setTextAlign(Paint.Align.LEFT);
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                downX = e.getX();
                downY = e.getY();
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_UP) {
                float dx = e.getX() - downX;
                if (Math.abs(dx) > 60) {
                    selected = Math.floorMod(selected + (dx > 0 ? -1 : 1), ids.length);
                    spin += dx > 0 ? 40 : -40;
                    invalidate();
                    return true;
                }

                float cx = getWidth()/2f, cy = getHeight()*.40f;
                float orbit = Math.min(getWidth(), getHeight())*.34f;
                int hit = -1;
                float best = Float.MAX_VALUE;
                for (int i = 0; i < ids.length; i++) {
                    double a = Math.toRadians(i*40 - 160 + spin*.05f);
                    float x = cx + (float)Math.cos(a)*orbit;
                    float y = cy + (float)Math.sin(a)*orbit*.72f;
                    float d = (x-e.getX())*(x-e.getX()) + (y-e.getY())*(y-e.getY());
                    if (d < best && d < 20000) {
                        best = d;
                        hit = i;
                    }
                }
                if (hit >= 0) {
                    selected = hit;
                    invalidate();
                    open(ids[selected]);
                } else if (e.getY() > getHeight()-105) {
                    open(ids[selected]);
                }
                return true;
            }
            return true;
        }
    }
}
