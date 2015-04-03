package co.yishun.onemoment.app.net.request;

import android.app.Fragment;
import android.content.Context;
import co.yishun.onemoment.app.config.Config;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.builder.LoadBuilder;

/**
 * Created by Carlos on 2/9/15.
 */
public abstract class Request<R> {
    protected LoadBuilder<Builders.Any.B> builder;
    protected final String key = Config.getPrivateKey();
    protected Context mContext;


    /**
     * @return url of the request target.
     */
    abstract protected String getUrl();

    public Request<R> with(Context w) {
        builder = Ion.with(w);
        mContext = w;
        return this;
    }

    public Request<R> with(Fragment w) {
        builder = Ion.with(w);
        mContext = w.getActivity();
        return this;
    }

    public Request<R> with(android.support.v4.app.Fragment w) {
        builder = Ion.with(w);
        mContext = w.getActivity();
        return this;
    }

    public abstract void setCallback(final FutureCallback<R> callback);

    abstract protected void check();

}
