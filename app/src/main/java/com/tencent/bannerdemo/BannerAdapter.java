package com.tencent.bannerdemo;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Greyzhou on 2017/10/19.
 */

public abstract class BannerAdapter extends PagerAdapter {
    //真实的item数量
    public abstract int getItemCount();

    public abstract View getItemView(int position);

    @Override
    public final int getCount() {
        return getItemCount() < 1 ? getItemCount() : getItemCount() + 2;
    }

    @Override
    public final boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        View itemView = getItemView(convertItemPosition(position));
        container.addView(itemView);
        return itemView;
    }

    @Override
    public final void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public final int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private int convertItemPosition(int position){
        //viewPager真正的可用的个数
        int realCount = getItemCount();
        //内层没有可用的Item则换回为零
        if (realCount == 0)
            return 0;
        int realPosition = (position - 1) % realCount;
        if (realPosition < 0)
            realPosition += realCount;
        return realPosition;
    }
}
