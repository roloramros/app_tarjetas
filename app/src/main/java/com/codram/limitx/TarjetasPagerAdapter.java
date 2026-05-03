package com.codram.limitx;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TarjetasPagerAdapter extends FragmentStateAdapter {
    private final TarjetasFragment cupFragment;
    private final TarjetasFragment usdFragment;
    private boolean isSubscriptionActive = true;

    public TarjetasPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        cupFragment = TarjetasFragment.newInstance();
        usdFragment = TarjetasFragment.newInstance();
    }

    public void setSubscriptionActive(boolean isActive) {
        if (this.isSubscriptionActive != isActive) {
            this.isSubscriptionActive = isActive;
            cupFragment.setSubscriptionActive(isActive);
            usdFragment.setSubscriptionActive(isActive);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? cupFragment : usdFragment;
    }

    @Override
    public int getItemCount() {
        return isSubscriptionActive ? 2 : 1; // Only 1 (CUP) if inactive
    }

    public TarjetasFragment getCupFragment() { return cupFragment; }
    public TarjetasFragment getUsdFragment() { return usdFragment; }
}
