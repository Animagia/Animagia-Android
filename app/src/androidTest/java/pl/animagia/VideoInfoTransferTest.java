package pl.animagia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

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
                new VideoData(film_title, Uri.parse("file:///android_asset/oscar_nord.jpg"));
        intent.putExtra(VideoData.NAME_OF_INTENT_EXTRA, originalData);
        Activity activityUnderTest = rule.launchActivity(intent);


        Parcelable dataReceivedViaIntent =
                activityUnderTest.getIntent().getParcelableExtra(VideoData.NAME_OF_INTENT_EXTRA);
        Assert.assertEquals(originalData, dataReceivedViaIntent);


    }

}
