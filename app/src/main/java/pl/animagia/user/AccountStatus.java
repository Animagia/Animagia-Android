package pl.animagia.user;

public enum AccountStatus {

    ACTIVE("Aktywne"),
    EXPIRING("Wygasające"),
    INACTIVE("Nieaktywne"),
    UNKNOWN("");

    private final String friendlyName;

    AccountStatus(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    static String getPrefKey() {
        return "accountStatus";
    }

}
