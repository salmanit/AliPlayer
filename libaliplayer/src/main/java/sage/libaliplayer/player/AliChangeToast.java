package sage.libaliplayer.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import sage.libaliplayer.R;

/**
 * Created by Sage on 2017/8/14.
 * Description:音量或者亮度改变的时候调用，方便自定义
 */

public class AliChangeToast {
    Context context;
    ImageView iv_toast;
    ProgressBar pb_toast;

    public AliChangeToast(Context context) {
        this.context = context;
    }

    protected void volumeChange(int current, int max) {
        changeShow(R.drawable.ali_card_player_gesture_sound_big, current, max);
    }

    protected void screenBrightnessChange(float current) {
        changeShow(R.drawable.ali_card_player_gesture_bright_big, (int) (current * 100), 100);
    }

    protected void changeShow(int resDrawable, int current, int max) {
        if (mFloatLayout == null) {
            createFloatView();
        }
        if (mFloatLayout != null && mFloatLayout.getParent() != null) {
            iv_toast.setImageResource(resDrawable);
            pb_toast.setMax(max);
            pb_toast.setProgress(current);
        }
    }

    protected void cancelToast() {
        if (mFloatLayout != null) {
            if (mWindowManager != null) {
                mWindowManager.removeView(mFloatLayout);
                mFloatLayout=null;
            }
        }
    }


    WindowManager mWindowManager;
    WindowManager.LayoutParams wmParams;
    View mFloatLayout;
    private void createFloatView() {
        //获取LayoutParams对象
        wmParams = new WindowManager.LayoutParams();

        //获取的是LocalWindowManager对象
        mWindowManager = ((Activity) context).getWindowManager();

        //获取的是CompatModeWrapper对象
        //mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.format = PixelFormat.RGBA_8888;

        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.CENTER;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mFloatLayout = LayoutInflater.from(context).inflate(R.layout.toast_volume_change, null);
        mWindowManager.addView(mFloatLayout, wmParams);
        iv_toast = (ImageView) mFloatLayout.findViewById(R.id.iv_toast);
        pb_toast = (ProgressBar) mFloatLayout.findViewById(R.id.pb_toast);

    }

}
