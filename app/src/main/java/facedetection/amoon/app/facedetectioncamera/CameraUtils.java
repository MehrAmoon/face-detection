package facedetection.amoon.app.facedetectioncamera;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nguyenbon on 7/7/15.
 */
public final class CameraUtils {

    private static final double ASPECT_TOLERANCE = 0.1;

    public static int findFrontFacingCameraID() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(i, info);
            if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static Camera.Size getOptimalPreviewSize(int displayOrientation,
                                                    int width,
                                                    int height,
                                                    Camera.Parameters parameters) {
        double targetRatio = (double) width / height;
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = height;

        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = (double) height / width;
        }

        // Try to find an size match aspect ratio and size

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        // Cannot find the one match the aspect ratio, ignore
        // the requirement

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;

            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return (optimalSize);
    }

    public static Camera.Size getBestAspectPreviewSize(int displayOrientation,
                                                       int width,
                                                       int height,
                                                       Camera.Parameters parameters) {
        return (getBestAspectPreviewSize(displayOrientation, width, height,
                parameters, 0.0d));
    }

    public static Camera.Size getBestAspectPreviewSize(int displayOrientation,
                                                       int width,
                                                       int height,
                                                       Camera.Parameters parameters,
                                                       double closeEnough) {
        double targetRatio = (double) width / height;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = (double) height / width;
        }

        List<Size> sizes = parameters.getSupportedPreviewSizes();

        Collections.sort(sizes,
                Collections.reverseOrder(new SizeComparator()));

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(ratio - targetRatio);
            }

            if (minDiff < closeEnough) {
                break;
            }
        }

        return (optimalSize);
    }

    private static class SizeComparator implements
            Comparator<Size> {
        @Override
        public int compare(Camera.Size lhs, Size rhs) {
            int left = lhs.width * lhs.height;
            int right = rhs.width * rhs.height;

            if (left < right) {
                return (-1);
            } else if (left > right) {
                return (1);
            }

            return (0);
        }
    }
}
