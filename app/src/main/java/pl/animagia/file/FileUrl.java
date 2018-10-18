package pl.animagia.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class FileUrl {

    public static String getText(String html){
        String line = getLine(html);
        int firstIndex = line.indexOf("<h2>Pliki do pobrania</h2>") + "<h2>Pliki do pobrania</h2>".length();
        String customString = "";
        if (line.equals("")) {
            return line;
        } else {
            customString = line.substring(firstIndex, line.length() );
        }
        System.out.println(customString);
        return customString;
    }

    private static String getLine(String html) {
        Boolean read = true;
        String urlLine = "";
        BufferedReader reader = new BufferedReader(new StringReader(html));
        try {
            String line = reader.readLine();
            while(line != null && read){
                if(line.contains("<h2>Pliki do pobrania</h2>")){
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

