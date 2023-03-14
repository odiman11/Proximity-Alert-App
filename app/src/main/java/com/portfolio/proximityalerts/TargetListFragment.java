package com.portfolio.proximityalerts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.portfolio.proximityalerts.databinding.MainFragmentBinding;
import com.portfolio.proximityalerts.databinding.TargetListFragmentBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class TargetListFragment extends Fragment {
    private TargetListFragmentBinding binding;
    ListView targetListView;
    FloatingActionButton deleteBtn;
    HashMap<String, String> targetList;
    CustomListAdapter customListAdapter;
    RadarManager radarManager;
    View previewsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = TargetListFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        targetList = RadarManager.getSavedTargetList();
        targetListView = binding.lvTargetsList;
        customListAdapter = new CustomListAdapter(getActivity().getApplicationContext(), targetList);
        targetListView.setAdapter(customListAdapter);
        deleteBtn = view.findViewById(R.id.btn_delete_targetList);
        radarManager = RadarManager.getInstance(getContext());

        targetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteBtn.setVisibility(View.VISIBLE);
                TextView tvSource = view.findViewById(R.id.textView_item_mmsi);
                if(previewsView != null){
                    previewsView.setBackgroundColor(getResources().getColor(R.color.white));
                }
                view.setBackgroundColor(getResources().getColor(R.color.teal_700));
                previewsView = view;


                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mmsi = tvSource.getText().toString();
                        removeItem(mmsi, i);
                    }
                });
            }
        });
    }
    private void removeItem(String mmsi, int index){
        if(targetList.containsKey(mmsi)){
            targetList.remove(mmsi);
        }
        RadarManager.removeFromTargetList(mmsi);
        customListAdapter.removeItem(index);
        customListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        radarManager.saveSavedList();
    }
}
