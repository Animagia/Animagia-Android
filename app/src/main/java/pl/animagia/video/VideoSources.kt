package pl.animagia.video

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import android.net.Uri;

fun prepareFromAsset(activity: android.support.v7.app.AppCompatActivity): MediaSource {

    val dataSourceFactory: DataSource.Factory = object : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return AssetDataSource(activity)
        }
    }

    val videoSource = ExtractorMediaSource(Uri.parse("assets:///big-buck-bunny_trailer.webm"),
            dataSourceFactory, DefaultExtractorsFactory(), null, null)

    return videoSource;

}
