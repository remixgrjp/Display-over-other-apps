package asia.remix.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.core.app.NotificationCompat;

public class LayerService extends Service{
	final static String TAG = "LayerService";
	final static String sTitle = "Overlay service";
	final static String sChannelId = "Channel ID";
	final static int iNotificationID = 1;//0はstartForeground()で通知表示されない

	View view;
	WindowManager windowManager;

	@Override
	public void onCreate(){
		super.onCreate();

		LayoutInflater layoutInflater = LayoutInflater.from( this );
		view = layoutInflater.inflate( R.layout.overlay, /*ViewGroup*/null );

		int iType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		if( Build.VERSION.SDK_INT >= 26 ){
			iType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//×S7Edge、HTL23
		}
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT
		,	WindowManager.LayoutParams.WRAP_CONTENT
		,	iType
		,	  LayoutParams.FLAG_NOT_FOCUSABLE       //フォーカスを奪わない(下の画面の操作ができなくなるため)
			| LayoutParams.FLAG_FULLSCREEN          //OverlapするViewを全画面表示
			| LayoutParams.FLAG_NOT_TOUCH_MODAL     //モーダル以外のタッチを背後のウィンドウへ送信
			| LayoutParams.FLAG_NOT_TOUCHABLE       //画面操作を無効化
		//  | LayoutParams.FLAG_LAYOUT_NO_LIMITS    //ステータスバーをActivityにかぶせるように表示、画面外への拡張を許可
		//  | LayoutParams.FLAG_LAYOUT_INSET_DECOR  //FLAG_LAYOUT_IN_SCREENで必要なoption
		//  | LayoutParams.FLAG_LAYOUT_IN_SCREEN    //FLAG_FULLSCREEN(OverlapするViewを全画面表示)
		//  | LayoutParams.FLAG_HARDWARE_ACCELERATED//GPUを使用して画面描画(ハードウェアアクセラレーションの使用)
		,	PixelFormat.TRANSLUCENT //viewを透明にする
		);

		windowManager = (WindowManager)getApplicationContext().getSystemService( Context.WINDOW_SERVICE );
		windowManager.addView( view, params );//android.view.WindowManager$BadTokenException: Unable to add window android.view.ViewRootImpl$W@c486642 -- permission denied for window type 2003
	}

	boolean bActive = false;//stopで初期化
	@Override
	public int onStartCommand( Intent intent, int flags, int startId ){
		if( bActive ){
			return super.onStartCommand( intent, flags, startId );
		}else{
			bActive = true;
		}

		Context context = getApplicationContext();

		if( Build.VERSION.SDK_INT >= 26 ){
			//API26 Android 8以上 通知を送信する前に通知チャネルを作成する必要がある
			//https://developer.android.com/training/notify-user/channels.html
			NotificationChannel channel
			= new NotificationChannel( sChannelId, sTitle , NotificationManager.IMPORTANCE_DEFAULT );
			NotificationManager notificationManager
			= (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
			notificationManager.createNotificationChannel( channel );
		}

		NotificationCompat.Builder builder;
		if( Build.VERSION.SDK_INT >= 26 ){
			builder = new NotificationCompat.Builder( context, sChannelId );
		}else{
			builder = new NotificationCompat.Builder( context );
		}
		builder.setContentTitle( sTitle );
		builder.setSmallIcon( android.R.drawable.btn_star );
		builder.setColor( android.graphics.Color.RED );
		builder.setWhen( System.currentTimeMillis() );

		//通知はタップ応答する必要があり、ここではMainActivityを立ち上げる
		Intent i = new Intent( new Intent( getApplication(), MainActivity.class ) );
		builder.setContentIntent( PendingIntent.getActivity( context, 0, i, 0 ) );

		Notification notification = builder.build();
		notification.flags = Notification.FLAG_NO_CLEAR;//スワイプ・全消去しても通知消さない
		startForeground( iNotificationID, notification );

		return super.onStartCommand( intent, flags, startId );
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if( null != view ){
			windowManager.removeView( view );
		}
		if( Build.VERSION.SDK_INT >= 26 ){
			stopForeground( Service.STOP_FOREGROUND_DETACH );
		}else{
			stopForeground( true );//true:通知削除
			stopSelf();
		}
	}

	@Override
	public IBinder onBind( Intent intent ){
		return null;
	}
}