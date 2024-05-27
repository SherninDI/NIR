package com.example.nir;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.nir.databinding.ActivityGroupDataBinding;

public class GroupDataActivity extends AppCompatActivity  {



    private final String TAG =  GroupDataActivity.class.getSimpleName();
    private AppBarConfiguration appBarConfiguration;
    private ActivityGroupDataBinding binding;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

//        Button button = findViewById(R.id.cancel_settings);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "cancel");
//            }
//        });

        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_group_data);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_group_data);
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Bundle bundle = getIntent().getExtras();
            position = bundle.getInt("group_position");
            if (bundle != null) {
                if (destination.getLabel().equals("Название группы")) {
                    NavArgument argument = new NavArgument.Builder().setDefaultValue(position).build();
                    destination.addArgument("group_position", argument);
                    return;
                } else if (destination.getLabel().equals("Список воздействий")) {
                    NavArgument argument = new NavArgument.Builder().setDefaultValue(position).build();
                    destination.addArgument("group_position", argument);

                } else if (destination.getLabel().equals("Новое воздействие")) {
                    NavArgument argument = new NavArgument.Builder().setDefaultValue(position).build();
                    destination.addArgument("group_position", argument);
                }
            }


        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

        } else {
//            setTitle("Новая группа");
        }

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ept_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_group_data);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    
}