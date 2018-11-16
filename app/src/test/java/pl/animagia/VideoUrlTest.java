package pl.animagia;

import org.junit.Test;
import pl.animagia.video.VideoUrl;

import static org.junit.Assert.*;

public class VideoUrlTest {

    private final String ASSERT_TEXT = "https://www.google.com";

    private final String HTML = "<!DOCTYPE html>\n" +
            "\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "    <head>\n" +
            "    </head>\n" +
            "\n" +
            "<body class=\"home page-template page-template-templates page-template-page-welcome page-template-templatespage-welcome-php page page-id-32\">" +
            "<main class=\"with-sidebar\"><article class=\"page\">\n" +
            "<video id='amagi' class=\"video-js vjs-16-9 vjs-big-play-centered\" style=\"width: 100%;\"\n" +
            "               controls=\"true\" oncontextmenu=\"return false;\"\n" +
            "               poster=\"\" preload=\"metadata\"\n" +
            "               data-setup='{}'>\n" +
            "            <source src=\"https://www.google.com\" type=\"video/webm\" />\n" +
            "        </video>\n" +
            "    </article>\n" +
            "</main>\n" +
            "</body>\n" +
            "\n" +
            "</html>";

    @Test
    public void getUrlTest() {
        String testText = VideoUrl.getUrl(HTML);
        assertEquals(ASSERT_TEXT,testText);
    }
}