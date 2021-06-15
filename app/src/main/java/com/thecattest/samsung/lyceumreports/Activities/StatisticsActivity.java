package com.thecattest.samsung.lyceumreports.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thecattest.samsung.lyceumreports.R;

public class StatisticsActivity extends AppCompatActivity {

    private NavController navController;

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        findViews();
        setListeners();
    }

    private void findViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.topAppBar);
    }

    private void setListeners() {
        bottomNavigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavDestination dest = navController.getCurrentDestination();
        if (dest != null) {
            if (dest.getId() == R.id.diagramFragment) {
                bottomNavigation.getMenu().findItem(R.id.diagram).setChecked(true);
            } else {
                bottomNavigation.getMenu().findItem(R.id.table).setChecked(true);
            }
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.diagram) {
                navController.navigate(R.id.action_table_to_diagram);
                return true;
            }
            if (item.getItemId() == R.id.table) {
                navController.navigate(R.id.action_diagram_to_table);
                return true;
            }
        } catch (java.lang.IllegalArgumentException ignored) {}
        return false;
    }
}