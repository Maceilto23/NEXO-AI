package com.nexoai.ultra;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

public class AssistantActivity extends Activity {
    EditText input,context; TextView output,status; BrowserBridge bridge; String sourceText=""; boolean processReadOnly=false;
    final String[][] MODES={{"Minha cara","auto"},{"Responder","reply"},{"Rápido","quick"},{"Otimizar","optimize"},{"Profissional","professional"},{"Educado","polite"},{"Direto","direct"},{"Convincente","persuasive"},{"Natural","natural"},{"Firme","firm"},{"Corrigir","correct"}};

    @Override public void onCreate(Bundle b){ super.onCreate(b); extract(); render(); }

    private void extract(){
        Intent i=getIntent(); String a=i.getAction();
        if(Intent.ACTION_PROCESS_TEXT.equals(a)){ CharSequence cs=i.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT); sourceText=cs==null?"":cs.toString(); processReadOnly=i.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY,false); }
        else if(Intent.ACTION_SEND.equals(a)){ sourceText=i.getStringExtra(Intent.EXTRA_TEXT); }
        if(sourceText==null) sourceText="";
    }

    private void render(){
        ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(7,17,31)); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); Ui.pad(root,this,18); sv.addView(root);
        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); ImageView icon=new ImageView(this); icon.setImageResource(R.drawable.nexo_ai_avatar); head.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,46),Ui.dp(this,46))); TextView tt=Ui.title(this,"Nexo AI",24); tt.setPadding(Ui.dp(this,10),0,0,0); head.addView(tt); root.addView(head);
        TextView sub=Ui.text(this,"Escolha a intenção. O texto é processado no seu servidor do InfinityFree.",13); sub.setPadding(0,8,0,12); root.addView(sub);
        input=box("Texto ou mensagem",sourceText,150); context=box("Contexto adicional (opcional)","",90); root.addView(input); root.addView(context);
        GridLayout grid=new GridLayout(this); grid.setColumnCount(2); grid.setUseDefaultMargins(true);
        for(String[] m:MODES){ Button b=Ui.button(this,m[0]); b.setOnClickListener(v->run(m[1])); GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=0; p.height=Ui.dp(this,50); p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); p.setMargins(Ui.dp(this,3),Ui.dp(this,3),Ui.dp(this,3),Ui.dp(this,3)); grid.addView(b,p); }
        root.addView(grid);
        status=Ui.text(this,"Pronto.",13); status.setPadding(0,12,0,6); root.addView(status);
        output=Ui.text(this,"A resposta aparecerá aqui.",17); output.setTextIsSelectable(true); output.setBackground(Ui.card(this)); Ui.pad(output,this,16); root.addView(output,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); actions.setPadding(0,12,0,0);
        Button copy=Ui.button(this,"Copiar"); copy.setOnClickListener(v->copy()); actions.addView(copy,new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));
        Button use=Ui.button(this,"Usar texto"); use.setOnClickListener(v->useResult()); LinearLayout.LayoutParams up=new LinearLayout.LayoutParams(0,Ui.dp(this,50),1); up.setMargins(Ui.dp(this,8),0,0,0); actions.addView(use,up); root.addView(actions);
        bridge=new BrowserBridge(this); bridge.view().setVisibility(View.GONE); root.addView(bridge.view(),new LinearLayout.LayoutParams(1,1)); setContentView(sv);
    }

    private EditText box(String hint,String value,int minH){ EditText e=new EditText(this); e.setHint(hint); e.setText(value); e.setTextColor(Color.WHITE); e.setHintTextColor(Color.rgb(120,139,163)); e.setTextSize(15); e.setGravity(Gravity.TOP); e.setMinHeight(Ui.dp(this,minH)); GradientDrawable g=new GradientDrawable(); g.setColor(Color.rgb(14,27,45)); g.setStroke(Ui.dp(this,1),Color.rgb(50,70,96)); g.setCornerRadius(Ui.dp(this,16)); e.setBackground(g); Ui.pad(e,this,13); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,10); e.setLayoutParams(p); return e; }

    private void run(String mode){
        String text=input.getText().toString().trim(); if(text.isEmpty()){Toast.makeText(this,"Informe um texto.",Toast.LENGTH_SHORT).show();return;}
        if(Prefs.pin(this).trim().isEmpty()){Toast.makeText(this,"Abra o Nexo AI e configure o PIN primeiro.",Toast.LENGTH_LONG).show();return;}
        status.setText("Pensando…"); output.setText("Processando com segurança no servidor…");
        bridge.generate(this,mode,text,context.getText().toString(),new BrowserBridge.Callback(){ public void ok(String s){ output.setText(s); status.setText("Concluído."); } public void fail(String e){ output.setText(e); status.setText("Falha na conexão."); } });
    }

    private void copy(){ String s=output.getText().toString(); ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("Nexo AI",s)); Toast.makeText(this,"Copiado",Toast.LENGTH_SHORT).show(); }
    private void useResult(){ String s=output.getText().toString(); if(Intent.ACTION_PROCESS_TEXT.equals(getIntent().getAction()) && !processReadOnly){ Intent r=new Intent(); r.putExtra(Intent.EXTRA_PROCESS_TEXT,s); setResult(RESULT_OK,r); finish(); } else copy(); }
    @Override protected void onDestroy(){ if(bridge!=null)bridge.destroy(); super.onDestroy(); }
}
