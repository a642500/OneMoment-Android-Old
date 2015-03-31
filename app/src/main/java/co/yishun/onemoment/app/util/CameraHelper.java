/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.yishun.onemoment.app.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import co.yishun.onemoment.app.config.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Camera related utilities.
 */
public class CameraHelper {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static boolean isFrontCamera = false;
    private static final String TAG = LogUtil.makeTag(CameraHelper.class);

    /**
     * Iterate over supported camera preview sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param sizes        Supported camera preview sizes.
     * @param targetWidth  The width of the view.
     * @param targetHeight The height of the view.
     * @return Best match camera preview size to fit in the view.
     */
    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int targetWidth, int targetHeight) {
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minHeightDiff = Double.MAX_VALUE;
        double minWidthDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            LogUtil.v("iter height", "width: " + size.width + ", height: " + size.height);
            if (size.height >= targetHeight && Math.abs(size.height - targetHeight) <= minHeightDiff
                    && size.width >= targetWidth && Math.abs(size.width - targetWidth) <= minWidthDiff) {
                optimalSize = size;
                minHeightDiff = Math.abs(size.height - targetHeight);
                minWidthDiff = Math.abs(size.width - targetWidth);
            }
        }
        LogUtil.i("selected size", "width: " + optimalSize.width + ", height: " + optimalSize.height);
        return optimalSize;
    }

    /**
     * @return the default camera on the device. Return null if there is no camera on the device.
     */
    public static Camera getCameraInstance() {
        return isFrontCamera ? getDefaultFrontFacingCameraInstance() : getDefaultBackFacingCameraInstance();
    }

    public static void setFrontCamera(boolean isFront) {
        isFrontCamera = isFront;
    }

    public static boolean isFrontCamera() {
        return isFrontCamera;
    }


    /**
     * @return the default rear/back facing camera on the device. Returns null if camera is not
     * available.
     */
    public static Camera getDefaultBackFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * @return the default front facing camera on the device. Returns null if camera is not
     * available.
     */
    public static Camera getDefaultFrontFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }


    /**
     * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
     *                 or Camera.CameraInfo.CAMERA_FACING_BACK.
     * @return the default camera on the device. Returns null if camera is not available.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static Camera getDefaultCamera(int position) {
        // Find the total number of cameras available
        int mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);

            }
        }

        return null;
    }

    /**
     * Creates a media file in the {@code Environment.DIRECTORY_PICTURES} directory. The directory
     * is persistent and available to other applications like gallery.
     *
     * @param type Media type. Can be video or image.
     * @return A file object pointing to the newly created file.
     */
    public static File getOutputMediaFile(int type, Context context) {
        File mediaStorageDir = context.getDir(Config.VIDEO_STORE_DIR, Context.MODE_PRIVATE);

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            throw new IllegalStateException("unsupported type");
        }
        return mediaFile;
    }

    /**
     * Get Converted path from origin media file path.
     *
     * @param path to the media file
     * @return path to the converted file.
     */
    public static String getConvertedMediaFile(String path) {
        File file = new File(path);
        String s = file.getParentFile().toString() + "/CON_" + file.getName();
        LogUtil.i(TAG, s);
        return s;
    }

    @Deprecated
    public static String getThumbFilePath(String convertedFilePath) {
        File file = new File(convertedFilePath);
        String parent = file.getParentFile().getParent();
        String re = parent + Config.VIDEO_THUMB_STORE_DIR + getThumbFileName(convertedFilePath);
        return re;
    }

    public static String getThumbFileName(String videoPathOrThumbPath) {
        File file = new File(videoPathOrThumbPath);
        String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        return fileName + ".png";
    }

    public static String createThumbImage(Context context, String videoPath) throws IOException {
        File thumbStorageDir = context.getDir(Config.VIDEO_THUMB_STORE_DIR, Context.MODE_PRIVATE);
        File thumbFile = new File(thumbStorageDir.getPath() + File.separator + getThumbFileName(videoPath));

        if (thumbFile.exists()) thumbFile.delete();
        FileOutputStream fOut = new FileOutputStream(thumbFile);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
        return thumbFile.getPath();
    }

}
