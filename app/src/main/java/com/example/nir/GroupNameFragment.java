package com.example.nir;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class GroupNameFragment extends Fragment {

    private final String POSITION = "group_position";
    private int position;
    private static final String FILE_NAME = "groups.grf";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null) {
            position = bundle.getInt(POSITION);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_name, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText editTitle = view.findViewById(R.id.text_title);
        TextView textNumber = view.findViewById(R.id.group_number);
        TextView textTime = view.findViewById(R.id.group_time);
        Button continueButton = view.findViewById(R.id.continue_button);
        InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();
        File file = new File(this.getActivity().getFilesDir(), FILE_NAME);
        FileHandler fileHandler = new FileHandler(file);
        try {
            byte[] group = fileHandler.readBytesFromPosition(position);
            GroupFormat groupFormat = new GroupFormat(group);
            editTitle.setText(groupFormat.readTitle());
            String number = getString(R.string.group_name_text, groupFormat.readNumber());
            String time = getString(R.string.group_time_text, groupFormat.readTime());
            textNumber.setText(number);
            textTime.setText(time);
            continueButton.setOnClickListener(v -> {
                String title = editTitle.getText().toString();
                if (inputValidatorHelper.isValidTitle(title)) {
                    if (!inputValidatorHelper.isNullOrEmpty(title)) {
                        byte empty[] = new byte[15];
                        groupFormat.writeEmptyTitle();
                        groupFormat.writeFileId();
                        groupFormat.writeTitle(title);
                        groupFormat.writeCRC();

                        FileHandler saveFileHandler = new FileHandler(file);
                        try {
                            saveFileHandler.writeBytesToPosition(group, position);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        saveFileHandler.close();

                        Toast.makeText(this.getActivity(), "Название группы сохранено", Toast.LENGTH_SHORT).show();

                    } else {
                        editTitle.setError("Поле не должно быть пустым");
                    }

                } else {
                    editTitle.setError("Неверное название");
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileHandler.close();

    }
}