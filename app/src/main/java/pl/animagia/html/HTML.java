
package pl.animagia.html;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTML extends AsyncTask<Void, Void, String> {


    @Override
    protected String doInBackground(Void... voids) {
        URL url = null;
        HttpsURLConnection urlConnection = null;
        String htmll = "";
        try {
            url = new URL("https://animagia.pl/amagi-brilliant-park-odc-1/");
            urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder html = new StringBuilder();
            for (String line; (line = reader.readLine()) != null;) {
                if(line.contains("https://static.animagia.pl/video/stream/serve_stream.php/Amagi1")){
                    html.append(line);
                }
            }
            in.close();
            htmll = html.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmll;
    }
}