package com.codram.limitx;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TarjetasPagerAdapter extends FragmentStateAdapter {
    private final TarjetasFragment cupFragment;
    private final TarjetasFragment usdFragment;

    public TarjetasPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        cupFragment = TarjetasFragment.newInstance();
        usdFragment = TarjetasFragment.newInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? cupFragment : usdFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public TarjetasFragment getCupFragment() { return cupFragment; }
    public TarjetasFragment getUsdFragment() { return usdFragment; }
}
