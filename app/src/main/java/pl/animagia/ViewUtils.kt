package pl.animagia

import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.IllegalStateException
import java.util.*

fun getPlayerControlView(mediaPlayer: PlayerView): PlayerControlView {

    var controlView: PlayerControlView? = null;

    for (child in getChildrenOf(mediaPlayer)){
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


fun getChildrenOf(viewGroup: ViewGroup): List<View> {

    val children: MutableList<View> = ArrayList();

    for (i in 0 until viewGroup.childCount){
        val child = viewGroup.getChildAt(i);
        children.add(child);
    }

    return Collections.unmodifiableList(children);

}

