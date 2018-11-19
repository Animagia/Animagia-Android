package pl.animagia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.exoplayer2.C;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class VideoInfoTransferTest {

    @Rule
    public ActivityTestRule<FullscreenPlaybackActivity> rule =
            new ActivityTestRule<>(FullscreenPlaybackActivity.class, false, false);

    @Test
    public void infoPassedViaIntent_isIdenticalToOriginal() {

        Intent intent = new Intent();
        final String film_title = "A feature film";
        VideoData originalData =
                new VideoData(film_title, "file:///android_asset/oscar_nord.jpg", "https://animagia.pl",1);
        final String url = "http://dl3.webmfiles.org/big-buck-bunny_trailer.webm";
        intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, originalData);
        intent.putExtra(VideoData.NAME_OF_URL, url);
        Activity activityUnderTest = rule.launchActivity(intent);


        Parcelable dataReceivedViaIntent =
                activityUnderTest.getIntent().getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);
        Assert.assertEquals(originalData, dataReceivedViaIntent);


    }

}
