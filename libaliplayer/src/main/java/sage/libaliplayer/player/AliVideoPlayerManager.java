package sage.libaliplayer.player;

/**
 * Created by XiaoJianjun on 2017/5/5.
 * 视频播放器管理器.
 */
public class AliVideoPlayerManager {

    private AliVideoPlayer mVideoPlayer;

    private AliVideoPlayerManager() {
    }

    private static AliVideoPlayerManager sInstance;

    public static synchronized AliVideoPlayerManager instance() {
        if (sInstance == null) {
            sInstance = new AliVideoPlayerManager();
        }
        return sInstance;
    }

    public void setCurrentNiceVideoPlayer(AliVideoPlayer videoPlayer) {
        mVideoPlayer = videoPlayer;
    }

    public void releaseNiceVideoPlayer() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
    }

    public boolean onBackPressd() {
        if (mVideoPlayer != null) {
            if (mVideoPlayer.isFullScreen()) {
                return mVideoPlayer.exitFullScreen();
            } else if (mVideoPlayer.isTinyWindow()) {
                return mVideoPlayer.exitTinyWindow();
            } else {
                mVideoPlayer.release();
                return false;
            }
        }
        return false;
    }
}
