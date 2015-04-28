package com.yujin.demo;

import android.app.Activity;
import android.os.Bundle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MainActivityA extends Activity {
	private CameraPreview preview;
	private Camera camera;
	private ToneGenerator tone;
	private static final int OPTION_SNAPSHOT = 0;
	private MediaRecorder mediarecorder;
	private SurfaceView surfaceview;
	private SurfaceHolder surfaceHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 窗口设置为全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		preview = new CameraPreview(this);

		setContentView(preview);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case OPTION_SNAPSHOT:
			// 拍摄照片
			camera.takePicture(shutterCallback, null, jpegCallback);
			break;
		case BIND_AUTO_CREATE:
			// 拍摄照片
			media();
			break;
		}
		return true;
	}

	// 返回照片的JPEG格式的数据
	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Parameters ps = camera.getParameters();
			if (ps.getPictureFormat() == PixelFormat.JPEG) {
				// 存储拍照获得的图片
				String path = save(data);
				// 将图片交给Image程序处理
				Uri uri = Uri.fromFile(new File(path));
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				intent.setDataAndType(uri, "image/jpeg");
				startActivity(intent);
			}
		}
	};
	// 快门按下的时候onShutter()被回调
	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			if (tone == null)
				// 发出提示用户的声音
				tone = new ToneGenerator(AudioManager.STREAM_MUSIC,
						ToneGenerator.MAX_VOLUME);
			tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
		}
	};

	private String save(byte[] data) {
		String path = "/sdcard/" + System.currentTimeMillis() + ".jpg";
		try {
			// 判断SD卡上是否有足够的空间
			String storage = Environment.getExternalStorageDirectory().toString();
			StatFs fs = new StatFs(storage);
			long available = fs.getAvailableBlocks() * fs.getBlockSize();
			if (available < data.length) {
				// 空间不足直接返回空
				return null;
			}
			File file = new File(path);
			if (!file.exists())
				// 创建文件
				file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return path;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_SNAPSHOT, 0, R.string.snapshot).setIcon(R.drawable.icon);
		menu.add(0, BIND_AUTO_CREATE, 0, "xixixii");
		return super.onCreateOptionsMenu(menu);
	}

	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		SurfaceHolder mHolder;

		public CameraPreview(Context context) {
			super(context);
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		// Sureface创建的时候，此方法被调用
		public void surfaceCreated(SurfaceHolder holder) {
			// 打开摄像头，获得Camera对象
			camera = Camera.open();
			try {
				// 设置显示
				camera.setPreviewDisplay(holder);
			} catch (IOException exception) {
				camera.release();
				camera = null;
			}
		}

		// Surface销毁的时候，此方法被调用
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			// 释放Camera
			camera.release();
			camera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// 已经获得Surface的width和height，设置Camera的参数
			Camera.Parameters parameters = camera.getParameters();
			// parameters.setPreviewSize(w, h);
			parameters.setFocusMode("auto");
			camera.setParameters(parameters);
			// 开始预览
			camera.startPreview();
		}

	}

	public void media() {
		mediarecorder = new MediaRecorder();// 创建mediarecorder对象
		// 设置录制视频源为Camera(相机)
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
		mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		// 设置录制的视频编码h263 h264
		mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoSize(176, 144);
		// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoFrameRate(20);
		// mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
		// 设置视频文件输出的路径
		mediarecorder.setOutputFile("/sdcard/love.mp4");
		try {
			// 准备录制
			mediarecorder.prepare();
			// 开始录制
			mediarecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
