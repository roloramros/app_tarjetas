package com.codram.limitx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.codram.limitx.data.local.entity.TarjetaEntity;
import com.codram.limitx.utils.TransactionDialogHelper;
import java.util.ArrayList;
import java.util.List;

public class TarjetasFragment extends Fragment {

    private RecyclerView rvTarjetas;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TarjetasAdapter adapter;
    private List<TarjetaEntity> tarjetas = new ArrayList<>();
    private Runnable onRefreshListener;
    private TransactionDialogHelper.OnTransactionAddedListener transactionListener;
    private boolean isSubscriptionActive = true;

    public static TarjetasFragment newInstance() {
        return new TarjetasFragment();
    }

    public void setSubscriptionActive(boolean isActive) {
        this.isSubscriptionActive = isActive;
        if (adapter != null) {
            adapter.setSubscriptionActive(isActive);
        }
    }

    public void setOnRefreshListener(Runnable listener) {
        this.onRefreshListener = listener;
    }

    public void setTransactionListener(TransactionDialogHelper.OnTransactionAddedListener listener) {
        this.transactionListener = listener;
    }

    private List<TarjetaEntity> pendingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarjetas, container, false);
        rvTarjetas = view.findViewById(R.id.rvTarjetas);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        rvTarjetas.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefresh.setOnRefreshListener(() -> {
            if (onRefreshListener != null) onRefreshListener.run();
        });

        // Si hay datos esperando, los aplicamos ahora que la vista existe
        if (pendingData != null) {
            updateData(pendingData);
            pendingData = null;
        }

        return view;
    }

    public void updateData(List<TarjetaEntity> nuevasTarjetas) {
        if (rvTarjetas == null) {
            // La vista aún no se ha creado, guardamos los datos para después
            this.pendingData = nuevasTarjetas;
            return;
        }

        this.tarjetas = nuevasTarjetas;
        if (adapter == null) {
            adapter = new TarjetasAdapter(tarjetas, transactionListener, isSubscriptionActive);
            rvTarjetas.setAdapter(adapter);
        } else {
            adapter.updateData(tarjetas);
        }
        
        showLoading(false);
        if (tarjetas.isEmpty()) {
            rvTarjetas.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTarjetas.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    public void showLoading(boolean isLoading) {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(isLoading);
        }
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
