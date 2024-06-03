package com.example.nir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.*;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.*;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.nir.databinding.FragmentGroupDataBinding;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.util.*;

public class GroupDataFragment extends Fragment {
    private final String TAG = GroupDataFragment.class.getSimpleName();
    private FragmentGroupDataBinding binding;
    private ArrayList<ItemEpt> ept = new ArrayList<>();
    private RecyclerView eptList;
    private EptAdapter eptAdapter;
    private EptNameAdapter eptNameAdapter;
    private int position;

    private final String FILE_NAME = "groups.grf";
    private final String SAVE_FILE_NAME = "save.grf";

    private final String POSITION = "group_position";

    private byte[] groupsByte = new byte[51200];
    private int groupSize = 512;

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
        binding = FragmentGroupDataBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eptList = view.findViewById(R.id.ept_list);

        Log.i("data", String.valueOf(position));

        TableLayout tableLayout = view.findViewById(R.id.table1);
        SwitchCompat switchCompat = view.findViewById(R.id.switch_list);

        openEpt();



        byte[] group = new byte[groupSize];
        File file = new File(getActivity().getFilesDir(), FILE_NAME);
        FileHandler fileHandler = new FileHandler(file);
        try {
            group = fileHandler.readBytesFromPosition(position);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GroupFormat groupFormat = new GroupFormat(group);

        AlertDialog.Builder builderCode = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogViewCode = inflater.inflate(R.layout.dialog_step, null);
        builderCode.setView(dialogViewCode);
        builderCode.setPositiveButton("Сохранить", null);
        builderCode.setNegativeButton("Удалить", null);
        builderCode.setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builderCode.create();
        switchCompat.setChecked(false);

        if (ept.size() != 0) {
            eptAdapter = new EptAdapter(getActivity(), ept);
            eptList.setAdapter(eptAdapter);
            eptAdapter.notifyDataSetChanged();
            eptAdapter.setOnEptClickListener((pos, itemView) -> {
                editEpt(pos,  dialog, groupFormat);
            });
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View view = tableLayout.getChildAt(0);
                TableRow row = (TableRow) view;
                if (view instanceof TableRow) {
                    TextView eptNum = row.findViewById(R.id.tvEptNumber);
                    eptNum.setText(R.string.col_num);
                    TextView eptValue= row.findViewById(R.id.tvEptValue);
                    eptValue.setText(R.string.col_val);
                    if (isChecked) {
                        TextView eptName = row.findViewById(R.id.tvEptAmpl);
                        eptName.setText(R.string.switch_list_on);
                        TextView eptTime = row.findViewById(R.id.tvEptTime);
                        eptTime.setText("");
                        openEpt();

                        if (ept.size() != 0) {
                            eptNameAdapter = new EptNameAdapter(getActivity(), ept);
                            eptList.setAdapter(eptNameAdapter);
                            eptNameAdapter.notifyDataSetChanged();
                            eptNameAdapter.setOnEptNameClickListener((pos, itemView) -> {
                                editEpt(pos,  dialog, groupFormat);
                                eptNameAdapter = new EptNameAdapter(getActivity(), ept);
                                eptList.setAdapter(eptNameAdapter);
                                eptNameAdapter.notifyDataSetChanged();
                            });
                        }
                    }
                    else {
                        TextView eptAmpl = row.findViewById(R.id.tvEptAmpl);
                        eptAmpl.setText(R.string.col_ampl);
                        TextView eptTime = row.findViewById(R.id.tvEptTime);
                        eptTime.setText(R.string.col_time);
                        openEpt();


                        if (ept.size() != 0) {
                            eptAdapter = new EptAdapter(getActivity(), ept);
                            eptList.setAdapter(eptAdapter);
                            eptAdapter.notifyDataSetChanged();
                            eptAdapter.setOnEptClickListener((pos, itemView) -> {
                                editEpt(pos,  dialog, groupFormat);
                                eptAdapter = new EptAdapter(getActivity(), ept);
                                eptList.setAdapter(eptAdapter);
                                eptAdapter.notifyDataSetChanged();
                            });
                        }
                    }
                }
            }
        });



        fileHandler.close();
    }

    public void editEpt(int pos, AlertDialog dialog, GroupFormat groupFormat) {
        ItemEpt itemEpt = eptAdapter.getItem(pos);
        dialog.setTitle(itemEpt.getEptNameText());

        int ampl = itemEpt.getEptAmpl();
        int stepTime = itemEpt.getEptTime();

        dialog.show();

        EditText editAmpl = dialog.findViewById(R.id.step_ampl);
        EditText editStepTime = dialog.findViewById(R.id.step_time);
        editAmpl.setText(String.valueOf(ampl & 0xFFFF));
        editStepTime.setText(String.valueOf(stepTime & 0xFFFF));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int newAmpl = Integer.parseInt(String.valueOf(editAmpl.getText()));
            int newTime = Integer.parseInt(String.valueOf(editStepTime.getText()));
            int time = 0;
            groupFormat.writeAmpl(newAmpl, pos);
            groupFormat.writeTimeInt(time);
            groupFormat.writeStepTime(newTime,pos);
            for (int i = 0; i < groupFormat.readStepCount(); i++) {
                time +=groupFormat.readStepTime(i);
            }
            groupFormat.writeTimeInt(time);
            groupFormat.writeCRC();
            saveEpt(groupFormat.getBytes());
            openEpt();
            dialog.dismiss();
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            int stepCount = groupFormat.readStepCount();
            int time = 0;

            groupFormat.deleteStep(pos);
            groupFormat.writeStepCount(stepCount - 1);
            for (int i = 0; i < groupFormat.readStepCount(); i++) {
                time +=groupFormat.readStepTime(i);
            }
            groupFormat.writeTimeInt(time);
            groupFormat.writeCRC();

            File file1 = new File(getActivity().getFilesDir(), FILE_NAME);
            FileHandler fileHandler1 = new FileHandler(file1);
            try {
                fileHandler1.writeBytesToPosition(groupFormat.getBytes(), position);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileHandler1.close();
            ept.remove(pos);
            eptAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
    }



    public void openEpt() {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getActivity());
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        ept.clear();
        byte[] group = new byte[groupSize];
        File file = new File(getActivity().getFilesDir(), FILE_NAME);
        FileHandler fileHandler = new FileHandler(file);
        try {
            group = fileHandler.readBytesFromPosition(position);
            GroupFormat groupFormat = new GroupFormat(group);
            for (int i = 0; i < 480 / 6; i++) {
                int stepValue = groupFormat.readValue(i);
                int stepAmpl = groupFormat.readAmpl(i);
                int stepTime = groupFormat.readStepTime(i);
                if (stepValue != 0) {
                    ItemEpt itemEpt = databaseAdapter.getEptByValue(stepValue);
                    ItemEpt newItem = new ItemEpt(
                            itemEpt.getEptNameText(),
                            itemEpt.getEptValue(),
                            itemEpt.getEptType(),
                            stepAmpl,
                            stepTime
                    );
                    ept.add(newItem);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.close();
    }


    public void saveEpt(byte[] group) {
        File file = new File(getActivity().getFilesDir(), FILE_NAME);
        FileHandler fileHandler = new FileHandler(file);
        try {
            fileHandler.writeBytesToPosition(group, position);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.close();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}