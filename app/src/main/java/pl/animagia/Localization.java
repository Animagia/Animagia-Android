package pl.animagia;

public enum Localization {

    HONORIFICS("?altsub=no"), NO_HONORIFICS("?altsub=yes"), DUB("?dub=yes");

    private final String queryString;


    Localization(String queryString) {
        this.queryString = queryString;
    }


    public String getQueryString() {
        return queryString;
    }

}
