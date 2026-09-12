package com.nexoai.ultra;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.PixelFormat;import android.graphics.Color;import android.view.*;import android.widget.*;import android.provider.Settings;

public class OverlayService extends Service {
    WindowManager wm; View bubble; WindowManager.LayoutParams lp;
    @Override public void onCreate(){ super.onCreate(); channel(); startForeground(41,notification()); if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;} show(); }
    private void show(){ wm=(WindowManager)getSystemService(WINDOW_SERVICE); TextView v=new TextView(this); v.setText("🧠"); v.setTextSize(26); v.setGravity(Gravity.CENTER); v.setTextColor(Color.WHITE); android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable(); g.setColor(Color.rgb(16,28,47)); g.setStroke(Ui.dp(this,2),Color.rgb(94,234,212)); g.setShape(android.graphics.drawable.GradientDrawable.OVAL); v.setBackground(g); bubble=v;
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE; lp=new WindowManager.LayoutParams(Ui.dp(this,62),Ui.dp(this,62),type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT); lp.gravity=Gravity.TOP|Gravity.END; lp.x=12; lp.y=300; wm.addView(v,lp);
        v.setOnTouchListener(new View.OnTouchListener(){ float x,y; int sx,sy; long down; public boolean onTouch(View vv,android.view.MotionEvent e){ switch(e.getAction()){ case 0:x=e.getRawX();y=e.getRawY();sx=lp.x;sy=lp.y;down=System.currentTimeMillis();return true; case 2:lp.x=sx-(int)(e.getRawX()-x);lp.y=sy+(int)(e.getRawY()-y);wm.updateViewLayout(vv,lp);return true; case 1:if(System.currentTimeMillis()-down<250)open();return true;}return false;}});
    }
    private void open(){ Intent i=new Intent(this,AssistantActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(i); }
    private void channel(){ if(Build.VERSION.SDK_INT>=26){ NotificationChannel c=new NotificationChannel("nexoai","Nexo AI",NotificationManager.IMPORTANCE_LOW); c.setDescription("Mantém o balão inteligente disponível"); getSystemService(NotificationManager.class).createNotificationChannel(c);} }
    private Notification notification(){ Intent i=new Intent(this,MainActivity.class); PendingIntent p=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT); Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"nexoai"):new Notification.Builder(this); return b.setContentTitle("Nexo AI ativo").setContentText("Toque no balão 🧠 para responder rapidamente").setSmallIcon(com.nexoai.ultra.R.drawable.ic_brain).setContentIntent(p).setOngoing(true).build(); }
    @Override public int onStartCommand(Intent i,int f,int id){ return START_STICKY; }
    @Override public void onDestroy(){ if(wm!=null&&bubble!=null)wm.removeView(bubble); super.onDestroy(); }
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
