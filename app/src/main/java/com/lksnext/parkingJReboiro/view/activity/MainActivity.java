package com.lksnext.parkingJReboiro.view.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ActivityMainBinding binding;
    NavController navController;
    AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.flFragment);
        navController = navHostFragment.getNavController();

        bottomNavigationView = binding.bottomNavigationView;
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Evita navegar si ya estás en el destino
            int currentDest = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
            if (itemId == R.id.newres && currentDest != R.id.realizarReservaFragment) {
                navController.navigate(R.id.realizarReservaFragment);
                return true;
            } else if (itemId == R.id.reservations && currentDest != R.id.consultarReservasFragment) {
                navController.navigate(R.id.consultarReservasFragment);
                return true;
            } else if (itemId == R.id.notifications && currentDest != R.id.notificacionesFragment) {
                navController.navigate(R.id.notificacionesFragment);
                return true;
            } else if (itemId == R.id.person && currentDest != R.id.profileFragment) {
                navController.navigate(R.id.profileFragment);
                return true;
            }
            return false;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.dashboardFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.post(() -> {
                    actualizarSeleccionNavegacion(destination.getId());
                });
            }
        });
    }

    private void actualizarSeleccionNavegacion(int destinationId) {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }

        if (destinationId == R.id.realizarReservaFragment) {
            bottomNavigationView.getMenu().findItem(R.id.newres).setChecked(true);
        } else if (destinationId == R.id.consultarReservasFragment) {
            bottomNavigationView.getMenu().findItem(R.id.reservations).setChecked(true);
        } else if (destinationId == R.id.notificacionesFragment) {
            bottomNavigationView.getMenu().findItem(R.id.notifications).setChecked(true);
        } else if (destinationId == R.id.profileFragment) {
            bottomNavigationView.getMenu().findItem(R.id.person).setChecked(true);
        }
    }

    public void forzarSeleccionMenu(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}