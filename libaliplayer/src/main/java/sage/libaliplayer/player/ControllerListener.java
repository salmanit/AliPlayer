package sage.libaliplayer.player;

import android.view.View;

/**
 * Created by Sage on 2017/8/8.
 * Description:
 */

public interface ControllerListener {

    public void onClick(View v);

    public void playerState(int playerState);
}
