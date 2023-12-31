package com.example.nir;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupEptBinding;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class GroupEptFragment extends Fragment {
    private final String TAG = GroupEptFragment.class.getSimpleName();
    private File file;
    private FileHandler fileHandler;
    private byte[] group = new byte[512];
    private int position;
    private int ept_position;
    private int code = 0;

    private FragmentGroupEptBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGroupEptBinding.inflate(inflater, container, false);
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
        Spinner typeSpinner = view.findViewById(R.id.spinner_type);

        List<String> types = new ArrayList<>();
        types.add("f"); types.add("d"); types.add("m"); types.add("p");
        ArrayAdapter<String> spinnerTypeAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, types);
        spinnerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(spinnerTypeAdapter);

        Bundle bundle = getArguments();
        if (bundle != null) {
//            if (bundle.getBoolean("add")) {
//
//            }
            code = bundle.getInt("code");
            String codeName = bundle.getString("codeName");
            codeText.setText(codeName);
            position = bundle.getInt("group_position");
        }

        file = new File(getActivity().getFilesDir(), "groups.grf");
        fileHandler = new FileHandler(file);
        try {
            group = fileHandler.readBytesFromPosition(position);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.e(TAG, "group "+ position+" " + bytesToHex(group));

        binding.codes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("group_position", position);
                NavHostFragment.findNavController(GroupEptFragment.this)
                        .navigate(R.id.action_GroupEptFragment_to_CodesFragment, bundle);
            }
        });

        binding.saveCodeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(codeAmpl.getText()) && !TextUtils.isEmpty(codeTime.getText())) {
                    String type = typeSpinner.getSelectedItem().toString();
                    int ampl = Integer.parseInt(String.valueOf(codeAmpl.getText()));
                    int time = Integer.parseInt(String.valueOf(codeTime.getText()));
                    GroupFormat groupFormat = new GroupFormat(group);
                    int stepCount = groupFormat.readStepCount();
                    groupFormat.writeStep(type, code, ampl, time, stepCount);
                    groupFormat.writeStepCount(stepCount + 1);

                    Log.e(TAG, String.valueOf(stepCount));
                    try {
                        fileHandler.writeBytesToPosition(group, position);
                        NavHostFragment.findNavController(GroupEptFragment.this)
                                .navigate(R.id.action_GroupEptFragment_to_GroupDataFragment);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {

                }


            }
        });

        binding.cancelCodeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}