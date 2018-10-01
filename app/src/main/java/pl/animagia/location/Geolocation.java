package pl.animagia.location;

public class Geolocation {

    public static final String WRONG_GEOLOCATION = "Nie jesteś w Polsce";

    public static boolean checkLocation(String source) {
        if (source.equals("")){
            return false;
        } else {
            return true;
        }
    }

}
