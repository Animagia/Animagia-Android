package pl.animagia;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;
import pl.animagia.user.Cookies;
import pl.animagia.video.VideoUrl;


import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(AndroidJUnit4.class)
public class HTMLTest {
    private CatalogFragment fragment;
    private final String url = "https://animagia.pl/";
    private String videoUrlWithBadToken = "";
    private String videoUrlWithGoodToken = "";
    private final String firstUrl = "https://animagia.pl/";
    private final String secondUrl = "https://animagia.pl/";
    private final String cookie = Cookies.LOGIN;



    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, false, false);


    @Test
    public void getHtmlCookieTest(){
        Intent intent = new Intent();
        rule.launchActivity(intent);
        fragment = new CatalogFragment();
        rule.getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frame_for_content, fragment).commitAllowingStateLoss();
        final CountDownLatch signalBad = new CountDownLatch(1);
        HTML.getHtmlCookie(url, InstrumentationRegistry.getContext(), Cookies.COOKIE_NOT_FOUND, new VolleyCallback() {
            @Override
            public void onSuccess (String result){
                videoUrlWithBadToken =  VideoUrl.getUrl(result);
                System.out.println(videoUrlWithBadToken);
                signalBad.countDown();
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                getHtmlCookieTest();
            }
        });
        try {
            signalBad.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final CountDownLatch signalGood = new CountDownLatch(1);
        HTML.getHtmlCookie(url, InstrumentationRegistry.getContext(), cookie, new VolleyCallback() {
            @Override
            public void onSuccess (String result){
                videoUrlWithGoodToken =  VideoUrl.getUrl(result);
                System.out.println(videoUrlWithGoodToken);
                signalGood.countDown();
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                getHtmlCookieTest();
            }
        });
        try {
            signalGood.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertThat(videoUrlWithBadToken,anyOf(containsString(firstUrl), containsString(secondUrl)));
        Assert.assertThat(videoUrlWithGoodToken,anyOf(containsString(firstUrl), containsString(secondUrl)));
    }
}