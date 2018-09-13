package pl.animagia;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import pl.animagia.error.Alerts;
import pl.animagia.html.HTML;
import pl.animagia.html.VolleyCallback;


public class TermsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terms, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTermsOnText();
    }

    public void setTermsOnText(){
        String url = "https://animagia.pl/regulamin/";
        HTML.getHtml(url, getContext(), new VolleyCallback() {
            @Override
            public void onSuccess (String result){
                TextView textView = getView().findViewById(R.id.textView2);
                String text = getTerms(result);
                textView.setText(text);
                textView.setMovementMethod(new ScrollingMovementMethod());
            }

            @Override
            public void onFailure(VolleyError volleyError) {
                DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setTermsOnText();
                    }
                };

                if (volleyError instanceof NoConnectionError) {
                    Alerts.internetConnectionError(getContext(), onClickTryAgain);
                }
            }
        });
    }

    public String getTerms(String html) {
        Boolean read = true;
        StringBuilder terms = new StringBuilder();
        BufferedReader reader = new BufferedReader(new StringReader(html));

        try {
            String line = reader.readLine();
            while(line != null && read){
                line = reader.readLine();
                if (line.contains("<article ")) {
                    line = reader.readLine();
                    read = false;
                    while(!line.contains("</article>")){
                        terms.append(line).append("\n");
                        line = reader.readLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return terms.toString();
    }
}
