package com.example.nir;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.nir.databinding.FragmentCodesBinding;


import java.util.ArrayList;
import java.util.List;

public class CodesFragment extends Fragment {
    private final String TAG = CodesFragment.class.getSimpleName();
    private List<String> groups = new ArrayList<>();
    private DatabaseAdapter databaseAdapter;

    private ArrayList<ItemEpt> ept = new ArrayList<ItemEpt>();

    private RecyclerView codeList;
    private EptAdapter codeAdapter;


    private FragmentCodesBinding binding;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCodesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        codeList = view.findViewById(R.id.code_list);
        Spinner groupSpinner = view.findViewById(R.id.spinner_group);

        databaseAdapter = new DatabaseAdapter(getActivity());
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        groups = databaseAdapter.getAllGroups();

        ArrayAdapter<String> spinnerGroupAdapter = new SpinnerAdapter(getActivity(), R.layout.spinner_text, groups);
        spinnerGroupAdapter.setDropDownViewResource(R.layout.spinner_dropdown_text);
        groupSpinner.setAdapter(spinnerGroupAdapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerAdapter.flag = true;
                ept = databaseAdapter.getAllEptById(position);
                codeAdapter = new EptAdapter(getActivity(), ept);
                codeList.setAdapter(codeAdapter);
                codeAdapter.notifyDataSetChanged();
                Log.i(TAG, position + " " + id);
                onClick();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ept = databaseAdapter.getAllEpt();
                codeAdapter = new EptAdapter(getActivity(), ept);
                codeList.setAdapter(codeAdapter);
                codeAdapter.notifyDataSetChanged();
                Log.e(TAG, String.valueOf(ept.size()));

                onClick();
            }
        });






//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(GroupEptFragment.this)
//                        .navigate(R.id.action_GroupEptFragment_to_GroupDataFragment);
//            }
//        });
    }

    private void onClick() {
        if (ept.size() != 0) {
            codeAdapter.setOnEptClickListener(new EptAdapter.EptClickListener() {
                @Override
                public void onEptClick(int position, View itemView) {
                    ItemEpt itemEpt = ept.get(position);
                    Log.e(TAG, String.valueOf(itemEpt.getEptValue()));
//                            String s = ept.get(position);
//                            Log.e(TAG, s);

                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseAdapter.close();
        SpinnerAdapter.flag = false;
        binding = null;
    }
}