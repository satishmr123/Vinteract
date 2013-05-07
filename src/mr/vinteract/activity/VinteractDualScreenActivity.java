package mr.vinteract.activity;

import mr.vinteract.R;
import mr.vinteract.utils.VinteractApplication;
import mr.vinteract.utils.VinteractVideoPreview;
import mr.vinteract.utils.VinteractVideoView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class VinteractDualScreenActivity extends Activity implements
		OnTouchListener, OnPreparedListener, OnErrorListener, Camera.PreviewCallback {

	private Camera mCamera;
	private VinteractVideoPreview customVideoView;
	private VinteractVideoView vv;
	private String path = VinteractApplication.mVideoFilePath;
	private boolean readyToPlay;
	private ProgressBar progress;
	int currentPosition;

	private ImageButton backButton;
	private ImageButton flip_camera;
	private ImageButton settingsButton;
	private RelativeLayout mBottomBar;

	private Runnable mRunnableforBottomBar;
	private Handler mTimerHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_vinteract_full);

		openCamera();
		FrameLayout framelayout = (FrameLayout) findViewById(R.id.cameraPreview);
		customVideoView = new VinteractVideoPreview(this, mCamera);
		customVideoView.setPreviewCallback(this);
		framelayout.addView(customVideoView);
		framelayout.setOnTouchListener(this);

		vv = (VinteractVideoView) findViewById(R.id.vinteractVideoView);
		vv.setOnErrorListener(this);
		vv.setOnPreparedListener(this);
		readyToPlay = false;
		currentPosition = 0;

		stopMedia(null);

		Uri uri = Uri.parse(path);
		MediaController controller = new MediaController(this);
		this.vv.setMediaController(controller);
		this.vv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.seekTo(currentPosition);
				mp.start();
			}
		});
		setScreenDimention();
		vv.setVideoURI(uri);
		vv.requestFocus();

		mBottomBar = (RelativeLayout) findViewById(R.id.bottom_bar);
		mBottomBar.setVisibility(View.VISIBLE);

		mRunnableforBottomBar = new Runnable() {
			@Override
			public void run() {
				mBottomBar.setVisibility(View.INVISIBLE);
			}
		};

		backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showExitAlert("Want to Stop ?");
			}
		});

		flip_camera = (ImageButton) findViewById(R.id.flip_camera);
		flip_camera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				asyncSwitchCamera();
			}
		});

		settingsButton = (ImageButton) findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(VinteractDualScreenActivity.this,
						"Settings not implemented yet", Toast.LENGTH_SHORT)
						.show();
			}
		});
		
		hideBottomBarLater();
	}

	private void setScreenDimention() {
		int scaledHeight = 0;
		int scaledWidth = 0;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int videoHeight = VinteractApplication.VIDEO_HEIGHT;
		int videoWidth = VinteractApplication.VIDEO_WIDTH;
		if (0 != videoHeight && 0 != videoWidth) {
			float alpha1 = (float) metrics.heightPixels / (float) videoHeight;
			float alpha2 = (float) metrics.widthPixels / (float) videoWidth;
			if (alpha1 < alpha2) {
				scaledHeight = (int) (alpha1 * videoHeight);
				scaledWidth = (int) (alpha1 * videoWidth);
			} else {
				scaledHeight = (int) (alpha2 * videoHeight);
				scaledWidth = (int) (alpha2 * videoWidth);
			}
		} else {
			scaledHeight = (int) (metrics.widthPixels * 3 / (float) 4);
			scaledWidth = metrics.widthPixels;
		}
		vv.setVideoAspect(scaledWidth, scaledHeight);
	}

	public void onPrepared(MediaPlayer mp) {
		Log.i("", "prepared");
		mp.setLooping(true);
		// video size check (media is a video if size is defined, audio if not)
		// int h = mp.getVideoHeight();
		// int w = mp.getVideoWidth();
		mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Log.i(this.getClass().getName(), "percent: " + percent);
				progress.setProgress(percent);
			}
		});
		// onSeekCompletionListener declaration
		mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				if (!mp.isPlaying()) {
					playMedia(null);
				}
			}
		});

		mp.setOnCompletionListener(null);

		readyToPlay = true;
		// int time = vv.getDuration();
		int time_elapsed = vv.getCurrentPosition();
		Log.i("time elapsed", " " + time_elapsed);
		progress.setProgress(time_elapsed);

		progress.setOnTouchListener(new OnTouchListener() {

			// enables changing of the current playback position
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ProgressBar pb = (ProgressBar) v;

				int newPosition = (int) (100 * event.getX() / pb.getWidth());
				if (newPosition > pb.getSecondaryProgress()) {
					newPosition = pb.getSecondaryProgress();
				}

				switch (event.getAction()) {
				// update position when finger is DOWN/MOVED/UP
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_UP:
					pb.setProgress(newPosition);
					vv.seekTo((int) newPosition * vv.getDuration() / 100);
					break;
				}
				return true;
			}
		});
	}

	public void playMedia(View v) {

		if (readyToPlay) {
			vv.start();
		} else {
			vv.pause();
		}
	}

	public void stopMedia(View v) {

		if (vv.getCurrentPosition() != 0) {
			vv.pause();
			vv.seekTo(0);
			progress.setProgress(0);
		}
	}

	@Override
	public void onStop() {
		vv.stopPlayback();
		super.onStop();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		return false;
	}

	private void showExitAlert(String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage(message)
				.setPositiveButton("YES",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								Log.d("", "attempt to stop preview");
								finishWithCameraRealease();
							}
						})
				.setNegativeButton("NO", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						return;
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onResume() {
		Log.d("", "onResume");
		super.onResume();
		openCamera();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("", "onPause");
		releaseCamera();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("", "onBackpressed");
		showExitAlert("Want to Exit ?");
	}

	private void hideBottomBarLater() {
		mTimerHandler.removeCallbacks(mRunnableforBottomBar, null);
		mTimerHandler.postDelayed(mRunnableforBottomBar, 3000);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mBottomBar.isShown()) {
				mBottomBar.setVisibility(View.INVISIBLE);
			} else {
				mBottomBar.setVisibility(View.VISIBLE);
				hideBottomBarLater();
			}
		}
		return false;
	}

	@SuppressLint("NewApi")
	private void openCamera() {
		if (mCamera == null) {
			Log.d("", "Open new Camera Object");
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				try {
					Log.d("", "Trying to open camera with id = "
							+ VinteractApplication.mCameraIDToOpen);
					mCamera = Camera.open(VinteractApplication.mCameraIDToOpen);
				} catch (RuntimeException RE) {
					RE.printStackTrace();
					finishWithCameraRealease();
				}
			} else {
				mCamera = Camera.open();
			}
			if (mCamera == null) {
				Log.e("",
						"Error: Sorry your device does not have unused Camera.");
				finishWithCameraRealease();
				return;
			}
			if (customVideoView != null) {
				Log.d("", "Set preview with new camera object onPause");
				customVideoView.setCamera(mCamera);
				customVideoView.setPreviewCallback(this);
			}
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
			Log.d("", "release preview Camera");
		}
	}

	private void finishWithCameraRealease() {
		Log.d("", "release cam & finish");
		releaseCamera();
		finish();
	}

	@SuppressLint("NewApi")
	private synchronized void asyncSwitchCamera() {
		Thread switchCams = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("", "asyncSwitchCamera try to switch camera");
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					int mNumCameras = Camera.getNumberOfCameras();
					Log.d("", "This phone has " + mNumCameras + " Cameras");
					if (mNumCameras < 1) {
						Log.e("", "Could not find any camera on this phone");
						return;
					}
					boolean camera_Pref = VinteractApplication
							.getCameraPreference();
					Log.d("", "camera facing preference = " + camera_Pref);

					int cameraIDtoOpen = CameraInfo.CAMERA_FACING_BACK;
					if (camera_Pref == true) {
						Log.d("", "Trying to Open front camera");
						cameraIDtoOpen = CameraInfo.CAMERA_FACING_FRONT;
					} else {
						Log.d("", "Trying to Open Back camera");
						cameraIDtoOpen = CameraInfo.CAMERA_FACING_BACK;
					}
					camera_Pref = !camera_Pref;
					VinteractApplication.setCameraPreference(camera_Pref);
					try {
						Log.d("", "Stop prvious camera preview");
						customVideoView.stopPreview();

						Log.d("", "release camera object");
						mCamera.release();

						Log.d("", "Trying to open camera with id = "
								+ cameraIDtoOpen);
						mCamera = Camera.open(cameraIDtoOpen);

						Log.d("", "Set preview with new camera object");
						customVideoView.refreshCameraPreview(mCamera);
						customVideoView.setPreviewCallback(VinteractDualScreenActivity.this);

						VinteractApplication.mCameraIDToOpen = cameraIDtoOpen;
					} catch (RuntimeException RE) {
						RE.printStackTrace();
						Log.e("", "RuntimeException during new camera setup "
								+ RE);
					}
				}
			}
		});
		switchCams.setName("Switch_Camera");
		switchCams.start();
	}
	
	@Override
	public void onPreviewFrame(byte[] bytebuffer, Camera camera) {
		Log.d("", "onPreviewFrame called with data of " + bytebuffer.length);
	}
}
