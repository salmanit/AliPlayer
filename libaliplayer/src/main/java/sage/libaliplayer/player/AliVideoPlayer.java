package sage.libaliplayer.player;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;



/**
 * 播放器
 */
public class AliVideoPlayer extends FrameLayout
        implements AliVideoPlayerControl {

    public static final int STATE_ERROR = -1;          // 播放错误
    public static final int STATE_IDLE = 0;            // 播放未开始
    public static final int STATE_PREPARING = 1;       // 播放准备中
    public static final int STATE_PREPARED = 2;        // 播放准备就绪
    public static final int STATE_PLAYING = 3;         // 正在播放
    public static final int STATE_PAUSED = 4;          // 暂停播放
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     **/
    public static final int STATE_BUFFERING_PLAYING = 5;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停)
     **/
    public static final int STATE_BUFFERING_PAUSED = 6;
    public static final int STATE_COMPLETED = 7;       // 播放完成

    public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    public static final int PLAYER_TINY_WINDOW = 12;   // 小窗口播放器

    private int mCurrentState = STATE_IDLE;
    private int mPlayerState = PLAYER_NORMAL;

    private Context mContext;
    private FrameLayout mContainer;
    private AliVideoPlayerController mController;
    private String mUrl;
    private AliVcMediaPlayer mMediaPlayer;

    private int mBufferPercentage;
    private boolean hiddenTime;
    
    public AliVideoPlayer(Context context) {
        this(context, null);
    }

    public AliVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
        setController(mController =new AliVideoPlayerController(mContext));

    }

    public void setUrl(String url,boolean hiddenTime){
        setUrl(url);
        this.hiddenTime=hiddenTime;
    }
    public void setUrl(String url) {
        mUrl = url;
        if(TextUtils.isEmpty(mUrl)){
            return;
        }
        if(mController!=null){
            mController.showCenterPlayUi();
        }
    }

    public AliVideoPlayerController getmController() {
        return mController;
    }

    public void setController(AliVideoPlayerController controller) {
        if(mContainer!=null)
            mContainer.removeView(mController);
        mController = controller;
        mController.setNiceVideoPlayer(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mController, params);
    }
    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new AliVcMediaPlayer(getContext(),mSurfaceView);
            mMediaPlayer.setVideoScalingMode(MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            // 设置图像适配屏幕，适配最短边，超出部分裁剪
//            mMediaPlayer.setVideoScalingMode(MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            //设置缺省编码类型：0表示硬解；1表示软解；
            //如果缺省为硬解，在使用硬解时如果解码失败，会尝试使用软解
            //如果缺省为软解，则一直使用软解，软解较为耗电，建议移动设备尽量使用硬解
            mMediaPlayer.setDefaultDecoder(0);


            mMediaPlayer.setPreparedListener(new VideoPrepareListener());             //播放器就绪事件
            mMediaPlayer.setErrorListener(new VideoErrorListener());                   //异常错误事件
            mMediaPlayer.setInfoListener(new VideoInfolistener());                     //信息状态监听事件
            mMediaPlayer.setSeekCompleteListener(seekCompleteListener);     //seek结束事件（备注：直播无seek操作）
            mMediaPlayer.setCompletedListener(completedListener);            //播放结束事件
            mMediaPlayer.setVideoSizeChangeListener(videoSizeChangeListener);
            //画面大小变化事件
            mMediaPlayer.setBufferingUpdateListener(bufferingUpdateListener);
            //缓冲信息更新事件
            //准备开始播放
            mCurrentState = STATE_PREPARING;
            mController.setControllerState(mPlayerState, mCurrentState);
            mMediaPlayer.prepareAndPlay(mUrl);
        }
    }

    private MediaPlayer.MediaPlayerSeekCompleteListener  seekCompleteListener=new MediaPlayer.MediaPlayerSeekCompleteListener() {
        @Override
        public void onSeekCompleted() {
            DebugLogUtil.i("onSeekCompleted=================");
        }
    };

    private MediaPlayer.MediaPlayerCompletedListener completedListener=new MediaPlayer.MediaPlayerCompletedListener() {
        @Override
        public void onCompleted() {
            DebugLogUtil.i("onCompleted=================");
            mCurrentState = STATE_COMPLETED;
            mController.setControllerState(mPlayerState, mCurrentState);
            AliVideoPlayerManager.instance().setCurrentNiceVideoPlayer(null);
        }
    };
    private MediaPlayer.MediaPlayerVideoSizeChangeListener videoSizeChangeListener=new MediaPlayer.MediaPlayerVideoSizeChangeListener() {
        @Override
        public void onVideoSizeChange(int i, int i1) {
            DebugLogUtil.i("onVideoSizeChange=================="+i+"/"+i1);
        }
    };

    private MediaPlayer.MediaPlayerBufferingUpdateListener bufferingUpdateListener=new MediaPlayer.MediaPlayerBufferingUpdateListener() {
        @Override
        public void onBufferingUpdateListener(int i) {
            DebugLogUtil.i("onBufferingUpdateListener==================="+i);
            mBufferPercentage = i;
        }
    };
    private void initSurfaceView() {
        if (mSurfaceView == null) {
            mSurfaceView = new SurfaceView(mContext);
            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    holder.setKeepScreenOn(true);
                    DebugLogUtil.i("surfaceCreated============="+holder.isCreating());
                    if (mMediaPlayer != null) {
                        // 对于从后台切换到前台,需要重设surface;部分手机锁屏也会做前后台切换的处理
                        mMediaPlayer.setVideoSurface(mSurfaceView.getHolder().getSurface());
                    } else {
                        // 创建并启动播放器
                        initMediaPlayer();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    DebugLogUtil.i("surfaceChanged============"+width+"/"+height);
                    if (mMediaPlayer != null)
                        mMediaPlayer.setSurfaceChanged();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    DebugLogUtil.i("surfaceDestroyed===============");
                    if (mMediaPlayer != null)
                        mMediaPlayer.releaseVideoSurface();
                }
            });
        }
    }

    SurfaceView mSurfaceView;
    private void addTextureView() {
        mContainer.removeView(mSurfaceView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity=Gravity.CENTER;
        mContainer.addView(mSurfaceView, 0, params);
    }
    @Override
    public void start() {
        if(TextUtils.isEmpty(mUrl)){
            return;
        }
        AliVideoPlayerManager.instance().releaseNiceVideoPlayer();
        AliVideoPlayerManager.instance().setCurrentNiceVideoPlayer(this);
        if (mCurrentState == STATE_IDLE
                || mCurrentState == STATE_ERROR
                || mCurrentState == STATE_COMPLETED) {
            initSurfaceView();
            addTextureView();

        }
    }

    @Override
    public void restart() {
        if (mCurrentState == STATE_PAUSED) {
            mMediaPlayer.play();
            mCurrentState = STATE_PLAYING;
            mController.setControllerState(mPlayerState, mCurrentState);
        }
        if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaPlayer.play();
            mCurrentState = STATE_BUFFERING_PLAYING;
            mController.setControllerState(mPlayerState, mCurrentState);
        }
    }

    @Override
    public void pause() {
        if (mCurrentState == STATE_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            mController.setControllerState(mPlayerState, mCurrentState);
        }
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            mController.setControllerState(mPlayerState, mCurrentState);
        }
    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return mPlayerState == PLAYER_FULL_SCREEN;
    }

    @Override
    public boolean isTinyWindow() {
        return mPlayerState == PLAYER_TINY_WINDOW;
    }

    @Override
    public boolean isNormal() {
        return mPlayerState == PLAYER_NORMAL;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }


    private class VideoInfolistener implements AliVcMediaPlayer.MediaPlayerInfoListener {
        public void onInfo(int what, int extra){
            DebugLogUtil.i("MediaPlayerInfoListener=========="+what);
            switch (what)
            {
                case MediaPlayer.MEDIA_INFO_UNKNOW:
                    // 未知
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    // 开始缓冲
                    if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_BUFFERING_PAUSED;
                    } else {
                        mCurrentState = STATE_BUFFERING_PLAYING;
                    }
                    mController.setControllerState(mPlayerState, mCurrentState);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    // 结束缓冲
                    if (mCurrentState == STATE_BUFFERING_PLAYING) {
                        mCurrentState = STATE_PLAYING;
                        mController.setControllerState(mPlayerState, mCurrentState);
                    }
                    if (mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_PAUSED;
                        mController.setControllerState(mPlayerState, mCurrentState);
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    // 首帧显示时间
                    mCurrentState = STATE_PLAYING;
                    mController.setControllerState(mPlayerState, mCurrentState);
                    break;
            }

        }
    }

    private class VideoPrepareListener implements AliVcMediaPlayer.MediaPlayerPreparedListener{
        @Override
        public void onPrepared() {
            //更新视频总进度
            DebugLogUtil.i("onPrepared=========");
            mCurrentState = STATE_PREPARED;
            mController.setControllerState(mPlayerState, mCurrentState);
            thisWidth=getWidth();
            thisHeight=getHeight();
            if(hiddenTime){
                if(mController!=null){
                    mController.hiddenTime();
                }
            }
        } }

    private class VideoErrorListener implements AliVcMediaPlayer.MediaPlayerErrorListener {
        public void onError(int what, int extra) {
            DebugLogUtil.i("onError========="+what);
            mCurrentState = STATE_ERROR;
            mController.setControllerState(mPlayerState, mCurrentState);
            switch(what)
            {
                case MediaPlayer.ALIVC_ERR_ILLEGALSTATUS:
                    // 非法状态！
                    break;
                case MediaPlayer.ALIVC_ERR_NO_NETWORK:
                    //report_error("视频资源或网络不可用！", true);
                    break;
                case MediaPlayer.ALIVC_ERR_INVALID_INPUTFILE:
                    //视频资源或网络不可用！
                    break;
                case MediaPlayer.ALIVC_ERR_NO_SUPPORT_CODEC:
                    //无支持的解码器!
                    break;
//                case MediaPlayer.ALIVC_ERR_FUNCTION_DENY:
//                    //无此操作权限!
//                    break;
                case MediaPlayer.ALIVC_ERR_UNKNOWN:
                    //未知错误!
                    break;
                case MediaPlayer.ALIVC_ERR_NOTAUTH:
                    //未鉴权!
                    break;
                case MediaPlayer.ALIVC_ERR_READD:
                    //资源访问失败!
                    break;
                default:
                    //播放器错误!
                    break;
            }
        }
    }

    /**这里的宽高为播放控件的*/
    private void handleTextViewSize(int width,int height){
        int videoWidth=mMediaPlayer.getVideoWidth();
        int videoHeight=mMediaPlayer.getVideoHeight();

        ViewGroup.LayoutParams params=mSurfaceView.getLayoutParams();
            if(videoWidth*1f/videoHeight>=width*1f/height){
                int realHeight=videoHeight*width/videoWidth;

                params.height=realHeight;
                params.width=ViewGroup.LayoutParams.MATCH_PARENT;


            }else if(videoWidth*1f/videoHeight<width*1f/height){
                int realWidth=videoWidth*height/videoHeight;
                params.width=realWidth;
                params.height=ViewGroup.LayoutParams.MATCH_PARENT;
            }
        mSurfaceView.setLayoutParams(params);
        DebugLogUtil.i("width===="+(videoWidth*1f/videoHeight+"**********"+width*1f/height));
        DebugLogUtil.i(videoWidth+"/"+videoHeight+"==width======"+width+"/"+height+"===="+params.width+"/"+params.height);
    }


    private int thisWidth;//控件原始的宽
    private int thisHeight;//高
    /**
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.content中.
     */
    @Override
    public void enterFullScreen() {
        if (mPlayerState == PLAYER_FULL_SCREEN) return;

        // 隐藏ActionBar、状态栏，并横屏
        AliUtil.hideActionBar(mContext);
        AliUtil.scanForActivity(mContext)
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        this.removeView(mContainer);
        ViewGroup contentView = (ViewGroup) AliUtil.scanForActivity(mContext)
                .findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mContainer, params);
        handleTextViewSize(getResources().getDisplayMetrics().heightPixels,getResources().getDisplayMetrics().widthPixels);
        mPlayerState = PLAYER_FULL_SCREEN;
        mController.setControllerState(mPlayerState, mCurrentState);
    }

    /**
     * 退出全屏，移除mTextureView和mController，并添加到非全屏的容器中。
     *
     * @return true退出全屏.
     */
    @Override
    public boolean exitFullScreen() {
        if (mPlayerState == PLAYER_FULL_SCREEN) {
            AliUtil.showActionBar(mContext);
            AliUtil.scanForActivity(mContext)
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            ViewGroup contentView = (ViewGroup) AliUtil.scanForActivity(mContext)
                    .findViewById(android.R.id.content);
            contentView.removeView(mContainer);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContainer, params);
            handleTextViewSize(thisWidth,thisHeight);
            mPlayerState = PLAYER_NORMAL;
            mController.setControllerState(mPlayerState, mCurrentState);
            return true;
        }
        return false;
    }

    /**
     * 进入小窗口播放，小窗口播放的实现原理与全屏播放类似。
     */
    @Override
    public void enterTinyWindow() {
        if (mPlayerState == PLAYER_TINY_WINDOW) return;
        this.removeView(mContainer);
        ViewGroup contentView = (ViewGroup) AliUtil.scanForActivity(mContext)
                .findViewById(android.R.id.content);
        // 小窗口的宽度为屏幕宽度的60%，长宽比默认为16:9，右边距、下边距为8dp。
        LayoutParams params = new LayoutParams(
                (int) (AliUtil.getScreenWidth(mContext) * 0.6f),
                (int) (AliUtil.getScreenWidth(mContext) * 0.6f * 9f / 16f));
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.rightMargin = AliUtil.dp2px(mContext, 8f);
        params.bottomMargin = AliUtil.dp2px(mContext, 8f);
        contentView.addView(mContainer, params);
        handleTextViewSize(params.width,params.height);
        mPlayerState = PLAYER_TINY_WINDOW;
        mController.setControllerState(mPlayerState, mCurrentState);
    }

    /**
     * 退出小窗口播放
     */
    @Override
    public boolean exitTinyWindow() {
        if (mPlayerState == PLAYER_TINY_WINDOW) {
            ViewGroup contentView = (ViewGroup) AliUtil.scanForActivity(mContext)
                    .findViewById(android.R.id.content);
            contentView.removeView(mContainer);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContainer, params);
                handleTextViewSize(thisWidth,thisWidth);
            mPlayerState = PLAYER_NORMAL;
            mController.setControllerState(mPlayerState, mCurrentState);
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.destroy();
            mMediaPlayer = null;
        }
        mContainer.removeView(mSurfaceView);
        mSurfaceView=null;
        if (mController != null) {
            mController.reset();
        }
        mCurrentState = STATE_IDLE;
        mPlayerState = PLAYER_NORMAL;

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }


}
