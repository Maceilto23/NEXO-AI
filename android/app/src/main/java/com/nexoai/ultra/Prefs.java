package com.nexoai.ultra;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private static final String P = "nexoai_v2";
    private static final String DEFAULT_SITE = "https://nexoai.page.gd";

    public static SharedPreferences sp(Context c){ return c.getSharedPreferences(P, Context.MODE_PRIVATE); }
    public static String site(Context c){ return sp(c).getString("site", DEFAULT_SITE); }
    public static String pin(Context c){ return sp(c).getString("pin", ""); }
    public static String style(Context c){ return sp(c).getString("style", "Natural, inteligente, educado, claro, objetivo e sem parecer texto de IA."); }
}
