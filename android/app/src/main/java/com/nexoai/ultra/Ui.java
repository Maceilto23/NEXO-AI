package com.nexoai.ultra;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.*;
import android.content.Context;

public final class Ui {
    public static int dp(Context c,int v){ return (int)(v*c.getResources().getDisplayMetrics().density+0.5f); }
    public static TextView title(Context c,String s,int size){ TextView t=new TextView(c); t.setText(s); t.setTextColor(Color.rgb(248,250,252)); t.setTextSize(size); t.setTypeface(null,1); return t; }
    public static TextView text(Context c,String s,int size){ TextView t=new TextView(c); t.setText(s); t.setTextColor(Color.rgb(203,213,225)); t.setTextSize(size); return t; }
    public static Button button(Context c,String s){
        Button b=new Button(c); b.setText(s); b.setTextColor(Color.rgb(8,17,31)); b.setAllCaps(false); b.setTextSize(15); b.setTypeface(null,1);
        GradientDrawable g=new GradientDrawable(); g.setColor(Color.rgb(94,234,212)); g.setCornerRadius(dp(c,16)); b.setBackground(g);
        return b;
    }
    public static GradientDrawable card(Context c){ GradientDrawable g=new GradientDrawable(); g.setColor(Color.rgb(16,28,47)); g.setCornerRadius(dp(c,20)); return g; }
    public static void pad(View v,Context c,int p){ int d=dp(c,p); v.setPadding(d,d,d,d); }
}
