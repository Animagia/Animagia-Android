package pl.animagia;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.animagia.location.Geolocation;
import pl.animagia.video.VideoUrl;

import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

@RunWith(AndroidJUnit4.class)
public class GeolocationTest{
    CatalogFragment fragment;
    private final String HTML =  "<!DOCTYPE html \n" +
            "\tPUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\"\n" +
            "\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">\n" +
            "<html>\n" +
            "<head>\n" +
            "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-2\" />\n" +
            "\t<meta name=\"Description\" content=\"Opis zawartości strony\" />\n" +
            "\t<meta name=\"Keywords\" content=\"Wyrazy kluczowe\" />\n" +
            "\t<meta name=\"Author\" content=\"Autor strony\" />\n" +
            "\t<title>Tytuł strony</title>\n" +
            "</head>\n" +
            "<video id='amagi' class=\"video-js vjs-16-9 vjs-big-play-centered\" style=\"width: 100%;\"\n" +
            "               controls=\"true\" oncontextmenu=\"return false;\"\n" +
            "               data-setup='{}'>\n" +
            "            <source src=\"\" type=\"video/webm\" />\n" +
            "        </video>\n" +
            "<frameset cols=\"180,*\" border=\"0\" frameborder=\"0\" framespacing=\"0\">\n" +
            "  <frame name=\"spis\" noresize=\"noresize\" frameborder=\"0\" src=\"spis.html\" />\n" +
            "  <frame name=\"strona\" noresize=\"noresize\" frameborder=\"0\" src=\"home.html\" />\n" +
            "  <noframes><body><a href=\"spis.html\">spis treści</a></body></noframes>\n" +
            "</frameset>\n" +
            "</html>";

    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, false, false);


    @Before
    public void setup() {
        Intent intent = new Intent();
        rule.launchActivity(intent);
    }


    @Test
    public void getLoginAndPasswordTest() {
        fragment = new CatalogFragment();

        rule.getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frame_for_content, fragment).commitAllowingStateLoss();

        final String url = VideoUrl.getUrl(HTML);

        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!Geolocation.checkLocation(url)){
                                    fragment.setText(Geolocation.WRONG_GEOLOCATION);
                                }
                            }
                        });
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                }
            };
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView text = fragment.getActivity().findViewById(R.id.geo_text_view);
        String txt = text.getText().toString();
        Assert.assertEquals(Geolocation.WRONG_GEOLOCATION, txt);

    }

}
