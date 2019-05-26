package com.aananth.nonprofit.ocreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener,
        Camera.PictureCallback, Camera.ShutterCallback{
    final String TAG = "DBG_" + this.getClass().getName();
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    CameraEngine mCameraEngine;

    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSurfaceView = findViewById(R.id.camera_frame);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceView.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCameraEngine != null && mCameraEngine.isOn()) {
            mCameraEngine.stop();
        }

        mSurfaceHolder.removeCallback(this);
    }


    public String detectText(Bitmap bitmap) {
        Context context = getApplicationContext();

        TessDataManager.initTessTrainedData(context);
        String path = TessDataManager.getTrainedDataPath();
        TessBaseAPI tessBaseAPI = new TessBaseAPI();


        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(path, "eng"); //Init the Tess with the trained data file, with english language

        //For example if we want to only detect numbers
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");


        tessBaseAPI.setImage(bitmap);

        String text = tessBaseAPI.getUTF8Text();

        Log.d(TAG, "Got data: " + text);
        tessBaseAPI.end();

        return text;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCameraEngine == null) {
            mCameraEngine = CameraEngine.New(mSurfaceHolder);
        }

        if (!mCameraEngine.isOn()) {
            mCameraEngine.start();
        }
    }


    @Override
    public void onClick(View v) {
        if (mCameraEngine.isOn()) {
            mCameraEngine.requestFocus();
            try {
                sleep(1000);
            }
            catch (Exception e) {
                Log.d(TAG, "Timer exception!");
            }
            mCameraEngine.takeShot(this, this, this);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data == null) {
            Log.d(TAG, "Got a null picture!");
            return;
        }

        Camera.Parameters params = camera.getParameters(); // mCamera is a Camera object
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        if (sizes.isEmpty()) {
            Log.d(TAG, "Couldn't get camera parameters!");
            return;
        }
        int width = (int) sizes.get(0).width;
        int height = (int) sizes.get(0).height;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(data));
        detectText(bmp);
    }

    @Override
    public void onShutter() {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
