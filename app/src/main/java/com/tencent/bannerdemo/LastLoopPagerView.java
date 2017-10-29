package com.tencent.bannerdemo;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greyzhou on 2017/10/26.
 */

public class LastLoopPagerView extends ViewPager {
    private Context mContext;
    //外部Adapter
    private PagerAdapter mExternalAdapter;
    //真正起作用的内部Adapter
    private InnerAdapter mInnerAdapter;
    private List<OnPageChangeListener> mOnPageChangeListeners;
    //滑动速度控制器
    private ViewPagerScroller mScroller;
    //防止setCurrentItem出现Anr的最大换页跨度
    private int MAX_SET_CURRENT_ITEM_OFFSET = 8;

    public LastLoopPagerView(Context context) {
        super(context);
        this.mContext = context;
        init(context);
    }

    public LastLoopPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context);
    }

    private void init(Context context){
        mContext = context;
        if (mOnPageChangeListener != null) {
            super.removeOnPageChangeListener(mOnPageChangeListener);
        }
        super.addOnPageChangeListener(mOnPageChangeListener);
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

    private DataSetObserver mDataSetObserver;
    private void initAdapter(PagerAdapter adapter){
        if(adapter == null){
            return;
        }
        mExternalAdapter = adapter;
        mInnerAdapter = new InnerAdapter(mExternalAdapter);
        super.setAdapter(mInnerAdapter);
        //当调用外部adapter的notifyDataSetChanged方法时，会调用InnerAdapter的notifyDataSetChanged方法
        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        };
        mExternalAdapter.registerDataSetObserver(mDataSetObserver);
        setCurrentItem(mInnerAdapter.getStartSelectItem(),false);
    }

    private InnerAdapter getInnerAdapter(){
        return mInnerAdapter;
    }

    public void scrollToNext(){
        setCurrentItem(getCurrentItem() + 1,true);
    }

    public void scrollToPre(){
        setCurrentItem(getCurrentItem() -1,true);
    }

    //外部必须调用此方法来更新数据
    private void notifyDataSetChanged(){
        if(mInnerAdapter == null){
            return;
        }
        if(isOutOfMaxOffset(mInnerAdapter.getStartSelectItem())){
            setCurrentItem(mInnerAdapter.getStartSelectItem(),false);
        }else{
            getAdapter().notifyDataSetChanged();
            //需要重新设置下adapter，不然动画会出问题，要注意，此处调用super，不是this
            super.setAdapter(getAdapter());
            //重新设置起始位置
            setCurrentItem(mInnerAdapter.getStartSelectItem(),false);
        }
    }

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
        Log.d("anr","setCurrentItem");
        if (isOutOfMaxOffset(item)){
            setFirstLayout();
            //必须有notifyDataSetChanged，而且顺序不能变，不知道为什么
            mInnerAdapter.notifyDataSetChanged();
        }
        super.setCurrentItem(item);
    }


    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        Log.d("anr","setCurrentItem smoothScroll");
        if (isOutOfMaxOffset(item)){
            setFirstLayout();
            //必须有notifyDataSetChanged，而且顺序不能变，不知道为什么
            mInnerAdapter.notifyDataSetChanged();
        }
        super.setCurrentItem(item, smoothScroll);
    }

    private void setFirstLayout(){
        try {
            Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
            mFirstLayout.setAccessible(true);
            mFirstLayout.set(this, true);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isOutOfMaxOffset(int position){
        return Math.abs(position-getCurrentItem()) > MAX_SET_CURRENT_ITEM_OFFSET;
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mExternalAdapter != null){
            mExternalAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
    }



    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(mInnerAdapter == null){
                return;
            }
            int externalPosition = mInnerAdapter.getExternalItemPosition(position);
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrolled(externalPosition,positionOffset,positionOffsetPixels);
                    }
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            if(mInnerAdapter == null){
                return;
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

    public static class InnerAdapter extends PagerAdapter{
        private PagerAdapter mAdapter;
        private int MAX_COUNT = Integer.MAX_VALUE;
        //private int MAX_COUNT = 200;

        public InnerAdapter(PagerAdapter adapter){
            mAdapter = adapter;
        }

        @Override
        public final int getCount() {
            return MAX_COUNT;
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

        //主要用于真实View的索引
        public int getExternalItemPosition(int position){
            return position % mAdapter.getCount();
        }

        private int getStartSelectItem(){
            // 我们设置当前选中的位置为MAX_COUNT / 2,这样开始就能往左滑动
            // 但是要保证这个值与getRealPosition 的 余数为0，因为要从第一页开始显示
            int currentItem = MAX_COUNT / 2;
            if(currentItem % mAdapter.getCount()  ==0 ){
                return currentItem;
            }
            // 直到找到从0开始的位置
            while (currentItem % mAdapter.getCount()  != 0){
                currentItem++;
            }
            return currentItem;
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
