package pl.animagia.user;

public class User {

    private static User INSTANCE;
    private String cookie;

    private User() {
    }

    public static User getInstance() {
        if (INSTANCE == null)
            synchronized (User.class) {
                if (INSTANCE == null)
                    INSTANCE = new User();
            }
        return INSTANCE;
    }

    public void setCookie(String cookie){
        this.cookie = cookie;
    }

    public String getCookie(){
        return this.cookie;
    }
}