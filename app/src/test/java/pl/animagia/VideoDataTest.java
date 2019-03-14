package pl.animagia;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoDataTest {

    private VideoData shake;
    private String title = "Shake-chan";
    private String poster = "shake.jpg";
    private String video = "syake.mkv";
    private int numberOfEpisodes = 1;

    @Before
    public void setUp() throws Exception {
        shake = new VideoData(title, poster, video, numberOfEpisodes);
    }

    @Test
    public void getTitleTest() {
        String shakeTitle = shake.getTitle();
        assertEquals(title, shakeTitle);
    }

    @Test
    public void getPosterAsssetUriTest() {
        String shakePoster = shake.getThumbnailAsssetUri();
        assertEquals(poster, shakePoster);
    }

    @Test
    public void getVideoUrlTest() {
        String shakeVideo = shake.getVideoUrl();
        assertEquals(video,shakeVideo);
    }

    @Test
    public void getEpisodesTest() {
        int shakeNumberOfEpisodes = shake.getEpisodes();
        assertEquals(numberOfEpisodes,shakeNumberOfEpisodes);
    }
}