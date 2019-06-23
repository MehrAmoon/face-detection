package facedetection.amoon.app.facedetectioncamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by M.Amoon on 6/22/2019.

using and customized bonnguyen.camerafacedetection library
        */

public class AndroidCameraActivity extends Activity implements SurfaceHolder.Callback {

    public static String TAG = "CameraFaceDetectionActivity";
    private final int MAX_WIDTH = 1000;
    private final int MIN_WIDTH = 700;
    private final int MAX_HEIGHT = 1800;
    private final int MIN_HEIGHT = 1200;
    private final int MAX_CENTER_X = 250;
    private final int MIN_CENTER_X = -250;
    private final int MAX_CENTER_Y = 250;
    private final int MIN_CENTER_Y = -250;
    private Camera mCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button capture;
    private boolean mIsTakingPicture = false;
    final int PIC_CROP = 1;

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.stopFaceDetection();
            }
            Bitmap croppedImg = Bitmap.createBitmap(rotationBinaryBitmap(arg0), 100,MAX_CENTER_Y,MIN_WIDTH+200, MIN_HEIGHT);

            byte[] rotatedBinary = bitmapToBin(croppedImg);
            File file = new File(getExternalFilesDir(null), "avatar.jpg");
            try {
                file.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            FileOutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(rotatedBinary);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Intent backIntent = new Intent();
            backIntent.putExtra("url", file.getPath());
            setResult(RESULT_OK, backIntent);
            mIsTakingPicture = false;
            finish();
        }
    };

    //when face detected capture button change to enable and rectangle color change
    Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (mIsTakingPicture) return;
            if (faces.length == 1 && checkFaceDetectIsValid(faces[0].rect)) {
                surfaceView.setBackgroundResource(R.drawable.face_detect_active);
                capture.setEnabled(true);
            } else {
                cancelTakePhoto();
            }

        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection_camera);
        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        capture = (Button) findViewById(R.id.captureImage);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsTakingPicture = true;
                if (mCamera != null) {
                    mCamera.takePicture(null,
                            null, myPictureCallback_JPG);
                }
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        startCamera();
        capture.bringToFront();
    }

    private void startCamera() {
        mCamera = Camera.open(CameraUtils.findFrontFacingCameraID());
        mCamera.setFaceDetectionListener(faceDetectionListener);
        mCamera.startFaceDetection();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        if (holder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int orentation = 90;
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if (display.getRotation() == Surface.ROTATION_0) {
                orentation = 90;
                mCamera.setDisplayOrientation(90);
            } else if (display.getRotation() == Surface.ROTATION_270) {
                orentation = 180;
                mCamera.setDisplayOrientation(180);
            }

            Camera.Parameters mParameters = mCamera.getParameters();
            Camera.Size bestSize = CameraUtils.getBestAspectPreviewSize(orentation, width, height, mParameters);

            mParameters.setPreviewSize(bestSize.width, bestSize.height);
            mCamera.setParameters(mParameters);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            Log.e("CustomCamera", "Could not preview the image.", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        cancelTakePhoto();
        destroyCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancelTakePhoto();
            destroyCamera();

            Intent backIntent = new Intent();
            setResult(RESULT_CANCELED, backIntent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //when face not inside the rectangle change enable of button and rectangle color
    private void cancelTakePhoto() {
        surfaceView.setBackgroundResource(R.drawable.face_detect_inactive);
        capture.setEnabled(false);
    }

    //rectangle to select face
    private boolean checkFaceDetectIsValid(Rect rect) {
        boolean isCenterX = rect.centerX() >= MIN_CENTER_X && rect.centerX() <= MAX_CENTER_X;
        boolean isCenterY = rect.centerY() >= MIN_CENTER_Y && rect.centerX() <= MAX_CENTER_Y;
        boolean isWidth = rect.width() >= MIN_WIDTH && rect.width() <= MAX_WIDTH;
        boolean isHeight = rect.height() >= MIN_HEIGHT && rect.height() <= MAX_HEIGHT;
        boolean isValid = isCenterX && isCenterY && isWidth && isHeight;
        return isValid;
    }

    //need it when croping
    private Bitmap rotationBinaryBitmap(byte[] data) {
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length, bitmap_options);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(CameraUtils.findFrontFacingCameraID(), info);
        return rotate(realImage, info.orientation);
    }

    //create bitman and use in bitmap rotation
    private Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private byte[] bitmapToBin(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTakePhoto();
        destroyCamera();

        Intent backIntent = new Intent();
        setResult(RESULT_CANCELED, backIntent);
        finish();
    }

    private void destroyCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.setFaceDetectionListener(null);
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


}
