package mr.vinteract.utils;

import android.app.Application;
import android.content.Context;
import android.hardware.Camera.CameraInfo;

public class VinteractApplication extends Application {
	
	public static VinteractApplication instance;
	public static int mCameraIDToOpen = CameraInfo.CAMERA_FACING_FRONT; 
	private static boolean mcameraFacing = true;//front
	
	/* ---------------- User Input ------------------*/
	public static String mVideoFilePath = "/mnt/sdcard/m.mp4";//internal SDcard
	//public static String mVideoFilePath = "/mnt/extSdCard/m.mp4";//External SDcard
	public static int VIDEO_WIDTH = 352;
	public static int VIDEO_HEIGHT = 288;
	
	public VinteractApplication ()
	{
		VinteractApplication.instance = this;
	}
	
    public static Context getContext() {
        return VinteractApplication.instance;
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    }

	public static void setCameraPreference(boolean camera_Pref) {
		mcameraFacing = camera_Pref;
	}

	public static boolean getCameraPreference() {
		return mcameraFacing;
	}
    
    
}