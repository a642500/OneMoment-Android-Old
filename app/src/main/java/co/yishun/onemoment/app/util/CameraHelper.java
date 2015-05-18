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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.sync.Data;

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
    private static final String TAG = LogUtil.makeTag(CameraHelper.class);
    private static boolean isFrontCamera = false;

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
//        double minHeightDiff = Double.MAX_VALUE;
//        double minWidthDiff = Double.MAX_VALUE;

        double ratio = ((double) targetWidth) / targetHeight;
        LogUtil.i(TAG, "target height: " + targetHeight + ", target ratio: " + ratio);
        double minRatioDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            if (size.height < targetHeight || size.width < targetWidth) continue;
            //select whose ratio is the most close
            double sizeRatio = ((float) size.width) / size.height;
            double ratioDiff = Math.abs(sizeRatio - ratio);
            if (ratioDiff < minRatioDiff) {
                minRatioDiff = ratioDiff;
                optimalSize = size;
            } else if (ratioDiff == minRatioDiff && size.height > targetHeight && size.height < optimalSize.height) {
                //if ratio is the same, select whose height is the most close
                optimalSize = size;
            }
            LogUtil.v("iter height", "width: " + size.width + ", height: " + size.height + ", ratio: " + sizeRatio);
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

    public static boolean isFrontCamera() {
        return isFrontCamera;
    }

    public static void setFrontCamera(boolean isFront) {
        isFrontCamera = isFront;
    }

    public static void releaseCamera(Camera camera) {
        camera.release();
    }

    /**
     * @return the default rear/back facing camera on the device. Returns null if camera is not
     * available.
     */
    private static Camera getDefaultBackFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * @return the default front facing camera on the device. Returns null if camera is not
     * available.
     */
    private static Camera getDefaultFrontFacingCameraInstance() {
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
        try {
            for (int i = 0; i < mNumberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == position) {
                    return Camera.open(i);
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e(TAG, "Fail to connect to camera service", e);
        }
        return null;
    }

    public static String getOutputMediaPath(Context context, Type type, @Nullable Long unixTimestamp) {
        return getOutputMediaFile(context, type, unixTimestamp).getPath();
    }

    public static File getOutputMediaFile(Context context, Type type, @Nullable Long timestamp) {
        File mediaStorageDir = context.getDir(Config.VIDEO_STORE_DIR, Context.MODE_PRIVATE);
        LogUtil.i(TAG, "timestamp: " + timestamp);
        String time = new SimpleDateFormat(Config.TIME_FORMAT).format(timestamp == null ? new Date() : timestamp * 1000);
        LogUtil.i(TAG, "formatted time: " + time);
        return new File(mediaStorageDir.getPath() + File.separator + type.getPrefix(context) + Config.URL_HYPHEN + time + Config.URL_HYPHEN + timestamp + type.getSuffix());
    }

    public static File getOutputMediaDir(Context context) {
        return context.getDir(Config.VIDEO_STORE_DIR, Context.MODE_PRIVATE);
    }

    public static File getOutputMediaFile(Context context, Type type, @NonNull File file) {
        return getOutputMediaFile(context, type, parseTimeStamp(file));
    }

    /**
     * Return supposing name of synced type of a video on server
     *
     * @param context
     * @param syncedVideo
     * @return
     */
    public static File getOutputMediaFile(Context context, Data syncedVideo) {
        return getOutputMediaFile(context, Type.SYNCED, syncedVideo.getTimeStamp());
    }

    /**
     * return media type by file path.
     * <p>
     * You must ensure the file is one of {@link Type}. Otherwise it may cause wrong result.
     *
     * @param filePath of the media file
     * @return type of the media file
     */
    public static Type whatTypeOf(String filePath) {
        File file = new File(filePath);
        Type type = null;
        if (file.getName().length() > 3) {
            String prefix = file.getName().substring(0, 3);
            switch (prefix) {
                case "LOC":
                    type = Type.LOCAL;
                    break;
                case "VID":
                    type = Type.RECORDED;
                    break;
                case "LAT":
                    type = Type.LARGE_THUMB;
                    break;
                case "MIT":
                    type = Type.MICRO_THUMB;
                    break;
                default:
                    type = Type.SYNCED;
                    break;
            }
        }
        return type;
    }

    public static long parseTimeStamp(File file) {
        return parseTimeStamp(file.getPath());
    }

    public static long parseTimeStamp(String pathOrFileName) {
        return Long.parseLong(pathOrFileName.substring(pathOrFileName.lastIndexOf(Config.URL_HYPHEN) + 1, pathOrFileName.lastIndexOf(".")));
    }

    /**
     * Create full screen thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    public static String createLargeThumbImage(Context context, String videoPath) throws IOException {
        return createThumbImage(context, videoPath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
    }

    /**
     * Create micro thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    public static String createThumbImage(Context context, String videoPath) throws IOException {
        return createThumbImage(context, videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
    }

    /**
     * Create target kind thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    private static String createThumbImage(Context context, String videoPath, int kind) throws IOException {
        File thumbFile = getOutputMediaFile(context, kind == MediaStore.Images.Thumbnails.FULL_SCREEN_KIND ? Type.LARGE_THUMB : Type.MICRO_THUMB, new File(videoPath));
        Log.i(TAG, "create thumb image: " + thumbFile.getPath());
        if (thumbFile.exists()) thumbFile.delete();
        FileOutputStream fOut = new FileOutputStream(thumbFile);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
        return thumbFile.getPath();
    }

    public enum Type {
        SYNCED {
            @Override
            public String getPrefix(Context context) {
                return AccountHelper.getIdentityInfo(context).get_id();
            }

            @Override
            public String getSuffix() {
                return Config.VIDEO_FILE_SUFFIX;
            }
        },
        RECORDED {
            @Override
            public String getPrefix(Context context) {
                return "VID";
            }

            @Override
            public String getSuffix() {
                return Config.VIDEO_FILE_SUFFIX;
            }
        },
        LOCAL {
            @Override
            public String getPrefix(Context context) {
                return "LOC";
            }

            @Override
            public String getSuffix() {
                return Config.VIDEO_FILE_SUFFIX;
            }
        },
        LARGE_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "LAT";
            }

            @Override
            public String getSuffix() {
                return Config.THUMB_FILE_SUFFIX;
            }
        },
        MICRO_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "MIT";
            }

            @Override
            public String getSuffix() {
                return Config.THUMB_FILE_SUFFIX;
            }
        };

        public abstract String getPrefix(Context context);

        public abstract String getSuffix();
    }

}
