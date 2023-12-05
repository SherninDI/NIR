package com.example.nir;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentCodesBinding;


import java.util.ArrayList;
import java.util.List;

public class CodesFragment extends Fragment {
    private final String TAG = CodesFragment.class.getSimpleName();
    private ArrayList<ItemEpt> ept = new ArrayList<>();
    private DatabaseAdapter databaseAdapter;
    private EptAdapter eptAdapter;
    private FragmentCodesBinding binding;

    private int code;
    private String codeName;
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

        EditText searchText = view.findViewById(R.id.text_search_code);
        RecyclerView eptList = view.findViewById(R.id.code_list);
        Spinner groupSpinner = view.findViewById(R.id.spinner_group);

        databaseAdapter = new DatabaseAdapter(getActivity());
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();
        List<String> groups = new ArrayList<>();
        groups.add("Все");
        groups.addAll(databaseAdapter.getAllGroups());

        ArrayAdapter<String> spinnerGroupAdapter = new SpinnerAdapter(getActivity(), R.layout.spinner_text, groups);
        spinnerGroupAdapter.setDropDownViewResource(R.layout.spinner_dropdown_text);
        groupSpinner.setAdapter(spinnerGroupAdapter);
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, position + " " + id);
                if (position == 0) {
                    ept.clear();
                    ept = databaseAdapter.getAllEpt();
                } else {
                    ept.clear();
                    ept = databaseAdapter.getAllEptById(position - 1);
                }

                eptAdapter = new EptAdapter(getActivity(), ept);
                eptList.setAdapter(eptAdapter);
                onClickListener();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.addEpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("code", code);
                bundle.putString("codeName", codeName);
                bundle.putBoolean("add", true);
                NavHostFragment.findNavController(CodesFragment.this)
                        .navigate(R.id.action_CodesFragment_to_GroupEptFragment, bundle);
            }
        });

        binding.cancelEpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = searchText.getText().toString();
                if (!search.isEmpty()) {
                    ept = searchData(ept, search);
                    eptAdapter = new EptAdapter(getActivity(), ept);
                    eptList.setAdapter(eptAdapter);
                    onClickListener();
                }
            }
        });

    }

    private void onClickListener() {
        if (ept.size() != 0) {
            eptAdapter.setOnEptClickListener(new EptAdapter.EptClickListener() {
                @Override
                public void onEptClick(int position, View itemView) {
                    ItemEpt itemEpt = ept.get(position);
                    code = itemEpt.getEptValue();
                    codeName = itemEpt.getEpt();
                }
            });
        }
    }

    private ArrayList<ItemEpt> searchData(ArrayList<ItemEpt> eptList, String search) {
        ArrayList<ItemEpt> searchList = new ArrayList<>();
        for (ItemEpt ept: eptList) {
            if (ept.getEptNameText().toLowerCase().contains(search.toLowerCase()) || ept.getEptValueText().toLowerCase().contains(search.toLowerCase())) {
                searchList.add(ept);
            }
        }
        return searchList;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseAdapter.close();
        binding = null;
    }
}