package com.tencent.bannerdemo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greyzhou on 2017/10/19.
 */

public class LoopPagerView extends ViewPager{
    private List<OnPageChangeListener> mOnPageChangeListeners;

    //只是作为外部数据接入的载体，真正起作用的是InnerAdapter
    private ILoopPagerAdapter mLoopAdapter;
    //真正起作用的内部Adapter
    private InnerAdapter mInnerAdapter;
    //是否在自动滚动状态
    private boolean mIsAutoScroll = false;
    //自动滚动延迟时间
    private int mAutoScrollDelayInMillisecond = 1000;
    //自动滚动方向
    private boolean mIsScrollBackward = false;

    public LoopPagerView(Context context) {
        super(context);
        init(context);
    }

    public LoopPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        if (mOnPageChangeListener != null) {
            super.removeOnPageChangeListener(mOnPageChangeListener);
        }
        super.addOnPageChangeListener(mOnPageChangeListener);
        //处理ViewPager获取焦点时的操作
        // TODO: 2017/10/20 触屏操作需要额外考虑下
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Log.d("greyzhou","onFocusChange true");
                    stopAutoScroll();
                }else{
                    Log.d("greyzhou","onFocusChange false");
                    startAutoScroll();
                }
            }
        });
    }

    public void setBannerAdapter(ILoopPagerAdapter adapter){
        mLoopAdapter = adapter;
        mInnerAdapter = new InnerAdapter() {
            @Override
            public int getItemCount() {
                if(mLoopAdapter != null){
                    return mLoopAdapter.getItemCount();
                }
                return 0;
            }

            @Override
            public View getItemView(int position) {
                if(mLoopAdapter != null){
                    return mLoopAdapter.getItemView(position);
                }
                return null;
            }
        };
        super.setAdapter(mInnerAdapter);
        //设置开始位置
        setCurrentItem(mInnerAdapter.getInnerHeadItemPosition(),false);
        mIsScrollBackward = true;
        startAutoScroll();
    }

    public void startAutoScroll(){
        if(mIsAutoScroll){
            return;
        }
        mIsAutoScroll = true;
        scrollToNextDelayed();
    }

    public void stopAutoScroll(){
        mIsAutoScroll = false;
        removeCallbacks(scrollToNextTask);
    }

    private void scrollToNextDelayed(){
        postDelayed(scrollToNextTask,mAutoScrollDelayInMillisecond);
    }

    private Runnable scrollToNextTask = new Runnable() {
        @Override
        public void run() {
            if(!mIsScrollBackward){
                setCurrentItem(getCurrentItem() + 1,true);
            }else{
                setCurrentItem(getCurrentItem() - 1,true);
            }
        }
    };

    //外界调用不起作用
    @Override
    public void setAdapter(PagerAdapter adapter) {

    }

    @Override
    public PagerAdapter getAdapter() {
        return mInnerAdapter;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item,true);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    @Override
    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(listener);
        }
    }

    @Override
    public void clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.clear();
        }
    }
    private int previousPosition = -1;

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d("mark","onPageScrolled:"+position+"->"+positionOffset);
            //处理回调
            int realPosition = mInnerAdapter.getExternalItemPosition(position);
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrolled(realPosition,positionOffset,positionOffsetPixels);
                    }
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            if(mInnerAdapter == null){
                return;
            }
            //如果上次onPageSelect的position是边界值，则丢弃这次回调处理
            // 因为上次回调已经对边界矫正结果进行了处理
            if(mInnerAdapter.isBorderPosition(previousPosition)){
                previousPosition = position;
                return;
            }
            previousPosition = position;
            //矫正回调的值
            if(mInnerAdapter.isBackBorderPosition(position)){
                position = mInnerAdapter.getInnerHeadItemPosition();
            }
            if(mInnerAdapter.isFrontBorderPosition(position)){
                position = mInnerAdapter.getInnerEndItemPosition();
            }

            Log.d("greyzhou","onPageSelected:"+position);

            if(mIsAutoScroll){
                scrollToNextDelayed();
            }
            int externalPosition = mInnerAdapter.getExternalItemPosition(position);
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageSelected(externalPosition);
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if(mInnerAdapter == null){
                return;
            }
            //在IDLE做调整，是保证滚动动画结束时调用setCurrentItem，防止闪白屏
            if(state == ViewPager.SCROLL_STATE_IDLE){
                adjustCurrentItemAtBorder();
            }
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrollStateChanged(state);
                    }
                }
            }
        }
    };

    //核心方法，调整映射当前的item
    private void adjustCurrentItemAtBorder(){
        if(mInnerAdapter == null){
            return;
        }
        //强制回到映射位置
        if(getCurrentItem() == 0){
            setCurrentItem(mInnerAdapter.getInnerEndItemPosition(),false);
        }
        if(getCurrentItem() == mInnerAdapter.getCount() - 1){
            setCurrentItem(mInnerAdapter.getInnerHeadItemPosition(),false);
        }
    }

    //内部Adapter类
    private static abstract class InnerAdapter extends PagerAdapter{
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
            View itemView = getItemView(getExternalItemPosition(position));
            container.addView(itemView);
            return itemView;
        }

        // TODO: 2017/10/19 这个方法需要研究下
        @Override
        public final void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public final int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public int getInnerHeadItemPosition(){
            return 1;
        }

        public int getInnerEndItemPosition(){
            return getItemCount();
        }

        //主要用于真实View的索引
        public int getExternalItemPosition(int position){
            //viewPager真正的可用的个数
            int realCount = getItemCount();
            if (realCount == 0)
                return 0;
            int realPosition = (position - 1) % realCount;
            if (realPosition < 0)
                realPosition += realCount;
            return realPosition;
        }

        public boolean isBorderPosition(int position){
            return isFrontBorderPosition(position) || isBackBorderPosition(position);
        }

        public boolean isFrontBorderPosition(int position){
            return position == 0;
        }

        public boolean isBackBorderPosition(int position){
            return position == getCount() - 1;
        }
    }

    public interface ILoopPagerAdapter{
        int getItemCount();

        View getItemView(int position);
    }
}
