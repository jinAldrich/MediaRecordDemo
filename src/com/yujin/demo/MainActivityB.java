package com.yujin.demo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * class name：VideoCameraActivity<BR>
 * class description：CATCH THE VIDEODATA SEND TO RED5<BR>
 * PS： <BR>
 * 
 * @version 1.00 2011/11/05
 * @author CODYY)peijiangping
 */
public class MainActivityB extends Activity implements
		SurfaceHolder.Callback, MediaRecorder.OnErrorListener,
		MediaRecorder.OnInfoListener {
	private static final int mVideoEncoder = MediaRecorder.VideoEncoder.H264;
	private LocalSocket receiver, sender;
	private LocalServerSocket lss;
	private MediaRecorder mMediaRecorder = null;
	private boolean mMediaRecorderRecording = false;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	private Thread t;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		mSurfaceView = (SurfaceView) this.findViewById(R.id.dView);
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceView.setVisibility(View.VISIBLE);
		try {
			receiver = new LocalSocket();
			lss = new LocalServerSocket("VideoCamera");
			receiver.connect(new LocalSocketAddress("VideoCamera"));
			receiver.setReceiveBufferSize(500000);
			receiver.setSendBufferSize(500000);
			sender = lss.accept();
			sender.setReceiveBufferSize(500000);
			sender.setSendBufferSize(500000);
		} catch (IOException e) {
			finish();
			return;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mMediaRecorderRecording) {
			stopVideoRecording();
			try {
				lss.close();
				receiver.close();
				sender.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		finish();
	}

	private void stopVideoRecording() {
		System.out.println("stopVideoRecording");
		if (mMediaRecorderRecording || mMediaRecorder != null) {
			if (t != null)
				t.interrupt();
			releaseMediaRecorder();
		}
	}

	private void startVideoRecording() {
		(t = new Thread() {
			public void run() {
				int frame_size = 20000;
				byte[] buffer = new byte[1024 * 64];
				int num, number = 0;
				InputStream fis = null;
				try {
					fis = receiver.getInputStream();
				} catch (IOException e1) {
					return;
				}
				number = 0;
				releaseMediaRecorder();
				while (true) {
					System.out.println("ok");
					try {
						num = fis.read(buffer, number, frame_size);
						number += num;
						if (num < frame_size) {
							System.out.println("recoend break");
							break;
						}
					} catch (IOException e) {
						System.out.println("exception break");
						break;
					}
				}
				initializeVideo();
				number = 0;
				Consumer consumer = new Publisher();// Publisher继承了Consumer
				Thread consumerThread = new Thread((Runnable) consumer);
				consumer.setRecording(true);// 设置线程状态;
				consumerThread.start();// 开始发布数据流
				DataInputStream dis = new DataInputStream(fis);
				try {
					dis.read(buffer, 0, 32);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				byte[] aa = { 0x01, 0x42, (byte) 0x80, 0x0A, (byte) 0xFF,
						(byte) 0xE1, 0x00, 0x12, 0x67, 0x42, (byte) 0x80, 0x0A,
						(byte) 0xE9, 0x02, (byte) 0xC1, 0x29, 0x08, 0x00, 0x00,
						0x1F, 0x40, 0x00, 0x04, (byte) 0xE2, 0x00, 0x20, 0x01,
						0x00, 0x04, 0x68, (byte) 0xCE, 0x3C, (byte) 0x80 };
				consumer.putData(System.currentTimeMillis(), aa, 33);
				while (true) {
					try {
						int h264length = dis.readInt();
						number = 0;
						while (number < h264length) {
							int lost = h264length - number;
							num = fis.read(buffer, 0, frame_size < lost ? frame_size : lost);
							number += num;
							consumer.putData(System.currentTimeMillis(),
									buffer, num);
						}
					} catch (IOException e) {
						break;
					}
				}
				consumer.setRecording(false);// 设置线程状态;
			}
		}).start();
	}

	private boolean initializeVideo() {
		System.out.println("initializeVideo");
		if (mSurfaceHolder == null)
			return false;
		mMediaRecorderRecording = true;
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setVideoFrameRate(20);
		mMediaRecorder.setVideoSize(352, 288);
		mMediaRecorder.setVideoEncoder(mVideoEncoder);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setMaxDuration(0);
		mMediaRecorder.setMaxFileSize(0);
		mMediaRecorder.setOutputFile(sender.getFileDescriptor());
		try {
			mMediaRecorder.setOnInfoListener(this);
			mMediaRecorder.setOnErrorListener(this);
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IOException exception) {
			releaseMediaRecorder();
			finish();
			return false;
		}
		return true;
	}

	private void releaseMediaRecorder() {
		System.out.println("Releasing media recorder.");
		if (mMediaRecorder != null) {
			if (mMediaRecorderRecording) {
				try {
					mMediaRecorder.setOnErrorListener(null);
					mMediaRecorder.setOnInfoListener(null);
					mMediaRecorder.stop();
				} catch (RuntimeException e) {
					System.out.println("stop fail: " + e.getMessage());
				}
				mMediaRecorderRecording = false;
			}
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		System.out.println("surfaceChanged");
		mSurfaceHolder = holder;
		if (!mMediaRecorderRecording) {
			initializeVideo();
			startVideoRecording();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("surfaceCreated");
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("surfaceDestroyed");
		mSurfaceView = null;
		mSurfaceHolder = null;
		mMediaRecorder = null;
		if (t != null) {
			t.interrupt();
		}
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		switch (what) {
		case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
			System.out.println("MEDIA_RECORDER_INFO_UNKNOWN");
			break;
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
			System.out.println("MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
			break;
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
			System.out.println("MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED");
			break;
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
			System.out.println("MEDIA_RECORDER_ERROR_UNKNOWN");
			finish();
		}
	}

}