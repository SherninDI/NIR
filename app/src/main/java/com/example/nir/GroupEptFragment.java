package com.example.nir;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupEptBinding;

import java.util.ArrayList;
import java.util.List;


public class GroupEptFragment extends Fragment {

    private List<String> groups = new ArrayList<>();

    private DatabaseAdapter databaseAdapter;

    private List<String> ept = new ArrayList<>();

    private RecyclerView codeList;
    private EptAdapter codeAdapter;

    private FragmentGroupEptBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGroupEptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        codeList = getView().findViewById(R.id.code_list);
        Spinner groupSpinner = getView().findViewById(R.id.spinner_group);

        databaseAdapter = new DatabaseAdapter(getActivity());
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        groups = databaseAdapter.getAllGroups();

        ArrayAdapter<String> spinnerGroupAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,groups);
        spinnerGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(spinnerGroupAdapter);
        groupSpinner.setSelection(-1);



        ept = databaseAdapter.getAllEpt();
        codeAdapter = new EptAdapter(getActivity(), ept);
        codeList.setAdapter(codeAdapter);


//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(GroupEptFragment.this)
//                        .navigate(R.id.action_GroupEptFragment_to_GroupDataFragment);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseAdapter.close();
        binding = null;
    }
}