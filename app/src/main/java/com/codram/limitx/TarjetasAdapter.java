package com.codram.limitx;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.api.TarjetaResponse;
import java.util.List;
import java.util.Locale;

public class TarjetasAdapter extends RecyclerView.Adapter<TarjetasAdapter.TarjetaViewHolder> {

    private List<TarjetaResponse> tarjetas;

    public TarjetasAdapter(List<TarjetaResponse> tarjetas) {
        this.tarjetas = tarjetas;
    }

    @NonNull
    @Override
    public TarjetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarjeta, parent, false);
        return new TarjetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaViewHolder holder, int position) {
        TarjetaResponse tarjeta = tarjetas.get(position);
        holder.bind(tarjeta);
    }

    @Override
    public int getItemCount() {
        return tarjetas.size();
    }

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCardName, tvBankName, tvCardNumber, tvCardLimit;

        public TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tvCardName);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvCardLimit = itemView.findViewById(R.id.tvCardLimit);
        }

        public void bind(TarjetaResponse tarjeta) {
            tvCardName.setText(tarjeta.getNombre());
            tvBankName.setText(tarjeta.getBanco());
            tvCardNumber.setText(obfuscateCardNumber(tarjeta.getNumero()));
            String limitText = String.format(Locale.US, "$%,.2f %s", tarjeta.getLimiteMensual(), tarjeta.getMoneda());
            tvCardLimit.setText(limitText);
        }

        private String obfuscateCardNumber(String number) {
            if (number == null || number.length() <= 4) {
                return "****";
            }
            return "**** **** **** " + number.substring(number.length() - 4);
        }
    }
}
