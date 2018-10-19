package pl.animagia;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.animagia.file.FileUrl;


import static org.junit.Assert.*;

public class FileUrlTest {

    private final String ASSERT_TEXT = "<h3>Aruku to Iu Koto</h3><p><a href=\"https://google.com\">[Animagia.pl] Aruku to Iu Koto 1080p.mkv</a></p><p>Aruku to Iu Koto i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p><h3>Łososik (Shake-chan)</h3><p><a href=\"https://wikipedia.org\">[Animagia.pl] Shake-chan 720p.mkv</a></p><p>Shake-chan i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p>";

    private final String HTML1 ="<!DOCTYPE html>\n" +
                "\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "    <head>\n" +
                "    </head>\n" +
                "\n" +
                "    <body class=\"page-template page-template-templates page-template-page-account page-template-templatespage-account-php page page-id-87 logged-in admin-bar no-customize-support\">\n" +
                "<main class=\"with-sidebar\"><article class=\"page\">\n" +
                "                    <h2>Konto premium</h2>\n" +
                "\n" +
                "\n" +
                "                    <p><strong>Aktywne.</strong> <a href=\"https://animagia.pl/amagi-brilliant-park-odc-1/\">Zacznij oglądać anime!</a></p><p><small>Jeśli chcesz zrezygnować, <a href=\"https://www.sandbox.paypal.com/myaccount/autopay\">anuluj cykliczną płatność</a> w PayPal.</small></p><h2>Zakupione anime</h2><p>Brak zakupionych anime. <a href=\"https://animagia.pl/sklep\">Przejdź do sklepu</a></p><h2>Pliki do pobrania</h2><h3>Aruku to Iu Koto</h3><p><a href=\"https://google.com\">[Animagia.pl] Aruku to Iu Koto 1080p.mkv</a></p><p>Aruku to Iu Koto i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p><h3>Łososik (Shake-chan)</h3><p><a href=\"https://wikipedia.org\">[Animagia.pl] Shake-chan 720p.mkv</a></p><p>Shake-chan i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p>\n" +
                "                \n" +
                "                \n" +
                "\n" +
                "\n" +
                "    \n" +
                "    </article></main>\n" +
                "</body>\n" +
                "\n" +
                "</html>";

    private final String HTML2 ="<!DOCTYPE html>\n" +
            "\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "    <head>\n" +
            "    </head>\n" +
            "\n" +
            "    <body class=\"page-template page-template-templates page-template-page-account page-template-templatespage-account-php page page-id-87 logged-in admin-bar no-customize-support\">\n" +
            "<main class=\"with-sidebar\"><article class=\"page\">\n" +
            "                    <h2>Konto premium</h2>\n" +
            "\n" +
            "\n" +
            "                    <p><strong>Aktywne.</strong> <a href=\"https://animagia.pl/amagi-brilliant-park-odc-1/\">Zacznij oglądać anime!</a></p><p><small>Jeśli chcesz zrezygnować, <a href=\"https://www.sandbox.paypal.com/myaccount/autopay\">anuluj cykliczną płatność</a> w PayPal.</small></p><h2>Zakupione anime</h2><p>Brak zakupionych anime. <a href=\"https://animagia.pl/sklep\">Przejdź do sklepu</a></p><h3>Aruku to Iu Koto</h3><p><a href=\"https://google.com\">[Animagia.pl] Aruku to Iu Koto 1080p.mkv</a></p><p>Aruku to Iu Koto i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p><h3>Łososik (Shake-chan)</h3><p><a href=\"https://wikipedia.org\">[Animagia.pl] Shake-chan 720p.mkv</a></p><p>Shake-chan i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p>\n" +
            "                \n" +
            "                \n" +
            "\n" +
            "\n" +
            "    \n" +
            "    </article></main>\n" +
            "</body>\n" +
            "\n" +
            "</html>";
    private final String HTML3 ="<!DOCTYPE html>\n" +
            "\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "    <head>\n" +
            "    </head>\n" +
            "\n" +
            "    <body class=\"page-template page-template-templates page-template-page-account page-template-templatespage-account-php page page-id-87 logged-in admin-bar no-customize-support\">\n" +
            "<main class=\"with-sidebar\"><article class=\"page\">\n" +
            "                    <h2>Konto premium</h2>\n" +
            "\n" +
            "\n" +
            "                    <p><strong>Aktywne.</strong> <a href=\"https://animagia.pl/amagi-brilliant-park-odc-1/\">Zacznij oglądać anime!</a></p><p><small>Jeśli chcesz zrezygnować, <a href=\"https://www.sandbox.paypal.com/myaccount/autopay\">anuluj cykliczną płatność</a> w PayPal.</small></p><h2>Zakupione anime</h2><p>Brak zakupionych anime. <a href=\"https://animagia.pl/sklep\">Przejdź do sklepu</a></p><h3>Aruku to Iu Koto</h3><p><a href=\"https://google.com\">[Animagia.pl] Aruku to Iu Koto 1080p.mkv</a></p><p>Aruku to Iu Koto i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p><h3>Łososik (Shake-chan)</h3><p><a href=\"https://wikipedia.org\">[Animagia.pl] Shake-chan 720p.mkv</a></p><p>Shake-chan i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p>\n" +
            "                \n" +
            "                \n" +
            "\n" +
            "\n" +
            "    \n" +
            "    </artice></main>\n" +
            "</body>\n" +
            "\n" +
            "</html>";
    private final String HTML4 ="<!DOCTYPE html>\n" +
            "\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "    <head>\n" +
            "    </head>\n" +
            "\n" +
            "    <body class=\"page-template page-template-templates page-template-page-account page-template-templatespage-account-php page page-id-87 logged-in admin-bar no-customize-support\">\n" +
            "<main class=\"with-sidebar\"><article class=\"page\">\n" +
            "                    <h2>Konto premium</h2>\n" +
            "\n" +
            "\n" +
            "                    <p>\n" +
            "                       <strong>Aktywne.</strong> \n" +
            "                       <a href=\"https://animagia.pl/amagi-brilliant-park-odc-1/\">Zacznij oglądać anime!</a>\n" +
            "                   </p>\n" +
            "                   <p>\n" +
            "                       <small>Jeśli chcesz zrezygnować, <a href=\"https://www.sandbox.paypal.com/myaccount/autopay\">anuluj cykliczną płatność</a> w PayPal.</small>\n" +
            "                   </p>\n" +
            "                   <h2>Zakupione anime</h2>\n" +
            "                   <p>Brak zakupionych anime. <a href=\"https://animagia.pl/sklep\">Przejdź do sklepu</a></p>\n" +
            "                   <h2>Pliki do pobrania</h2>\n" +
            "                   <h3>Aruku to Iu Koto</h3>\n" +
            "                   <p>\n" +
            "                       <a href=\"https://google.com\">[Animagia.pl] Aruku to Iu Koto 1080p.mkv</a>\n" +
            "                   </p>\n" +
            "                   <p>Aruku to Iu Koto i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p>\n" +
            "                   <h3>Łososik (Shake-chan)</h3>\n" +
            "                   <p>\n" +
            "                       <a href=\"https://wikipedia.org\">[Animagia.pl] Shake-chan 720p.mkv</a>\n" +
            "                   </p>\n" +
            "                   <p>Shake-chan i jego tłumaczenie są na otwartej licencji. Zobacz <a href=\"https://animagia.pl/credits\">uzanania autorstwa</a>.</p>\n" +
            "                \n" +
            "                \n" +
            "\n" +
            "\n" +
            "    \n" +
            "    </article></main>\n" +
            "</body>\n" +
            "\n" +
            "</html>";
    @Test
    public void getTextTest() {

        String testText = FileUrl.getText(HTML1);
        String testText2 = FileUrl.getText(HTML2);
        String testText3 = FileUrl.getText(HTML3);
        String testText4 = FileUrl.getText(HTML4);
        assertEquals(ASSERT_TEXT,testText);
        assertEquals("",testText2);
        assertEquals("",testText3);
        assertEquals(ASSERT_TEXT,testText4);

    }
}