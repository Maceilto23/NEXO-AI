package com.nexoai.ultra;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    EditText site,pin,style; TextView status; BrowserBridge bridge;

    @Override public void onCreate(Bundle b){ super.onCreate(b); render(); }

    private void render(){
        ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(7,17,31));
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); Ui.pad(root,this,20); sv.addView(root);

        LinearLayout hero=new LinearLayout(this); hero.setGravity(Gravity.CENTER_VERTICAL); hero.setOrientation(LinearLayout.HORIZONTAL); hero.setBackground(Ui.card(this)); Ui.pad(hero,this,16);
        ImageView icon=new ImageView(this); icon.setImageResource(R.drawable.nexo_ai_avatar); hero.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,58)));
        LinearLayout heroText=new LinearLayout(this); heroText.setOrientation(LinearLayout.VERTICAL); heroText.setPadding(Ui.dp(this,14),0,0,0);
        heroText.addView(Ui.title(this,"Nexo AI",25)); heroText.addView(Ui.text(this,"Copiloto inteligente para responder e reescrever sem sair do fluxo.",14));
        hero.addView(heroText,new LinearLayout.LayoutParams(0,-2,1)); root.addView(hero);

        TextView guide=Ui.text(this,"1. Configure o endereço do site e o PIN.\n2. Teste a conexão.\n3. Ative a assistente flutuante.\n4. Selecione um texto e use Compartilhar → Nexo AI.",14);
        guide.setBackground(Ui.card(this)); Ui.pad(guide,this,16); LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,-2); gp.setMargins(0,14,0,14); root.addView(guide,gp);

        Button overlay=Ui.button(this,"Ativar assistente flutuante"); overlay.setOnClickListener(v->toggleOverlay()); root.addView(overlay,buttonParams());

        root.addView(Ui.title(this,"Configuração",19));
        site=field("Site Nexo AI",Prefs.site(this),false); pin=field("PIN privado",Prefs.pin(this),true); style=field("Meu estilo padrão",Prefs.style(this),false);
        root.addView(site); root.addView(pin); root.addView(style);

        Button save=Ui.button(this,"Salvar configurações"); save.setOnClickListener(v->save()); root.addView(save,buttonParams());
        Button test=Ui.button(this,"Testar conexão"); test.setOnClickListener(v->test()); root.addView(test,buttonParams());
        status=Ui.text(this,"Servidor ainda não testado.",13); status.setPadding(0,4,0,14); root.addView(status);

        TextView security=Ui.text(this,"A chave OpenAI não fica no APK nem no GitHub. Ela permanece apenas no config.local.php do InfinityFree.",13); security.setBackground(Ui.card(this)); Ui.pad(security,this,14); root.addView(security);

        bridge=new BrowserBridge(this); WebViewHolder.attachHidden(root,bridge.view());
        setContentView(sv);
    }

    private LinearLayout.LayoutParams buttonParams(){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,Ui.dp(this,54)); p.setMargins(0,12,0,6); return p; }

    private EditText field(String hint,String value,boolean password){
        EditText e=new EditText(this); e.setHint(hint); e.setText(value); e.setTextColor(Color.WHITE); e.setHintTextColor(Color.rgb(120,139,163)); e.setTextSize(14); if(password)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        GradientDrawable g=new GradientDrawable(); g.setColor(Color.rgb(14,27,45)); g.setStroke(Ui.dp(this,1),Color.rgb(50,70,96)); g.setCornerRadius(Ui.dp(this,16)); e.setBackground(g); Ui.pad(e,this,13);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,8,0,0); e.setLayoutParams(p); return e;
    }

    private boolean save(){
        String s=site.getText().toString().trim(); String p=pin.getText().toString().trim(); String st=style.getText().toString().trim();
        if(!s.startsWith("https://")){ Toast.makeText(this,"Use o endereço HTTPS do seu site.",Toast.LENGTH_LONG).show(); return false; }
        if(p.length()<6){ Toast.makeText(this,"Informe o mesmo PIN configurado no InfinityFree.",Toast.LENGTH_LONG).show(); return false; }
        Prefs.sp(this).edit().putString("site",s).putString("pin",p).putString("style",st).apply(); Toast.makeText(this,"Configurações salvas",Toast.LENGTH_SHORT).show(); return true;
    }

    private void test(){
        if(!save()) return; status.setText("Testando conexão pelo navegador seguro…");
        bridge.health(this,new BrowserBridge.Callback(){ public void ok(String s){status.setText("✅ "+s);} public void fail(String e){status.setText("❌ "+e);} });
    }

    private void toggleOverlay(){
        if(Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)){
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
            Toast.makeText(this,"Permita exibir sobre outros apps e volte para ativar.",Toast.LENGTH_LONG).show(); return;
        }
        Intent i=new Intent(this,OverlayService.class); if(Build.VERSION.SDK_INT>=26)startForegroundService(i); else startService(i); Toast.makeText(this,"Assistente Nexo AI ativada",Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy(){ if(bridge!=null)bridge.destroy(); super.onDestroy(); }

    static final class WebViewHolder { static void attachHidden(LinearLayout root,android.webkit.WebView w){ w.setVisibility(View.GONE); root.addView(w,new LinearLayout.LayoutParams(1,1)); } }
}
