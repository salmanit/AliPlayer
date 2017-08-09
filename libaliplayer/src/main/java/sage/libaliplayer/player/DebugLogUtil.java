package sage.libaliplayer.player;

import android.util.Log;

import sage.libaliplayer.BuildConfig;

/**
 * Created by Sage on 2017/8/9.
 * Description:
 */

public class DebugLogUtil {

    public static boolean debug= BuildConfig.DEBUG;
    public static void i(String msg){
        if(debug){
            Log.i("DebugLogUtil",msg);
        }
    }
}
