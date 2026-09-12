package com.nexoai.ultra;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private static final String P="nexoai";
    public static SharedPreferences sp(Context c){ return c.getSharedPreferences(P, Context.MODE_PRIVATE); }
    public static String backend(Context c){ return sp(c).getString("backend", "https://nexoai-api.onrender.com/api.php"); }
    public static String token(Context c){ return sp(c).getString("token", "NexoAI_Maceilto_2026_X9K7P4"); }
    public static String style(Context c){ return sp(c).getString("style", "Natural, inteligente, educado, claro, objetivo e sem parecer texto de IA."); }
}
