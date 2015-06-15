package me.toxz.squarethumbnailvideoview.library;

/**
 * Created by yyz on 6/15/15.
 */
public abstract class SingleVideoAdapter implements VideoAdapter {
    @Override public int getCount() {
        return 1;
    }

    @Override public Object getItem(int position) {
        return null;
    }

    @Override public long getItemId(int position) {
        return 0;
    }

    @Override public boolean isEmpty() {
        return false;
    }
}
