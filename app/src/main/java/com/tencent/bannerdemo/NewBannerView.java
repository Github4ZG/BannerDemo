package com.tencent.bannerdemo;

import android.content.Context;
import android.graphics.Camera;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.LinkedList;

/**
 * Created by Greyzhou on 2017/10/20.
 */

public class NewBannerView extends FrameLayout{
    private Context mContext;
    private BannerAdapter mAdapter;
    private LoopPagerAdapter mInnerLoopAdapter;
    private LastLoopPagerView mLoopViewPager;

    //自动滚动相关参数

    //是否在自动滚动状态
    private boolean mIsAutoScroll = false;
    //自动滚动延迟时间
    private int mAutoScrollDelayInMillisecond = 1000;
    //自动滚动方向
    private boolean mIsScrollBackward = false;

    public NewBannerView(Context context){
        super(context);
        init(context);
    }

    public NewBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        initLoopPagerView();
        initListener();
    }

    public void setAdapter(BannerAdapter adapter){
        mAdapter = adapter;
        mInnerLoopAdapter = new LoopPagerAdapter();
        mInnerLoopAdapter.setAdapter(mAdapter);
        mLoopViewPager.setAdapter(mInnerLoopAdapter);
        //startAutoScroll(false);
    }

    private void initLoopPagerView(){
        mLoopViewPager = new LastLoopPagerView(mContext);
        addView(mLoopViewPager,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        //添加左右预览
        mLoopViewPager.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mLoopViewPager.setClipToPadding(false);
        mLoopViewPager.setPadding(100,0,100,0);
        mLoopViewPager.setPageMargin(-80);
        //设置页面切换动画
        mLoopViewPager.setPageTransformer(true,new ZoomOutPageTransformer());
        mLoopViewPager.setOffscreenPageLimit(2);
    }

    private void initListener(){
        mLoopViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(mIsAutoScroll){
                    scrollToNextDelayed();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //处理ViewPager获取焦点时的操作
        // TODO: 2017/10/20 触屏操作需要额外考虑下
        mLoopViewPager.setOnFocusChangeListener(new OnFocusChangeListener() {
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
    }

    /**
     *  带有View重用机制以及ViewHolder机制的内部PagerAdapter
     */
    private static class LoopPagerAdapter extends PagerAdapter{
        //private SparseArray<View> mItemArrays = new SparseArray<>();
        private BannerAdapter mItemAdapter;
        private LinkedList<View> mViewCache = new LinkedList<>();

        public void setAdapter(BannerAdapter itemAdapter){
            mItemAdapter = itemAdapter;
            //外部调用BannerAdapter的notifyDataSetChanged方法，会最终调用LoopPagerAdapter的notifyDataSetChanged方法
            if(mItemAdapter != null){
                mItemAdapter.setViewPagerObserver(new BannerAdapter.DataSetObserver() {
                    @Override
                    public void onChanged() {
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getCount() {
            return mItemAdapter != null ? mItemAdapter.getItemCount() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.e("test","instantiateItem " + position);
            if(mItemAdapter == null){
                Log.e("test","instantiateItem " + "ItemAdapter is null");
                return null;
            }
            View itemView = null;
            if(mViewCache.size() != 0){
                itemView = mViewCache.removeFirst();
            }
            if(itemView == null){
                ViewHolder viewHolder = mItemAdapter.onCreateViewHolder(container);
                mItemAdapter.onBindViewHolder(viewHolder,position);
                itemView = viewHolder.itemView;
                itemView.setTag(viewHolder);
                if(container.indexOfChild(itemView) == -1){
                    container.addView(itemView);
                }
                return itemView;
            }else{
                ViewHolder viewHolder = (ViewHolder) itemView.getTag();
                mItemAdapter.onBindViewHolder(viewHolder,position);
                if(container.indexOfChild(itemView) == -1){
                    container.addView(itemView);
                }
                return itemView;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.e("test","destroyItem " + position);
            View contentView = (View) object;
            container.removeView(contentView);
            mViewCache.add(contentView);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    /**
     *  供外部重写的Adapter
     */
    public static abstract class BannerAdapter<T extends ViewHolder>{
        private DataSetObserver mViewPagerObserver;
        public abstract T onCreateViewHolder(ViewGroup parent);
        public abstract void onBindViewHolder(T holder, int position);
        public abstract int getItemCount();
        public void setViewPagerObserver(DataSetObserver observer) {
            synchronized (this) {
                mViewPagerObserver = observer;
            }
        }
        public void notifyDataSetChanged(){
            synchronized (this) {
                if(mViewPagerObserver != null){
                    mViewPagerObserver.onChanged();
                }
            }
        }
        public interface DataSetObserver{
            void onChanged();
        }
    }

    /**
     *  供外部重写的ViewHolder
     */
    public static abstract class ViewHolder{
        public final View itemView;
        public ViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
    }

    /**
     * 自动滚动的相关方法
     */
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
            if(mLoopViewPager == null){
                return;
            }
            if(!mIsScrollBackward){
                mLoopViewPager.scrollToNext();
            }else{
                mLoopViewPager.scrollToPre();
            }
        }
    };

    /**
     * 页面切换动画
     */
    public static class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;
        private static final float MAX_ROTATE = 30;
        private Camera camera = new Camera();

        @Override
        public void transformPage(View page, float position) {
            Log.d("mark","transformPage-->"+position);
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float rotate = 20 * Math.abs(position);
            if (position < -1) {
               /* page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setRotationY(rotate);*/
            } else if (position < 0) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setRotationY(rotate);
            } else if (position >= 0 && position < 1) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setRotationY(-rotate);
            } else if (position >= 1) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setRotationY(-rotate);
            }
        }
    }

    public class BannerTransformer implements ViewPager.PageTransformer {
        private static final float MAX_SCALE = 1.0f;
        private static final float MIN_SCALE = 0.8f;

        @Override
        public void transformPage(View page, float position) {
            if (position < -1) {//(-Infinity,-1)
                page.setScaleX(MIN_SCALE);
                page.setScaleY(MIN_SCALE);
            } else if (position <= 0) {//[-1,0]
                float scaleFactor =  MIN_SCALE+(1-Math.abs(position))*(MAX_SCALE-MIN_SCALE);
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            } else if (position <= 1) {//(0,1]
                float scaleFactor =  MIN_SCALE+(1-Math.abs(position))*(MAX_SCALE-MIN_SCALE);
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            } else {//(1,Infinity)
                page.setScaleX(MIN_SCALE);
                page.setScaleY(MIN_SCALE);
            }
        }
    }
}
