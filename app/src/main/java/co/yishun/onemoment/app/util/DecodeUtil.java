package co.yishun.onemoment.app.util;

import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;
import co.yishun.onemoment.app.config.Config;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharsetEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Carlos on 2015/3/27.
 */
public class DecodeUtil {
    private static final String TAG = LogUtil.makeTag(DecodeUtil.class);

    public static String decode(String string) {
        if (string == null) return null;
        try {
            LogUtil.v(TAG, "origin text: " + string);
            int split = string.indexOf(':');
            String iv = string.substring(0, split);
            String etext = string.substring(split + 1, string.length());
            byte[] ivT = Base64.decode(iv.getBytes("UTF-8"), Base64.DEFAULT);
            byte[] etextT = Base64.decode(etext.getBytes("UTF-8"), Base64.DEFAULT);

            LogUtil.d(TAG, "iv: " + iv);
            LogUtil.d(TAG, "etext: " + etext);
            LogUtil.d(TAG, "ivT: " + new String(ivT));
            LogUtil.d(TAG, "etextT: " + new String(etextT));


            byte[] key = Base64.decode(Config.AES_KEY.getBytes("UTF-8"), Base64.DEFAULT);
            LogUtil.d(TAG, "key: " + new String(key));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivT));
            byte[] re = cipher.doFinal(etextT);
            String s = new String(re);
            String json = s.substring(0, s.lastIndexOf('}')+1);
            LogUtil.v(TAG, "json: " + json);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
