package com.petrolinc.flashlightlampeled;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.petrolinc.flashlightlampeled.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity implements Callback {

	//   			android:configChanges="keyboardHidden|orientation|screenSize">
	//            android:screenOrientation="portrait"
//	<string name ="pref_sound">Sound</string>
//	<string name ="pref_sound_summ">Sound enabled</string>
//	<string name="pref_categ_interface">Interface and sound</string>
	 	private ImageButton btnSwitch;
	 	private ImageButton btnSetting;
	 	private ImageView light;
	 	private ImageView lamp;
	 	//private ImageView lamp;
	 	private SharedPreferences userSettings;
	 	private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	    private Camera camera;
	    private boolean isFlashOn;
	    private boolean hasFlash;
	    private boolean sound;
	    private boolean lightOnStartup;
	    private Parameters params;
	    private MediaPlayer mp; //Needed to play sound
		private	SurfaceView preview; // Some devices seems to need a surfaceview in order to activate the flash
		private	SurfaceHolder mHolder;

		private AdView adView;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		////System.out.println("onCreate() ");
		setContentView(R.layout.activity_main);
		
		
		checkFlashSupport(); //If flash not supported --> Exit

		//Create the adView
		initLowerAdBanner();
		
		//Association of the attributes to layout's elements
		light = (ImageView)findViewById(R.id.img_light);
		btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
		btnSetting = (ImageButton) findViewById(R.id.btn_setting);
		lamp = (ImageView)findViewById(R.id.img_lamp);
		
		initView();
		//Set the Listeners
		setBtnSwitchOnClickListener(); //Set the listener of btn_switch in a separate method
		setBtnSettingOnClickListener();
		
		//Init the Camera
		initCamera();
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false); //Load the ddefault preferences if the user has never made any change
		
	}
	
	private void initView(){
		DisplayMetrics dimension = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dimension);
		BitmapDrawable drawable = (BitmapDrawable) lamp.getDrawable();
		Bitmap nImage = Bitmap.createScaledBitmap(drawable.getBitmap(), dimension.widthPixels, dimension.heightPixels, false);
		lamp.setImageBitmap(nImage);
		drawable = null;
	}
	
	private void checkFlashSupport(){
		
		hasFlash = getApplicationContext().getPackageManager() //If the device has a flash, hasFlash = true;
		        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		 
		if (!hasFlash) {
		    // device doesn't support flash
		    // Show alert message and close the application
		    AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
		            .create();
		    alert.setTitle("Error");
		    alert.setMessage("Sorry, your device doesn't support flash light!");
		    alert.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		            // closing the application
		            finish();
		        }
		    });
		    alert.show();
		    return;
		}
	}
	
	private void getCamera() {
	    if (camera == null) {
	        try {
	            camera = Camera.open();
	            try {
					camera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            params = camera.getParameters();
	        } catch (RuntimeException e) {
	            Log.e("Camera Error. Failed to Open. Error: ",""+ e.getMessage());
	        }
	    }
	}
	
	private void turnOnFlash() {
	    if (!isFlashOn) {
	        if (camera == null || params == null) {
	            return;
	        }
	        // play sound
	       // //System.out.println("ON sound= "+sound);
	        if(sound)
	        		playSound();
	       if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
	    	   		light.setVisibility(View.VISIBLE);
	       
	        params = camera.getParameters();
	        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
	        camera.setParameters(params);
	        camera.startPreview();
	        isFlashOn = true;
	         
	        // changing button/switch image
	        toggleButtonImage();
	        //Prevent the screen from turning off
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    }
	}
	private void turnOffFlash() {
	    if (isFlashOn) {
	        if (camera == null || params == null) {
	            return;
	        }
	        // play sound
	        //System.out.println("OFF sound= "+sound);
	        if(sound)
	        		playSound();
	        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
	        		light.setVisibility(View.INVISIBLE);
	        params = camera.getParameters();
	        params.setFlashMode(Parameters.FLASH_MODE_OFF);
	        camera.setParameters(params);
	        camera.stopPreview();
	        isFlashOn = false;
	        //Re allows the screen to turn off
	      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        // changing button/switch image
	        toggleButtonImage();
	    }
	}
	
private void setBtnSwitchOnClickListener(){
	
	btnSwitch.setOnClickListener(new View.OnClickListener() {
		 
	    @Override
	    public void onClick(View v) {
	        if (isFlashOn) {
	            // turn off flash
	            turnOffFlash();
	        } else {
	            // turn on flash
	            turnOnFlash();
	        }
	    }
	});
}

private void setBtnSettingOnClickListener(){
	btnSetting.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this,
				      SettingsActivity.class);
				      startActivity(intent);
			
		}
	});
}

private void toggleButtonImage(){
    if(isFlashOn){
        btnSwitch.setImageResource(R.drawable.button_on);
    }else{
        btnSwitch.setImageResource(R.drawable.button_off);
    }
}

private void playSound(){
	new Thread(new Runnable(){
		public void run(){
			if(isFlashOn){
		        mp = MediaPlayer.create(MainActivity.this, R.raw.switch14);
		    }else{
		        mp = MediaPlayer.create(MainActivity.this, R.raw.switch14);
		    }
		    mp.setOnCompletionListener(new OnCompletionListener() {
		 
		        @Override
		        public void onCompletion(MediaPlayer mp) {
		            // TODO Auto-generated method stub
		        	mp.reset();
		            mp.release();
		        }
		    }); 
		    mp.start();
		}
		
	}).start();
}  

@Override
protected void onDestroy() {
	adView.destroy();
	super.onDestroy();
	 //System.out.println("onDestroy()");
	 
    
}

@Override
protected void onPause() {
	adView.pause();
	//System.out.println("onPause()");
	userSettings.unregisterOnSharedPreferenceChangeListener(prefListener);
    super.onPause();
}

@Override
protected void onRestart() {
    super.onRestart();
    getCamera();
    //System.out.println("onRestart()");
    
}

@Override
protected void onResume() {
    super.onResume();
    adView.resume();
    userSettings = PreferenceManager.getDefaultSharedPreferences(this);
    refreshSettings(userSettings);
    
    		
    prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
  	  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
  		  
  		    refreshSettings(prefs);
  		    
  		  }
  		};
  		
  	userSettings.registerOnSharedPreferenceChangeListener(prefListener);  
    getCamera();
   if(lightOnStartup){
	  turnOnFlash(); 
   }
   else{
	   turnOffFlash();
   }
    //System.out.println("onResume()");
    //toggleButtonImage();

    // on resume turn on the flash
    /*if(!isFlashOn){
        	turnOnFlash();
    }*/
    
}
private void refreshSettings(SharedPreferences prefs){
	sound =  prefs.getBoolean("pref_sound", true);
	lightOnStartup= prefs.getBoolean("pref_light_start", true);
	//System.out.println("refreshSettings sound= "+sound);
	//System.out.println("refreshSettings isFlashOn "+isFlashOn);
	reloadAndDisplayComponents();
}
@Override
protected void onStart() {
    super.onStart();
    //System.out.println("onStart()");
    // on starting the app get the camera params
    getCamera();
}

@Override
protected void onStop() {
    super.onStop();
    //System.out.println("onStop()");
    // on stop release the camera
    if (camera != null) {
        camera.release();
        camera = null;
    }
}
////Callbacks
@Override
public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {
	//System.out.println("surfacChanged()");

	
}
@Override
public void surfaceCreated(SurfaceHolder holder) {
	//System.out.println("surfaceCreated()");

	mHolder = holder;
    try {
    	 
		camera.setPreviewDisplay(mHolder);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
@Override
public void surfaceDestroyed(SurfaceHolder holder) {
   // camera.stopPreview();
	//System.out.println("surfaceDestroyed()");
    mHolder = null;
}
@Override
public void onConfigurationChanged(Configuration newConfig){
	super.onConfigurationChanged(newConfig);
	//System.out.println("onConfigurationChanged()");
	setContentView(R.layout.activity_main);
   reloadAndDisplayComponents();
}

private void reloadAndDisplayComponents(){
	Configuration currentConfig = getResources().getConfiguration();
	 // Checks the orientation of the screen
	//System.out.println("reloadAndDysplayComponents isFlashOn "+isFlashOn);
    if (currentConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    	initLandscape();
    	if(isFlashOn){
    		turnOnFlash();
    	}
    	else{
    		turnOffFlash();
    	}
    } else if (currentConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
       initPortrait();
       if(isFlashOn){
   		turnOnFlash();
   		light.setVisibility(View.VISIBLE);
       }
       else{
    	   turnOffFlash();
    	   light.setVisibility(View.INVISIBLE);
       }
    }
	
}
private void initPortrait(){
initLowerAdBanner();
light = (ImageView)findViewById(R.id.img_light);
btnSwitch = (ImageButton)findViewById(R.id.btnSwitch);
btnSetting = (ImageButton) findViewById(R.id.btn_setting);
setBtnSwitchOnClickListener();
setBtnSettingOnClickListener();
toggleButtonImage();
initCamera();
	}

private void initLandscape(){
	initLowerAdBanner();
	btnSwitch = (ImageButton)findViewById(R.id.btnSwitch);
	btnSetting = (ImageButton) findViewById(R.id.btn_setting);
	setBtnSwitchOnClickListener();
	setBtnSettingOnClickListener();
	toggleButtonImage();
	initCamera();
	}

private void initCamera(){
	preview = (SurfaceView) findViewById(R.id.PREVIEW);
	//Some devices need the next 2 lines to activate the flash
	mHolder = preview.getHolder();
	mHolder.addCallback(this);
	getCamera(); // Try to get a camera instance

}

private void initLowerAdBanner(){
	adView= (AdView)findViewById(R.id.adView);
	AdRequest adRequest = new AdRequest.Builder().build();
	adView.loadAd(adRequest);
}
}
