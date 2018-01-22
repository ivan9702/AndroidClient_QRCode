package com.startek.fm220;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * Created by ivan.lin on 2017/7/14.
 */

public class AutoPlayImageView extends ImageView {
    private Integer[] imgs;
    private int index;
    private MyTask myTask;
    private boolean isCircle = true;


    public AutoPlayImageView(Context context) {
        this(context, null);
    }

    public AutoPlayImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoPlayImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (imgs != null && imgs.length > 0) {
            init();
        }
    }

    /**
     * 初始化显示的图片和定时器
     */
    private void init() {
        setBackgroundResource(imgs[index]);
        if (myTask != null) {
            myTask.stop();
            myTask = null;
        }
        myTask = new MyTask();
        myTask.start();
    }

    /**
     * 初始化显示的图片和定时器
     */
    private void stop() {
        //setBackgroundResource(imgs[index]);
        if (myTask != null) {
            myTask.stop();
            myTask = null;
        }
       // myTask = new MyTask();
        //myTask.stop();
    }

    /**
     * 定时器的实现
     */
    public class MyTask implements Runnable {
        private int AUTO_PLAY_TIME = 2000;
        private boolean has_auto_play = false;

        @Override
        public void run() {
            if (has_auto_play) {
                disappearAnim();
                postDelayed(this, AUTO_PLAY_TIME);
            }
        }

        public void start() {
            if (!has_auto_play) {
                has_auto_play = true;
                //开启任务之前移除之前的任务，保证只有一个任务在运行
                removeCallbacks(this);
                postDelayed(this, AUTO_PLAY_TIME);
            }
        }

        public void stop() {
            has_auto_play = false;
            removeCallbacks(this);
        }

    }

    /**
     * 淡出的动画，同时监听动画状态，动画完成后执行淡入动画
     */
    private void disappearAnim() {
        if (index >= imgs.length - 1 && !isCircle) {
            return;
        }
        AlphaAnimation anim = new AlphaAnimation(1, 0);
        anim.setDuration(830);
        anim.setFillAfter(true);
        startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setBackgroundResource(imgs[++index]);
                showAnim();
                //实现循环轮播
                if (isCircle) {
                    if (index == imgs.length - 1) {
                        index = -1;
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 淡入的动画
     */
    private void showAnim() {
        AlphaAnimation anim = new AlphaAnimation(0, 1);
        anim.setDuration(830);
        anim.setFillAfter(true);
        startAnimation(anim);
    }

    /**
     * 开启定时器
     */
    public void startTask() {
        if (myTask != null) {
            myTask.start();
        }
    }

    /**
     * 设置图片数据用于显示,有返回值便于链式调用
     *
     * @param imgs 图片数据
     * @return
     */
    public AutoPlayImageView setData(Integer[] imgs) {
        this.imgs = imgs;
        init();
        return this;
    }

    /**
     * 设置图片数据用于關閉,有返回值便于链式调用
     *
     * @return
     */
    public AutoPlayImageView stopData() {
        this.imgs = imgs;
        stop();
        return this;
    }

    /**
     * 设置是否循环轮播
     *
     * @param isCircle
     * @return
     */
    public AutoPlayImageView setCircle(boolean isCircle) {
        this.isCircle = isCircle;
        return this;
    }

}