package com.example.nir;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.nir.databinding.ActivityGroupDataBinding;

public class GroupDataActivity extends AppCompatActivity implements GroupSettingsFragment.onCancelListener {



    private final String TAG =  GroupDataActivity.class.getSimpleName();
    private AppBarConfiguration appBarConfiguration;
    private ActivityGroupDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


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
        Log.e(TAG, String.valueOf(R.id.nav));
        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            if (bundle.getBoolean("new")) {
//                navController.getGraph().setStartDestination(R.id.GroupSettingsFragment);
//            } else {
//                navController.getGraph().setStartDestination(R.id.GroupDataFragment);
//            }
////            ept = databaseAdapter.getAllEpt(id);
////            setTitle(name);
////            eptAdapter = new EptAdapter(this, ept);
////            eptList.setAdapter(eptAdapter);
//        } else {
////            setTitle("Новая группа");
//        }

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_group_data);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void cancel() {
        super.onBackPressed();
    }
}