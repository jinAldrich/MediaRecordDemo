package com.yujin.demo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import org.json.JSONObject;

public class FileAccessI implements Serializable {

	RandomAccessFile oSavedFile;
	long nPos;

	public FileAccessI() throws IOException {
		this("", 0);
	}

	public FileAccessI(String sName, long nPos) throws IOException {
		oSavedFile = new RandomAccessFile(sName, "rw");// ����һ����������ļ��࣬�ɶ�дģʽ
		this.nPos = nPos;
		oSavedFile.seek(nPos);
	}

	public synchronized int write(byte[] b, int nStart, int nLen) {
		int n = -1;
		try {
			oSavedFile.write(b, nStart, nLen);
			n = nLen;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}

	// ÿ�ζ�ȡ102400�ֽ�
	public synchronized Detail getContent(long nStart) {
		Detail detail = new Detail();
		detail.b = new byte[102400];
		try {
			oSavedFile.seek(nStart);
			detail.length = oSavedFile.read(detail.b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return detail;
	}

	public class Detail {

		public byte[] b;
		public int length;
	}

	// ��ȡ�ļ�����
	public long getFileLength() {
		Long length = 0l;
		try {
			length = oSavedFile.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return length;
	}
	
	
	/**
	 * ����ƵͼƬ����
	 * 
	 * @param mStr
	 * @return
	 * @throws Exception
	 */
	public static String f_uploadVedio(String mStr) throws Exception {
//		String mResult = Usual.mEmpty;
//		String fileType = Usual.mEmpty;
		String mResult = null;
		String fileType = null;
		int startPosL = 0;
		RandomAccessFile oSavedFile = null;
		JSONObject jsonObject = new JSONObject(mStr);
		String vedioJsonStr = jsonObject.getString("VEDIO");
		byte[] vedioBytes = null;
//		byte[] vedioBytes = Usual.f_fromBase64String(vedioJsonStr);
		startPosL = (Integer) jsonObject.get("start"); // ���տͻ��˵Ŀ�ʼλ��(�ļ���ȡ�����ֽڴ�С)
		fileType = (String) jsonObject.getString("filetype");
		String fileName = (String) jsonObject.getString("FileName");
		if (fileType.equals("picture")) {
			oSavedFile = new RandomAccessFile("E:\\" + fileName + ".jpg", "rw");
		} else if (fileType.equals("photo")) {
			oSavedFile = new RandomAccessFile("E:\\" + fileName + ".jpg", "rw");
		} else if (fileType.equals("voice")) {
			oSavedFile = new RandomAccessFile("E:\\" + fileName + ".mp3", "rw");
		} else if (fileType.equals("video")) {
			oSavedFile = new RandomAccessFile("E:\\" + fileName + ".mp4", "rw");
		}
		// ���ñ�־λ,��־�ļ��洢��λ��
		oSavedFile.seek(startPosL);
		oSavedFile.write(vedioBytes);
		oSavedFile.close();
		mResult = "000";
		return mResult;
	}
}