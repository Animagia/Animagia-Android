package pl.animagia;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.VolleyError;
import pl.animagia.html.HtClient;
import pl.animagia.html.VolleyCallback;
import pl.animagia.user.CookieStorage;

import java.util.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PopupMenu.OnMenuItemClickListener,
        FragmentManager.OnBackStackChangedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private List<Anime> animeInCatalog = Collections.emptyList();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar actionBarView = findViewById(R.id.toolbar);
        setSupportActionBar(actionBarView);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, actionBarView, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setMenuItemFont(navigationView.getMenu().getItem(4));
        setMenuItemFont(navigationView.getMenu().getItem(5));


        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if(savedInstanceState == null) {

            findViewById(R.id.catalog_fetching_progress).setVisibility(View.VISIBLE);

            HtClient.fetchCatalogJson(this, new VolleyCallback() {
                private Handler retryHandler;

                @Override
                public void onSuccess(String result) {
                    animeInCatalog = HtClient.parseCatalog(result);
                    ((ViewGroup) findViewById(R.id.frame_for_content)).removeAllViews();
                    activateFragment(new CatalogFragment());
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    makeHomeButtonClickable();
                }

                @Override
                public void onFailure(VolleyError volleyError) {

                    final VolleyCallback callback = this;
                    if(retryHandler == null) {
                        retryHandler = new Handler();
                    }
                    retryHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.catalog_fetching_error).setVisibility(View.VISIBLE);
                            HtClient.fetchCatalogJson(MainActivity.this, callback);
                        }
                    }, 1500);
                }
            });
        } else {
            animeInCatalog = savedInstanceState.getParcelableArrayList("anime_parcel");
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            makeHomeButtonClickable();

            for (Fragment fragment : getSupportFragmentManager().getFragments()) {

                if (fragment.getClass().getPackage().getName().
                        startsWith(this.getClass().getPackage().getName())) {

                    FragmentTransaction fragmentTransaction =
                            getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame_for_content, fragment);
                    fragmentTransaction.commit();

                    onBackStackChanged();

                    break;
                }
            }
        }

    }


    private void makeHomeButtonClickable() {
        Toolbar actionBarView = findViewById(R.id.toolbar);
        int childCount = actionBarView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = actionBarView.getChildAt(i);
            if(v instanceof AppCompatImageButton) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDrawerHomeButtonClicked();
                    }
                });
            }
        }
    }


    void updateUsernameInHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        final View headView = navigationView.getHeaderView(0);

        TextView textView = headView.findViewById(R.id.userEmail);
        ImageView imageView = headView.findViewById(R.id.login);
        Button button = headView.findViewById(R.id.account_view_icon_button);

        if (isLogged()) {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setText(getUserDisplayName());
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
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("anime_parcel", new ArrayList<>(animeInCatalog));
    }


    private void setMenuItemFont(MenuItem item){
        TypefaceSpan typefaceSpan = new TypefaceSpan("sans-serif-light");
        SpannableString spanString = new SpannableString(item.getTitle());
        spanString.setSpan(typefaceSpan, 0, item.getTitle().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        item.setTitle(spanString);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            getSupportActionBar().setTitle(R.string.drawer_item_watch);
            activateFragment(new ShopFragment());
        } else if (id == R.id.nav_watch) {
            getSupportActionBar().setTitle(R.string.drawer_item_watch);
            activateFragment(new CatalogFragment());
        } else if (id == R.id.nav_account) {
            getSupportActionBar().setTitle(R.string.drawer_item_account);
            activateFragment(new AccountFragment());
        } else if (id == R.id.nav_contact_info) {
            getSupportActionBar().setTitle(R.string.drawer_item_contact_info);
            activateFragment(new ContactInfoFragment());
        } else if (id == R.id.nav_documents) {
            getSupportActionBar().setTitle(R.string.drawer_item_misc_info);
            activateFragment(new InfoFragment());
        } else if (id == R.id.nav_downloads) {
            getSupportActionBar().setTitle(R.string.drawer_item_downloads);
            activateFragment(new FilesFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        changeHomeButtonToHamburger();

        return true;
    }


    private void onDrawerHomeButtonClicked() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(0 == count) {
            drawerLayout.openDrawer(Gravity.LEFT);
        } else {
            onBackPressed();
        }
    }


    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(0 == count) {
            changeHomeButtonToHamburger();
        } else {
            changeHomeButtonToArrow();
        }
    }


    private void changeHomeButtonToArrow() {
        drawerToggle.setDrawerIndicatorEnabled(false);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }


    private void changeHomeButtonToHamburger() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        drawerToggle.setDrawerIndicatorEnabled(true);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        NavigationView navigationView = findViewById(R.id.nav_view);
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }


    void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        if(!(fragment instanceof CatalogFragment)) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }


    private boolean isLogged(){
        String cookie = CookieStorage.getCookie(this);
        return !(cookie.equals(CookieStorage.COOKIE_NOT_FOUND));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            CookieStorage.clearLoginCredentials(this);
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


    String getUserDisplayName() {
        String cookie = CookieStorage.getCookie(this);
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            int first_index = cookie.indexOf('=');
            int last_index = cookie.indexOf('%');
            try {
                return cookie.substring(first_index + 1, last_index);
            } catch (StringIndexOutOfBoundsException e) {
                return "";
            }
        }
        return "";
    }


    public List<Anime> getAnimeInCatalog() {
        return Collections.unmodifiableList(animeInCatalog);
    }


}
