package com.nexoai.ultra;

import android.app.*;import android.os.*;import android.provider.Settings;import android.content.*;import android.net.Uri;import android.graphics.Color;import android.text.InputType;import android.view.*;import android.widget.*;

public class MainActivity extends Activity {
    EditText backend, token, style; TextView testStatus;
    @Override public void onCreate(Bundle b){ super.onCreate(b); render(); }
    private void render(){
        ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(8,17,31));
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); Ui.pad(root,this,20); sv.addView(root);
        root.addView(Ui.title(this,"🧠 Nexo AI Ultra",28));
        TextView sub=Ui.text(this,"Seu copiloto de respostas instantâneas para WhatsApp e qualquer app com texto selecionável.",16); sub.setPadding(0,8,0,18); root.addView(sub);
        LinearLayout status=new LinearLayout(this); status.setOrientation(LinearLayout.VERTICAL); status.setBackground(Ui.card(this)); Ui.pad(status,this,16); root.addView(status,new LinearLayout.LayoutParams(-1,-2));
        status.addView(Ui.title(this,"Atalhos inteligentes",19));
        status.addView(Ui.text(this,"1. Salve o token privado.\n2. Teste a conexão.\n3. Ative o balão flutuante.\n4. Selecione texto → Nexo AI ou Compartilhar → Nexo AI.",14));
        Button overlay=Ui.button(this,"Ativar / abrir balão flutuante"); overlay.setOnClickListener(v->toggleOverlay()); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,Ui.dp(this,54)); bp.setMargins(0,16,0,12); root.addView(overlay,bp);
        root.addView(Ui.title(this,"Configuração",19));
        backend=field("URL do backend PHP",Prefs.backend(this));
        token=field("Token privado do aplicativo",Prefs.token(this)); token.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        style=field("Meu estilo padrão",Prefs.style(this));
        root.addView(backend); root.addView(token); root.addView(style);
        Button save=Ui.button(this,"Salvar configurações"); save.setOnClickListener(v->save()); root.addView(save,bp);
        Button test=Ui.button(this,"🧪 Testar servidor"); test.setOnClickListener(v->testServer()); root.addView(test,bp);
        testStatus=Ui.text(this,"Servidor ainda não testado.",13); testStatus.setPadding(0,2,0,12); root.addView(testStatus);
        TextView note=Ui.text(this,"A chave OpenAI permanece somente no servidor. O APK envia apenas o texto selecionado e seu token privado para autenticação.",13); note.setPadding(0,8,0,20); root.addView(note);
        setContentView(sv);
    }
    private void save(){
        String b=backend.getText().toString().trim(), t=token.getText().toString().trim(), s=style.getText().toString().trim();
        if(b.isEmpty()||!b.startsWith("https://")){Toast.makeText(this,"Use uma URL HTTPS válida",Toast.LENGTH_LONG).show();return;}
        if(t.isEmpty()){Toast.makeText(this,"Informe o mesmo token configurado no servidor",Toast.LENGTH_LONG).show();return;}
        Prefs.sp(this).edit().putString("backend",b).putString("token",t).putString("style",s).apply(); Toast.makeText(this,"Configurações salvas",Toast.LENGTH_SHORT).show();
    }
    private void testServer(){ save(); testStatus.setText("⏳ Testando " + backend.getText().toString().trim() + " ..."); ApiClient.health(this,new ApiClient.Callback(){ public void ok(String s){runOnUiThread(()->testStatus.setText("✅ "+s));} public void fail(String e){runOnUiThread(()->testStatus.setText("❌ "+e));}}); }
    private EditText field(String hint,String value){ EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(100,116,139)); e.setTextColor(Color.WHITE); e.setText(value); e.setTextSize(14); e.setSingleLine(false); GradientDrawableX.apply(e,this); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,8,0,8); e.setLayoutParams(p); return e; }
    private void toggleOverlay(){
        if(Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)){ startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); Toast.makeText(this,"Permita Exibir sobre outros apps e volte para ativar",Toast.LENGTH_LONG).show(); return; }
        Intent i=new Intent(this,OverlayService.class); if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i); Toast.makeText(this,"Balão Nexo AI ativado",Toast.LENGTH_SHORT).show();
    }
    static class GradientDrawableX { static void apply(EditText e,Context c){ android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable(); g.setColor(Color.rgb(16,28,47)); g.setStroke(Ui.dp(c,1),Color.rgb(51,65,85)); g.setCornerRadius(Ui.dp(c,14)); e.setBackground(g); Ui.pad(e,c,12);} }
}
