package asia.remix.overlay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{
	final static String TAG = "MainActivity";

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		if( Build.VERSION.SDK_INT >= 23 ){
			if( ! android.provider.Settings.canDrawOverlays( this ) ){
				Intent intent = new Intent(
					android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
				,	android.net.Uri.parse( "package:" + getPackageName() )
				);
				launcherPermissionOverLay.launch( intent );
			}
		}else{//API23未満実行不可
			Toast.makeText( getApplication(), "must API23", Toast.LENGTH_LONG ).show();
			finish();
		}
	}

	/**
	* 画面オーバーレイ表示許可要求結果
	*/
	ActivityResultLauncher<Intent> launcherPermissionOverLay = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		new ActivityResultCallback<ActivityResult>(){
			@Override
			public void onActivityResult( ActivityResult result ){
				//Backキーで戻るためresultCodeが必ずRESULT_CANCELED
				Log.d( TAG, "launcherPermissionOverLay#onActivityResult()" );
				if( ! android.provider.Settings.canDrawOverlays( getApplication() ) ){
					Toast.makeText( getApplication(), "must permission Overlay", Toast.LENGTH_LONG ).show();
					finish();
				}
			}
		} 
	);

	public void onClickStart( View view ){
		Intent intent = new Intent( getApplication(), LayerService.class );
		if( Build.VERSION.SDK_INT >= 26 ){
			//5秒以内に起動したサービスクラスでstartForegroundメソッドを呼び出す
			Log.d( TAG, String.format( "API%d startForegroundService()", android.os.Build.VERSION.SDK_INT ) );
			startForegroundService( intent );
		}else{//API26未満
			Log.d( TAG, String.format( "API%d startService()", android.os.Build.VERSION.SDK_INT ) );
			startService( intent );
		}
	}

	public void onClickStop( View view ){
		stopService( new Intent( getApplication(), LayerService.class ) );
	}
}