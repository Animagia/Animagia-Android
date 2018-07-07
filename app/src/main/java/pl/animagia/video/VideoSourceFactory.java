package pl.animagia.video;

import android.app.Activity;
import android.net.Uri;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

/**
 * @deprecated To be replaced by Kotlin implementation.
 */
public class VideoSourceFactory {

    private VideoSourceFactory() {
    }

    private static MediaSource prepareRemoteMediaSource(Activity activity) {

        //FIXME unused code

        DefaultBandwidthMeter optionalBandwidthMeter = null;

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(activity,
                Util.getUserAgent(activity, "Animagia"), optionalBandwidthMeter);


        File file = new File("/storage/emulated/0/Movies/calc/b01.mkv");
        Uri mp4VideoUri = Uri.fromFile(file);

        mp4VideoUri = Uri.parse("http://dl3.webmfiles.org/big-buck-bunny_trailer.webm");


        return new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mp4VideoUri);
    }

}
