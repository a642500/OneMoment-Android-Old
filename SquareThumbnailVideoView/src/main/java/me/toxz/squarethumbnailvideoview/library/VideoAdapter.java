package me.toxz.squarethumbnailvideoview.library;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by yyz on 6/10/15.
 */
public interface VideoAdapter<T> {
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    int getCount();

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    T getItem(int position);

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    long getItemId(int position);

    /**
     * Get a path to video that plays at the specified order position in the data set.
     *
     * @param position The position of the item within the adapter's data set of the item whose video's path
     *                 we want.
     * @return A View corresponding to the data at the specified position.
     */
    String getVideoPath(int position);

    /**
     * @return true if this adapter doesn't contain any data.  This is used to determine
     * whether the empty view should be displayed.  A typical implementation will return
     * getCount() == 0 but since getCount() includes the headers and footers, specialized
     * adapters might want a different behavior.
     */
    boolean isEmpty();

    /**
     * Call to set bitmap of the thumbnail ImageView.
     *
     * @param thumbnailImageView ImageView need to set image
     * @param bitmap             bitmap set by {@link SquareThumbnailVideoView#setThumbnailBitmap(Bitmap)}, null if you haven't set it.
     * @return false if you didn't fill thumbnail image
     */
    boolean setThumbnailImage(ImageView thumbnailImageView, Bitmap bitmap);
}
