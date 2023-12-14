package com.example.nir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupDataBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GroupDataFragment extends Fragment {
    private final String TAG = GroupDataFragment.class.getSimpleName();
    private FragmentGroupDataBinding binding;
    private DatabaseAdapter databaseAdapter;
    private ArrayList<ItemEpt> ept = new ArrayList<>();

    private RecyclerView eptList;
    private EptAdapter eptAdapter;

    private int position;

    private File file;
    private FileHandler fileHandler;

    private byte[] groupsByte = new byte[51200];
    private byte[] group = new byte[512];
    private int groupSize = 512;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGroupDataBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eptList = view.findViewById(R.id.ept_list);

        databaseAdapter = new DatabaseAdapter(getActivity());
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        Bundle bundle = getArguments();
        if (bundle != null) {
            position = bundle.getInt("group_position");
//            String name = bundle.getString("group_name");
//            getActivity().setTitle(getString(R.string.group, name));
        }

        Log.e(TAG,"group pos " + position);

        file = new File(getActivity().getFilesDir(), "groups.grf");
        fileHandler = new FileHandler(file);
        try {
            group = fileHandler.readBytesFromPosition(position);
            Log.i(TAG,position + " " + bytesToHex(group));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GroupFormat groupFormat = new GroupFormat(group);
        for (int i = 0; i < 480 / 6; i++) {
            int stepValue = groupFormat.readValue(i);
            if (stepValue != 0) {
                ept.add(databaseAdapter.getEptByValue(stepValue));
            }
        }

        AlertDialog.Builder builderCode = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogViewCode = inflater.inflate(R.layout.dialog_step, null);
        builderCode.setView(dialogViewCode);
        builderCode.setTitle(R.string.settings_button);
        builderCode.setPositiveButton("Сохранить", null);
        builderCode.setNegativeButton("Удалить", null);
        builderCode.setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builderCode.create();

        if (ept.size() != 0) {
            eptAdapter = new EptAdapter(getActivity(), ept);
            eptList.setAdapter(eptAdapter);
            eptAdapter.setOnEptClickListener(new EptAdapter.EptClickListener() {
                @Override
                public void onEptClick(int position, View itemView) {

                    int ampl = groupFormat.readAmpl(position);
                    int stepTime = groupFormat.readStepTime(position);

                    dialog.show();

                    EditText editAmpl = dialog.findViewById(R.id.step_ampl);
                    EditText editStepTime = dialog.findViewById(R.id.step_time);
                    editAmpl.setText(String.valueOf(ampl & 0xFFFF));
                    editStepTime.setText(String.valueOf(stepTime & 0xFFFF));

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int newAmpl = Integer.parseInt(String.valueOf(editAmpl.getText()));
                            int newTime = Integer.parseInt(String.valueOf(editStepTime.getText()));
                            groupFormat.writeAmpl(newAmpl, position);
                            groupFormat.writeStepTime(newTime,position);
                            dialog.dismiss();
                        }
                    });
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e(TAG, bytesToHex(groupFormat.readStep(position)));
                            int stepCount = groupFormat.readStepCount();
                            groupFormat.writeStepCount(stepCount - 1);
                            groupFormat.deleteStep(position);
                            dialog.dismiss();
                            ept.remove(position);
                            eptAdapter.notifyDataSetChanged();
                        }
                    });



                }
            });
        }

        binding.addEpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("group_position", position);
                NavHostFragment.findNavController(GroupDataFragment.this)
                        .navigate(R.id.action_GroupDataFragment_to_GroupEptFragment, bundle);
            }
        });

        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("group_position", position);
                NavHostFragment.findNavController(GroupDataFragment.this)
                        .navigate(R.id.action_GroupDataFragment_to_GroupSettingsFragment, bundle);
            }
        });

        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fileHandler.deleteBytesFromPosition(position);
                    Intent intent = new Intent(getActivity(), DataActivity.class);
                    startActivity(intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        binding.saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupFormat groupFormat = new GroupFormat(group);
                groupFormat.writeCRC();
                try {
                    fileHandler.writeBytesToPosition(group, position);
                    Intent intent = new Intent(getActivity(), DataActivity.class);
                    startActivity(intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fileHandler.close();
        binding = null;
    }
}