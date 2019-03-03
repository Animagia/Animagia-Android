package pl.animagia

import android.view.ViewGroup
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import java.lang.IllegalStateException

fun getPlayerControlView(viewGroup: ViewGroup): PlayerControlView {

    var controlView: PlayerControlView? = null;

    for (i in 0 until viewGroup.childCount){
        val child = viewGroup.getChildAt(i);
        if(child is PlayerControlView) {
            if(controlView != null) {
                throw IllegalStateException("Media player has too many controllers.")
            }
            controlView = child;
        }
    }

    if(controlView == null) {
        throw IllegalStateException("Media player has no controller.")
    }

    return controlView;

}
