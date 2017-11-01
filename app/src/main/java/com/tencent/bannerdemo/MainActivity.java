package com.tencent.bannerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private NewBannerView viewPager;
    private ImageView imageView;
    private final int[] images = {
            R.drawable.singertype1,
            R.drawable.singertype2,
            R.drawable.singertype3,
            R.drawable.singertype4
    } ;
    private Button mBtn;
    private final ArrayList<String> urls = new ArrayList<>();
    private NewBannerView.BannerAdapter mBannerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urls.add("http://i.imgur.com/1opCuAV.jpg");
        urls.add("https://i.imgur.com/oRwPfHE.jpg");
        urls.add("https://i.imgur.com/4xiZiML.png");
        urls.add("https://i.imgur.com/5DyG9Sn.jpg");
        mBtn = (Button) findViewById(R.id.btn2);
        viewPager = (NewBannerView) findViewById(R.id.vp);
        imageView = (ImageView) findViewById(R.id.imageView);
        mBannerAdapter = new NewBannerView.BannerAdapter<BannerViewHolder>(){
            @Override
            public BannerViewHolder onCreateViewHolder(ViewGroup parent) {
                Log.e("test","onCreateViewHolder ");
                FrameLayout container = new FrameLayout(MainActivity.this);
                ImageView view = new ImageView(MainActivity.this);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                container.addView(view);
                parent.addView(container);
                return new BannerViewHolder(container);
            }

            @Override
            public void onBindViewHolder(BannerViewHolder holder, int position) {
                Log.e("test","onBindViewHolder ");
                Glide.with(MainActivity.this).load(urls.get(position)).placeholder(R.mipmap.ic_launcher).into(holder.mImageView);
                //holder.mImageView.setImageResource(images[position]);
            }

            @Override
            public int getItemCount() {
                return urls.size();
            }
        };
        viewPager.setAdapter(mBannerAdapter);
        //Glide.with(MainActivity.this).load(urls[0]).placeholder(R.mipmap.ic_launcher).into(imageView);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urls.clear();
                urls.add("https://i.imgur.com/26nwphp.jpg");
                urls.add("https://i.imgur.com/wBxaaZH.jpg");
                urls.add("https://i.imgur.com/QsukwAs.jpg");
                mBannerAdapter.notifyDataSetChanged();
                //viewPager.notifyDataSetChanged();
            }
        });
    }
    public static class BannerViewHolder extends NewBannerView.ViewHolder{
        private ImageView mImageView;
        public BannerViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) (
                    (FrameLayout) itemView).getChildAt(0);
        }
    }


}
