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
	// ϵͳ��Ƶ�ļ�
	File viodFile;
	MediaRecorder mRecorder;
	// ��ʾ��Ƶ��SurfaceView
	SurfaceView sView;
	// ��¼�Ƿ����ڽ���¼��
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
		// stop��ť������
		stop.setEnabled(false);
		// ����Surface����Ҫά���Լ��Ļ�����
		sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// ���÷ֱ���
		sView.getHolder().setFixedSize(320, 280);
		// ���ø������������Ļ�Զ��ر�
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

		//����SD���Ƿ���ں�·����ش���
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
				Toast.makeText(this, "SD�������ڣ���忨��", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				// ����MediaPlayer����
				mRecorder = new MediaRecorder();
				mRecorder.reset();
				/*
				 * camera = Camera.open(); camera.unlock();
				 * camera.setDisplayOrientation(0); mRecorder.setCamera(camera);
				 */
				// ��������¼����Ƶ����Ƶ�ļ�
				viodFile = new File(PATH + "/myvideo.mp4");
				if (!viodFile.exists())
					viodFile.createNewFile();
				// ���ô���˷�ɼ�����
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				// ���ô�����ͷ�ɼ�ͼ��
				mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				// ������Ƶ����Ƶ�������ʽ
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				// ������Ƶ�ı����ʽ��
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				// ����ͼ������ʽ
				mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
				mRecorder.setOrientationHint(90);
				// mRecorder.setVideoSize(320, 280);
				// mRecorder.setVideoFrameRate(5);
				mRecorder.setOutputFile(viodFile.getAbsolutePath());
				// ָ��SurfaceView��Ԥ����Ƶ
				mRecorder.setPreviewDisplay(sView.getHolder().getSurface());
				mRecorder.prepare();
				// ��ʼ¼��
				mRecorder.start();
				// ��record��ť������
				record.setEnabled(false);
				// ��stop��ť����
				stop.setEnabled(true);
				isRecording = true;
				copyFile("myvideo_");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.stop:
			// �������¼��
			if (isRecording) {
				// ֹͣ¼��
				mRecorder.stop();
				// �ͷ���Դ
				mRecorder.release();
				mRecorder = null;
				// ��record��ť����
				record.setEnabled(true);
				// ��stop��ť������
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
				int len = (int)in.length();//ȡ���ļ����ȣ��ֽ�����
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
	* ��ʾ�������л�ʣ���ֽ��� 
	* @param in 
	*/ 
	private static void showAvailableBytes(InputStream in) {
		try {
			Log.i(TAG, "��ǰ�ֽ��������е��ֽ���Ϊ:" + in.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
}
