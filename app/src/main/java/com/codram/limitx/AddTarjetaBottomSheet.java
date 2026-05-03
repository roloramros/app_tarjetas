package com.codram.limitx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaRequest;
import com.codram.limitx.data.api.TarjetaResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.switchmaterial.SwitchMaterial;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AddTarjetaBottomSheet extends DialogFragment {

    public interface OnTarjetaAddedListener {
        void onTarjetaAdded();
    }

    private OnTarjetaAddedListener listener;
    private TextInputEditText etNombre, etNumero;
    private Spinner spinnerBanco, spinnerMoneda;
    private SwitchMaterial swTcp;
    private String[] bancos = {"BPA", "BANDEC", "METRO", "CLASICA", "TROPICAL"};
    private String[] monedasBpa = {"CUP", "MLC"};
    private String[] monedasUsd = {"USD"};

    public void setOnTarjetaAddedListener(OnTarjetaAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_add_tarjeta, null);

        etNombre = view.findViewById(R.id.etNombre);
        etNumero = view.findViewById(R.id.etNumero);
        spinnerBanco = view.findViewById(R.id.spinnerBanco);
        spinnerMoneda = view.findViewById(R.id.spinnerMoneda);
        swTcp = view.findViewById(R.id.swTcp);

        setupSpinners(view);

        android.app.Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Añadir Nueva Tarjeta")
                .setView(view)
                .setPositiveButton("GUARDAR TARJETA", null)
                .setNegativeButton("Cancelar", null)
                .create();
        
        dialog.setCanceledOnTouchOutside(false);
        
        dialog.setOnShowListener(d -> {
            ((androidx.appcompat.app.AlertDialog) d).getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> saveTarjeta());
        });

        return dialog;
    }

    private void setupSpinners(View view) {
        ArrayAdapter<String> bancoAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, bancos);
        bancoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanco.setAdapter(bancoAdapter);

        // Initial adapter for moneda
        ArrayAdapter<String> initialMonedaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monedasBpa);
        initialMonedaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(initialMonedaAdapter);

        spinnerBanco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBanco = bancos[position];
                if (selectedBanco.equals("CLASICA") || selectedBanco.equals("TROPICAL")) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monedasUsd);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMoneda.setAdapter(adapter);
                    spinnerMoneda.setEnabled(false);
                    swTcp.setVisibility(View.GONE);
                    swTcp.setChecked(false);
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monedasBpa);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMoneda.setAdapter(adapter);
                    spinnerMoneda.setEnabled(true);
                    swTcp.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveTarjeta() {
        String nombre = etNombre.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String banco = spinnerBanco.getSelectedItem().toString();
        String moneda = spinnerMoneda.getSelectedItem().toString();

        if (nombre.isEmpty() || numero.isEmpty()) {
            Toast.makeText(getContext(), "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double limite = 0.0;
        if (banco.equals("CLASICA")) {
            limite = 0.0;
        } else if (banco.equals("TROPICAL")) {
            limite = 5000.0;
        } else {
            // BPA, BANDEC, METRO
            if (swTcp.isChecked()) {
                limite = 0.0;
            } else {
                if (moneda.equals("CUP")) {
                    limite = 120000.0;
                } else if (moneda.equals("MLC")) {
                    limite = 5000.0;
                }
            }
        }
        
        TarjetaRequest request = new TarjetaRequest(nombre, numero, banco, moneda, limite, true);
        
        SessionManager sessionManager = new SessionManager(getContext());
        String token = "Bearer " + sessionManager.getToken();

        ApiClient.getService().createTarjeta(token, request).enqueue(new Callback<TarjetaResponse>() {
            @Override
            public void onResponse(Call<TarjetaResponse> call, Response<TarjetaResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tarjeta añadida con éxito", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onTarjetaAdded();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al añadir tarjeta: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TarjetaResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
