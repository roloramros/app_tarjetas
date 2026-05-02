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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTarjetaBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etNombre, etNumero, etLimite;
    private Spinner spinnerBanco, spinnerMoneda;
    private String[] bancos = {"BPA", "BANDEC", "METRO", "CLASICA", "TROPICAL"};
    private String[] monedas = {"CUP", "USD"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_tarjeta, container, false);

        etNombre = view.findViewById(R.id.etNombre);
        etNumero = view.findViewById(R.id.etNumero);
        etLimite = view.findViewById(R.id.etLimite);
        spinnerBanco = view.findViewById(R.id.spinnerBanco);
        spinnerMoneda = view.findViewById(R.id.spinnerMoneda);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        setupSpinners();

        btnSave.setOnClickListener(v -> saveTarjeta());

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<String> bancoAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, bancos);
        bancoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanco.setAdapter(bancoAdapter);

        ArrayAdapter<String> monedaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monedas);
        monedaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(monedaAdapter);

        spinnerBanco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBanco = bancos[position];
                if (selectedBanco.equals("CLASICA") || selectedBanco.equals("TROPICAL")) {
                    spinnerMoneda.setSelection(1); // USD
                    spinnerMoneda.setEnabled(false);
                } else {
                    spinnerMoneda.setEnabled(true);
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
        String limiteStr = etLimite.getText().toString().trim();

        if (nombre.isEmpty() || numero.isEmpty() || limiteStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double limite = Double.parseDouble(limiteStr);
        TarjetaRequest request = new TarjetaRequest(nombre, numero, banco, moneda, limite, true);
        
        SessionManager sessionManager = new SessionManager(getContext());
        String token = "Bearer " + sessionManager.getToken();

        ApiClient.getService().createTarjeta(token, request).enqueue(new Callback<TarjetaResponse>() {
            @Override
            public void onResponse(Call<TarjetaResponse> call, Response<TarjetaResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tarjeta añadida con éxito", Toast.LENGTH_SHORT).show();
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
