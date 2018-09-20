package pl.animagia;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECTED_ITEM = "selected item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        View headView = navigationView.getHeaderView(0);
        ImageView image = headView.findViewById(R.id.login);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( MainActivity.this, LoginActivity.class);
                startActivity(intent);
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

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
            activateFragment(new ShopFragment());
        } else if (id == R.id.nav_watch) {
            activateFragment(new CatalogFragment());
        } else if (id == R.id.nav_account) {

        } else if (id == R.id.nav_contact_info) {

        } else if (id == R.id.nav_documents) {
            activateFragment(new DocumentListFragment());
        } else if (id == R.id.nav_terms_and_conditions) {
            activateFragment(new TermsFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void activateFragment(Fragment fragment) {
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

}
