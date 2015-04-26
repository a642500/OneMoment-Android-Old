package co.yishun.onemoment.app.sync;

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.net.request.sync.Data;
import co.yishun.onemoment.app.net.request.sync.DeleteVideo;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.net.request.sync.GetVideoList;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.dao.Dao;
import com.koushikdutta.ion.Ion;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * <p>
 * Created by Carlos on 3/10/15.
 */
@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = LogUtil.makeTag(SyncAdapter.class);
    ContentResolver mContentResolver;

    public SyncAdapter(Context context) {
        super(context, true);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        LogUtil.i(TAG, "onPerformSync, account: " + account.name + ", Bundle: " + extras);
        //see:http://developer.android.com/training/sync-adapters/creating-sync-adapter.html

        new GetVideoList().with(getContext()).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
            } else if (result.getCode() == ErrorCode.SUCCESS) {
                sync(toMap(result.getDatas()));
            } else LogUtil.e(TAG, "get video list failed: " + result.getCode());
        });

    }

    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class)
    Dao<Moment, Integer> dao;


    private void sync(Map<Integer, Data> videosOnServer) {
        LogUtil.i(TAG, "video got, start sync");
        for (Moment moment : dao) {
            Integer key = Integer.parseInt(moment.getTime());
            Data video = videosOnServer.get(key);
            LogUtil.v(TAG, "sync iter: " + moment.toString());
            if (video != null) {
                LogUtil.v(TAG, "on server: " + video.toString());
                //if server has today moment
                if (video.getTimeStamp() > moment.getTimeStamp()) {
                    //if server is newer, download and delete older
                    LogUtil.v(TAG, "download: " + video.toString());
                    downloadVideo(video, moment);
                } else if (video.getTimeStamp() < moment.getTimeStamp()) {
                    //if local is newer, upload and delete older
                    LogUtil.v(TAG, "upload: " + video.toString());
                    uploadMoment(moment, video);
                } else {
                    LogUtil.v(TAG, "same, do nothing");
                }
                //the video sync ok, remove
                videosOnServer.remove(key);
            } else uploadMoment(moment, null);//server not have today moment, upload
        }
        for (Data data : videosOnServer.values())
            downloadVideo(data, null);//other unhandled video mean they need download
        syncDone();
    }

    /**
     * send broadcast to notify finishing syncing
     */
    private void syncDone() {
        Intent intent = new Intent(SYNC_BROADCAST);
        intent.putExtra(SYNC_BROADCAST_EXTRA_IS_CHANGED, isChange);
        LogUtil.i(TAG, "sync done, send a broadcast. isChange: " + isChange);
        getContext().sendBroadcast(intent);
    }

    /**
     * whether local data update while sync. if true, need to notify some ui update
     */
    boolean isChange = false;

    public static final String SYNC_BROADCAST = "co.yishun.onemoment.app.sync.done";
    public static final String SYNC_BROADCAST_EXTRA_IS_CHANGED = "is_changed";


    private UploadManager mUploadManager;

    private UploadManager getUploadManager() {
        if (mUploadManager == null) {
            mUploadManager = new UploadManager();
        }
        return mUploadManager;
    }

    private void uploadMoment(Moment moment, Data videoToDelete) {
        LogUtil.i(TAG, "upload a moment: " + moment.getPath());
        String qiNiuKey = getQiniuVideoFileName(moment);
        new GetToken().setFileName(qiNiuKey).with(getContext()).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
            } else if (result.getCode() != ErrorCode.SUCCESS) LogUtil.e(TAG, "get token failed: " + result.getCode());
            else
                getUploadManager().put(moment.getPath(),
                        qiNiuKey,
                        result.getData().getToken(),
                        (s, responseInfo, jsonObject) -> {
                            LogUtil.i(TAG, responseInfo.toString());
                            if (videoToDelete != null) deleteVideo(videoToDelete);
                            LogUtil.i(TAG, "a moment upload ok: " + moment.getPath());
                        },
                        new UploadOptions(null, Config.MIME_TYPE, true, null, null)
                );
        });
    }

    @Background void deleteVideo(@NonNull Data videoOnServer) {
        LogUtil.i(TAG, "delete a video: " + videoOnServer.getQiuniuKey());
        new DeleteVideo().setFileName(videoOnServer.getQiuniuKey()).with(getContext()).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
            } else if (result.getCode() != ErrorCode.SUCCESS)
                LogUtil.e(TAG, "delete video token failed: " + result.getCode());
        });

    }

    private boolean deleteMoment(@NonNull Moment moment) {
        LogUtil.i(TAG, "delete a moment: " + moment.getPath());
        File momentFile = moment.getFile();
        File thumb = new File(moment.getThumbPath());
        File thumbL = new File(moment.getLargeThumbPath());

        if (momentFile.exists()) momentFile.delete();
        if (thumb.exists()) thumb.delete();
        if (thumbL.exists()) thumbL.delete();

        try {
            return dao.delete(moment) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * download a video from server, then register in database.
     *
     * @param aVideoOnServer video to download.
     * @param momentOld      old local moment. It will be deleted if it isn't null.
     */
    private void downloadVideo(Data aVideoOnServer, @Nullable Moment momentOld) {
        LogUtil.i(TAG, "download a video: " + aVideoOnServer.getQiuniuKey());
        File fileTemp = CameraHelper.getOutputMediaFile(getContext(), aVideoOnServer);
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder().url(Config.getResourceUrl(getContext()) + aVideoOnServer.getQiuniuKey()).get().build();
//            Response response = client.newCall(request).execute();
//            response.body().

        Ion.with(getContext()).load(Config.getResourceUrl(getContext()) + aVideoOnServer.getQiuniuKey())
                .write(fileTemp).setCallback((e, result) -> {
            try {
                if (e != null) {
                    throw e;
                }
                File file = CameraHelper.getOutputMediaFile(getContext(), CameraHelper.Type.LOCAL, aVideoOnServer.getTimeStamp());
                if (momentOld != null && !deleteMoment(momentOld)) {
                    LogUtil.e(TAG, "delete old local moment failed: " + momentOld.getPath());
                }
                if (!fileTemp.renameTo(file)) {
                    LogUtil.e(TAG, "rename synced to local failed: " + fileTemp.getPath());
                } else {
                    String pathToThumb = CameraHelper.createThumbImage(getContext(), file.getPath());
                    String pathToLargeThumb = CameraHelper.createLargeThumbImage(getContext(), file.getPath());
                    dao.create(Moment.from(aVideoOnServer, file.getPath(), pathToThumb, pathToLargeThumb));
                    LogUtil.i(TAG, "a video download ok: " + aVideoOnServer.getQiuniuKey());
                    isChange = true;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

    }

    private String getQiniuVideoFileName(Moment moment) {
        String re = AccountHelper.getIdentityInfo(getContext()).get_id() + Config.URL_HYPHEN + moment.getTime() + Config.URL_HYPHEN + moment.getTimeStamp() + Config.VIDEO_FILE_SUFFIX;
        LogUtil.i(TAG, "qiniu filename: " + re);
        return re;
    }

    /**
     * convert array to HashMap
     * <p>
     * No generics is from Apache ArrayUtils, and generics version is from <a href="http://stackoverflow.com/questions/6416346/adding-generics-to-arrayutils-tomap">Stack Overflow</a>
     *
     * @param array
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(Object[] array) {
        if (array == null) {
            return null;
        }

        final Map<K, V> map = new HashMap<K, V>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            if (object instanceof Map.Entry) {
                Map.Entry<K, V> entry = (Map.Entry<K, V>) object;
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof Object[]) {
                Object[] entry = (Object[]) object;
                if (entry.length < 2) {
                    throw new IllegalArgumentException("Array element " + i
                            + ", '" + object + "', has a length less than 2");
                }
                map.put((K) entry[0], (V) entry[1]);
            } else {
                throw new IllegalArgumentException("Array element " + i + ", '"
                        + object + "', is neither of type Map.Entry nor an Array");
            }
        }
        return map;
    }
}
