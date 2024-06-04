package com.example.nir;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupEptBinding;

import java.io.File;
import java.io.IOException;


public class GroupEptFragment extends Fragment {
    private final String TAG = GroupEptFragment.class.getSimpleName();
    private final String POSITION = "group_position";
    private final String FILE_NAME = "groups.grf";
    private final String SAVE_FILE_NAME = "save.grf";
    private File file;
    private FileHandler fileHandler;
    private byte[] group = new byte[512];
    private int position;
    private int ept_position;
    private int code = 0;
    private String type;
    private FragmentGroupEptBinding binding;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null) {
            position = bundle.getInt(POSITION);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGroupEptBinding.inflate(inflater, container, false);
        setHasOptionsMenu(false);
        return binding.getRoot();
    }

    public static String bytesToHex(byte[] byteArray)
    {
        String hex = "";
        // Iterating through each byte in the array
        for (byte i : byteArray) {
            hex += String.format("%02X", i);
            hex += " ";
        }
        return hex;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView codeText = view.findViewById(R.id.text_code);
        EditText codeAmpl = view.findViewById(R.id.text_ampl);
        EditText codeTime = view.findViewById(R.id.text_step_time);

        Log.i("ept", String.valueOf(position));
        Bundle bundle = getArguments();
        if (bundle != null) {
            code = bundle.getInt("code");
            type = bundle.getString("type");
            String codeName = bundle.getString("codeName");
            codeText.setText(codeName);
            position = bundle.getInt(POSITION);
        }

        file = new File(getActivity().getFilesDir(), FILE_NAME);
        fileHandler = new FileHandler(file);
        try {
            group = fileHandler.readBytesFromPosition(position);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();
        GroupFormat groupFormat = new GroupFormat(group);
//        Log.e(TAG, "group "+ position+" " + bytesToHex(group));


        binding.saveCodeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputValidatorHelper.isNullOrEmpty(codeAmpl.getText().toString())
                        && Integer.parseInt(codeAmpl.getText().toString()) <= 100
                        && Integer.parseInt(codeAmpl.getText().toString()) >= 0) {
                    if (!inputValidatorHelper.isNullOrEmpty(codeTime.getText().toString())
                            && Integer.parseInt(codeTime.getText().toString()) <= 5999
                            && Integer.parseInt(codeTime.getText().toString()) >= 0) {

                        int stepCount = groupFormat.readStepCount();
                        int time = 0;

                        int ampl = Integer.parseInt(String.valueOf(codeAmpl.getText()));
                        int stepTime = Integer.parseInt(String.valueOf(codeTime.getText()));

                        groupFormat.writeStep(type, code, ampl, stepTime, stepCount);
                        groupFormat.writeTimeInt(time);
                        groupFormat.writeStepCount(stepCount + 1);
                        for (int i = 0; i < groupFormat.readStepCount(); i++) {
                            time +=groupFormat.readStepTime(i);
                        }
                        groupFormat.writeTimeInt(time);
                        Log.e("time", String.valueOf(time));
                        groupFormat.writeCRC();

                        try {
                            fileHandler.writeBytesToPosition(group, position);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        fileHandler.close();
                        NavHostFragment.findNavController(GroupEptFragment.this)
                                .navigate(R.id.action_GroupEptFragment_to_GroupDataFragment);

                    } else {
                        codeTime.setError("Неверный формат Времени шага");
                    }
                } else {
                    codeAmpl.setError("Неверный формат Амплитуды");
                }

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}