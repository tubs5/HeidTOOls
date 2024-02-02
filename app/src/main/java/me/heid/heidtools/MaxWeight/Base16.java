package me.heid.heidtools.MaxWeight;
import com.google.common.io.BaseEncoding;

public class Base16 {
    public static final int NO_WRAP = 0;
    public static byte[] encode(byte[] input , int flag) {

        return BaseEncoding.base16().encode(input).getBytes();
    }
    public static byte[]decode(String str, int flag) {
        return BaseEncoding.base16().decode(str);
    }

    public static String encodeToString(byte[] toByteArray, int noWrap) {
        return new String(encode(toByteArray,noWrap));
    }
}