package pl.animagia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.android.billingclient.api.BillingClient;
import pl.animagia.user.Cookies;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener {

    private static final String SELECTED_ITEM = "selected item";

    BillingClient billingClient;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        int index = savedInstanceState == null ? R.id.nav_watch :
                savedInstanceState.getInt(SELECTED_ITEM, R.id.nav_watch);
        simulateClickOnDrawerItem(index);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final View headView = navigationView.getHeaderView(0);

        TextView textView = headView.findViewById(R.id.userEmail);
        ImageView imageView = headView.findViewById(R.id.login);
        Button button = headView.findViewById(R.id.account_view_icon_button);


        if (isLogged()) {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setText(getUsername());
        }
        else {
            textView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
            textView.setText(R.string.guest);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,v);
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                if (isLogged()) {
                    popupMenu.inflate(R.menu.logged_user_menu);
                }
                else {
                    popupMenu.inflate(R.menu.unlogged_user_menu);
                }
                popupMenu.show();
            }
        });

        setMenuItemFont(navigationView.getMenu().getItem(4));
        setMenuItemFont(navigationView.getMenu().getItem(5));

    }


    private void setMenuItemFont(MenuItem item){
        TypefaceSpan typefaceSpan = new TypefaceSpan("sans-serif-light");
        SpannableString spanString = new SpannableString(item.getTitle());
        spanString.setSpan(typefaceSpan, 0, item.getTitle().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        item.setTitle(spanString);
    }


    private void simulateClickOnDrawerItem(int itemId) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(itemId).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().findItem(itemId));
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_shop) {
            getSupportActionBar().setTitle("  Sklep");
            //getSupportActionBar().setIcon(R.drawable.ic_shopping_basket);
            activateFragment(new ShopFragment());
        } else if (id == R.id.nav_watch) {
            getSupportActionBar().setTitle("  Oglądaj");
            //getSupportActionBar().setIcon(R.drawable.ic_video_library);
            activateFragment(new CatalogFragment());
        } else if (id == R.id.nav_account) {
            getSupportActionBar().setTitle("  Konto");
            //getSupportActionBar().setIcon(R.drawable.ic_account_box);
            activateFragment(new AccountFragment());
        } else if (id == R.id.nav_contact_info) {
            getSupportActionBar().setTitle("  Kontakt");
            //getSupportActionBar().setIcon(R.drawable.ic_mail_outline);
            activateFragment(new ContactInfoFragment());
        } else if (id == R.id.nav_documents) {
            getSupportActionBar().setTitle("  Informacje");
            //getSupportActionBar().setIcon(R.drawable.ic_info_outline);
            activateFragment(new InfoFragment());
        } else if (id == R.id.nav_downloads) {
            getSupportActionBar().setTitle("  Pliki");
            //getSupportActionBar().setIcon(R.drawable.ic_file_download);
            activateFragment(new FilesFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        int idOfCheckedItem = getIdOfCheckedItem(menu);

        if(idOfCheckedItem != -1) {
            outState.putInt(SELECTED_ITEM, idOfCheckedItem);
        }

    }


    private static int getIdOfCheckedItem(Menu menu) {
        for(int index = 0; index<menu.size(); index++) {
            MenuItem item = menu.getItem(index);
            if(item.isChecked()) {
                return item.getItemId();
            } else if(item.hasSubMenu()) {
                int idFromSubMenu = getIdOfCheckedItem(item.getSubMenu());
                if(idFromSubMenu != -1) {
                    return idFromSubMenu;
                }
            }
        }
        return -1;
    }

    private boolean isLogged(){
        boolean logIn = false;
        String cookie = Cookies.getCookie(Cookies.LOGIN, this);
        System.out.println(cookie);
        if (!cookie.equals(Cookies.COOKIE_NOT_FOUND)){
            logIn = true;
        }

        return logIn;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            Cookies.removeCookie(Cookies.LOGIN, this);
            Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
        else if (id == R.id.login_button) {
            activateFragment(new LoginFragment());
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private String getUsername() {
        String cookie = Cookies.getCookie(Cookies.LOGIN, this);
        if (!cookie.equals(Cookies.COOKIE_NOT_FOUND)){
            int first_index = cookie.indexOf('=');
            int last_index = cookie.indexOf('%');
            return cookie.substring(first_index + 1, last_index);
        }
        return "";
    }
}
