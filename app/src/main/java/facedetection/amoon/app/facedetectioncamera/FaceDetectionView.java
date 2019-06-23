package facedetection.amoon.app.facedetectioncamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Region;
import android.media.FaceDetector.Face;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by nguyenbon on 7/7/15.
 */
public class FaceDetectionView extends AppCompatImageView {
    public Face[] myFace;
    public int numberOfFaceDetected;
    private float myEyesDistance;

    public FaceDetectionView(Context context) {
        super(context);
    }

    public FaceDetectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(3);
        for (int i = 0; i < numberOfFaceDetected; i++) {
            Face face = myFace[i];
            PointF myMidPoint = new PointF();
            face.getMidPoint(myMidPoint);
            myEyesDistance = face.eyesDistance();
            //canvas.clipRect(0,0,50,50, Region.Op.DIFFERENCE);
            //canvas.drawRect(0,0,100,100, myPaint);
           canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2),
                    (int) (myMidPoint.y - myEyesDistance * 2),
                    (int) (myMidPoint.x + myEyesDistance * 2),
                    (int) (myMidPoint.y + myEyesDistance * 2), myPaint);
        }

       // super.onDraw(canvas);



    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
}
