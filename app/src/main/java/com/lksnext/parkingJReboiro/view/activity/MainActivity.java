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
            if (itemId == R.id.newres && currentDest != R.id.mainFragment) {
                navController.navigate(R.id.mainFragment);
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
            actualizarSeleccionNavegacion(destination.getId());
            // Oculta la barra en el dashboard
            if (destination.getId() == R.id.dashboardFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void actualizarSeleccionNavegacion(int destinationId) {
        // Asignar el elemento correcto según el destino
        if (destinationId == R.id.realizarReservaFragment || destinationId == R.id.mainFragment) {
            bottomNavigationView.setSelectedItemId(R.id.newres);
        } else if (destinationId == R.id.consultarReservasFragment) {
            bottomNavigationView.setSelectedItemId(R.id.reservations);
        } else if (destinationId == R.id.notificacionesFragment) {
            bottomNavigationView.setSelectedItemId(R.id.notifications);
        } else if (destinationId == R.id.profileFragment) {
            bottomNavigationView.setSelectedItemId(R.id.person);
        }
        // Nota: Si no es ninguno de estos destinos, no cambiamos la selección actual
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}