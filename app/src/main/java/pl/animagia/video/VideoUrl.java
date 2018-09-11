package pl.animagia.video;

import java.util.concurrent.ExecutionException;

import pl.animagia.html.HTML;

public class VideoUrl {

    public static String getUrl(){
        String url;
        String line = null;
        try {
            line = new HTML().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        int firstIndex = line.indexOf("source src=") + "source src=".length()+1;
        int last = line.lastIndexOf(" type=");

        return url = line.substring(firstIndex, last-1);
    }
}
