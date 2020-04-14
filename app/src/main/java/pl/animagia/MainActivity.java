package pl.animagia;

import android.os.Bundle;
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
import android.widget.*;
import pl.animagia.user.CookieStorage;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PopupMenu.OnMenuItemClickListener,
        FragmentManager.OnBackStackChangedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private SingleProductFragment fragmentSavedBeforeRotation;
    private SingleProductFragment optionalFragmentToImmediatelyShow;

    public static String OPTIONAL_NAME_OF_PRODUCT_TO_IMMEDIATELY_SHOW = "productToShow";


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

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if(getIntent().hasExtra(OPTIONAL_NAME_OF_PRODUCT_TO_IMMEDIATELY_SHOW)) {
            Anime a = Anime.valueOf(getIntent().getStringExtra
                    (OPTIONAL_NAME_OF_PRODUCT_TO_IMMEDIATELY_SHOW));
            startByShowingProduct(a);
        } else if(!rebuildFromFragments(savedInstanceState)) {
            activateFragment(new CatalogFragment());
        }

    }


    private void startByShowingProduct(Anime a) {
        optionalFragmentToImmediatelyShow = SingleProductFragment.newInstance(a);
        ShopFragment shop = new ShopFragment();
        activateFragment(shop);
    }


    private boolean rebuildFromFragments(final Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            return false;
        }
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if(f instanceof SingleProductFragment) {
                fragmentSavedBeforeRotation = (SingleProductFragment) f;
                ShopFragment shop = new ShopFragment();
                activateFragment(shop);
                return true;
            } else if(f instanceof TopLevelFragment) {
                activateFragment(f);
                return true;
            }
        }
        return false;
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if(optionalFragmentToImmediatelyShow != null) {
            changeHomeButtonToArrow();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_for_content, optionalFragmentToImmediatelyShow)
                    .addToBackStack(null).commit();
            optionalFragmentToImmediatelyShow = null;
        } else if (fragmentSavedBeforeRotation != null) {
            changeHomeButtonToArrow();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            //apparently no need to re-add to back stack after orientation change
            fragmentTransaction.replace(R.id.frame_for_content, fragmentSavedBeforeRotation)
                    .commit();
            fragmentSavedBeforeRotation = null;
        }

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
        }
    }


    void changeHomeButtonToArrow() {
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
    }


    void activateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_for_content, fragment);
        fragmentTransaction.commit();
    }


    private boolean isLogged(){
        boolean logIn = false;
        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, this);
        System.out.println(cookie);
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            logIn = true;
        }

        return logIn;
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


    String getUsername() {
        String cookie = CookieStorage.getCookie(CookieStorage.LOGIN_CREDENTIALS_KEY, this);
        if (!cookie.equals(CookieStorage.COOKIE_NOT_FOUND)){
            int first_index = cookie.indexOf('=');
            int last_index = cookie.indexOf('%');
            return cookie.substring(first_index + 1, last_index);
        }
        return "";
    }
}
