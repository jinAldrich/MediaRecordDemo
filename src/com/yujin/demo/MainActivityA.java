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
		// ��������Ϊȫ��
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
			// ������Ƭ
			camera.takePicture(shutterCallback, null, jpegCallback);
			break;
		case BIND_AUTO_CREATE:
			// ������Ƭ
			media();
			break;
		}
		return true;
	}

	// ������Ƭ��JPEG��ʽ������
	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Parameters ps = camera.getParameters();
			if (ps.getPictureFormat() == PixelFormat.JPEG) {
				// �洢���ջ�õ�ͼƬ
				String path = save(data);
				// ��ͼƬ����Image������
				Uri uri = Uri.fromFile(new File(path));
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				intent.setDataAndType(uri, "image/jpeg");
				startActivity(intent);
			}
		}
	};
	// ���Ű��µ�ʱ��onShutter()���ص�
	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			if (tone == null)
				// ������ʾ�û�������
				tone = new ToneGenerator(AudioManager.STREAM_MUSIC,
						ToneGenerator.MAX_VOLUME);
			tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
		}
	};

	private String save(byte[] data) {
		String path = "/sdcard/" + System.currentTimeMillis() + ".jpg";
		try {
			// �ж�SD�����Ƿ����㹻�Ŀռ�
			String storage = Environment.getExternalStorageDirectory().toString();
			StatFs fs = new StatFs(storage);
			long available = fs.getAvailableBlocks() * fs.getBlockSize();
			if (available < data.length) {
				// �ռ䲻��ֱ�ӷ��ؿ�
				return null;
			}
			File file = new File(path);
			if (!file.exists())
				// �����ļ�
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

		// Sureface������ʱ�򣬴˷���������
		public void surfaceCreated(SurfaceHolder holder) {
			// ������ͷ�����Camera����
			camera = Camera.open();
			try {
				// ������ʾ
				camera.setPreviewDisplay(holder);
			} catch (IOException exception) {
				camera.release();
				camera = null;
			}
		}

		// Surface���ٵ�ʱ�򣬴˷���������
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			// �ͷ�Camera
			camera.release();
			camera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// �Ѿ����Surface��width��height������Camera�Ĳ���
			Camera.Parameters parameters = camera.getParameters();
			// parameters.setPreviewSize(w, h);
			parameters.setFocusMode("auto");
			camera.setParameters(parameters);
			// ��ʼԤ��
			camera.startPreview();
		}

	}

	public void media() {
		mediarecorder = new MediaRecorder();// ����mediarecorder����
		// ����¼����ƵԴΪCamera(���)
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// ����¼����ɺ���Ƶ�ķ�װ��ʽTHREE_GPPΪ3gp.MPEG_4Ϊmp4
		mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		// ����¼�Ƶ���Ƶ����h263 h264
		mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		// ������Ƶ¼�Ƶķֱ��ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴�
		mediarecorder.setVideoSize(176, 144);
		// ����¼�Ƶ���Ƶ֡�ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴�
		mediarecorder.setVideoFrameRate(20);
		// mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
		// ������Ƶ�ļ������·��
		mediarecorder.setOutputFile("/sdcard/love.mp4");
		try {
			// ׼��¼��
			mediarecorder.prepare();
			// ��ʼ¼��
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
