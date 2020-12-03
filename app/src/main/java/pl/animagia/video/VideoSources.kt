package pl.animagia.video

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource;
import android.net.Uri
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util

fun prepareFromAsset(activity: android.support.v7.app.AppCompatActivity, url: String): MediaSource {

    val dataSourceFactory: DataSource.Factory = DataSource.Factory {
        val source = DefaultHttpDataSource(Util.getUserAgent(activity, "animagia"), null)

        source.setRequestProperty("Range", "0-1023")

        source
    }

    val videoSource : MediaSource
        videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
    return videoSource
}
