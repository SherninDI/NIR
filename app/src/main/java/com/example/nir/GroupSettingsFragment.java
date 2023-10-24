package com.example.nir;

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
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupSettingsBinding;

import java.io.File;
import java.io.IOException;
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
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = activity.getIntent().getExtras();
        file = new File(activity.getFilesDir(), "groups.grf");
        if (bundle != null) {
            if (bundle.getBoolean("new")) {
                position = bundle.getInt("size");
                activity.setTitle(getString(R.string.new_group));
            } else {
                position = bundle.getInt("group");
                name = bundle.getString("name");
                activity.setTitle(getString(R.string.group, name));
            }
        } else {

        }
        try {
            cancelListener = (onCancelListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    private FragmentGroupSettingsBinding binding;

    private DatabaseAdapter databaseAdapter;

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
        FileHandler fileHandler = new FileHandler(file);
        int groupSize = 512;


        try {
            byte[] group = fileHandler.readBytesFromPosition(groupSize, groupSize * position);
            Log.e(TAG, bytesToHex(group));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Закрываем файл
            fileHandler.close();
        }

        Log.e(TAG, String.valueOf(position));

        binding.cancelSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelListener.cancel();
            }
        });

        binding.saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // save to file


                Bundle bundle = new Bundle();
                bundle.putInt("group_position", position);
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