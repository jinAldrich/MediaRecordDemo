package com.yujin.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public static final String TAG = "yujin";
	Button record, stop;
	// 系统视频文件
	File viodFile;
	MediaRecorder mRecorder;
	// 显示视频的SurfaceView
	SurfaceView sView;
	// 记录是否正在进行录制
	boolean isRecording = false;
	Camera camera;
	public static String PATH = null;
	public static long MAX_LENGTH = 1 * 1024 * 1024; //10M
	public static RandomAccessFile in = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(TAG, "---onCreate---");
		record = (Button) findViewById(R.id.record);
		stop = (Button) findViewById(R.id.stop);
		sView = (SurfaceView) findViewById(R.id.dView);
		// stop按钮不可用
		stop.setEnabled(false);
		// 设置Surface不需要维护自己的缓冲区
		sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 设置分辨率
		sView.getHolder().setFixedSize(320, 280);
		// 设置该组件不会让屏幕自动关闭
		sView.getHolder().setKeepScreenOn(true);
		record.setOnClickListener(this);
		stop.setOnClickListener(this);
		try {
			PATH = Environment.getExternalStorageDirectory().getCanonicalFile().toString();
			in = new RandomAccessFile(PATH + "/myvideo.mp4", "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//测试SD卡是否存在和路径相关代码
		Log.i(TAG, "Environment.getExternalStorageState(): " + Environment.getExternalStorageState());
		Log.i(TAG, "Environment.getExternalStorageDirectory(): " + Environment.getExternalStorageDirectory());
		try {
			Log.i(TAG, "getCanonicalFile(): " + Environment.getExternalStorageDirectory().getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "Environment.getDataDirectory(): " + Environment.getDataDirectory());
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {

		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.record:
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Toast.makeText(this, "SD卡不存在，请插卡！", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				// 创建MediaPlayer对象
				mRecorder = new MediaRecorder();
				mRecorder.reset();
				/*
				 * camera = Camera.open(); camera.unlock();
				 * camera.setDisplayOrientation(0); mRecorder.setCamera(camera);
				 */
				// 创建保存录制视频的视频文件
				viodFile = new File(PATH + "/myvideo.mp4");
				if (!viodFile.exists())
					viodFile.createNewFile();
				// 设置从麦克风采集声音
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				// 设置从摄像头采集图像
				mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				// 设置视频、音频的输出格式
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				// 设置音频的编码格式、
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				// 设置图像编码格式
				mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
				mRecorder.setOrientationHint(90);
				// mRecorder.setVideoSize(320, 280);
				// mRecorder.setVideoFrameRate(5);
				mRecorder.setOutputFile(viodFile.getAbsolutePath());
				// 指定SurfaceView来预览视频
				mRecorder.setPreviewDisplay(sView.getHolder().getSurface());
				mRecorder.prepare();
				// 开始录制
				mRecorder.start();
				// 让record按钮不可用
				record.setEnabled(false);
				// 让stop按钮可用
				stop.setEnabled(true);
				isRecording = true;
				copyFile("myvideo_");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.stop:
			// 如果正在录制
			if (isRecording) {
				// 停止录制
				mRecorder.stop();
				// 释放资源
				mRecorder.release();
				mRecorder = null;
				// 让record按钮可用
				record.setEnabled(true);
				// 让stop按钮不可用
				stop.setEnabled(false);
			}
			break;
		default:
			break;
		}
	
	}
	
	static int i = 1;
	static long filePointer = 0;
	public void copyFile(String name) {
		Log.i(TAG, "---copyFile---");
		Log.i(TAG, "---0---");
		try {
			Log.i(TAG, "---1---");
			in.seek(filePointer);
			Log.i(TAG, "---2---");
			RandomAccessFile out = null;
			Log.i(TAG, "---3---");
			File outFile = new File(PATH + "/" + name + (i++) + ".mp4");
			Log.i(TAG, "---4---");
			MediaCodec mMediaCodec = null;
			Log.i(TAG, "outFile.length(): " + outFile.length());
			while(outFile.length() > MAX_LENGTH) {
				Log.i(TAG, "start copy...." + i);
				out = new RandomAccessFile(PATH + "/myvideo " + (i++) + ".mp4", "rw");
				int len = (int)in.length();//取得文件长度（字节数）
				byte[] b= new byte[len];
				in.read(b);
				filePointer = in.getFilePointer();
				out.write(b);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "---FileNotFoundException---");
			e.printStackTrace();
		} catch (IOException e) {
			Log.i(TAG, "---IOException---");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 
	* 显示输入流中还剩的字节数 
	* @param in 
	*/ 
	private static void showAvailableBytes(InputStream in) {
		try {
			Log.i(TAG, "当前字节输入流中的字节数为:" + in.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
}
