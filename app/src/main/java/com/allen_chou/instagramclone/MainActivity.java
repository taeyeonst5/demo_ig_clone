package com.allen_chou.instagramclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.allen_chou.instagramclone.Fragment.HomeFragment;
import com.allen_chou.instagramclone.Fragment.NotificationFragment;
import com.allen_chou.instagramclone.Fragment.ProfileFragment;
import com.allen_chou.instagramclone.Fragment.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectFragment = null;
    private boolean isTryExitApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(createNavigationItemSelectedListener());

        Bundle intent = getIntent().getExtras();
        //導去ProfileFragment一定要有UserId...
        if (intent != null) {
            String publisher = intent.getString(CommentActivity.PUBLISHER_ID);

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileId", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    @NonNull
    private BottomNavigationView.OnNavigationItemSelectedListener createNavigationItemSelectedListener() {
        return new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        selectFragment = new HomeFragment();
                        break;
                    case R.id.navigation_search:
                        selectFragment = new SearchFragment();
                        break;
                    case R.id.navigation_add:
                        selectFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;
                    case R.id.navigation_favorite:
                        selectFragment = new NotificationFragment();
                        break;
                    case R.id.navigation_profile:
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selectFragment = new ProfileFragment();
                        break;
                }
                if (selectFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectFragment).commit();
                }
                return true;
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (!isTryExitApp) {
            Toast.makeText(this, "Click again to quit app", Toast.LENGTH_SHORT).show();
            isTryExitApp = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isTryExitApp = false;
                }
            }, 5000);
        } else {
            super.onBackPressed();
        }
    }
}
