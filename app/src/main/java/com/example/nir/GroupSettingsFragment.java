package com.example.nir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.nir.databinding.FragmentGroupSettingsBinding;

import java.util.Objects;

public class GroupSettingsFragment extends Fragment {

    public interface onCancelListener {
        public void cancel();
    }

    onCancelListener cancelListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

        Bundle bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("new")) {
                getActivity().setTitle(getString(R.string.new_group));
            } else {
                int id = bundle.getInt("group");
                String name = bundle.getString("name");
                getActivity().setTitle(getString(R.string.group, name));
            }
        } else {

        }

        binding.cancelSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelListener.cancel();
            }
        });

        binding.saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(GroupSettingsFragment.this)
                        .navigate(R.id.action_GroupSettingsFragment_to_GroupDataFragment);
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}