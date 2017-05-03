package com.lisn.dsvideorecord;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.icu.text.AlphabeticIndex;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private Button recordbutton;
    private Button stopbutton;
    private RelativeLayout layout;
    private Camera myCamera;
    private SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.getInstance().init(getApplicationContext());
        initScreen();
        setContentView(R.layout.activity_main);
        initView();
        initCamera();
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.camera_show_view);
        recordbutton = (Button) findViewById(R.id.recordbutton);
        stopbutton = (Button) findViewById(R.id.stopbutton);
        layout = (RelativeLayout) findViewById(R.id.layout);

        recordbutton.setOnClickListener(this);
        stopbutton.setOnClickListener(this);

        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(1280, 720);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mSurfaceHolder = holder;
//                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurfaceHolder = null;
            }
        });
    }

    /**
     * 找前置摄像头,没有则返回-1
     *
     * @return cameraId
     */
    private int findFrontFacingCamera() {
        int cameraId = -1;
        //获取摄像头个数
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    /**
     * 找后置摄像头,没有则返回-1
     *
     * @return cameraId
     */
    private int findBackFacingCamera() {
        int cameraId = -1;
        //获取摄像头个数
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    //检查设备是否有摄像头
    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public void scan(View v){
//        Toast.makeText(this, "---="+findFrontFacingCamera(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "---="+findBackFacingCamera(), Toast.LENGTH_SHORT).show();
    }

    //初始化Camera设置
    public void initCamera() {
        if (myCamera == null ) {
            myCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); //前置摄像头
//            myCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); //后置摄像头
            Log.e("-----", "camera.open");
        }
        if (myCamera != null ) {
            try {
                Camera.Parameters myParameters = myCamera.getParameters();
//                myParameters.setPreviewSize(176, 144);
//                //设置对焦模式
//                myParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//前置摄像头无法自动对焦
                myCamera.setParameters(myParameters);
                myCamera.setPreviewDisplay(mSurfaceHolder);

                myCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("----", "initCamera: "+e );
                Toast.makeText(MainActivity.this, "初始化相机错误"+e,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recordbutton:
                Record();
                break;
            case R.id.stopbutton:
                Stop();
                break;
        }
    }

    private void Stop() {
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.lock();
            myCamera.release();
            myCamera = null;
        }
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private MediaRecorder mediaRecorder;

    private void Record() {
        try {
            if (mediaRecorder == null) {
                File videoFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".mp4");
                mediaRecorder = new MediaRecorder();
                myCamera.unlock();
                mediaRecorder.setCamera(myCamera);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                    Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setVideoSize(320, 240);
//                mediaRecorder.setVideoSize(1920, 1080);
                mediaRecorder.setVideoFrameRate(5);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
//                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
//                mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
                mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

                mediaRecorder.setOrientationHint(270);
            }
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //初始化屏幕设置
    public void initScreen() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        // 设置横屏显示
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }
}
