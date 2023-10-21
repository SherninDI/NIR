package com.example.nir;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GroupActivity extends AppCompatActivity {

    EditText editName;
    Button add;
    Button settings;
    Button save;
    Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        editName = findViewById(R.id.text_name);
        add = findViewById(R.id.add_ept);
        settings = findViewById(R.id.settings);
        save = findViewById(R.id.save_changes);
        delete = findViewById(R.id.delete);


        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    add.setVisibility(View.VISIBLE);
                    settings.setVisibility(View.VISIBLE);
                    save.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);

                } else {
                    add.setVisibility(View.INVISIBLE);
                    settings.setVisibility(View.INVISIBLE);
                    save.setVisibility(View.INVISIBLE);
                    delete.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}