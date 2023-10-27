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
    private final String TAG = GroupEptFragment.class.getSimpleName();
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



//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(GroupEptFragment.this)
//                        .navigate(R.id.action_GroupEptFragment_to_GroupDataFragment);
//            }
//        });

        binding.codes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(GroupEptFragment.this)
                        .navigate(R.id.action_GroupEptFragment_to_CodesFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}