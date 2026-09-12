package com.nexoai.ultra;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.view.*;import android.widget.*;import java.util.*;

public class AssistantActivity extends Activity {
    EditText input, context; TextView output, status; String processText; boolean processReadOnly=false;
    final String[][] MODES={{"👑 Minha Cara","auto"},{"🧠 Responder","reply"},{"⚡ Rápido","quick"},{"✨ Otimizar","optimize"},{"💼 Profissional","professional"},{"🤝 Educado","polite"},{"🎯 Direto","direct"},{"🔥 Convincente","persuasive"},{"😊 Natural","natural"},{"⚠️ Firme","firm"},{"✍️ Corrigir","correct"}};
    @Override public void onCreate(Bundle b){ super.onCreate(b); extract(); render(); }
    private void extract(){ Intent i=getIntent(); String a=i.getAction(); if(Intent.ACTION_PROCESS_TEXT.equals(a)){ CharSequence cs=i.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT); processText=cs==null?"":cs.toString(); processReadOnly=i.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY,false);} else if(Intent.ACTION_SEND.equals(a)){ processText=i.getStringExtra(Intent.EXTRA_TEXT); } if(processText==null)processText=""; }
    private void render(){
        ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(8,17,31)); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); Ui.pad(root,this,16); sv.addView(root);
        root.addView(Ui.title(this,"🧠 Nexo AI",24)); TextView sub=Ui.text(this,"Escolha a intenção. A IA ajusta o tom automaticamente.",14); sub.setPadding(0,4,0,12); root.addView(sub);
        input=box("Texto selecionado ou mensagem",processText,140); root.addView(input);
        context=box("Contexto adicional (opcional)","",90); root.addView(context);
        GridLayout grid=new GridLayout(this); grid.setColumnCount(2); grid.setUseDefaultMargins(true); for(String[] m:MODES){ Button b=Ui.button(this,m[0]); b.setOnClickListener(v->run(m[1])); GridLayout.LayoutParams gp=new GridLayout.LayoutParams(); gp.width=0; gp.height=Ui.dp(this,50); gp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); gp.setMargins(Ui.dp(this,3),Ui.dp(this,3),Ui.dp(this,3),Ui.dp(this,3)); grid.addView(b,gp);} root.addView(grid);
        status=Ui.text(this,"Pronto.",13); status.setPadding(0,12,0,6); root.addView(status);
        output=Ui.text(this,"A resposta aparecerá aqui.",17); output.setTextIsSelectable(true); output.setBackground(Ui.card(this)); Ui.pad(output,this,16); root.addView(output,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); Button copy=Ui.button(this,"📋 Copiar"); copy.setOnClickListener(v->copy()); Button replace=Ui.button(this,"✅ Usar texto"); replace.setOnClickListener(v->useText()); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,52),1); p.setMargins(4,12,4,0); actions.addView(copy,p); actions.addView(replace,p); root.addView(actions);
        setContentView(sv);
    }
    private EditText box(String hint,String val,int minH){ EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(100,116,139)); e.setTextColor(Color.WHITE); e.setText(val); e.setTextSize(15); e.setMinHeight(Ui.dp(this,minH)); e.setGravity(Gravity.TOP); MainActivity.GradientDrawableX.apply(e,this); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,6,0,8); e.setLayoutParams(p); return e; }
    private void run(String mode){ String t=input.getText().toString().trim(); if(t.isEmpty()){Toast.makeText(this,"Cole ou selecione um texto primeiro",Toast.LENGTH_SHORT).show();return;} status.setText("⚡ Gerando resposta inteligente..."); output.setText("Analisando contexto e tom..."); ApiClient.generate(this,mode,t,context.getText().toString(),new ApiClient.Callback(){ public void ok(String s){runOnUiThread(()->{output.setText(s);status.setText("✓ Pronto");});} public void fail(String e){runOnUiThread(()->{output.setText("Erro: "+e);status.setText("Falha na conexão");});}}); }
    private void copy(){ ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("Nexo AI",output.getText())); Toast.makeText(this,"Copiado",Toast.LENGTH_SHORT).show(); }
    private void useText(){ String s=output.getText().toString(); if(Intent.ACTION_PROCESS_TEXT.equals(getIntent().getAction())&&!processReadOnly){ Intent r=new Intent(); r.putExtra(Intent.EXTRA_PROCESS_TEXT,s); setResult(RESULT_OK,r); finish(); } else { copy(); Toast.makeText(this,"Texto copiado. Volte ao WhatsApp e cole.",Toast.LENGTH_LONG).show(); } }
}
