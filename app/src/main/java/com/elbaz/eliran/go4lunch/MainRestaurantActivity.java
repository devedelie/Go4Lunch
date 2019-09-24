package com.elbaz.eliran.go4lunch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.adapters.PageAdapter;
import com.elbaz.eliran.go4lunch.auth.ProfileSettingsActivity;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.fragments.MapViewFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainRestaurantActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    View rootView;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    // Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    private int[] tabIcons = {R.drawable.ic_mapview_icon, R.drawable.ic_listview_icon, R.drawable.ic_workmates_icon};

    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public Context mContext;
    public TabLayout tabs;
    public Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Context for the fragments
        mContext = this;
        // Get RootView for snackBarMessage
        rootView = getWindow().getDecorView().getRootView();
        // Configure the basic design structure of the app with tabs and viewPager
        this.configureViewPagerAndTabs();
        this.configureToolbarWithDrawer();
        this.configureDrawerLayoutAndNavigationView();
//        this.setupTabIcons();
        this.verifyPlacesSDK();
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_main_restaurant; }

    public void verifyPlacesSDK(){
        // Verify OR Initialize "Places SDK" on the device
        if (!Places.isInitialized()) {
            // Initialize Places.
            Places.initialize(getApplicationContext(), BuildConfig.GOOGLE_API_KEY);
            // Create a new Places client instance.
            PlacesClient placesClient = Places.createClient(this);
        }
    }

    // Multifunction Toolbar with drawer and search
    protected void configureToolbarWithDrawer(){
        // Get the toolbar view inside the activity layout
        this.toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

    /**
     * ViewPager configuration + Tab Layout
     */
    protected void configureViewPagerAndTabs(){
        //Get ViewPager from layout
        ViewPager pager = findViewById(R.id.activity_main_restaurant_viewpager);
        //Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(mContext, getSupportFragmentManager()));
        // Set the offscreenLimit - loads 2 fragments simultaneously offScreen, to improves fluency of visual load
        pager.setOffscreenPageLimit(2);

        //Get TabLayout from layout
        TabLayout tabs= findViewById(R.id.activity_main_restaurant_tabs);
        //Glue TabLayout and ViewPager together
        tabs.setupWithViewPager(pager);
        //Design purpose. Tabs have the same width
        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

//    // Set Icons for tabs
//    protected void setupTabIcons() {
//        this.tabs.getTabAt(0).setIcon(tabIcons[0]);
//        this.tabs.getTabAt(1).setIcon(tabIcons[1]);
//        this.tabs.getTabAt(2).setIcon(tabIcons[2]);
//    }

    /**
     * Inflate the top-menu (menu with search and parameters icons)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu and add it to the Toolbar
        getMenuInflater().inflate(R.menu.menu_activity_main_restaurant, menu);
        return true;
    }

    /**
     * Navigation drawer config
     */
    protected void configureDrawerLayoutAndNavigationView(){
        // Configure drawer layout
        this.drawerLayout = findViewById(R.id.main_restaurant_activity_drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Configure NavigationView & set item selection listener
        this.navigationView = findViewById(R.id.drawer_restaurant_main_activity);
        View headerView = this.navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.navigation_header_name);
        TextView userEmail = headerView.findViewById(R.id.navigation_header_email);
        ImageView userImage = headerView.findViewById(R.id.navigation_header_image);
        // set user name and email
        userName.setText(this.getCurrentUser().getDisplayName());
        userEmail.setText(this.getCurrentUser().getEmail());
        // Set Image
        if (this.getCurrentUser() != null) {
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(userImage);
            }
        }
        // Set listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Drawer item selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int order = menuItem.getOrder();
        Log.d(TAG, "Test onNavigationItemSelected: "+ order);
        switch (order){
            case 0:
                // Your lunch action
                break;
            case 1:
                // settings
                this.goToProfileSettings();
                break;
            case 2:
                // logout
                this.signOutUserFromFirebase();
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goToProfileSettings(){
        Log.d(TAG, "goToProfileSettings: ");
        Intent intent = new Intent(this, ProfileSettingsActivity.class);
        startActivity(intent);
    }

    // OptionMenu item selection (Search places auto-complete)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int order = item.getOrder();
        if (order == 0){
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.OPENING_HOURS,
                    Place.Field.WEBSITE_URI,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.PRICE_LEVEL,
                    Place.Field.RATING,
                    Place.Field.LAT_LNG);

            // Bias results to Paris region (use 'bounds' variable in below filter)
            RectangularBounds bounds = RectangularBounds.newInstance(
                    new LatLng(48.832304, 2.239726),
                    new LatLng(48.900962, 2.42124));

            // Start the autocomplete intent. (OVERLAY + ESTABLISHMENT + FR)
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setCountry("FR")
                    .setLocationBias(bounds)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            Log.d(TAG, "onOptionsItemSelected: check");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: code is " + requestCode +" "+ resultCode);
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "onActivityResult Place: " + place.getLatLng().latitude +" " + " " + place.getLatLng().longitude + place.getName() + ", " + place.getId() +" "+ place.getAddress()+ " " + place.getPhoneNumber()+ " " + place.getWebsiteUri() + " " + place.getPriceLevel()+ " " + place.getRating());
                moveCamera(place.getLatLng(), 15f);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, "onActivityResult Error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void moveCamera (LatLng latLng, float zoom){
        // Call method in fragment - Move to location after selection
        MapViewFragment mapViewFragment = new MapViewFragment();
        (mapViewFragment).moveCamera(new LatLng(latLng.latitude, latLng.longitude), zoom);
    }


    // --------------------
    // REST REQUESTS
    // --------------------
    // 1 - Create http requests (SignOut & Delete)

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(origin == SIGN_OUT_TASK)
                    finish();
            }
        };
    }

}
