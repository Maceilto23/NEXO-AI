package com.nexoai.ultra;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.view.*;
import android.widget.ImageView;

public class OverlayService extends Service {
    WindowManager wm; View bubble; WindowManager.LayoutParams lp;
    @Override public void onCreate(){ super.onCreate(); channel(); startForeground(41,notification()); if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;} show(); }
    private void show(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE); ImageView v=new ImageView(this); v.setImageResource(R.drawable.ic_robot); v.setPadding(Ui.dp(this,6),Ui.dp(this,6),Ui.dp(this,6),Ui.dp(this,6)); bubble=v;
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        lp=new WindowManager.LayoutParams(Ui.dp(this,48),Ui.dp(this,48),type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT); lp.gravity=Gravity.TOP|Gravity.END; lp.x=10; lp.y=320; wm.addView(v,lp);
        v.setOnTouchListener(new View.OnTouchListener(){ float x,y; int sx,sy; long down; public boolean onTouch(View vv,android.view.MotionEvent e){ switch(e.getAction()){ case android.view.MotionEvent.ACTION_DOWN:x=e.getRawX();y=e.getRawY();sx=lp.x;sy=lp.y;down=System.currentTimeMillis();return true; case android.view.MotionEvent.ACTION_MOVE:lp.x=sx-(int)(e.getRawX()-x);lp.y=sy+(int)(e.getRawY()-y);wm.updateViewLayout(vv,lp);return true; case android.view.MotionEvent.ACTION_UP:if(System.currentTimeMillis()-down<250)open();return true;}return false;} });
    }
    private void open(){ Intent i=new Intent(this,AssistantActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(i); }
    private void channel(){ if(Build.VERSION.SDK_INT>=26){ NotificationChannel c=new NotificationChannel("nexoai","Nexo AI",NotificationManager.IMPORTANCE_LOW); c.setDescription("Mantém o assistente flutuante disponível"); getSystemService(NotificationManager.class).createNotificationChannel(c);} }
    private Notification notification(){ Intent i=new Intent(this,MainActivity.class); PendingIntent p=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT); Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"nexoai"):new Notification.Builder(this); return b.setContentTitle("Nexo AI ativo").setContentText("Assistente flutuante disponível").setSmallIcon(R.drawable.ic_robot).setContentIntent(p).setOngoing(true).build(); }
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){ if(wm!=null&&bubble!=null)wm.removeView(bubble); super.onDestroy(); }
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
