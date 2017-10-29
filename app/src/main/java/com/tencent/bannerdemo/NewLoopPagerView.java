package com.tencent.bannerdemo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greyzhou on 2017/10/19.
 */

public class NewLoopPagerView extends ViewPager{
    private Context mContext;
    //真正起作用的内部Adapter
    private InnerAdapter mInnerAdapter;
    private List<OnPageChangeListener> mOnPageChangeListeners;
    //滑动速度控制器
    private ViewPagerScroller mScroller;
    private int mPreviewLimit = 0;

    //是否在自动滚动状态
    private boolean mIsAutoScroll = false;
    //自动滚动延迟时间
    private int mAutoScrollDelayInMillisecond = 1000;
    //自动滚动方向
    private boolean mIsScrollBackward = false;

    public NewLoopPagerView(Context context) {
        super(context);
        init(context);
    }

    public NewLoopPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mContext = context;
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
                    if(mIsAutoScroll){
                        stopAutoScroll();
                    }
                }else{
                    if(mIsAutoScroll){
                        startAutoScroll(true);
                    }
                }
            }
        });

        initViewPagerScroller();
    }

    private void initViewPagerScroller(){
        mScroller = new ViewPagerScroller(mContext);
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, mScroller);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreviewLimit(int previewLimit){
        mPreviewLimit = previewLimit;
    }

    //外部必须调用此方法来更新数据
    public void notifyDataSetChanged(){
        getAdapter().notifyDataSetChanged();
        //需要重新设置下adapter，不然动画会出问题，要注意，此处调用super，不是this
        super.setAdapter(getAdapter());
        //重新设置起始位置
        setCurrentItem(mInnerAdapter.getInnerHeadItemPosition(),false);
    }

    private void initAdapter(PagerAdapter adapter){
        mInnerAdapter = new InnerAdapter(adapter);
        mInnerAdapter.setPreviewLimit(mPreviewLimit);
        super.setAdapter(mInnerAdapter);
        //设置开始位置
        setCurrentItem(mInnerAdapter.getInnerHeadItemPosition(),false);
        //startAutoScroll(true);
    }


    private InnerAdapter getInnerAdapter(){
        return mInnerAdapter;
    }

    //核心方法，调整映射当前的item
    private void adjustCurrentItemAtBorder(){
        if(mInnerAdapter == null){
            return;
        }
        Log.d("mark",getCurrentItem()+"");
        //强制回到映射位置
        if(mInnerAdapter.isFrontBorderPosition(getCurrentItem())){
            Log.d("mark","setCurrentItem--"+mInnerAdapter.getInnerEndItemPosition());
            mScroller.setScrollDuration(0);
            setCurrentItem(mInnerAdapter.getInnerEndItemPosition(),true);
            mScroller.restoreDefaultScrollDuration();
        }
        if(mInnerAdapter.isBackBorderPosition(getCurrentItem())){
            Log.d("mark","setCurrentItem--"+mInnerAdapter.getInnerHeadItemPosition());
            mScroller.setScrollDuration(0);
            setCurrentItem(mInnerAdapter.getInnerHeadItemPosition(),true);
            mScroller.restoreDefaultScrollDuration();
        }
    }

    public void startAutoScroll(boolean scrollBackward){
        if(mIsAutoScroll){
            return;
        }
        mIsAutoScroll = true;
        mIsScrollBackward = scrollBackward;
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

    @Override
    public void setAdapter(PagerAdapter adapter) {
        initAdapter(adapter);
    }

    @Override
    public PagerAdapter getAdapter() {
        return getInnerAdapter();
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
    private int mScrolledPosition = -1;
    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d("mark","onPageScrolled:"+position+"->"+positionOffset);
            mScrolledPosition = position;
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
            Log.d("mark","onPageSelected:"+position);
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
            Log.d("mark","onPageScrollStateChanged-->"+state+"-->"+mScrolledPosition);
            if(state == ViewPager.SCROLL_STATE_SETTLING){
                //if(mInnerAdapter.isBorderPosition(mScrolledPosition)){
                    mInterceptKeyEvent = true;
                //}
            }
            //在IDLE做调整，是保证滚动动画结束时调用setCurrentItem，防止闪白屏
            if(state == ViewPager.SCROLL_STATE_IDLE){
                adjustCurrentItemAtBorder();
                if(!mInnerAdapter.isBorderPosition(mScrolledPosition)){
                    mInterceptKeyEvent = false;
                }
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

    private boolean mInterceptKeyEvent = false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(mInterceptKeyEvent){
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    //内部Adapter类
    private static class InnerAdapter extends PagerAdapter{
        private PagerAdapter mAdapter;
        // TODO: 2017/10/24 如果大于1，转换的边界position有改变，以及与getCount的数值校验
        private int mPreviewLimit = 0;

        public InnerAdapter(PagerAdapter adapter){
            mAdapter = adapter;
        }

        public void setPreviewLimit(int previewLimit){
            mPreviewLimit = previewLimit > 0 ? previewLimit : 0;
        }

        @Override
        public final int getCount() {
            return mAdapter.getCount() < 1 ? mAdapter.getCount() : mAdapter.getCount() + 2 + 2 * mPreviewLimit;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return mAdapter.isViewFromObject(view, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return mAdapter.instantiateItem(container, getExternalItemPosition(position));
        }

        // TODO: 2017/10/19 这个方法需要研究下
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mAdapter.destroyItem(container, getExternalItemPosition(position), object);
        }

        @Override
        public final int getItemPosition(Object object) {
            return mAdapter.getItemPosition(object);
        }

        public int getInnerHeadItemPosition(){
            return 1 + mPreviewLimit;
        }

        public int getInnerEndItemPosition(){
            return mAdapter.getCount() + mPreviewLimit;
        }

        //主要用于真实View的索引
        public int getExternalItemPosition(int position){
            //viewPager真正的可用的个数
            int realCount = mAdapter.getCount();
            if (realCount == 0)
                return 0;
            int realPosition = (position - 1 - mPreviewLimit) % realCount;
            if (realPosition < 0)
                realPosition += realCount;
            return realPosition;
        }

        public boolean isBorderPosition(int position){
            return isFrontBorderPosition(position) || isBackBorderPosition(position);
        }

        public boolean isFrontBorderPosition(int position){
            return position == 0 + mPreviewLimit;
        }

        public boolean isBackBorderPosition(int position){
            return position == getCount() - 1 - mPreviewLimit;
        }
    }

    /**
     * ViewPager 滚动速度设置
     *
     */
    public static class ViewPagerScroller extends Scroller {
        private int mScrollDuration = 250;             // 滑动速度

        public void setScrollDuration(int duration){
            this.mScrollDuration = duration;
        }

        public void restoreDefaultScrollDuration(){
            mScrollDuration = 250;
        }

        public ViewPagerScroller(Context context) {
            super(context);
        }

        public ViewPagerScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mScrollDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mScrollDuration);
        }
    }
}
