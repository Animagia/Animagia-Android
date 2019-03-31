package pl.animagia.file;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class FileUrl { //FIXME is this still used anywhere?

    private FileUrl(){
    }

    public static String getText(String html){
        List<String> lines = getLines(html);
        String customString = "";
        if (lines.isEmpty()) {
            return "";
        }
        else if (!lines.get(lines.size()-1).contains("</article>")){
            return "";
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : lines)
            {
                stringBuilder.append(s);
            }
            customString = stringBuilder.toString();
            int firstIndex = customString.indexOf("<h2>Pliki do pobrania</h2>") + "<h2>Pliki do pobrania</h2>".length();
            int lastIndex = customString.lastIndexOf("</p>") + "</p>".length();
            customString =  customString.substring(firstIndex, lastIndex);
            customString = customString.replace("  ", "").replace("> ",">").replace(" <a","  <a").replace(" <", "<");
        }
        return customString;
    }

    private static List<String> getLines(String html) {
        Boolean read = true;
        List<String> urlLines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(html));
        try {
            String line = reader.readLine();
            while(line != null && read){
                if(line.contains("<h2>Pliki do pobrania</h2>")){
                    urlLines.add(line);
                    read = false;
                }
                line = reader.readLine();
            }
            read = true;
            while(line != null && read){
                urlLines.add(line);
                if(line.contains("</article>")){
                    read = false;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return urlLines;
    }
}

