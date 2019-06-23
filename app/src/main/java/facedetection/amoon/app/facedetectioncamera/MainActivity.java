package facedetection.amoon.app.facedetectioncamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

/**
 * Created by M.Amoon on 6/22/2019.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CAMERA = 200;
    private FaceDetectionView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivPhoto = (FaceDetectionView) findViewById(R.id.ivPhoto);
        ivPhoto.setOnClickListener(this);

        //check camera permision and start camera intent
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.CAMERA};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Intent intent = new Intent(this, AndroidCameraActivity.class);
            startActivityForResult(intent, REQUEST_CAMERA);
        }

    }

    //return button when croped image show
    @Override
    public void onClick(View view) {

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.CAMERA};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            if (view == ivPhoto) {
                Intent intent = new Intent(this, AndroidCameraActivity.class);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }

    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data.hasExtra("url")) {
                String photoPath = data.getStringExtra("url");
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
                ivPhoto.setImageBitmap(bitmap);
            }
        }
    }

    //checking permision granted
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
