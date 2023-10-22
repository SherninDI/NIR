package com.example.nir;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private final String TAG = GroupActivity.class.getSimpleName();
    EditText editName;
    Button add;
    Button settings;
    Button save;
    Button delete;
    GridLayout grid;


    RecyclerView eptList;
    EptAdapter eptAdapter;
    private DatabaseAdapter databaseAdapter;
    public List<String> ept = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        editName = findViewById(R.id.text_name);
        add = findViewById(R.id.add_ept);
        settings = findViewById(R.id.settings);
        save = findViewById(R.id.save_changes);
        delete = findViewById(R.id.delete);
        grid = findViewById(R.id.grid1);

        eptList = findViewById(R.id.ept_list);

        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int id = bundle.getInt("group");
            String name = bundle.getString("name");
            ept = databaseAdapter.getAllEpt(id);
            setTitle(name);
            eptAdapter = new EptAdapter(this, ept);
            eptList.setAdapter(eptAdapter);
        } else {
            setTitle("Новая группа");
        }



//        grid.setVisibility(View.VISIBLE);
//
//        editName.setOnKeyListener(new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event)
//            {
//                if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER))
//                {
//                    Log.d(TAG, "ENTER");
//                    editName.setFocusable(false);
//                    editName.setFocusableInTouchMode(false);
//                    // сохраняем текст, введённый до нажатия Enter в переменную
//                    return true;
//                }
//                if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT))
//                {
//                    Log.d(TAG, "DOwn");
//                    // сохраняем текст, введённый до нажатия Enter в переменную
//                    return true;
//                }
////                editName.setFocusable(true);
//                return false;
//            }
//        });
////
//        editName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                editName.setFocusable(true);
//                editName.setFocusableInTouchMode(true);
//            }
//        });
//        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//
//                if (v == editName) {
//                    if (hasFocus) {
//                        grid.setVisibility(View.INVISIBLE);
////                    add.setVisibility(View.INVISIBLE);
////                    settings.setVisibility(View.INVISIBLE);
////                    save.setVisibility(View.INVISIBLE);
////                    delete.setVisibility(View.INVISIBLE);
//
//                    } else {
//                        grid.setVisibility(View.VISIBLE);
//                    }
//                }
//                Log.d(TAG, String.valueOf(hasFocus));
//
//            }
//        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "BACK");
//        editName.setFocusable(false); // true/false соответственно
//        editName.setFocusableInTouchMode(false); // true/false соответственно

    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Checks the orientation of the screen
//        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
//
//                Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
//
//                Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void setEditTextFocus(boolean isFocused, EditText editText) {
//        editText.setCursorVisible(isFocused);
//        editText.setFocusable(isFocused);
//        editText.setFocusableInTouchMode(isFocused);
//
//        if (isFocused) {
//            editText.requestFocus();
//        }
//    }
}