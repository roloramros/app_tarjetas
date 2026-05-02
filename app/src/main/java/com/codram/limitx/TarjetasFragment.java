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
import com.codram.limitx.data.api.TarjetaResponse;
import java.util.ArrayList;
import java.util.List;

public class TarjetasFragment extends Fragment {

    private RecyclerView rvTarjetas;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TarjetasAdapter adapter;
    private List<TarjetaResponse> tarjetas = new ArrayList<>();
    private Runnable onRefreshListener;
    private TarjetasAdapter.OnTransactionAddedListener transactionListener;

    public static TarjetasFragment newInstance() {
        return new TarjetasFragment();
    }

    public void setOnRefreshListener(Runnable listener) {
        this.onRefreshListener = listener;
    }

    public void setTransactionListener(TarjetasAdapter.OnTransactionAddedListener listener) {
        this.transactionListener = listener;
    }

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

        return view;
    }

    public void updateData(List<TarjetaResponse> nuevasTarjetas) {
        this.tarjetas = nuevasTarjetas;
        if (adapter == null) {
            adapter = new TarjetasAdapter(tarjetas, transactionListener);
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
        if (swipeRefresh != null) swipeRefresh.setRefreshing(isLoading);
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
