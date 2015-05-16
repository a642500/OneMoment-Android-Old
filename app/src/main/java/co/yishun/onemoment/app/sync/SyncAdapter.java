package co.yishun.onemoment.app.sync;

import android.accounts.Account;
import android.content.*;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.data.Contract;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.net.request.sync.Data;
import co.yishun.onemoment.app.net.request.sync.DeleteVideo;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.net.request.sync.GetVideoList;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.koushikdutta.ion.Ion;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.utils.Etag;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SystemService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * <p>
 * Created by Carlos on 3/10/15.
 */
@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String BUNLDE_IGNORE_NETWORK = "boolean_ignore_network";
    public static final String SYNC_BROADCAST_DONE = "co.yishun.onemoment.app.sync.done";
    public static final String SYNC_BROADCAST_UPDATE_UPLOAD = "co.yishun.onemoment.app.sync.update.upload";
    public static final String SYNC_BROADCAST_UPDATE_DOWNLOAD = "co.yishun.onemoment.app.sync.update.download";
    public static final String SYNC_BROADCAST_UPDATE_RECOVER = "co.yishun.onemoment.app.sync.update.recover";
    public static final String SYNC_BROADCAST_EXTRA_IS_UPLOAD_CHANGED = "is_upload_changed";
    public static final String SYNC_BROADCAST_EXTRA_IS_DOWNLOAD_CHANGED = "is_download_changed";
    public static final String SYNC_BROADCAST_EXTRA_IS_SUCCESS = "is_success";
    public static final String SYNC_BROADCAST_EXTRA_THIS_PROGRESS = "int_this_progress";
    public static final String SYNC_BROADCAST_EXTRA_TYPE_PROGRESS = "int_type_progress";
    public static final String SYNC_BROADCAST_EXTRA_ALL_PROGRESS = "int_all_progress";
    public static final int PROGRESS_NOT_AVAILABLE = -1;
    public static final int PROGRESS_ERROR = -2;
    private static final String TAG = LogUtil.makeTag(SyncAdapter.class);
    ContentResolver mContentResolver;
    @SystemService
    ConnectivityManager connectivityManager;
    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class)
    Dao<Moment, Integer> dao;
    int databaseNum = 0;
    /**
     * whether local data update while sync. if true, need to notify some ui update
     */
    boolean isUploadChanged = false;
    boolean isDownloadChanged = false;
    private UploadManager mUploadManager;

    public SyncAdapter(Context context) {
        super(context, true);
        mContentResolver = context.getContentResolver();
    }

    /**
     * check moment file integrity.
     */
    public static void checkAndSolveBadMoment(@NonNull Moment moment, Context context, OnCheckedListener listener) throws SQLException {
        Dao<Moment, Integer> dao = OpenHelperManager.getHelper(context, MomentDatabaseHelper.class).getDao(Moment.class);
        try {
            if (!repairVideo(dao, moment, context, listener)) {
                listener.onMomentDelete(moment);
                return;
            }
            if (!new File(moment.getLargeThumbPath()).exists()) {
                listener.onMomentStartRepairing(moment);
                String laP = CameraHelper.createLargeThumbImage(context, moment.getPath());
                moment.setPath(laP);
                LogUtil.e(TAG, "repair moment large thumb");
                dao.update(moment);
            }
            if (!new File(moment.getThumbPath()).exists()) {
                listener.onMomentStartRepairing(moment);
                String p = CameraHelper.createThumbImage(context, moment.getPath());
                moment.setPath(p);
                LogUtil.e(TAG, "repair moment thumb");
                dao.update(moment);
            }
            listener.onMomentOk(moment);
        } catch (IOException e) {
            e.printStackTrace();
            dao.delete(moment);
            LogUtil.e(TAG, "repair moment failed: IOException");
            listener.onMomentDelete(moment);
        }
    }

    private static boolean repairVideo(@NonNull Dao<Moment, Integer> dao, @NonNull Moment moment, Context context, OnCheckedListener listener) throws SQLException {
        File video = new File(moment.getPath());
        if (!video.exists()) {
            listener.onMomentStartRepairing(moment);
            if (moment.isPublic()) {
                CameraHelper.Type type = CameraHelper.whatTypeOf(video.getPath());
                File mayVideo = CameraHelper.getOutputMediaFile(context, CameraHelper.Type.LOCAL, video);
                if (type == CameraHelper.Type.SYNCED && mayVideo.exists()) {
                    //repair public path video is wrong recorded with private path
                    moment.setPath(mayVideo.getPath());
                    dao.update(moment);
                    LogUtil.d(TAG, "repair a moment, which is public but have wrong private path.");
                    return true;
                } else {
                    dao.delete(moment);
                    LogUtil.e(TAG, "unable repair a moment, which is public but have wrong private path.");
                    return false;
                }
            } else {
                //moment is private
                if (AccountHelper.isLogin(context)) {
                    CameraHelper.Type type = CameraHelper.whatTypeOf(video.getPath());
                    File mayVideo = CameraHelper.getOutputMediaFile(context, CameraHelper.Type.SYNCED, video);
                    if (type == CameraHelper.Type.LOCAL && mayVideo.exists()) {
                        moment.setPath(mayVideo.getPath());
                        dao.update(moment);
                        LogUtil.d(TAG, "repair a moment, which is private but have wrong public path.");
                        return true;
                    } else {
                        //if login, try repair by download from server
                        try {
                            File downloaded = Ion.with(context).load(Config.getResourceUrl(context) + getQiniuVideoFileName(context, moment)).write(mayVideo).get();
                            if (downloaded.exists()) {
                                String pathToThumb = CameraHelper.createThumbImage(context, downloaded.getPath());
                                String pathToLargeThumb = CameraHelper.createLargeThumbImage(context, downloaded.getPath());
                                moment.setPath(downloaded.getPath());
                                moment.setThumbPath(pathToThumb);
                                moment.setLargeThumbPath(pathToLargeThumb);
                                dao.update(moment);
                                return true;
                            } else throw new Exception("download failed");
                        } catch (Exception e) {
                            e.printStackTrace();
                            dao.delete(moment);
                            return false;
                        }
                    }
                } else {
                    Log.e(TAG, "assert failed! Check bad private moment when not log in, you must avoid read private when not login");
                    dao.delete(moment);
                    return false;
                }
            }
        } else return true;
    }

    private static String getQiniuVideoFileName(Context context, Moment moment) {
        String re = AccountHelper.getIdentityInfo(context).get_id() + Config.URL_HYPHEN + moment.getTime() + Config.URL_HYPHEN + moment.getTimeStamp() + Config.VIDEO_FILE_SUFFIX;
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

    /**
     * To execute sync. When sync end, it will call {@link #syncDone(boolean)} to send broadcast notify sync process ending. And in syncing process, it calls {@link #syncUpdate(UpdateType, int, int, int)} to broadcast progress.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        LogUtil.i(TAG, "onPerformSync, account: " + account.name + ", Bundle: " + extras);
        try {
            if (!AccountHelper.isLogin(getContext())) {
                LogUtil.i(TAG, "onPerformSync, but account has been logout, stop sync with notify syncDone");
                ContentResolver.setSyncAutomatically(account, Contract.AUTHORITY, false);
                return;
            }

            if (!extras.getBoolean(BUNLDE_IGNORE_NETWORK, false) && !connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                LogUtil.i(TAG, "cancel sync because network is not wifi");
                return;
            }
            //see:http://developer.android.com/training/sync-adapters/creating-sync-adapter.html

            new GetVideoList().with(getContext()).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    syncDone(false);
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    sync(toMap(result.getData()));
                } else {
                    LogUtil.e(TAG, "get video list failed: " + result.getCode());
                    syncDone(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "some uncatched exception in SyncAdapter");
        }

    }

    @Override public void onSyncCanceled() {
        //TODO how to handle cancel
        LogUtil.i(TAG, "onSyncCanceled");
        super.onSyncCanceled();
    }

    /**
     * compare local video with server's, and determine whether upload or download.
     * <p>
     * TODO Refactor: just determine which action to take, and commit the action task to a queue
     *
     * @param videosOnServer map of server's videos
     */
    private void sync(Map<Integer, Data> videosOnServer) {
        try {
            LogUtil.v(TAG, "video on server: " + videosOnServer.toString());
            LogUtil.i(TAG, "video got, start sync");
            List<Moment> toSyncedMoments = dao.queryBuilder().where().eq("owner", "LOC").or().eq("owner", AccountHelper.getIdentityInfo(getContext()).get_id()).query();

//            LogUtil.d(TAG, "copy " + databaseNum + " to sdcard");
//            File database = new File("/data/data/co.yishun.onemoment.app/databases/OneDataBase.db");
//            File copyed;
//            do {
//                copyed = new File("/sdcard/copyed" + databaseNum + ".db");
//                databaseNum++;
//
//            } while (copyed.exists());
//
//            FileUtils.copyFile(database, copyed);


            LogUtil.v(TAG, "queried moments: " + Arrays.toString(toSyncedMoments.toArray()));


            for (Moment moment : toSyncedMoments) {
                Integer key = Integer.parseInt(moment.getTime());
                Data video = videosOnServer.get(key);

                if (Thread.interrupted()) {
                    LogUtil.e("TAG", "interrupted");
                    syncDone(false);
                    return;
                }

                LogUtil.v(TAG, "sync iter: " + moment.toString());
                if (video != null) {
                    LogUtil.v(TAG, "on server: " + video.toString());
                    //if server has today moment
                    if (video.getTimeStamp() > moment.getTimeStamp()) {
                        //if server is newer, download and delete older
                        LogUtil.v(TAG, "download: " + video.toString());
                        CountDownLatch latch = new CountDownLatch(1);
                        downloadVideo(video, moment, latch);
                        latch.await();
                    } else if (video.getTimeStamp() < moment.getTimeStamp()) {
                        //if local is newer, upload and delete older
                        LogUtil.v(TAG, "upload: " + video.toString());
                        CountDownLatch latch = new CountDownLatch(1);
                        uploadMoment(moment, video, latch);
                        latch.await();
                    } else {
                        LogUtil.v(TAG, "same, do nothing");
                        checkMomentOwnerPrivate(moment);
                        syncUpdate(UpdateType.RECOVER, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                    }
                    //the video sync ok, remove
                    videosOnServer.remove(key);
                } else {
                    CountDownLatch latch = new CountDownLatch(1);
                    uploadMoment(moment, null, latch);//server not have today moment, upload
                    latch.await();
                }
            }
            for (Data data : videosOnServer.values()) {
                if (Thread.interrupted()) {
                    LogUtil.e("TAG", "interrupted");
                    return;
                }
                CountDownLatch latch = new CountDownLatch(1);
                downloadVideo(data, null, latch);//other unhandled video mean they need download
                latch.await();
            }
            syncDone(true);
        } catch (Exception e) {
            //catch all to avoid crash background
            e.printStackTrace();
            syncDone(false);
        }
    }

    /**
     * Ensure a moment and it's file is user private. if not, it will be change private.
     */
    private void checkMomentOwnerPrivate(@NonNull Moment moment) {
        Moment.lock(getContext());
        try {
            //set file name
            String path = moment.getPath();
            if (CameraHelper.whatTypeOf(path) == CameraHelper.Type.LOCAL) {
                File file = new File(path);
                File syncedFile = CameraHelper.getOutputMediaFile(getContext(), CameraHelper.Type.SYNCED, file);
                LogUtil.d(TAG, "check private: " + file);
                LogUtil.d(TAG, "syncedFile: " + syncedFile);
                if (file.renameTo(syncedFile)) {
                    moment.setPath(syncedFile.getPath());
                    dao.update(moment);
                    LogUtil.v(TAG, "update path");
                } else
                    LogUtil.e(TAG, "unable to rename when checkMomentOwnerPrivate, from " + file.getPath() + " to " + syncedFile.getPath());
            }
            //set database owner
            if (moment.isPublic()) {
                moment.setOwner(AccountHelper.getIdentityInfo(getContext()).get_id());
                dao.update(moment);
                LogUtil.v(TAG, "update owner");
            }
            LogUtil.i(TAG, "succeed in setting moment[ " + moment + "] private");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Moment.unlock();
        }
    }

    /**
     * send broadcast to notify finishing syncing
     */
    private void syncDone(boolean isSuccess) {
        Intent intent = new Intent(SYNC_BROADCAST_DONE);
        intent.putExtra(SYNC_BROADCAST_EXTRA_IS_UPLOAD_CHANGED, isUploadChanged);
        intent.putExtra(SYNC_BROADCAST_EXTRA_IS_DOWNLOAD_CHANGED, isDownloadChanged);
        intent.putExtra(SYNC_BROADCAST_EXTRA_IS_SUCCESS, isSuccess);
        LogUtil.i(TAG, "sync done, send a broadcast. isUploadChanged: " + isUploadChanged + ", isDownloadChanged: " + isDownloadChanged + ", isSuccess " + isSuccess);
        getContext().sendBroadcast(intent);
        isUploadChanged = false;
        isDownloadChanged = false;

        cleanFile();
    }

    private void cleanFile() {
        try {
            //delete useless video
            File mediaDir = CameraHelper.getOutputMediaDir(getContext());
            for (String s : mediaDir.list((dir, filename) -> filename.startsWith(CameraHelper.Type.RECORDED.getPrefix(getContext())))) {
                File toDeleted = new File(mediaDir, s);
                LogUtil.i(TAG, "delete: " + toDeleted);
                if (Thread.interrupted()) {
                    LogUtil.e("TAG", "interrupted");
                    return;
                }
                if (toDeleted.exists()) toDeleted.delete();
            }

            //delete unregistered local video
            for (String s : mediaDir.list((dir, filename) -> {
                try {
                    if (filename.startsWith(CameraHelper.Type.LOCAL.getPrefix(getContext()))) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("path", dir.getPath() + File.pathSeparator + filename);
                        LogUtil.v(TAG, "query for path: " + map.get("path").toString());
                        return (dao.queryBuilder().where().eq("path", new File(dir, filename).getPath()).countOf() == 0);
                    } else return false;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            })) {
                File toDeleted = new File(mediaDir, s);
                if (Thread.interrupted()) {
                    LogUtil.e("TAG", "interrupted");
                    return;
                }
                LogUtil.i(TAG, "delete: " + toDeleted);
                if (toDeleted.exists()) toDeleted.delete();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception when clean useless video");
            e.printStackTrace();
        }
    }

    /**
     * send broadcast to notify syncing progress update.
     * <p>
     * progress is from 0 to 100.
     * <p>
     * {@link #PROGRESS_NOT_AVAILABLE} means no progress data available.
     * {@link #PROGRESS_ERROR} means error occurred.
     */
    private void syncUpdate(UpdateType type, int thisProgress, int thisTypeProgress, int allProgress) {
        Intent intent = new Intent(type.getAction());
        intent.putExtra(SYNC_BROADCAST_EXTRA_THIS_PROGRESS, thisProgress);
        intent.putExtra(SYNC_BROADCAST_EXTRA_TYPE_PROGRESS, thisTypeProgress);
        intent.putExtra(SYNC_BROADCAST_EXTRA_ALL_PROGRESS, allProgress);
        LogUtil.i(TAG, "sync update progress, send a broadcast. type: " + type.name() + ", this progress: " + thisProgress + ", type progress: " + thisTypeProgress + ", all progress: " + allProgress);
        getContext().sendBroadcast(intent);
    }

    private UploadManager getUploadManager() {
        if (mUploadManager == null) {
            mUploadManager = new UploadManager();
        }
        return mUploadManager;
    }

    /**
     * upload a moment to server. if success, make moment private.
     *
     * @param moment        to upload
     * @param videoToDelete old version on server
     */
    private void uploadMoment(@NonNull Moment moment, @Nullable Data videoToDelete, @Nullable CountDownLatch latch) {
        LogUtil.i(TAG, "upload a moment: " + moment.getPath());
        String qiNiuKey = getQiniuVideoFileName(moment);
        syncUpdate(UpdateType.UPLOAD, 0, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
        new GetToken().setFileName(qiNiuKey).with(getContext()).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                if (latch != null) {
                    latch.countDown();
                }
            } else if (result.getCode() != ErrorCode.SUCCESS) {
                LogUtil.e(TAG, "get token failed: " + result.getCode());
                syncUpdate(UpdateType.UPLOAD, PROGRESS_ERROR, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                if (latch != null) {
                    latch.countDown();
                }
            } else {
                getUploadManager().put(moment.getPath(),
                        qiNiuKey,
                        result.getData().getToken(),
                        (s, responseInfo, jsonObject) -> {
                            LogUtil.i(TAG, responseInfo.toString());
                            if (responseInfo.isOK()) {
                                if (videoToDelete != null) deleteVideo(videoToDelete);
                                LogUtil.i(TAG, "a moment upload ok: " + moment.getPath());
                                checkMomentOwnerPrivate(moment);
                                isUploadChanged = true;
                                syncUpdate(UpdateType.UPLOAD, 100, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                                if (latch != null) {
                                    latch.countDown();
                                }
                            } else {
                                LogUtil.i(TAG, "a moment upload failed: " + moment.getPath());
                                syncUpdate(UpdateType.UPLOAD, PROGRESS_ERROR, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                                if (latch != null) {
                                    latch.countDown();
                                }
                            }
                        },
                        new UploadOptions(null, Config.MIME_TYPE, true, null, null)
                );
            }
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
        boolean re = false;

        Moment.lock(getContext());
        try {
            re = dao.delete(moment) == 1;
            if (re) {
                File momentFile = moment.getFile();
                File thumb = new File(moment.getThumbPath());
                File thumbL = new File(moment.getLargeThumbPath());

                if (momentFile.exists()) momentFile.delete();
                if (thumb.exists()) thumb.delete();
                if (thumbL.exists()) thumbL.delete();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            re = false;
        } finally {
            Moment.unlock();
        }


        return re;
    }

    /**
     * download a video from server, then register private moment in database.
     *
     * @param aVideoOnServer video to download.
     * @param momentOld      old local moment. It will do nothing if it is null.
     */
    private void downloadVideo(Data aVideoOnServer, @Nullable Moment momentOld, @Nullable CountDownLatch latch) {
        LogUtil.i(TAG, "download a video: " + aVideoOnServer.getQiuniuKey());
        File fileSynced = CameraHelper.getOutputMediaFile(getContext(), aVideoOnServer);
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder().url(Config.getResourceUrl(getContext()) + aVideoOnServer.getQiuniuKey()).get().build();
//            Response response = client.newCall(request).execute();
//            response.body().
        syncUpdate(UpdateType.DOWNLOAD, 0, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);

        //if file exist, just register private moment at database
        try {
            if (fileSynced.exists()) {
                if (Etag.file(fileSynced).equals(aVideoOnServer.getHash())) {
                    String pathToThumb = CameraHelper.createThumbImage(getContext(), fileSynced.getPath());
                    String pathToLargeThumb = CameraHelper.createLargeThumbImage(getContext(), fileSynced.getPath());
                    //don't need lock
                    //if file exist, just register/update private moment at database
                    if (momentOld == null) {
                        Moment moment = Moment.from(aVideoOnServer, fileSynced.getPath(), pathToThumb, pathToLargeThumb);
                        LogUtil.v(TAG, "create a recovered moment: " + moment);
                        dao.create(moment);
                        isDownloadChanged = true;
                    } else {
                        momentOld.setLargeThumbPath(pathToLargeThumb);
                        momentOld.setThumbPath(pathToThumb);
                        LogUtil.v(TAG, "update a recovered moment: " + momentOld);
                        dao.update(momentOld);
                        isDownloadChanged = true;

                    }
                    syncUpdate(UpdateType.DOWNLOAD, 100, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                    if (latch != null) {
                        latch.countDown();
                    }
                    return;
                } else {
                    fileSynced.delete();
                }
            }

            //download and register
            Ion.with(getContext()).load(Config.getResourceUrl(getContext()) + aVideoOnServer.getQiuniuKey())
                    .write(fileSynced).setCallback((e, result) -> {
                try {
                    if (e != null) {
                        throw e;
                    }
                    if (momentOld != null && !deleteMoment(momentOld)) {
                        LogUtil.e(TAG, "delete old local moment failed: " + momentOld.getPath());
                    }

                    String pathToThumb = CameraHelper.createThumbImage(getContext(), fileSynced.getPath());
                    String pathToLargeThumb = CameraHelper.createLargeThumbImage(getContext(), fileSynced.getPath());

                    dao.create(Moment.from(aVideoOnServer, fileSynced.getPath(), pathToThumb, pathToLargeThumb));

                    LogUtil.i(TAG, "a video download ok: " + aVideoOnServer.getQiuniuKey());
                    isDownloadChanged = true;
                    syncUpdate(UpdateType.DOWNLOAD, 100, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                    if (latch != null) {
                        latch.countDown();
                    }
                } catch (Exception e1) {
                    syncUpdate(UpdateType.DOWNLOAD, PROGRESS_ERROR, PROGRESS_NOT_AVAILABLE, PROGRESS_NOT_AVAILABLE);
                    if (latch != null) {
                        latch.countDown();
                    }
                    e1.printStackTrace();
                }
            });

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }


    }

    private String getQiniuVideoFileName(Moment moment) {
        return getQiniuVideoFileName(getContext(), moment);
    }

    enum UpdateType {
        UPLOAD {
            @Override String getAction() { return SYNC_BROADCAST_UPDATE_UPLOAD; }
        },
        DOWNLOAD {
            @Override String getAction() { return SYNC_BROADCAST_UPDATE_DOWNLOAD; }
        },
        RECOVER {
            @Override String getAction() {
                return SYNC_BROADCAST_UPDATE_RECOVER;
            }
        };

        abstract String getAction();

    }

    public interface OnCheckedListener {
        /**
         * Moment is intact to use.
         */
        void onMomentOk(Moment moment);

        /**
         * Moment is not intact. And try to repair it.
         */
        void onMomentStartRepairing(Moment moment);

        /**
         * Moment is unable to be repaired, and has been deleted from database, you should stop use this moment.
         */
        void onMomentDelete(Moment moment);
    }
}
