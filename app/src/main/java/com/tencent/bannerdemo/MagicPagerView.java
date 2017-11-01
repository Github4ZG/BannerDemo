package com.tencent.bannerdemo;

import android.content.Context;
import android.database.DataSetObserver;
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
 * Created by Greyzhou on 2017/10/26.
 */

public class MagicPagerView extends ViewPager {
    private IPagerStrategy mPagerStrategy;
    private ILayoutStrategy mLayoutStrategy;
    private Context mContext;
    private List<OnPageChangeListener> mOnPageChangeListeners;
    //滑动速度控制器
    private ViewPagerScroller mScroller;

    public MagicPagerView(Context context) {
        super(context);
        this.mContext = context;
        init(context);
    }

    public MagicPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mPagerStrategy = new LoopPagerStrategy();
        mLayoutStrategy = new VerticalLayoutStrategy();
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

    public void scrollToNext(){
        setCurrentItem(getCurrentItem() + 1,true);
    }

    public void scrollToPre(){
        setCurrentItem(getCurrentItem() -1,true);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if(mPagerStrategy != null){
            mPagerStrategy.setAdapter(adapter);
        }else{
            super.setAdapter(adapter);
        }
    }

    // TODO: 2017/10/31 需要多注意
    @Override
    public PagerAdapter getAdapter() {
        return super.getAdapter();
    }

    @Override
    public void setCurrentItem(int item) {
        Log.d("anr","setCurrentItem");
        if (mPagerStrategy != null){
            mPagerStrategy.setCurrentItem(item);
        }
        super.setCurrentItem(item);
    }


    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        Log.d("anr","setCurrentItem smoothScroll");
        if (mPagerStrategy != null){
            mPagerStrategy.setCurrentItem(item,smoothScroll);
        }
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

    @Override
    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        if(mLayoutStrategy != null){
            mLayoutStrategy.setPageTransformer(reverseDrawingOrder,transformer);
            return;
        }
        super.setPageTransformer(reverseDrawingOrder, transformer);
    }

    @Override
    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer, int pageLayerType) {
        if(mLayoutStrategy != null){
            mLayoutStrategy.setPageTransformer(reverseDrawingOrder,transformer,pageLayerType);
            return;
        }
        super.setPageTransformer(reverseDrawingOrder, transformer, pageLayerType);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mPagerStrategy != null){
            mPagerStrategy.onRelease();
            mPagerStrategy = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(mLayoutStrategy != null){
            return mLayoutStrategy.onInterceptTouchEvent(event);
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mLayoutStrategy != null){
            return mLayoutStrategy.onTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(mLayoutStrategy != null){
            return mLayoutStrategy.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(mPagerStrategy != null){
                mPagerStrategy.onPageScrolled(position,positionOffset,positionOffsetPixels);
            }else{
                notifyPageScrolled(position,positionOffset,positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if(mPagerStrategy != null){
                mPagerStrategy.onPageSelected(position);
            }else{
                notifyPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if(mPagerStrategy != null){
                mPagerStrategy.onPageScrollStateChanged(state);
            }else{
                notifyPageScrollStateChanged(state);
            }
        }
    };

    private void notifyPageScrolled(int position, float positionOffset, int positionOffsetPixels){
        if (mOnPageChangeListeners != null) {
            for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageScrolled(position,positionOffset,positionOffsetPixels);
                }
            }
        }
    }

    private void notifyPageSelected(int position){
        if (mOnPageChangeListeners != null) {
            for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageSelected(position);
                }
            }
        }
    }

    private void notifyPageScrollStateChanged(int state){
        if (mOnPageChangeListeners != null) {
            for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
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

    private interface IPagerStrategy {
        void setAdapter(PagerAdapter adapter);
        void setCurrentItem(int item);
        void setCurrentItem(int item, boolean smoothScroll);
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
        void onPageSelected(int position);
        void onPageScrollStateChanged(int state);
        void onRelease();
    }

    //非静态内部类，隐含MagicPagerView的指针，但是为了区分，必须显示调用
    private class LoopPagerStrategy implements IPagerStrategy{
        //防止setCurrentItem出现Anr的最大换页跨度
        private int MAX_SET_CURRENT_ITEM_OFFSET = 8;
        //真正起作用的内部Adapter
        private LoopAdapter mLoopAdapter;
        //外部Adapter
        private PagerAdapter mExternalAdapter;
        private DataSetObserver mDataSetObserver;

        @Override
        public void setAdapter(PagerAdapter adapter) {
            if(adapter == null){
                return;
            }
            mExternalAdapter = adapter;
            mLoopAdapter = new LoopAdapter(mExternalAdapter);
            MagicPagerView.super.setAdapter(mLoopAdapter);
            //当调用外部adapter的notifyDataSetChanged方法时，会调用InnerAdapter的notifyDataSetChanged方法
            mDataSetObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    notifyDataSetChanged();
                }
            };
            mExternalAdapter.registerDataSetObserver(mDataSetObserver);
            setCurrentItem(mLoopAdapter.getStartSelectItem(),false);
        }

        @Override
        public void setCurrentItem(int item) {
            if (isOutOfMaxOffset(item)){
                setFirstLayout();
                //必须有notifyDataSetChanged，而且顺序不能变，不知道为什么
                mLoopAdapter.notifyDataSetChanged();
            }
            MagicPagerView.super.setCurrentItem(item);
        }

        @Override
        public void setCurrentItem(int item, boolean smoothScroll) {
            if (isOutOfMaxOffset(item)){
                setFirstLayout();
                //必须有notifyDataSetChanged，而且顺序不能变，不知道为什么
                mLoopAdapter.notifyDataSetChanged();
            }
            MagicPagerView.super.setCurrentItem(item, smoothScroll);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(mLoopAdapter != null){
                MagicPagerView.this.notifyPageScrolled(mLoopAdapter.getExternalItemPosition(position),positionOffset,positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if(mLoopAdapter != null){
                MagicPagerView.this.notifyPageSelected(mLoopAdapter.getExternalItemPosition(position));
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onRelease() {
            if(mExternalAdapter != null){
                mExternalAdapter.unregisterDataSetObserver(mDataSetObserver);
                mExternalAdapter = null;
            }
        }

        private boolean isOutOfMaxOffset(int position){
            return Math.abs(position-MagicPagerView.this.getCurrentItem()) > MAX_SET_CURRENT_ITEM_OFFSET;
        }

        private void setFirstLayout(){
            try {
                Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
                mFirstLayout.setAccessible(true);
                mFirstLayout.set(MagicPagerView.this, true);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        //外部必须调用此方法来更新数据
        private void notifyDataSetChanged(){
            if(mLoopAdapter == null){
                return;
            }
            if(isOutOfMaxOffset(mLoopAdapter.getStartSelectItem())){
                setFirstLayout();
                //必须有notifyDataSetChanged，而且顺序不能变，不知道为什么
                mLoopAdapter.notifyDataSetChanged();
                MagicPagerView.super.setCurrentItem(mLoopAdapter.getStartSelectItem(),false);
            }else{
                mLoopAdapter.notifyDataSetChanged();
                //需要重新设置下adapter，不然动画会出问题，要注意，此处调用super，不是this
                MagicPagerView.super.setAdapter(mLoopAdapter);
                //重新设置起始位置
                MagicPagerView.super.setCurrentItem(mLoopAdapter.getStartSelectItem(),false);
            }
        }

        public class LoopAdapter extends PagerAdapter{
            private PagerAdapter mAdapter;
            private int MAX_COUNT = Integer.MAX_VALUE;
            //private int MAX_COUNT = 200;

            public LoopAdapter(PagerAdapter adapter){
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
    }

    private interface ILayoutStrategy{
        void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer);
        void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer, int pageLayerType);
        boolean onInterceptTouchEvent(MotionEvent event);
        boolean onTouchEvent(MotionEvent ev);
        boolean dispatchKeyEvent(KeyEvent event);
    }

    private class VerticalLayoutStrategy implements ILayoutStrategy{

        public VerticalLayoutStrategy(){
            //初始化切换方案
            MagicPagerView.super.setPageTransformer(true,new VerticalTransformerWrapper(null));
        }
        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            boolean intercept = MagicPagerView.super.onInterceptTouchEvent(swapTouchEvent(event));
            //If not intercept, touch event should not be swapped.
            swapTouchEvent(event);
            return intercept;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            return MagicPagerView.super.onTouchEvent(swapTouchEvent(ev));
        }

        //如果ViewPager中item包含可获焦点并响应KeyEvent的子控件，这种做法会影响item内部的焦点移动，因为KeyCode变了
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            Log.d("Event","First dispatch:"+event.getKeyCode());
            boolean dispatch = MagicPagerView.super.dispatchKeyEvent(swapKeyEvent(event));
            if(dispatch){
                Log.d("Event","Second dispatch:"+event.getKeyCode()+"true");
            }else{
                Log.d("Event","Second dispatch:"+event.getKeyCode()+"false");
            }
            //swapKeyEvent(event);
            return dispatch;
        }

        @Override
        public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
            MagicPagerView.super.setPageTransformer(reverseDrawingOrder,new VerticalTransformerWrapper(transformer));
        }

        @Override
        public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer, int pageLayerType) {
            MagicPagerView.super.setPageTransformer(reverseDrawingOrder,new VerticalTransformerWrapper(transformer),pageLayerType);
        }

        private MotionEvent swapTouchEvent(MotionEvent event) {
            float width = getWidth();
            float height = getHeight();

            float swappedX = (event.getY() / height) * width;
            float swappedY = (event.getX() / width) * height;

            event.setLocation(swappedX, swappedY);

            return event;
        }
        private KeyEvent swapKeyEvent(KeyEvent event) {
            int code = event.getKeyCode();
            if(code == KeyEvent.KEYCODE_DPAD_UP
                    || code == KeyEvent.KEYCODE_DPAD_DOWN
                    || code == KeyEvent.KEYCODE_DPAD_LEFT
                    || code == KeyEvent.KEYCODE_DPAD_RIGHT){
                switch (code){
                    case KeyEvent.KEYCODE_DPAD_UP:
                        code = KeyEvent.KEYCODE_DPAD_RIGHT;
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        code = KeyEvent.KEYCODE_DPAD_LEFT;
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        code = KeyEvent.KEYCODE_DPAD_DOWN;
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        code = KeyEvent.KEYCODE_DPAD_UP;
                        break;
                }
                return new KeyEvent(event.getDownTime(),event.getEventTime(),event.getAction(),code,event.getRepeatCount(),event.getMetaState(),event.getDeviceId(),event.getScanCode(),event.getFlags(),event.getSource());
            }
            return event;
        }
        private class VerticalTransformerWrapper implements ViewPager.PageTransformer {
            private static final float MAX_SCALE = 1.0f;
            private static final float MIN_SCALE = 0.8f;
            private PageTransformer mPageTransformer;
            public VerticalTransformerWrapper(PageTransformer pageTransformer){
                mPageTransformer = pageTransformer;
            }

            @Override
            public void transformPage(View view, float position) {
                Log.d("mark","transformPage-->"+position);
/*            float alpha = 0;
            if (0 <= position && position <= 1) {
                alpha = 1 - position;
            } else if (-1 < position && position < 0) {
                alpha = position + 1;
            }
            view.setAlpha(alpha);*/
/*                if (position < -1) {//(-Infinity,-1)
                    view.setScaleX(MIN_SCALE);
                    view.setScaleY(MIN_SCALE);
                } else if (position <= 0) {//[-1,0]
                    float scaleFactor =  MIN_SCALE+(1-Math.abs(position))*(MAX_SCALE-MIN_SCALE);
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);
                } else if (position <= 1) {//(0,1]
                    float scaleFactor =  MIN_SCALE+(1-Math.abs(position))*(MAX_SCALE-MIN_SCALE);
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);
                } else {//(1,Infinity)
                    view.setScaleX(MIN_SCALE);
                    view.setScaleY(MIN_SCALE);
                }*/
                view.setTranslationX(view.getWidth() * -position);
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);
                if(mPageTransformer != null){
                    mPageTransformer.transformPage(view,position);
                }
            }
        }
    }
}
