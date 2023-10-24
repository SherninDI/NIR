package com.example.nir;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupDataBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupDataFragment extends Fragment {
    private final String TAG = GroupDataFragment.class.getSimpleName();
    private FragmentGroupDataBinding binding;
    private DatabaseAdapter databaseAdapter;
    private List<String> ept = new ArrayList<>();

    private RecyclerView eptList;
    private EptAdapter eptAdapter;

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

        eptList = getView().findViewById(R.id.ept_list);

        databaseAdapter = new DatabaseAdapter(getActivity());
        databaseAdapter.createDataBase();
        databaseAdapter.openDataBase();

        Bundle bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("new")) {
                getActivity().setTitle(getString(R.string.new_group));
                ept.clear();
            } else {
                int id = bundle.getInt("group");
                String name = bundle.getString("name");
                ept.clear();
                ept = databaseAdapter.getAllEptById(id);
                getActivity().setTitle(getString(R.string.group, name));
                eptAdapter = new EptAdapter(getActivity(), ept);
                eptList.setAdapter(eptAdapter);
            }
        } else {

        }


        binding.addEpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(GroupDataFragment.this)
                        .navigate(R.id.action_GroupDataFragment_to_GroupEptFragment);
            }
        });

        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(GroupDataFragment.this)
                        .navigate(R.id.action_GroupDataFragment_to_GroupSettingsFragment);
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}