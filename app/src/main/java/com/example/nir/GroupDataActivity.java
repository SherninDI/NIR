package com.example.nir;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.nir.databinding.ActivityGroupDataBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.PendingIntent.getActivity;

public class GroupDataActivity extends AppCompatActivity  {

    private final String TAG =  GroupDataActivity.class.getSimpleName();
    private final String FILE_NAME = "groups.grf";
    private final String SAVE_FILE_NAME = "save.grf";
    private final String POSITION = "group_position";
    private static final String FLAG = "flag";
    private AppBarConfiguration appBarConfiguration;
    private ActivityGroupDataBinding binding;

    private int position;
    private String name;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        TextView titleGroup = findViewById(R.id.group_title);

        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_group_data);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_group_data);
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.getNavController());
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Bundle bundle = getIntent().getExtras();
            position = bundle.getInt(POSITION);

            if (destination.getLabel().equals("Название группы")) {
                NavArgument argument = new NavArgument.Builder().setDefaultValue(position).build();
                destination.addArgument(POSITION, argument);
            } else if (destination.getLabel().equals("Список воздействий")) {
                NavArgument argument = new NavArgument.Builder().setDefaultValue(position).build();
                destination.addArgument(POSITION, argument);
            } else if (destination.getLabel().equals("Новое воздействие")) {
                NavArgument argument = new NavArgument.Builder().setDefaultValue(position).build();
                destination.addArgument(POSITION, argument);
            }
        });


        try {
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            byte[] group = fileHandler.readBytesFromPosition(position);
            GroupFormat groupFormat = new GroupFormat(group);
            name = groupFormat.readTitle();
            fileHandler.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        titleGroup.setText(getString(R.string.group_title, position, name));

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_save_group:
                saveGroup();
                Toast.makeText(this, "Группа сохранена", Toast.LENGTH_SHORT).show();

//                if (checkTitle()) {
//
//                } else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setMessage("Навзание группы пустое")
//                            .setTitle("Ошибка")
//                            .setCancelable(true)
//                            .setPositiveButton("К названию", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    NavController navController = Navigation.findNavController(GroupDataActivity.this, R.id.nav_host_fragment_content_group_data);
//                                    navController.navigate(R.id.GroupNameFragment);
//                                }
//                            });
//
//                    AlertDialog alert = builder.create();
//                    alert.show();
//                }

                return true;
            case R.id.action_del_ept:
                Toast.makeText(this, "Группа удалена", Toast.LENGTH_SHORT).show();
                deleteGroup();
                return true;
            case R.id.action_group_list:
                if (checkTitle()) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Сохранить группу перед переходом?")
                            .setTitle("Сохранение")
                            .setCancelable(true)
                            .setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(GroupDataActivity.this, DataActivity.class);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveGroup();
                                    Intent intent = new Intent(GroupDataActivity.this, DataActivity.class);
                                    setResult(RESULT_OK, intent);
                                    finish();
//                                Intent intent = new Intent(GroupDataActivity.this, DataActivity.class);
//                                startActivity(intent);
                                }
                            });

                    AlertDialog alert = builder.create();

                    alert.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Название группы пустое")
                            .setTitle("Ошибка")
                            .setCancelable(true)
                            .setPositiveButton("К названию", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    NavController navController = Navigation.findNavController(GroupDataActivity.this, R.id.nav_host_fragment_content_group_data);
                                    navController.navigate(R.id.GroupNameFragment);
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveGroup() {
        try {
            File file = new File(this.getFilesDir(), FILE_NAME);
            FileHandler fileHandler = new FileHandler(file);
            byte[] groupsBytes = fileHandler.readBytes(51200);
            File saveFile = new File(this.getFilesDir(), SAVE_FILE_NAME);
            try(FileOutputStream fos = new FileOutputStream(saveFile)) {
                fos.write(groupsBytes);
            }
            catch(IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            fileHandler.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkTitle() {
        byte[] group = new byte[512];
        GroupFormat groupFormat;
        File file = new File(this.getFilesDir(), SAVE_FILE_NAME);
        FileHandler fileHandler = new FileHandler(file);
        try {
            group = fileHandler.readBytesFromPosition(position);
            groupFormat = new GroupFormat(group);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.close();
        if (groupFormat.readTitleLength() == 0) {
            return false;
        } else {
            return true;
        }
    }


    public void deleteGroup() {
        byte[] group = new byte[512];
        GroupFormat groupFormat = new GroupFormat(group);
        groupFormat.writeEmpty();
        groupFormat.writeFileId();
        groupFormat.writeNumber(position);
        groupFormat.writeCRC();

        File file = new File(this.getFilesDir(), SAVE_FILE_NAME);
        FileHandler fileHandler = new FileHandler(file);
        try {
            fileHandler.writeBytesToPosition(group, position);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.close();
        Intent intent = new Intent(this, DataActivity.class);
        setResult(RESULT_OK, intent);
        finish();

    }
    
}