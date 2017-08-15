package sage.libaliplayer.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.support.annotation.DrawableRes;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import sage.libaliplayer.R;

/**
 * Created by sage on 2017/8/08.
 * 播放器控制器.
 */
public class AliVideoPlayerController extends FrameLayout
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private Context mContext;
    private AliVideoPlayerControl mNiceVideoPlayer;
    private ImageView mImage;
    private ImageView mCenterStart;
    private LinearLayout mTop;
    private ImageView mBack;
    private TextView mTitle;
    private LinearLayout mBottom;
    private ImageView mRestartPause;
    private TextView mPosition;
    private TextView mDuration;
    private SeekBar mSeek;
    private ImageView mFullScreen;
    private LinearLayout mLoading;
    private TextView mLoadText;
    private LinearLayout mError;
    private TextView mRetry;
    private LinearLayout mCompleted;
    private TextView mReplay;
    private TextView mShare;

    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;
    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;

    public AliVideoPlayerController(Context context) {
        super(context);
        mContext = context;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        init();
        setAliChangeToast(new AliChangeToast(context));
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.ali_video_palyer_controller, this, true);
        mCenterStart = (ImageView) findViewById(R.id.center_start);
        mImage = (ImageView) findViewById(R.id.image);

        mTop = (LinearLayout) findViewById(R.id.top);
        mBack = (ImageView) findViewById(R.id.back);
        mTitle = (TextView) findViewById(R.id.title);

        mBottom = (LinearLayout) findViewById(R.id.bottom);
        mRestartPause = (ImageView) findViewById(R.id.restart_or_pause);
        mPosition = (TextView) findViewById(R.id.position);
        mDuration = (TextView) findViewById(R.id.duration);
        mSeek = (SeekBar) findViewById(R.id.seek);
        mFullScreen = (ImageView) findViewById(R.id.full_screen);

        mLoading = (LinearLayout) findViewById(R.id.loading);
        mLoadText = (TextView) findViewById(R.id.load_text);

        mError = (LinearLayout) findViewById(R.id.error);
        mRetry = (TextView) findViewById(R.id.retry);

        mCompleted = (LinearLayout) findViewById(R.id.completed);
        mReplay = (TextView) findViewById(R.id.replay);
        mShare = (TextView) findViewById(R.id.share);

        mCenterStart.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mRestartPause.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mRetry.setOnClickListener(this);
        mReplay.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mSeek.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
    }

    public void showCenterPlayUi() {
        if (mCenterStart != null) {
            mCenterStart.setVisibility(View.VISIBLE);
        }
    }

    public void hiddenTime() {
        if (mPosition != null) {
            mPosition.setVisibility(View.INVISIBLE);
        }
        if (mSeek != null) {
            mSeek.setVisibility(View.INVISIBLE);
        }
        if (mDuration != null) {
            mDuration.setVisibility(View.INVISIBLE);
        }
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public ImageView getCoverUI() {
        return mImage;
    }

    public void setImage(@DrawableRes int resId) {
        mImage.setImageResource(resId);
    }

    public void setNiceVideoPlayer(AliVideoPlayerControl niceVideoPlayer) {
        mNiceVideoPlayer = niceVideoPlayer;
        if (mNiceVideoPlayer.isIdle()) {
            mBack.setVisibility(View.GONE);
            mTop.setVisibility(View.VISIBLE);
            mBottom.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (controllerListener != null) {
            controllerListener.onClick(v);
        }
        if (v == mCenterStart) {
            if (mNiceVideoPlayer.isIdle()) {
                mNiceVideoPlayer.start();
            }
        } else if (v == mBack) {
            if (mNiceVideoPlayer.isFullScreen()) {
                mNiceVideoPlayer.exitFullScreen();
            } else if (mNiceVideoPlayer.isTinyWindow()) {
                mNiceVideoPlayer.exitTinyWindow();
            }
        } else if (v == mRestartPause) {
            if (mNiceVideoPlayer.isPlaying() || mNiceVideoPlayer.isBufferingPlaying()) {
                mNiceVideoPlayer.pause();
            } else if (mNiceVideoPlayer.isPaused() || mNiceVideoPlayer.isBufferingPaused()) {
                mNiceVideoPlayer.restart();
            }
        } else if (v == mFullScreen) {
            if (mNiceVideoPlayer.isNormal()) {
                mNiceVideoPlayer.enterFullScreen();
            } else if (mNiceVideoPlayer.isFullScreen()) {
                mNiceVideoPlayer.exitFullScreen();
            }
        } else if (v == mRetry) {
            mNiceVideoPlayer.release();
            mNiceVideoPlayer.start();
        } else if (v == mReplay) {
            mRetry.performClick();
        }
//        else if (v == mShare) {
//            Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
//        }
        else if (v == this) {
            if (mNiceVideoPlayer.isPlaying()
                    || mNiceVideoPlayer.isPaused()
                    || mNiceVideoPlayer.isBufferingPlaying()
                    || mNiceVideoPlayer.isBufferingPaused()) {
                setTopBottomVisible(!topBottomVisible);
            }
        }

    }

    ControllerListener controllerListener;

    public AliVideoPlayerController setControllerListener(ControllerListener controllerListener) {
        this.controllerListener = controllerListener;
        return this;
    }

    private void setTopBottomVisible(boolean visible) {
        mTop.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        topBottomVisible = visible;
        if (visible) {
            if (!mNiceVideoPlayer.isPaused() && !mNiceVideoPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    public void setControllerState(int playerState, int playState) {
        if (controllerListener != null) {
            controllerListener.playerState(playerState);
        }
        switch (playerState) {
            case AliVideoPlayer.PLAYER_NORMAL:
                mBack.setVisibility(View.GONE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.ali_ic_player_enlarge);
                break;
            case AliVideoPlayer.PLAYER_FULL_SCREEN:
                mBack.setVisibility(View.VISIBLE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.ali_ic_player_shrink);
                break;
            case AliVideoPlayer.PLAYER_TINY_WINDOW:
                mFullScreen.setVisibility(View.GONE);
                break;
        }
        switch (playState) {
            case AliVideoPlayer.STATE_IDLE:
                break;
            case AliVideoPlayer.STATE_PREPARING:
                // 只显示准备中动画，其他不显示
                mImage.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                mLoadText.setText("正在准备...");
                mError.setVisibility(View.GONE);
                mCompleted.setVisibility(View.GONE);
                mTop.setVisibility(View.GONE);
                mCenterStart.setVisibility(View.GONE);
                break;
            case AliVideoPlayer.STATE_PREPARED:
                startUpdateProgressTimer();
                break;
            case AliVideoPlayer.STATE_PLAYING:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ali_ic_player_pause);
                startDismissTopBottomTimer();
                break;
            case AliVideoPlayer.STATE_PAUSED:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ali_ic_player_start);
                cancelDismissTopBottomTimer();
                break;
            case AliVideoPlayer.STATE_BUFFERING_PLAYING:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ali_ic_player_pause);
                mLoadText.setText("正在缓冲...");
                startDismissTopBottomTimer();
                break;
            case AliVideoPlayer.STATE_BUFFERING_PAUSED:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ali_ic_player_start);
                mLoadText.setText("正在缓冲...");
                cancelDismissTopBottomTimer();
            case AliVideoPlayer.STATE_COMPLETED:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mImage.setVisibility(View.VISIBLE);
                mCompleted.setVisibility(View.VISIBLE);
                if (mNiceVideoPlayer.isFullScreen()) {
                    mNiceVideoPlayer.exitFullScreen();
                }
                if (mNiceVideoPlayer.isTinyWindow()) {
                    mNiceVideoPlayer.exitTinyWindow();
                }
                break;
            case AliVideoPlayer.STATE_ERROR:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mTop.setVisibility(View.VISIBLE);
                mError.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startUpdateProgressTimer() {
        cancelUpdateProgressTimer();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    AliVideoPlayerController.this.post(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 300);
    }

    private void updateProgress() {
        int position = mNiceVideoPlayer.getCurrentPosition();
        int duration = mNiceVideoPlayer.getDuration();
        int bufferPercentage = mNiceVideoPlayer.getBufferPercentage();
        mSeek.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeek.setProgress(progress);
        mPosition.setText(AliUtil.formatTime(position));
        mDuration.setText(AliUtil.formatTime(duration));
    }

    private void cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }

    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }


    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelDismissTopBottomTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mNiceVideoPlayer.isBufferingPaused() || mNiceVideoPlayer.isPaused()) {
            mNiceVideoPlayer.restart();
        }
        int position = (int) (mNiceVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        mNiceVideoPlayer.seekTo(position);
        startDismissTopBottomTimer();
    }

    /**
     * 控制器恢复到初始状态
     */
    public void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);

        mCenterStart.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.VISIBLE);

        mBottom.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.ali_ic_player_enlarge);

        mTop.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.GONE);

        mLoading.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                mVolume = -1;
                mBrightness = -1f;
                if (aliChangeToast != null) {
                    aliChangeToast.cancelToast();
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setAliChangeToast(AliChangeToast aliChangeToast) {
        this.aliChangeToast = aliChangeToast;
    }

    AliChangeToast aliChangeToast;
    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;
    /**
     * 最大声音
     */
    private int mMaxVolume;
    /**
     * 当前声音
     */
    private int mVolume = -1;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getY();

            int windowWidth = getWidth();
            int windowHeight = getHeight();

            if (mOldX > windowWidth * 3.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth * 2 / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent 手指滑动的距离百分比【纵向】
     */
    private void onVolumeSlide(float percent) {

        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;
        }
        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        if (aliChangeToast != null) {
            aliChangeToast.volumeChange(index, mMaxVolume);
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent 手指滑动的距离百分比【纵向】
     */
    private void onBrightnessSlide(float percent) {

        Window window = ((Activity) getContext()).getWindow();
        if (mBrightness < 0) {
            mBrightness = window.getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;
            // 显示

        }
        WindowManager.LayoutParams lpa = window.getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        window.setAttributes(lpa);
        if (aliChangeToast != null) {
            aliChangeToast.screenBrightnessChange(lpa.screenBrightness);
        }
    }

}
