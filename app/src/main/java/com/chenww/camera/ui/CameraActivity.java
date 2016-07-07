package com.chenww.camera.ui;
/**
 * �������յ�Activity
 * @author Chenww
 */
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends Activity {

	private SurfaceView mySurfaceView;
	private SurfaceHolder myHolder;
	private Camera myCamera;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// ��title 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        // ȫ�� 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        //���ò���
        setContentView(R.layout.activity_camera);
        
        Log.d("Demo", "oncreate");
        
        //��ʼ��surface
        initSurface();
        
        //����ÿ��߳̽������գ���ΪActivity��δ��ȫ��ʾ��ʱ�����޷��������յģ�SurfaceView��������ʾ
		new Thread(new Runnable() {
			@Override
			public void run() {
				//��ʼ��camera���Խ�����
		        initCamera();
			}
		}).start();
        
	}
	
	//��ʼ��surface
	@SuppressWarnings("deprecation")
	private void initSurface()
	{
		//��ʼ��surfaceview
		mySurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
		
		//��ʼ��surfaceholder
		myHolder = mySurfaceView.getHolder();
		myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
	}
	
	//��ʼ������ͷ
	private void initCamera() {
		
		//�����������ͷ
		if(checkCameraHardware(getApplicationContext()))
		{
			//��ȡ����ͷ����ѡǰ�ã���ǰ��ѡ���ã�
			if(openFacingFrontCamera())
			{
				Log.d("Demo", "openCameraSuccess");
				//���жԽ�
				autoFocus();
			}
			else {
				Log.d("Demo", "openCameraFailed");
			}
			
		}
	}
	
	//�Խ�������
	private void autoFocus() {
		
		try {
			//��Ϊ��������ͷ��Ҫʱ�䣬�������߳�˯����
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//�Զ��Խ�
		myCamera.autoFocus(myAutoFocus);
		
		//�Խ�������
		myCamera.takePicture(null, null, myPicCallback);
	}



	//�ж��Ƿ��������ͷ
	private boolean checkCameraHardware(Context context) {
		
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // �豸��������ͷ
            return true;
        } else {
            // �豸����������ͷ
            return false;
        }
        
    }
	
	//�õ���������ͷ
    private boolean openFacingFrontCamera() {
    	
    	//���Կ���ǰ������ͷ
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					Log.d("Demo", "tryToOpenCamera");
					myCamera = Camera.open(camIdx);
				} catch (RuntimeException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		
		//�������ǰ��ʧ�ܣ���ǰ�ã���������
		if (myCamera == null) {
			for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					try {
						myCamera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						return false;
					}
				}
			}
		}

		try {
			//�����myCameraΪ�Ѿ���ʼ����Camera����
			myCamera.setPreviewDisplay(myHolder);
		} catch (IOException e) {
			e.printStackTrace();
			myCamera.stopPreview();
	    	myCamera.release();
	    	myCamera = null;
		}
		
		myCamera.startPreview();
		
		return true;
	}
    
	//�Զ��Խ��ص�����(��ʵ��)
	private AutoFocusCallback myAutoFocus = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
		}
	};
	
	//���ճɹ��ص�����
	private PictureCallback myPicCallback = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			//������պ�ر�Activity
			CameraActivity.this.finish();
			
			//���õ�����Ƭ����270����ת��ʹ����ֱ
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			Matrix matrix = new Matrix();
			matrix.preRotate(270);
			bitmap = Bitmap.createBitmap(bitmap ,0,0, bitmap .getWidth(), bitmap .getHeight(),matrix,true);
			
			//����������ͼƬ�ļ�g
			String img_name=getIntent().getStringExtra("img_name");
            Log.d("Demo","hello"+img_name);
			File pictureFile = new File(getDir(), img_name+".jpg");
	        try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
	            fos.close();
	        } catch (Exception error) {
	        	Toast.makeText(CameraActivity.this, "false", Toast.LENGTH_SHORT).show();;
	        	Log.d("Demo", "������Ƭʧ��" + error.toString());
	        	error.printStackTrace();
	        	myCamera.stopPreview();
		    	myCamera.release();
		    	myCamera = null;
	        }
	        
	    	Log.d("Demo", "��ȡ��Ƭ�ɹ�");
	    	Toast.makeText(CameraActivity.this, "Success", Toast.LENGTH_SHORT).show();;
			myCamera.stopPreview();
			myCamera.release();
			myCamera = null;
		}
	};
	
	//��ȡ�ļ���
	private File getDir()
	{
		//�õ�SD����Ŀ¼
		File dir = Environment.getExternalStorageDirectory();
		
		if (dir.exists()) {
			return dir;
		}
		else {
			dir.mkdirs();
			return dir;
		}
	}
}
