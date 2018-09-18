package pl.animagia.video;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class VideoUrl {

    public static String getUrl(String html){
        String line = getLine(html);
        int firstIndex = line.indexOf("source src=") + "source src=".length()+1;
        int last = line.lastIndexOf(" type=");

        String customString = "";
        if (line.equals("")) {
            customString = "http://dl3.webmfiles.org/big-buck-bunny_trailer.webm";
        } else {
            customString =  line.substring(firstIndex, last-1);
        }

        return customString;
    }

    private static String getLine(String html) {
        Boolean read = true;
        String urlLine = "";
        BufferedReader reader = new BufferedReader(new StringReader(html));
        try {
            String line = reader.readLine();
            while(line != null && read){
                if(line.contains("<source src=\"")){
                    urlLine = line;
                    read = false;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return urlLine;
    }
}
