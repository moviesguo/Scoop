package com.mfw.roadbook.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.binggege.scoop.R;
import com.mfw.base.utils.DPIUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * Created by yujintao on 2017/3/15.
 * 简化版tablayout，为了支持我们特定的需求
 */

public class MfwTabLayout extends HorizontalScrollView {
    private static final String TAG = MfwTabLayout.class.getSimpleName();

    public static final int INDICATOR_DURATION = 500;

    //模式
    public static final int MODE_MATCH = 0;
    public static final int MODE_WRAP = 1;
    @Deprecated
    public static final int MODE_ADAPT = 2;

    public static final int MODE_EXACT = 3;


    //default value
    public static final int TAB_DEFAULT_COLOR = 0xff474747;
    public static final int TAB_DEFAULT_SELECT_COLOR = 0xff474747;
    public static final int TAB_DEFAULT_TAB_MINWIDTH = 40;
    public static final int INDICATOR_DEFAULT_HEIGHT = 4;
    public static final int INDICATOR_DEFAULT_COLOR = 0xffff8400;

    public static final int MIN_TAB_MARGIN = DPIUtil._10dp;

    //绑定viewpager需要的数据
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private DataSetObserver mInnerDataObeserver;

    //val
    private Tab mCurrentTab;


    //data
    private ArrayList<Tab> mTabs;
    private ArrayList<OnTabSelectedListener> mTabSelectedListeners;
    //view
    private InnerView mInnerView;
    private Paint mBottomLinePaint = new Paint();
    //view attr
    private ColorStateList mTabTextColorStateList;
    private int mMode;//tab填充模式
    private int mMinTabWidth;//tab最小宽度,只有在model为WRAP时起作用
    private int mTabStartMargin;//tab开始margin，距左边距的位置
    private int mTabEndMargin;//tab结束的margin,距右边距的位置
    private int mTabPaddingStart;//tab padding left
    private int mTabPaddingEnd;//tab padding right
    private int mTabPaddingTop;//tab padding top
    private int mTabPaddingBottom;//tab padding bottom
    private int mTabMargin = 10;//只有在tab模式为WRAP时起作用
    private int mIndicatorHeight;//跟随线的高度
    private int mIndicatorColor;//跟随线的颜色
    private Drawable mIndicatorDrawable;//跟随线的drawable,如果存在上面两个属性失效
    private boolean smileIndicatorEnable;//是否使用笑脸indicator，目前只有首页使用
    private boolean forceAequilate = false;//是否强制要求每个tab等宽
    private boolean drawIndicator = true; //是否绘制tab指示线

    private int mTabTitleAndIconPadding;
    private float mTextSize;
    private int mBottomLineHeight;//最底部的分割线，默认高度0不显示

    private int mSlipIndicatorColor = -1;//如果是-1，则没有
    private OnTabLayoutScrollListener mOnTabLayoutScrollListener;
    private OnIndicatorUpdateListener mIndicatorUpdateListener;
    private onMeasureSucceed mOnMeasureSucceed;

    private int minTabMargin = MIN_TAB_MARGIN;

    private boolean matchToWrap = false;

    public MfwTabLayout(Context context) {
        this(context, null);
    }

    public MfwTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        setHorizontalScrollBarEnabled(false);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.mfw_tab_layout, 0, R.style.MfwTabDefaultStyle);
        mMode = typedArray.getInt(R.styleable.mfw_tab_layout_mfwtab_mode, MODE_MATCH);
        if (mMode == MODE_ADAPT) {
            mMode = MODE_MATCH;
        }
        int colorTabDefault = typedArray.getColor(R.styleable.mfw_tab_layout_mfwtab_tab_text_color, TAB_DEFAULT_COLOR);
        int colorTabSelectedDefault = typedArray.getColor(R.styleable.mfw_tab_layout_mfwtab_tab_text_select_color, TAB_DEFAULT_SELECT_COLOR);
        mTabTextColorStateList = createColorStateList(colorTabDefault, colorTabSelectedDefault);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_text_size, DPIUtil.dip2px(16));
        mMinTabWidth = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_minwidth, TAB_DEFAULT_TAB_MINWIDTH);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_indicator_height, INDICATOR_DEFAULT_HEIGHT);
        mTabPaddingStart = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_padding_start, 0);
        mTabPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_padding_end, 0);
        mTabPaddingTop = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_padding_top, 0);
        mTabPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_padding_bottom, 0);
        mTabStartMargin = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_start_margin, 0);
        mTabEndMargin = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_end_margin, 0);
        mIndicatorColor = typedArray.getColor(R.styleable.mfw_tab_layout_mfwtab_indicator_color, INDICATOR_DEFAULT_COLOR);
        mTabMargin = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_tab_margin, mTabMargin);
        mIndicatorDrawable = typedArray.getDrawable(R.styleable.mfw_tab_layout_mfwtab_indicator_drawable);
        mTabTitleAndIconPadding = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_title_icon_padding, 0);
        mBottomLineHeight = typedArray.getDimensionPixelSize(R.styleable.mfw_tab_layout_mfwtab_bottom_line_height, 0);
        mSlipIndicatorColor = typedArray.getColor(R.styleable.mfw_tab_layout_mfwtab_slip_indicator_color, -1);
        typedArray.recycle();
        mTabs = new ArrayList<>();
        mTabSelectedListeners = new ArrayList<>();
        mInnerView = createInnerView(context, attrs);
        mInnerView.setPadding(mTabStartMargin, 0, mTabEndMargin, 0);
        addView(mInnerView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        mBottomLinePaint.setColor(Color.parseColor("#1e000000"));
        mBottomLinePaint.setStrokeWidth(mBottomLineHeight);

    }

    protected InnerView createInnerView(Context context, AttributeSet attrs) {
        return new InnerView(context, attrs);
    }


    /**
     * 直接关联viewpager
     *
     * @param viewPager
     */
    public void setupViewPager(final ViewPager viewPager) {
        if (viewPager == null) {
            throw new IllegalArgumentException("viewpager must not be null");
        }
        if (viewPager.getAdapter() == null) {
            throw new IllegalArgumentException("viewpager must has set adapter");
        }
        if (mPagerAdapter != null) {
            mPagerAdapter.unregisterDataSetObserver(mInnerDataObeserver);
        }
        mViewPager = viewPager;
        mPagerAdapter = mViewPager.getAdapter();
        if (mInnerDataObeserver == null) {
            mInnerDataObeserver = new InnerDataObserver();
        }
        mPagerAdapter.registerDataSetObserver(mInnerDataObeserver);
        viewPager.addOnPageChangeListener(new InnerScrollListener());
        addTabSelectListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                mViewPager.setCurrentItem(tab.position, false);
            }

            @Override
            public void onTabUnselected(Tab tab) {

            }
        });
        performViewPagerInject();

    }

    public void setSmileIndicatorEnable(boolean smileIndicatorEnable) {
        this.smileIndicatorEnable = smileIndicatorEnable;

        if (mInnerView != null) {
            mInnerView.setSmileIndicatorEnable(smileIndicatorEnable);
        }
    }

    public void setDrawIndicator(boolean draw) {
        this.drawIndicator = draw;
    }

    public void setForceAequilate(boolean forceAequilate) {
        this.forceAequilate = forceAequilate;
    }

    public void setMiniTabWidth(int miniTabWidth) {
        mMinTabWidth = miniTabWidth;
    }

    public void setTabPadding(int left, int top, int right, int bottom) {
        mTabPaddingStart = left;
        mTabPaddingTop = top;
        mTabPaddingEnd = right;
        mTabPaddingBottom = bottom;
    }

    public void setOnTabLayoutScrollListener(OnTabLayoutScrollListener onTabLayoutScrollListener) {
        mOnTabLayoutScrollListener = onTabLayoutScrollListener;
    }


    public void setOnIndicatorUpdateListener(OnIndicatorUpdateListener indicatorUpdateListener) {
        this.mIndicatorUpdateListener = indicatorUpdateListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (mOnTabLayoutScrollListener != null) {
            mOnTabLayoutScrollListener.onTabLayoutScroll(x, y);
        }
    }

    public void setMinTabMargin(int minTabMargin) {
        this.minTabMargin = minTabMargin;
    }

    public void setTabMargin(int mTabMargin) {
        this.mTabMargin = mTabMargin;
    }

    /**
     * 获取当前选中的tab
     *
     * @return
     */
    public Tab getSelectedTab() {
        return mCurrentTab;
    }

    /**
     * 初始化字符串数组
     *
     * @param titles title数组
     * @return 返回生成的tab
     */
    public ArrayList<Tab> setupStringArray(String[] titles) {
        return setupStringArray(titles, 0, true);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int width = Math.max(getWidth(), mInnerView.getWidth());
        if (mBottomLineHeight > 0 && mTabs.size() > 0) {
            canvas.drawLine(0, getHeight() - (float) mBottomLineHeight / 2, width, getHeight() - (float) mBottomLineHeight / 2, mBottomLinePaint);
        }
    }

    /**
     * 初始化字符串数组
     *
     * @param titles               title数组
     * @param defaultSelectedIndex 默认选择的index
     * @param needTriggerListener  是否触发回掉
     * @return 返回生成的tab
     */
    public ArrayList<Tab> setupStringArray(String[] titles, int defaultSelectedIndex, boolean needTriggerListener) {
        clear();
        mInnerView.removeAllViews();
        defaultSelectedIndex = titles.length < defaultSelectedIndex ? 0 : defaultSelectedIndex;
        for (int i = 0, size = titles.length; i < size; i++) {
            final Tab tab = newTab();
            tab.setTitle(titles[i]);
            addTabInterval(tab, i, i == defaultSelectedIndex, needTriggerListener);
        }
        return mTabs;
    }

    /**
     * @param index
     */
    public void setTabSelected(int index) {
        performTabSelect(index);
        selectTab(mTabs.get(index), true, true);
    }


    /**
     * 解析viewpager，添加tab
     */
    private void performViewPagerInject() {
        if (mPagerAdapter == null) {
            return;
        }
        mTabs.clear();
        mInnerView.removeAllViews();
        mCurrentTab = null;
        for (int index = 0, size = mPagerAdapter.getCount(); index < size; index++) {
            final Tab tab = newTab();
            tab.setTitle(mPagerAdapter.getPageTitle(index));
            addTabInterval(tab, index, mViewPager.getCurrentItem() == index, false);
        }
    }

    /**
     * 通过index获取tab
     *
     * @param index
     * @return
     */
    public Tab getTabAt(int index) {
        return (index < 0 || index >= getTabCount()) ? null : mTabs.get(index);
    }

    /**
     * 获取tab总数
     *
     * @return
     */
    public int getTabCount() {
        return mTabs.size();
    }


    /**
     * 设置tab mode
     *
     * @param mode
     */
    public void setTabMode(int mode) {
        if (mMode != mode) {
            mMode = mode;
            performViewPagerInject();
            requestLayout();
        }
    }

    /**
     * 获取当前tabmode
     *
     * @return
     */
    public int getTabMode() {
        return mMode;
    }


    /**
     * 添加手动添加Tab
     *
     * @param tab
     */
    public void addTab(Tab tab) {
        addTab(tab, mTabs.isEmpty());
    }


    /**
     * 手动添加tab
     *
     * @param tab        添加的tab
     * @param isSelected 是否选中
     */
    public void addTab(Tab tab, boolean isSelected) {
        addTab(tab, isSelected, false);

    }

    /**
     * 手动添加tab
     *
     * @param tab                 添加的tab
     * @param isSelected          是否选中
     * @param needTriggerListener 是否触发回掉
     */
    public void addTab(Tab tab, boolean isSelected, boolean needTriggerListener) {
        addTabInterval(tab, mTabs.size(), isSelected, needTriggerListener);
    }

    /**
     * 删除某个tab
     *
     * @param index
     */
    public void removeIndex(int index) {
        removeTab(mTabs.get(index));
    }

    /**
     * 选中某个tab
     *
     * @param position
     */
    public void selectTabPosition(int position) {
        selectTabPosition(position, true);
    }

    /**
     * 选中某个tab
     *
     * @param position
     */
    public void selectTabPosition(int position, boolean needTriggerListener) {
        if (mTabs.size() > position) {
            selectTab(mTabs.get(position), true, needTriggerListener);
        }
    }

    void scrollToPosition(int position, float positionOffset, boolean updateIndicator) {
        int scrollX = calculateScrollXForTab(position, positionOffset);
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.cancel();
        }
        scrollTo(scrollX, 0);
        if (updateIndicator) {
            mInnerView.setIndicatorPosition(position, positionOffset);
        }
    }

    /**
     * 计算跟随线位置
     *
     * @param position
     * @param positionOffset
     * @return
     */
    private int calculateScrollXForTab(int position, float positionOffset) {
        if (mMode == MODE_WRAP) {
            final View selectedChild = mInnerView.getChildAt(position);
            final View nextChild = position + 1 < mInnerView.getChildCount()
                    ? mInnerView.getChildAt(position + 1)
                    : null;
            final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;
            return selectedChild.getLeft()
                    + (int) (((selectedWidth + nextWidth) * 0.5f + mTabMargin) * positionOffset)
                    + (selectedChild.getWidth() / 2)
                    - (getWidth() / 2);
        }
        return 0;
    }

    /**
     * 添加onTabSelectedListener
     *
     * @param onTabSelectedListener
     */
    public void addTabSelectListener(OnTabSelectedListener onTabSelectedListener) {
        if (!mTabSelectedListeners.contains(onTabSelectedListener)) {
            mTabSelectedListeners.add(onTabSelectedListener);
        }
    }

    /**
     * 删除onTabSelectedListener
     *
     * @param onTabSelectedListener
     */
    public void removeTabSelectListener(OnTabSelectedListener onTabSelectedListener) {
        if (mTabSelectedListeners.contains(onTabSelectedListener)) {
            mTabSelectedListeners.remove(onTabSelectedListener);
        }
    }

    public void clearTabSelectListener() {
        mTabSelectedListeners.clear();
    }

    /**
     * 清楚数据状态
     */
    public void clear() {
        mInnerView.removeAllViews();
        mTabs.clear();
        mCurrentTab = null;
    }


    /**
     * 更新所有tab
     */
    public void notifyTabChanged() {
        for (Tab tab : mTabs) {
            tab.notifyTabChanged();
        }
    }


    /**
     * @param tab
     */
    public void removeTab(Tab tab) {
        mTabs.remove(tab);
        mInnerView.removeView(tab.root);
        resetTabPosition();
        if (tab == mCurrentTab && mTabs.size() > 0) {
            mCurrentTab = null;
            selectTab(mTabs.get(0), true, true);
        } else {
            if (mCurrentTab != null) {
                mInnerView.setIndicatorPosition(mCurrentTab.position, 0);
            }
        }
        requestLayout();
    }


    /**
     * 选中tab，更新indicator
     *
     * @param tab
     * @param updateIndicator
     */
    private void selectTab(Tab tab, boolean updateIndicator, boolean needTriggerListener) {
        if (mCurrentTab == tab) {
            scrollToPosition(tab.position, 0, false);
        } else {
            if (updateIndicator) {
                if (mCurrentTab == null) {
                    mInnerView.setIndicatorPosition(tab.position, 0);
                } else {
                    mInnerView.indicatorToPosition(tab.position);
                }
                animateToTabInterval(tab.position, false);
            }
        }
        if (mCurrentTab != null && needTriggerListener) {
            triggerTabUnselect(mCurrentTab);
        }
        mCurrentTab = tab;
        if (needTriggerListener) {
            triggerTabSelect(mCurrentTab);
        }
        performTabSelect(tab.position);
    }


    //滑动的animator
    private ValueAnimator mScrollAnimator;


    /**
     * 滚动到position的位置
     *
     * @param position
     */
    public void animateToTab(int position) {
        animateToTabInterval(position, true);
    }

    private void animateToTabInterval(int position, boolean needUpdateIndicator) {
        if (getWindowToken() == null || !ViewCompat.isLaidOut(this)) {
            scrollToPosition(position, 0f, true);
            return;
        }
        final int startScrollX = getScrollX();
        final int targetScrollX = calculateScrollXForTab(position, 0);
        if (startScrollX != targetScrollX) {
            if (mScrollAnimator == null) {
                mScrollAnimator = new ValueAnimator();
                mScrollAnimator.setInterpolator(new FastOutLinearInInterpolator());
                mScrollAnimator.setDuration(INDICATOR_DURATION);
                mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        scrollTo((Integer) animation.getAnimatedValue(), 0);
                    }
                });
            }
//            if (MfwCommon.DEBUG) {
//                MfwLog.d(TAG, "animateToTab  = " + position);
//            }
            mScrollAnimator.setIntValues(startScrollX, targetScrollX);
            mScrollAnimator.start();
        }
        if (needUpdateIndicator) {
            mInnerView.indicatorToPosition(position);
        }
    }

    /**
     * 触发tabselect的监听器
     *
     * @param tab
     */
    void triggerTabUnselect(Tab tab) {
        for (OnTabSelectedListener onTabSelectedListener : mTabSelectedListeners) {
            onTabSelectedListener.onTabUnselected(tab);
        }
    }


    /**
     * 触发tabselect的监听器
     *
     * @param tab
     */
    private void triggerTabSelect(Tab tab) {
        for (OnTabSelectedListener onTabSelectedListener : mTabSelectedListeners) {
            onTabSelectedListener.onTabSelected(tab);
        }
    }

    /**
     * 内部添加tab的方法
     *
     * @param tab                 要添加的tab
     * @param index               序号
     * @param isSelected          是否被选中
     * @param needTriggerListener 是否触发回掉函数
     */
    private void addTabInterval(Tab tab, int index, boolean isSelected, boolean needTriggerListener) {
        tab.tabLayout = this;
        if (tabIntervalObserver != null) {
            tabIntervalObserver.onTabIntervalObserverAdd(tab, index);
        }
        mInnerView.addView(tab.root, index, createLayoutParam());
        mTabs.add(index, tab);
        resetTabPosition();
        if (isSelected) {
            performTabSelect(index);
            selectTab(tab, true, needTriggerListener);
        }
        tab.updateTab();
        requestLayout();
    }

    /**
     * 重置tab的position
     */
    private void resetTabPosition() {
        for (int i = 0, size = mTabs.size(); i < size; i++) {
            mTabs.get(i).position = i;
        }
    }

    public interface TabIntervalObserver {
        void onTabIntervalObserverAdd(Tab tab, int index);
    }

    private TabIntervalObserver tabIntervalObserver;

    public void setTabIntervalObserver(TabIntervalObserver tabIntervalObserver) {
        this.tabIntervalObserver = tabIntervalObserver;
    }

    /**
     * 完成tab的select效果
     *
     * @param position
     */
    private void performTabSelect(int position) {
        int count = mInnerView.getChildCount();
        if (position < count && !mInnerView.getChildAt(position).isSelected()) {
            for (int index = 0; index < count; index++) {
                View view = mInnerView.getChildAt(index);
                view.setSelected(position == index);
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() == 0 || isInEditMode()) {
            return;
        }
        final ViewGroup viewGroup = (ViewGroup) getChildAt(0);
        final int paddingSum = viewGroup.getPaddingLeft() + viewGroup.getPaddingRight();//这个值也就是mTabStartMargin和mTabEndMargin
        final int childCount = viewGroup.getChildCount();

        if (mMode == MODE_MATCH || matchToWrap) {
            //match模式下，无论state是atMost还是exactly，直接用使用最大值
            final int mostWidth = MeasureSpec.getSize(widthMeasureSpec);
            //MATCH 模式下，如果添加的tab过多导致tab无法显示完整怎么处理？
            //目前的策略是增加滑动，同时使用mTabMargin,作为间距....所以尽量别出现这个情况好吗？？直接使用wrap
            final int contentWidth = mostWidth - paddingSum;
            int sumWidth = 0;
            for (int i = 0; i < childCount; i++) {
                final View innerView = viewGroup.getChildAt(i);
                sumWidth += innerView.getMeasuredWidth();
            }
            matchToWrap = sumWidth + mTabMargin * (childCount - 1) > contentWidth;
            if (mOnMeasureSucceed != null && sumWidth != 0 && matchToWrap && mMode == MODE_MATCH) {
                //sumWidth != 0 测量成功 ，matchToWrap 超出View可以横滑 ,  mMode == MODE_MATCH 只有第一次的时候mMode == MODE_MATCH，以后都变成了MODE_WRAP，防止重复进入
                mOnMeasureSucceed.canScrollStateChanged(true);
            } else if (mOnMeasureSucceed != null && sumWidth != 0 && !matchToWrap && mMode == MODE_WRAP) {
                //sumWidth != 0 测量成功 ，!matchToWrap 没有超出View不可以横滑 ,  mMode == MODE_WRAP 因为上一次可以横滑的时候mMode == MODE_WRAP，防止重复进入
                mOnMeasureSucceed.canScrollStateChanged(false);
            }
            if (matchToWrap) {//如果所有子view的大小加上最小间距大小已经超过了match大小的话，直接采用WRAP模式
                mMode = MODE_WRAP;
                for (int i = 0; i < childCount; i++) {
                    View inner = viewGroup.getChildAt(i);
                    if (i != childCount - 1) {
                        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) inner.getLayoutParams();
                        marginLayoutParams.setMargins(0, 0, mTabMargin, 0);
                    }
                }

                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            } else {
                mMode = MODE_MATCH;
            }

            int spaceWidth = contentWidth - sumWidth;
            int margin = Math.round(spaceWidth * 1f / childCount / 2);
            if (margin > 0) {
                for (int i = 0; i < childCount; i++) {
                    View inner = viewGroup.getChildAt(i);
                    updateInnerMargin((MarginLayoutParams) inner.getLayoutParams(), margin);
                }
            }

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (mMode == MODE_EXACT) {//强制模式，不考虑子view需要多大，直接平分给子view
            final int mostWidth = MeasureSpec.getSize(widthMeasureSpec);
            final int contentWidth = mostWidth - paddingSum;
            int avgWidth = Math.round(contentWidth * 1f / childCount);
            for (int i = 0; i < childCount; i++) {
                View innerView = viewGroup.getChildAt(i);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) innerView.getLayoutParams();
                layoutParams.width = Math.min(avgWidth, innerView.getMeasuredWidth());
                updateInnerMargin(layoutParams, Math.max(0, (avgWidth - layoutParams.width) / 2));
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (mMode == MODE_WRAP) {
            for (int i = 0; i < childCount; i++) {
                View inner = viewGroup.getChildAt(i);
                if (i != childCount - 1) {
                    MarginLayoutParams marginLayoutParams = (MarginLayoutParams) inner.getLayoutParams();
                    marginLayoutParams.setMargins(0, 0, mTabMargin, 0);
                }
            }


            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    private void updateInnerMargin(MarginLayoutParams layoutParams, int margin) {
        layoutParams.setMargins(margin, 0, margin, 0);
        mInnerView.setMargin(margin, margin);
    }


    private static final String getMeasureMode(int measureSpec) {
        String mode = "";
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.AT_MOST:
                mode = "at_most";
                break;
            case MeasureSpec.EXACTLY:
                mode = "exactly";
                break;
            case MeasureSpec.UNSPECIFIED:
                mode = "unspecified";
                break;
        }
        return mode;
    }

    public static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;

        return new ColorStateList(states, colors);
    }

    /**
     * 目前返回相同的layoutParam，后面方便扩展
     *
     * @return
     */
    private LinearLayout.LayoutParams createLayoutParam() {
        LinearLayout.LayoutParams layoutParams;
        if (mMode == MODE_MATCH) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0);
        } else {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0);
        }
        return layoutParams;
    }


    public Tab newTab() {
        Tab tab = new Tab(getContext());
        return tab;
    }
    //tab parent
    protected class InnerView extends LinearLayout {

        private int mLastPosition = -1;
        private float mPositionOffset = 0;//0-1
        private int indicatorLeft;
        private int indicatorRight;
        private Paint mIndicatorPaint;
        private Paint mBottomLinePaint;
        private Paint mFlipIndocatorPaint;
        Path path;
        private RectF leftRound;
        private RectF rightRound;

        private Path smilePath = new Path();


        private boolean smileIndicatorEnable;
        private Paint smileIndicatorPaint;

        private int marginLeft;
        private int marginRight;

        private boolean moveToRight = true;
        private int gapWidth = 0;
        private int smileIndicatorStartX = 0;
        private int smileWidth = DPIUtil.dip2px(30);


        public InnerView(Context context) {
            this(context, null);
        }

        public InnerView(Context context, @Nullable AttributeSet attrs) {
            super(context);
            //处理分割线
            int[] arr = new int[]{android.R.attr.divider, android.R.attr.showDividers, android.R.attr.dividerPadding};
            TypedArray typedArray = context.obtainStyledAttributes(attrs, arr);
            setDividerDrawable(typedArray.getDrawable(0));
            setShowDividers(typedArray.getInt(1, SHOW_DIVIDER_NONE));
            setDividerPadding(typedArray.getDimensionPixelSize(2, 0));
            typedArray.recycle();
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mIndicatorPaint.setColor(mIndicatorColor);
            mIndicatorPaint.setStyle(Paint.Style.FILL);

            smileIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            smileIndicatorPaint.setColor(Color.parseColor("#ffdb26"));
            smileIndicatorPaint.setStyle(Paint.Style.STROKE);
            smileIndicatorPaint.setStrokeWidth(DPIUtil.dip2px(2.5f));
            smileIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);

            mFlipIndocatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mFlipIndocatorPaint.setColor(mSlipIndicatorColor);
            mFlipIndocatorPaint.setStyle(Paint.Style.FILL);
            setWillNotDraw(false);
            path = new Path();
            path.setFillType(Path.FillType.WINDING);
            leftRound = new RectF();
            rightRound = new RectF();

        }


        @Override
        protected void dispatchDraw(Canvas canvas) {
            if (mSlipIndicatorColor != -1) {
                canvas.drawRect(indicatorLeft, 0, indicatorRight, getMeasuredHeight(), mFlipIndocatorPaint);
            }
            super.dispatchDraw(canvas);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            if (!drawIndicator) {
                return;
            }
//            if (MfwCommon.DEBUG) {
//                MfwLog.d(TAG, "draw indicator = " + indicatorLeft + "-" + indicatorRight);
//            }
            if (indicatorLeft >= 0 && indicatorRight > indicatorLeft) {
                if (smileIndicatorEnable) {
                    int indicatorWidth = indicatorRight - indicatorLeft;

                    smilePath.reset();
                    float lineX = 0;
                    float lineY = 0;

                    int gap = findGapWidth();
                    float rangeFloat = gap * 1.2f / DPIUtil.dip2px(40);

                    float range = (float) Math.floor(rangeFloat * 100) / 100;


                    int y = getHeight() - DPIUtil.dip2px(range + 8);

                    int offsetX = gap * 3 / 4 - (findStartX() + indicatorWidth / 2) + gap / 2;
                    double omega = 2 * Math.PI / gap;
                    int indicatorStart = indicatorLeft + ((indicatorWidth) / 2 - smileWidth / 2);
                    for (int i = indicatorStart; i <= indicatorStart + smileWidth; i++) {
                        lineX = i;

                        lineY = (float) (DPIUtil.dip2px(range) * Math.sin(omega * (i + offsetX)) + y);
//                        lineY = (float) ((-Math.cos(i/findGapWidth()/(2 * Math.PI)) + findStartX()*(findGapWidth()/(2*Math.PI))+1)/2*DPIUtil.dip2px(4)) + y;

                        if (i == indicatorStart) {
                            smilePath.moveTo(lineX, lineY);
                        } else {
                            smilePath.lineTo(lineX, lineY);
                        }

                    }

                    canvas.drawPath(smilePath, smileIndicatorPaint);


                } else if (mIndicatorDrawable != null) {
                    mIndicatorDrawable.setBounds(indicatorLeft, getHeight() - mIndicatorDrawable.getIntrinsicHeight() - mBottomLineHeight, indicatorRight, getHeight() - mBottomLineHeight);
                    mIndicatorDrawable.draw(canvas);
                } else {
                    final int height = getHeight() - mBottomLineHeight;
                    //下面的画线很坑，没办法...
                    path.reset();
                    path.moveTo(indicatorLeft, height);
                    leftRound.set(indicatorLeft, height - mIndicatorHeight, indicatorLeft + mIndicatorHeight * 2, height + mIndicatorHeight);
                    path.arcTo(leftRound, 180, 90);
                    path.lineTo(indicatorRight - mIndicatorHeight, height - mIndicatorHeight);
                    rightRound.set(indicatorRight - mIndicatorHeight * 2, height - mIndicatorHeight, indicatorRight, height + mIndicatorHeight);
                    path.arcTo(rightRound, 270, 90);
                    path.lineTo(indicatorLeft, height);
                    canvas.drawPath(path, mIndicatorPaint);
                }
            }


        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
//            if (MfwCommon.DEBUG) {
//                MfwLog.d(TAG, "draw onLayout = " + indicatorLeft + "-" + indicatorRight);
//            }
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
            } else {
                updateIndicator();
            }
        }

        private int findGapWidth() {

            View firstIndicator = getChildAt(mLastPosition);

            View secondIndicator = getChildAt(mLastPosition + 1);
            if (secondIndicator == null) {
                secondIndicator = getChildAt(mLastPosition - 1);
            }

            if (firstIndicator != null && secondIndicator != null) {
                int x1 = firstIndicator.getLeft() + (firstIndicator.getRight() - firstIndicator.getLeft()) / 2;
                int x2 = secondIndicator.getLeft() + (firstIndicator.getRight() - firstIndicator.getLeft()) / 2;

                gapWidth = Math.abs(x2 - x1);

            }

            return gapWidth;
        }

        private int findStartX() {

            final View firstIndicator = getChildAt(mLastPosition);
            if (firstIndicator != null) {
                int left = firstIndicator.getLeft();
                int right = firstIndicator.getRight();

                smileIndicatorStartX = left;
            }

            return smileIndicatorStartX;
        }

        private void updateIndicator() {
            final View currentView = getChildAt(mLastPosition);
            final View next = getChildAt(mLastPosition + 1);
            if (currentView == null) return;
            if (next != null) {
                int left = (int) (currentView.getLeft() + (next.getLeft() - currentView.getLeft()) * mPositionOffset);
                int right = (int) (currentView.getRight() + (next.getRight() - currentView.getRight()) * mPositionOffset);
                setIndicator(left, right);
            } else {
                setIndicator(currentView.getLeft(), currentView.getRight());
                mPositionOffset = 0;
            }
        }

        private void setIndicatorPosition(int position, float positionOffset) {
            mLastPosition = position;
            mPositionOffset = positionOffset;
            updateIndicator();
        }


        private void setIndicator(int left, int right) {
            if (indicatorLeft != left || indicatorRight != right) {

                indicatorLeft = left;
                indicatorRight = right;
//                if (MfwCommon.DEBUG) {
//                    MfwLog.d(TAG, "setIndicator left = " + left + " right " + right);
//                }
                ViewCompat.postInvalidateOnAnimation(this);
                if (mIndicatorUpdateListener != null) {
                    mIndicatorUpdateListener.onIndicatorUpdate(left, right);
                }
            }
        }

        public void setSmileIndicatorEnable(boolean smileIndicatorEnable) {
            this.smileIndicatorEnable = smileIndicatorEnable;
        }

        public void setMargin(int left, int right) {
            marginLeft = left;
            marginRight = right;
        }

        private ValueAnimator mIndicatorAnimator;

        private void indicatorToPosition(int position) {
            if (position == mLastPosition) return;
            if ((getWindowToken() == null || !ViewCompat.isLaidOut(this))) {
                return;
            }
            final View current = getChildAt(mLastPosition);
            final View next = getChildAt(position);

            if (current != null) {
                if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                    mIndicatorAnimator.cancel();
                }
                mIndicatorAnimator = new ValueAnimator();
                mIndicatorAnimator.setInterpolator(new FastOutSlowInInterpolator());
                mIndicatorAnimator.setDuration(INDICATOR_DURATION);

                mIndicatorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();
                        int left = (int) ((next.getLeft() - current.getLeft()) * fraction + current.getLeft());
                        int right = (int) ((next.getRight() - current.getRight()) * fraction + current.getRight());
                        setIndicator(left, right);
//                        if (MfwCommon.DEBUG) {
//                            MfwLog.d(TAG, "onAnimationUpdate left = " + left + " right " + right);
//                        }
                    }
                });
                mIndicatorAnimator.setIntValues(0, 1);
                mIndicatorAnimator.start();
                mLastPosition = position;
            } else {
                setIndicator(next.getLeft(), next.getRight());
                mLastPosition = position;


            }


        }

    }

    @IntDef(flag = true, value = {Gravity.LEFT, Gravity.RIGHT, Gravity.BOTTOM, Gravity.TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IconGravity {
    }


    public static class TabView extends FrameLayout {

        public Tab mTab;

        public TabView(Context context, Tab tab) {
            super(context);
            this.mTab = tab;
//            都不喜欢，注释掉
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                setForeground(getResources().getDrawable(R.drawable.mfw_tablayout_tab_select_bg));
//            } else {
//                setBackground(getResources().getDrawable(R.drawable.mfw_tablayout_tab_select_bg));
//            }
        }


        @Override
        public final boolean performClick() {
            super.performClick();
            mTab.tabLayout.selectTab(mTab, true, true);
            return true;
        }


        @Override
        public final void setSelected(boolean selected) {
            super.setSelected(selected);
            if (selected) {
//                mTab.textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
//                mTab.textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
            mTab.textView.setSelected(selected);
            if (mTab.customView != null) {
                mTab.customView.setSelected(selected);
            }
        }
    }


    /**
     * tab用来展现tablayout title的view，默认支持图标和文字，可以自定义view
     */
    public static class Tab {

        private TextView textView;
        private int position;
        MfwTabLayout tabLayout;
        private Drawable iconDrawable;
        private int iconWidth;
        private int iconHeight;
        private View customView;//自定义view
        private Object tag;


        private FrameLayout root;

        @IconGravity
        private int iconGravity = Gravity.RIGHT;

        Context mContext;

        private Tab(Context context) {
            mContext = context;
            root = new TabView(mContext, this);
            textView = new TextView(context);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.CENTER);
            root.addView(textView);
            root.setClickable(true);
        }


        private void resoveIconGravity(TextView textView, Drawable drawable, @IconGravity int gravity) {
            switch (gravity) {
                case Gravity.BOTTOM:
                    textView.setCompoundDrawables(null, null, drawable, null);
                    break;
                case Gravity.LEFT:
                    textView.setCompoundDrawables(drawable, null, null, null);
                    break;
                case Gravity.TOP:
                    textView.setCompoundDrawables(null, drawable, null, null);
                    break;
                case Gravity.RIGHT:
                default:
                    textView.setCompoundDrawables(null, null, null, drawable);
                    break;
            }
        }


        private void updateTab() {
            if (customView != null) {
                textView.setVisibility(View.GONE);
                root.removeView(customView);
                if (customView.getLayoutParams() == null) {
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.gravity = Gravity.CENTER;
                    root.addView(customView, lp);
                } else {
                    root.addView(customView);
                }
            } else {
                textView.setTextColor(tabLayout.mTabTextColorStateList);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabLayout.mTextSize);
                textView.setMinWidth(tabLayout.mMinTabWidth);
                textView.setCompoundDrawablePadding(tabLayout.mTabTitleAndIconPadding);
                if (iconDrawable != null) {
                    if (iconWidth != 0 && iconHeight != 0) {
                        iconDrawable.setBounds(0, 0, iconWidth, iconHeight);
                    } else {
                        iconDrawable.setBounds(0, 0, iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight());
                    }
                    resoveIconGravity(textView, iconDrawable, iconGravity);
                } else {
                    textView.setCompoundDrawables(null, null, null, null);
                }
            }

            root.setPadding(tabLayout.mTabPaddingStart, tabLayout.mTabPaddingTop, tabLayout.mTabPaddingEnd, tabLayout.mTabPaddingBottom);
            root.requestLayout();
        }

        public FrameLayout getRoot() {
            return root;
        }

        public TextView getTextView() {
            return textView;
        }

        /**
         * 更新自身tab
         */
        public void notifyTabChanged() {
            updateTab();
        }

        /**
         * 设置展现title
         *
         * @param title
         */
        public Tab setTitle(CharSequence title) {
            CharSequence origin = textView.getText();
            textView.setText(title);
            if (!TextUtils.isEmpty(origin) && !TextUtils.equals(title, origin)) {
                notifyTabChanged();
            }
            return this;
        }

        /**
         * 设置titleIcon,必须在添加tab前设定
         *
         * @param drawableID icon的drawable
         */
        public Tab setTitleIcon(@DrawableRes int drawableID) {
            iconDrawable = ContextCompat.getDrawable(mContext, drawableID);
            return this;
        }

        public Tab setIconGravity(@IconGravity int gravity) {
            this.iconGravity = gravity;
            return this;
        }

        public Tab setTag(Object tag) {
            this.tag = tag;
            return this;
        }

        public Object getTag() {
            return tag;
        }

        /**
         * 设置titleIcon,必须在添加tab前设定
         *
         * @param drawable icon的drawable
         */
        public Tab setTitleIcon(Drawable drawable) {
            iconDrawable = drawable;
            return this;
        }


        public Tab setIconWithAndHeight(int width, int height) {
            this.iconHeight = height;
            this.iconWidth = width;
            return this;
        }

        /**
         * 必须在添加tab前设定
         *
         * @param view
         */
        public Tab setCustomView(View view) {
            customView = view;
            return this;
        }

        public View getCustomView() {
            return customView;
        }

        public int getPosition() {
            return position;
        }


        @Override
        public String toString() {
            return "Tab{" +
                    "position=" + position +
                    '}';
        }
    }

    /**
     * 关联viewpager滚动使用的
     */
    private class InnerScrollListener implements ViewPager.OnPageChangeListener {
        private int scrollState;
        private int previousScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            boolean updateIndicator = !(scrollState == SCROLL_STATE_SETTLING
                    && previousScrollState == SCROLL_STATE_IDLE);
            scrollToPosition(position, positionOffset, updateIndicator);
        }

        @Override
        public void onPageSelected(int position) {
            final boolean updateIndicator = scrollState == SCROLL_STATE_IDLE
                    || (scrollState == SCROLL_STATE_SETTLING
                    && previousScrollState == SCROLL_STATE_IDLE);
            if (mCurrentTab != null && mCurrentTab.position == position) {
                return;
            }
            selectTab(mTabs.get(position), updateIndicator, true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            previousScrollState = scrollState;
            scrollState = state;
        }
    }

    /**
     * 内部使用的关联viewpagger的情况
     */
    private class InnerDataObserver extends DataSetObserver {
        public InnerDataObserver() {
            super();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            performViewPagerInject();

        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            performViewPagerInject();
        }
    }


    /**
     * tab的监听器
     */
    public interface OnTabSelectedListener {

        void onTabSelected(Tab tab);


        void onTabUnselected(Tab tab);
    }

    public interface OnTabLayoutScrollListener {
        void onTabLayoutScroll(int x, int y);
    }

    public interface OnIndicatorUpdateListener{
        void onIndicatorUpdate(int left,int right);

    }

    public interface onMeasureSucceed {
        void canScrollStateChanged(boolean can);
    }

    public void setStartAndEndMargin(int pxStart, int pxEnd) {
        if (mInnerView != null) {
            //这两个成员变量除了init()也没有别的地方用到，以防万一还是改掉它的值。
            mTabStartMargin = pxStart;
            mTabEndMargin = pxEnd;
            mInnerView.setPadding(pxStart, 0, pxEnd, 0);
        }
    }

    public void setOnMeasureSucceed(onMeasureSucceed onMeasureSucceed) {
        mOnMeasureSucceed = onMeasureSucceed;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
        }
    }
}
