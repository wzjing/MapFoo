package com.infinitytech.mapfoo

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.*
import java.lang.ref.WeakReference

class FullScreenBehavior<V : View>(private val context: Context?, private val attrs: AttributeSet?)
    : CoordinatorLayout.Behavior<V>(context, attrs) {

    companion object {
        public val PEEK_HEIGHT_AUTO = -1
        public val HIDE_THRESHOLD = 0.5f
        public val HIDE_FRICTION = 0.1f
        public val STATE_DRAGGING = 1
        public val STATE_SETTLING = 2
        public val STATE_EXPANDED = 3
        public val STATE_COLLAPSED = 4
        public val STATE_HIDDEN = 5
    }

    private var mParentHeight = 0
    private var mNestedScrollDy = 0
    private var mNestedScrolled = false
    private var mNestedScrollingChildRef: WeakReference<View?>? = null
    private var mViewRef: WeakReference<View?>? = null
    private var mMaximumVelocity = 0f
    private var mMinOffset = 0
    private var mMaxOffset = 0
    private var mPeekHeight = 0
    private var mState = 0
    private var mHideable = false
    private var mIgnoreEvents = false
    private var mTouchEventScrollingChild = false
    private var mViewDragHelper: ViewDragHelper? = null
    private var mVelocityTracker = lazy { VelocityTracker.obtain() }
    private var mActivePointerId = 0
    private var mInitialY = 0
    private var mLastNestedScrollBy = 0

    public var peerkHeight: Int = 100
        set(value) {
            field = value
            TODO("Something interlink")
        }

    init {
        mMaximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()
    }

    override fun onLayoutChild(parent: CoordinatorLayout?, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            child.fitsSystemWindows = true
        }
        val savedTop = child.top
        parent?.onLayoutChild(child, layoutDirection)
        mMinOffset = 0
        mMaxOffset = Math.max(mParentHeight - mPeekHeight, mMinOffset)

        when {
            mState == STATE_EXPANDED ->
                ViewCompat.offsetTopAndBottom(child, mMinOffset)
            mHideable && mState == STATE_HIDDEN ->
                ViewCompat.offsetTopAndBottom(child, mParentHeight)
            mState == STATE_COLLAPSED ->
                ViewCompat.offsetTopAndBottom(child, mMaxOffset)
            mState == STATE_DRAGGING || mState == STATE_SETTLING ->
                ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
        }

        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
        }

        mViewRef = WeakReference(child)
        mNestedScrollingChildRef = WeakReference(findScrollingChild(child))
        return true
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            mIgnoreEvents = true
            return false
        }

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }

        mVelocityTracker.value.addMovement(event)
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mTouchEventScrollingChild = false
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                if (mIgnoreEvents) {
                    mIgnoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val initialX = event.x.toInt()
                mInitialY = event.y.toInt()
                val scroll: View? = mNestedScrollingChildRef?.get()
                if (scroll != null && parent?.isPointInChildBounds(scroll, initialX, mInitialY) == true) {
                    mActivePointerId = event.getPointerId(event.actionIndex)
                    mTouchEventScrollingChild = true
                }
            }
        }

        if (!mIgnoreEvents && mViewDragHelper?.shouldInterceptTouchEvent(event) == true) {
            return true
        }

        val scroll: View? = mNestedScrollingChildRef?.get()
        return action == MotionEvent.ACTION_MOVE && scroll != null &&
                !mIgnoreEvents && mState != STATE_DRAGGING &&
                parent?.isPointInChildBounds(scroll, event.x.toInt(), event.y.toInt()) != true &&
                Math.abs(mInitialY - event.y) > mViewDragHelper?.touchSlop ?: 0
    }

    override fun onTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }
        val action = event.actionMasked
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        mViewDragHelper?.processTouchEvent(event)
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        mVelocityTracker.value.addMovement(event)
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (Math.abs(mInitialY - event.y) > mViewDragHelper?.touchSlop ?: 0) {
                mViewDragHelper?.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !mIgnoreEvents
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V,
                                     directTargetChild: View, target: View, axes: Int,
                                     type: Int): Boolean {
        mNestedScrollDy = 0
        mNestedScrolled = false
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View,
                                   dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val scrollChild = mNestedScrollingChildRef?.get()
        if (scrollChild != null) {
            return
        }
        val currentTop = child.top
        val newTop = currentTop - dy
        if (dy > 0) {
            if (newTop < mMinOffset) {
                consumed[1] = currentTop - mMinOffset
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setStateInternal(STATE_EXPANDED)
            } else {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            }
        } else if (dy < 0) {
            if (!target.canScrollVertically(-1)) {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            } else {
                consumed[1] = currentTop - mMaxOffset
            }
        }
        dispatchOnSlide(child.top)
        mLastNestedScrollBy = dy
        mNestedScrolled = false
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V,
                                    target: View, type: Int) {
        if (child.top == mMinOffset) {
            setStateInternal(STATE_EXPANDED)
            return
        }
        if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef?.get() || !mNestedScrolled) {
            return
        }
        var top: Int
        var targetState: Int
        when {
            mLastNestedScrollBy > 0 -> {
                top = mMinOffset
                targetState = STATE_HIDDEN
            }
            mHideable && shouldHide(child, getYVelocity()) -> {
                top = mParentHeight
                targetState = STATE_HIDDEN
            }
            mLastNestedScrollBy == 0 -> {
                val currentTop = child.top
                if (Math.abs(currentTop-mMinOffset) < Math.abs(currentTop -mMaxOffset)) {
                    top = mMaxOffset
                    targetState = STATE_EXPANDED
                } else {
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                }
            }
            else -> {
                top = mMaxOffset
                targetState = STATE_COLLAPSED
            }

        }
        if (mViewDragHelper?.smoothSlideViewTo(child, child.left, top) == true) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        mNestedScrolled = false

    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View,
                                  velocityX: Float, velocityY: Float): Boolean =
            mNestedScrollingChildRef != null &&
                    target == mNestedScrollingChildRef?.get() &&
                    (mState != STATE_EXPANDED ||
                            super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY))

    private fun findScrollingChild(view: View): View? = when {
        ViewCompat.isNestedScrollingEnabled(view) -> view
        view is ViewGroup -> (0..view.childCount).find {
            ViewCompat.isNestedScrollingEnabled(view.getChildAt(it))
        }?.let { view.getChildAt(it) } ?: findScrollingChild(view)
        else -> null
    }

    private fun reset() {

    }

    private fun dispatchOnSlide(top: Int) {

    }

    private fun setStateInternal(state: Int) {

    }

    private fun shouldHide(child: View, yvel: Float): Boolean {
        return false
    }

    private fun getYVelocity(): Float {
        mVelocityTracker.value.computeCurrentVelocity(1000, mMaximumVelocity)
        return mVelocityTracker.value.getYVelocity(mActivePointerId)
    }

    private val mDragCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
        }

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            return super.clampViewPositionVertical(child, top, dy)
        }

        override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
            return super.clampViewPositionHorizontal(child, left, dx)
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return super.getViewVerticalDragRange(child)
        }
    }

    private inner class SettleRunnable(private val view: View, private val targetState: Int): Runnable {
        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper?.continueSettling(true) == true) {
                ViewCompat.postOnAnimation(view, this)
            } else {
                setStateInternal(targetState)
            }
        }

    }

}