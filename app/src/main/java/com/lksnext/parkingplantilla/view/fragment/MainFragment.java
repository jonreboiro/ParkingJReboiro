package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textview.MaterialTextView;
import com.lksnext.parkingplantilla.R;


public class MainFragment extends Fragment {
    public MainFragment() {
        // Es necesario un constructor vacio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Asignar la vista (layout) al fragmento
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
