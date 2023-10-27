package com.example.nir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupSettingsBinding;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class GroupSettingsFragment extends Fragment {
    private final String TAG =  GroupSettingsFragment.class.getSimpleName();
    public interface onCancelListener {
        public void cancel();
    }
    onCancelListener cancelListener;
    private int position;
    private String name;
    private File file;

    private FileHandler fileHandler;
    private final int groupSize = 512;
    private byte[] group = new byte[groupSize];
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.e(TAG,"attach");
        Bundle bundle = activity.getIntent().getExtras();

        if (bundle != null) {
            if (bundle.getBoolean("new")) {
                position = bundle.getInt("size");
                activity.setTitle(getString(R.string.new_group));
            } else {
                position = bundle.getInt("group");
                name = bundle.getString("name");
                activity.setTitle(getString(R.string.group, name));
            }
        }
        try {
            cancelListener = (onCancelListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    private FragmentGroupSettingsBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGroupSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText editTitle = view.findViewById(R.id.text_title);
        EditText editTime = view.findViewById(R.id.text_time);
        RadioGroup modeGroup = view.findViewById(R.id.mode);
        RadioGroup spectreGroup = view.findViewById(R.id.spectre);
        RadioGroup freqGroup = view.findViewById(R.id.freq);

        file = new File(getActivity().getApplication().getFilesDir(),  "groups.grf");
        fileHandler = new FileHandler(file);

        try {
            group = fileHandler.readBytesFromPosition(groupSize, groupSize * position);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GroupFormat groupFormat = new GroupFormat(group);
//        Log.d(TAG, bytesToHex(groupFormat.getBytes()));
//        Log.d(TAG, String.valueOf(position));

        editTitle.setText(groupFormat.readTitle());
        editTime.setText(groupFormat.readTime());

        switch (groupFormat.readMode()) {
            case 0:
                modeGroup.check(R.id.mode_serial);
                break;
            case 1:
                modeGroup.check(R.id.mode_cycle);
                break;
            case 2:
                modeGroup.check(R.id.mode_complex);
                break;
        }

        switch (groupFormat.readSpectre()) {
            case 0:
                spectreGroup.check(R.id.spectre_disable);
                break;
            case 1:
                spectreGroup.check(R.id.spectre_enable1);
                break;
            case 2:
                spectreGroup.check(R.id.spectre_enable2);
                break;
        }

        switch (groupFormat.readMaxFreq()) {
            case 0:
                freqGroup.check(R.id.freq_15000);
                break;
            case 1:
                freqGroup.check(R.id.freq_1100);
                break;
            case 2:
                freqGroup.check(R.id.freq_1200);
                break;
            case 3:
                freqGroup.check(R.id.freq_1500);
                break;
        }

//
//        if (Arrays.equals(group, groupFormat.getBytes())) {
//
//        }

        binding.cancelSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelListener.cancel();
            }
        });

        binding.saveSettings.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                groupFormat.writeFileId();

                String title = editTitle.getText().toString();
                groupFormat.writeTitle(title);

                String time = editTime.getText().toString();
                groupFormat.writeTime(time);


                switch(modeGroup.getCheckedRadioButtonId()) {
                    case -1:
                        Toast.makeText(getActivity(), "Ничего не выбрано",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.mode_serial:
                        groupFormat.writeMode((byte) 0x00);
                        Toast.makeText(getActivity(), "ser",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.mode_cycle:
                        groupFormat.writeMode((byte) 0x01);
                        Toast.makeText(getActivity(), "cyc",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.mode_complex:
                        groupFormat.writeMode((byte) 0x02);
                        Toast.makeText(getActivity(), "comp",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

                switch(spectreGroup.getCheckedRadioButtonId()) {
                    case -1:
                        Toast.makeText(getActivity(), "Ничего не выбрано",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.spectre_disable:
                        groupFormat.writeSpectre((byte) 0x00);
                        Toast.makeText(getActivity(), "dis",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.spectre_enable1:
                        groupFormat.writeSpectre((byte) 0x01);
                        Toast.makeText(getActivity(), "en1",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.spectre_enable2:
                        groupFormat.writeSpectre((byte) 0x02);
                        Toast.makeText(getActivity(), "en2",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

                switch(freqGroup.getCheckedRadioButtonId()) {
                    case -1:
                        Toast.makeText(getActivity(), "Ничего не выбрано",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.freq_1100:
                        groupFormat.writeMaxFreq((byte) 0x01);
                        Toast.makeText(getActivity(), "1100",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.freq_1200:
                        groupFormat.writeMaxFreq((byte) 0x02);
                        Toast.makeText(getActivity(), "1200",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.freq_1500:
                        groupFormat.writeMaxFreq((byte) 0x03);
                        Toast.makeText(getActivity(), "1500",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.freq_15000:
                        groupFormat.writeMaxFreq((byte) 0x00);
                        Toast.makeText(getActivity(), "15000",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

                byte[] result = groupFormat.getBytes();
                // save to file
                try {
                    fileHandler.writeBytesToPosition(result, groupSize * position);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    fileHandler.close();
                }

//                Log.e(TAG, bytesToHex(result));
//                Log.e(TAG, new String(result, Charset.forName("windows-1251")));
//                Log.e(TAG, groupFormat.readTitle());
//                Log.e(TAG, groupFormat.readTime());
//                Log.e(TAG, String.valueOf(groupFormat.readMode()));
//                Log.e(TAG, String.valueOf(groupFormat.readSpectre()));
//                Log.e(TAG, String.valueOf(groupFormat.readMaxFreq()));
//                Log.e(TAG, String.valueOf(groupFormat.readExec() & 0xff));



                Bundle bundle = new Bundle();
                bundle.putInt("group_position", position);
                bundle.putString("group_name", groupFormat.readTitle());
                NavHostFragment.findNavController(GroupSettingsFragment.this)
                        .navigate(R.id.action_GroupSettingsFragment_to_GroupDataFragment, bundle);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public String bytesToHex(byte[] byteArray)
    {
        String hex = "";

        // Iterating through each byte in the array
        for (byte i : byteArray) {
            hex += String.format("%02X", i);
            hex += " ";
        }

        return hex;
    }

}