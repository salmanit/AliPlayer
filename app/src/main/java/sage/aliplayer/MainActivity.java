package sage.aliplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.alivc.player.AccessKey;
import com.alivc.player.AccessKeyCallback;
import com.alivc.player.AliVcMediaPlayer;
import sage.libaliplayer.player.AliVideoPlayer;
import sage.libaliplayer.player.AliVideoPlayerManager;
import sage.libaliplayer.player.ControllerListener;

public class MainActivity extends AppCompatActivity {

    String url2="rtmp://live.hkstv.hk.lxdns.com/live/hks";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AliVcMediaPlayer.init(this, "video_live", new AccessKeyCallback() {
            @Override
            public AccessKey getAccessToken() {
                return new AccessKey("QxJIheGFRL926hFX", "hipHJKpt0TdznQG2J4D0EVSavRH7mR");
            }
        }) ;
        AliVideoPlayer aliVideoPlayer= (AliVideoPlayer) findViewById(R.id.ali_player);
        aliVideoPlayer.setUrl(url2);

        AliVideoPlayer aliVideoPlayer2= (AliVideoPlayer) findViewById(R.id.ali_player2);
        aliVideoPlayer2.setUrl(url2,true);
        aliVideoPlayer2.getmController().setControllerListener(new ControllerListener() {
            @Override
            public void onClick(View v) {

            }

            @Override
            public void playerState(int playerState) {

            }
        });

        aliVideoPlayer2.autoRotateScreen(true);
    }

    @Override
    public void onBackPressed() {
        if(AliVideoPlayerManager.instance().onBackPressd()){
            return;
        }
        AliVideoPlayerManager.instance().releaseNiceVideoPlayer();
        super.onBackPressed();
    }






}
