package pl.animagia;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoInfoTransferTest {

    @Rule
    public ActivityTestRule<FullscreenPlaybackActivity> rule =
            new ActivityTestRule<>(FullscreenPlaybackActivity.class, false, false);

    @Test
    public void infoPassedViaIntent_isIdenticalToOriginal() {

        Intent intent = new Intent();
        final String film_title = "A feature film";
        Anime originalData = Anime.CHUUNIBYOU;
//                new Anime(
//                        film_title, "file:///android_asset/clapperboard.jpg", "https://animagia.pl",1,
//                        "", "", "", "", "", "", "", 1);
        final String url = "http://dl3.webmfiles.org/big-buck-bunny_trailer.webm";
        intent.putExtra("vd-dummy", originalData);
        Activity activityUnderTest = rule.launchActivity(intent);


        Parcelable dataReceivedViaIntent =
                activityUnderTest.getIntent().getParcelableExtra(Anime.NAME_OF_INTENT_EXTRA);
        Assert.assertEquals(originalData, dataReceivedViaIntent);


    }

}
