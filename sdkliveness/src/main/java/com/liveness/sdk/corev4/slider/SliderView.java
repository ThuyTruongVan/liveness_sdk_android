package com.liveness.sdk.corev4.slider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;


public class SliderView extends FrameLayout
        implements Runnable, View.OnTouchListener,
        SliderViewAdapter.DataSetListener, SliderPager.OnPageChangeListener {

    public static final int AUTO_CYCLE_DIRECTION_RIGHT = 0;
    public static final int AUTO_CYCLE_DIRECTION_LEFT = 1;
    public static final int AUTO_CYCLE_DIRECTION_BACK_AND_FORTH = 2;
    public static final String TAG = "Slider View : ";

    private final Handler mHandler = new Handler();
    private boolean mFlagBackAndForth;
    private boolean mIsAutoCycle;
    private int mAutoCycleDirection;
    private int mScrollTimeInMillis;
    private SliderViewAdapter mPagerAdapter;
    private SliderPager mSliderPager;
    private InfinitePagerAdapter mInfinitePagerAdapter;
    private OnSliderPageListener mPageListener;
    private boolean mIsInfiniteAdapter = true;
    private boolean mIsIndicatorEnabled = true;
    private int mPreviousPosition = -1;

    /*Constructor*/
    public SliderView(Context context) {
        super(context);
        setupSlideView(context);
    }

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupSlideView(context);
        setUpAttributes(context, attrs);
    }

    public SliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupSlideView(context);
        setUpAttributes(context, attrs);
    }
    /*Constructor*/

    /**
     * This class syncs all attributes from xml tag for this slider.
     *
     * @param context its android main context which is needed.
     * @param attrs   attributes from xml slider tags.
     */
    private void setUpAttributes(@NonNull Context context, AttributeSet attrs) {

        int sliderAnimationDuration = 800;
        int sliderScrollTimeInSec = 1;
        boolean sliderAutoCycleEnabled = true;
        boolean sliderStartAutoCycle = false;
        int sliderAutoCycleDirection = AUTO_CYCLE_DIRECTION_BACK_AND_FORTH;

        setSliderAnimationDuration(sliderAnimationDuration);
        setScrollTimeInSec(sliderScrollTimeInSec);
        setAutoCycle(sliderAutoCycleEnabled);
        setAutoCycleDirection(sliderAutoCycleDirection);
        setAutoCycle(sliderStartAutoCycle);


    }

    /**
     * @param listener is a callback of current item in sliderView.
     */
    public void setCurrentPageListener(OnSliderPageListener listener) {
        this.mPageListener = listener;
    }

    /**
     * @param pagerAdapter Set a SliderAdapter that will supply views
     *                     for this slider as needed.
     */
    public void setSliderAdapter(@NonNull SliderViewAdapter pagerAdapter) {
        mPagerAdapter = pagerAdapter;
        //set slider adapter
        mInfinitePagerAdapter = new InfinitePagerAdapter(pagerAdapter);
        //registerAdapterDataObserver();
        mSliderPager.setAdapter(mInfinitePagerAdapter);
        mPagerAdapter.dataSetChangedListener(this);
        // set slider on correct position whether its infinite or not.
        setCurrentPagePosition(0);
        mSliderPager.setPageTransformer(false, new SimpleTransformation());
    }

    /**
     * @param pagerAdapter Set a SliderAdapter that will supply views
     *                     for this slider as needed.
     */
    public void setSliderAdapter(@NonNull SliderViewAdapter pagerAdapter, boolean infiniteAdapter) {
        this.mIsInfiniteAdapter = infiniteAdapter;
        if (!infiniteAdapter) {
            this.mPagerAdapter = pagerAdapter;
            this.mSliderPager.setAdapter(pagerAdapter);
        } else {
            setSliderAdapter(pagerAdapter);
        }
    }


    public void setInfiniteAdapterEnabled(boolean enabled) {
        if (mPagerAdapter != null) {
            setSliderAdapter(mPagerAdapter, enabled);
        }
    }

    /**
     * @return Sliders Pager
     */
    public SliderPager getSliderPager() {
        return mSliderPager;
    }

    /**
     * @return adapter of current slider.
     */
    public PagerAdapter getSliderAdapter() {
        return mPagerAdapter;
    }

    /**
     * @return if is slider auto cycling or not?
     */
    public boolean isAutoCycle() {
        return mIsAutoCycle;
    }

    public void setAutoCycle(boolean autoCycle) {
        this.mIsAutoCycle = autoCycle;
    }

    /**
     * @param limit How many pages will be kept offscreen in an idle state.
     *              <p>You should keep this limit low, especially if your pages have complex layouts.
     *              * This setting defaults to 1.</p>
     */
    public void setOffscreenPageLimit(int limit) {
        mSliderPager.setOffscreenPageLimit(limit);
    }

    /**
     * @return sliding delay in seconds.
     */
    public int getScrollTimeInSec() {
        return mScrollTimeInMillis / 1000;
    }

    /**
     * @param time of sliding delay in seconds.
     */
    public void setScrollTimeInSec(int time) {
        mScrollTimeInMillis = time * 1000;
    }

    public int getScrollTimeInMillis() {
        return mScrollTimeInMillis;
    }

    public void setScrollTimeInMillis(int millis) {
        this.mScrollTimeInMillis = millis;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isAutoCycle()) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                stopAutoCycle();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // resume after ~2 seconds debounce.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAutoCycle();
                    }
                }, 2000);
            }
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSlideView(Context context) {
        mSliderPager = new SliderPager(context);
        mSliderPager.setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
        mSliderPager.setId(ViewCompat.generateViewId());
        LayoutParams sliderParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        addView(mSliderPager, 0, sliderParams);
        mSliderPager.setOnTouchListener(this);
        mSliderPager.addOnPageChangeListener(this);
    }

    /**
     * @param animation set slider animation manually .
     *                  it accepts {@link ##PageTransformer} animation classes.
     */
    public void setCustomSliderTransformAnimation(SliderPager.PageTransformer animation) {
        mSliderPager.setPageTransformer(false, animation);
    }

    /**
     * @param duration changes slider animation duration.
     */
    public void setSliderAnimationDuration(int duration) {
        mSliderPager.setScrollDuration(duration);
    }

    /**
     * @param duration     changes slider animation duration.
     * @param interpolator its animation duration accelerator
     *                     An interpolator defines the rate of change of an animation
     */
    public void setSliderAnimationDuration(int duration, Interpolator interpolator) {
        mSliderPager.setScrollDuration(duration, interpolator);
    }

    /**
     * This method handles correct position whether slider is on infinite mode or not
     *
     * @param position changes position of slider
     *                 items manually.
     */
    public void setCurrentPagePosition(int position) {
        mSliderPager.setCurrentItem(position, true);
    }

    /**
     * @return Nullable position of current sliding item.
     */
    public int getCurrentPagePosition() {

        if (getSliderAdapter() != null) {
            return getSliderPager().getCurrentItem() ;
        } else {
            throw new NullPointerException("Adapter not set");
        }
    }



    /**
     * @return number of items in {@link #SliderView#SliderViewAdapter)}
     */
    private int getAdapterItemsCount() {
        try {
            return getSliderAdapter().getCount();
        } catch (NullPointerException e) {
            Log.e(TAG, "getAdapterItemsCount: Slider Adapter is null so," +
                    " it can't get count of items");
            return 0;
        }
    }

    /**
     * This method stars the auto cycling
     */
    public void startAutoCycle() {
        //clean previous callbacks
        mHandler.removeCallbacks(this);

        //Run the loop for the first time
        mHandler.postDelayed(this, mScrollTimeInMillis);
    }

    /**
     * This method cancels the auto cycling
     */
    public void stopAutoCycle() {
        //clean callback
        mHandler.removeCallbacks(this);
    }

    /**
     * This method setting direction of sliders auto cycling
     * accepts constant values defined in {@link #SliderView} class
     * {@value AUTO_CYCLE_DIRECTION_LEFT}
     * {@value AUTO_CYCLE_DIRECTION_RIGHT}
     * {@value AUTO_CYCLE_DIRECTION_BACK_AND_FORTH}
     */
    public void setAutoCycleDirection(int direction) {
        mAutoCycleDirection = direction;
    }

    /**
     * @return direction of auto cycling
     * {@value AUTO_CYCLE_DIRECTION_LEFT}
     * {@value AUTO_CYCLE_DIRECTION_RIGHT}
     * {@value AUTO_CYCLE_DIRECTION_BACK_AND_FORTH}
     */
    public int getAutoCycleDirection() {
        return mAutoCycleDirection;
    }


    /**
     * This method handles sliding behaviors
     * which passed into {@link #SliderView#mHandler}
     * <p>
     * see {@link #SliderView#startAutoCycle()}
     */
    @Override
    public void run() {
        try {
            slideToNextPosition();
        } finally {
            if (mIsAutoCycle) {
                // continue the loop
                mHandler.postDelayed(this, mScrollTimeInMillis);
            }
        }
    }

    public void slideToNextPosition() {

        int currentPosition = mSliderPager.getCurrentItem();
        int adapterItemsCount = getAdapterItemsCount();
        if (adapterItemsCount > 1) {
            if (mAutoCycleDirection == AUTO_CYCLE_DIRECTION_BACK_AND_FORTH) {
                if (currentPosition % (adapterItemsCount - 1) == 0 && mPreviousPosition != getAdapterItemsCount() - 1 && mPreviousPosition != 0) {
                    mFlagBackAndForth = !mFlagBackAndForth;
                }
                if (mFlagBackAndForth) {
                    mSliderPager.setCurrentItem(currentPosition + 1, true);
                } else {
                    mSliderPager.setCurrentItem(currentPosition - 1, true);
                }
            }
            if (mAutoCycleDirection == AUTO_CYCLE_DIRECTION_LEFT) {
                mSliderPager.setCurrentItem(currentPosition - 1, true);
            }
            if (mAutoCycleDirection == AUTO_CYCLE_DIRECTION_RIGHT) {
                mSliderPager.setCurrentItem(currentPosition + 1, true);
            }
        }
        mPreviousPosition = currentPosition;
    }


    public void slideToPreviousPosition() {

        int currentPosition = mSliderPager.getCurrentItem();
        int adapterItemsCount = getAdapterItemsCount();

        if (adapterItemsCount > 1) {
            if (mAutoCycleDirection == AUTO_CYCLE_DIRECTION_BACK_AND_FORTH) {
                if (currentPosition % (adapterItemsCount - 1) == 0 && mPreviousPosition != getAdapterItemsCount() - 1 && mPreviousPosition != 0) {
                    mFlagBackAndForth = !mFlagBackAndForth;
                }
                if (mFlagBackAndForth && currentPosition < mPreviousPosition) {
                    mSliderPager.setCurrentItem(currentPosition - 1, true);
                } else {
                    mSliderPager.setCurrentItem(currentPosition + 1, true);
                }
            }
            if (mAutoCycleDirection == AUTO_CYCLE_DIRECTION_LEFT) {
                mSliderPager.setCurrentItem(currentPosition + 1, true);
            }
            if (mAutoCycleDirection == AUTO_CYCLE_DIRECTION_RIGHT) {
                mSliderPager.setCurrentItem(currentPosition - 1, true);
            }
        }
        mPreviousPosition = currentPosition;
    }

    //sync infinite pager adapter with real one
    @Override
    public void dataSetChanged() {
        if (mIsInfiniteAdapter) {
            mInfinitePagerAdapter.notifyDataSetChanged();
            mSliderPager.setCurrentItem(0, false);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // nothing to do
    }

    @Override
    public void onPageSelected(int position) {
        if (mPageListener != null) {
            mPageListener.onSliderPageChanged(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing to do
    }

    public interface OnSliderPageListener {

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        void onSliderPageChanged(int position);

    }
}
