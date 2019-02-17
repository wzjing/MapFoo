package com.infinitytech.mapfoo.utils

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.infinitytech.mapfoo.R
import java.lang.ref.WeakReference

@Suppress("RedundantVisibilityModifier", "unused", "MemberVisibilityCanPrivate")
class FullScreenBehavior<V : View>(context: Context, attrs: AttributeSet?)
    : CoordinatorLayout.Behavior<V>(context, attrs) {

    public class FullScreenCallback {
        public var stateChange: ((fullScreen: View, newState: Int) -> Unit)? = null
        public var slide: ((fullScreen: View, slideOffset: Float) -> Unit)? = null

        public fun onStateChange(stateChange: (fullScreen: View, newState: Int) -> Unit) {
            this.stateChange = stateChange
        }

        public fun onSlide(slide: (fullScreen: View, slideOffset: Float) -> Unit) {
            this.slide = slide
        }
    }

    companion object {
        public val PEEK_HEIGHT_AUTO = -1
        public val HIDE_THRESHOLD = 0.5f
        public val HIDE_FRICTION = 0.1f
        public val STATE_DRAGGING = 1
        public val STATE_SETTLING = 2
        public val STATE_EXPANDED = 3
        public val STATE_COLLAPSED = 4
        public val STATE_HIDDEN = 5

        public inline fun <reified V : View> from(view: V): FullScreenBehavior<V> {
            val params = view.layoutParams
            if (params is CoordinatorLayout.LayoutParams) {
                val behavior = params.behavior
                if (behavior is FullScreenBehavior) {
                    @Suppress("UNCHECKED_CAST")
                    return behavior as FullScreenBehavior<V>
                } else {
                    throw IllegalArgumentException(
                            "The view is not associated with FullScreenBehavior")
                }
            } else {
                throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            }
        }
    }

    private var mParentHeight: Int = 0
    private var mNestedScrollDy: Int = 0
    private var mNestedScrolled: Boolean = false
    private var mNestedScrollingChildRef: WeakReference<View?>? = null
    private var mViewRef: WeakReference<V?>? = null
    private var mMinOffset: Int = 0
    private var mMaxOffset: Int = 0
    private var mState: Int = 0
    private var mIgnoreEvents: Boolean = false
    private var mTouchEventScrollingChild: Boolean = false
    private var mViewDragHelper: ViewDragHelper? = null
    private var mVelocityTracker: VelocityTracker? = null
        get() {
            if (field == null) {
                field = VelocityTracker.obtain()
            }
            return field
        }
    private var mMaximumVelocity: Float = 0f
    private var mActivePointerId: Int = 0
    private var mInitialY: Int = 0
    private var mLastNestedScrollDy: Int = 0
    private var mPeekHeightAuto: Boolean = false
    private var mPeekHeightMin: Int = 0

    public var hideable: Boolean = false

    public var skipCollapsed: Boolean = false

    public var state: Int = STATE_COLLAPSED
        set(value) {
            if (field == value) return
            if (mViewRef == null) {
                if (state == STATE_COLLAPSED || state == STATE_EXPANDED ||
                        (hideable && state == STATE_HIDDEN)) {
                    field = value
                }
                return
            }
            val child: V? = mViewRef?.get()
            child ?: return
            val parent = child.parent
            if (parent != null && parent.isLayoutRequested && ViewCompat.isAttachedToWindow(child)) {
                child.post {
                    startSettlingAnimation(child, value)
                }
            } else {
                startSettlingAnimation(child, value)
            }


        }

    public var peekHeight: Int = 0
        set(value) {
            var layout = false
            if (value == PEEK_HEIGHT_AUTO) {
                if (!mPeekHeightAuto) {
                    mPeekHeightAuto = true
                    layout = true
                }
            } else if (mPeekHeightAuto || field != value) {
                mPeekHeightAuto = false
                field = Math.max(0, value)
                mMaxOffset = mParentHeight - value
                layout = true
            }
            if (layout && mState == STATE_COLLAPSED) {
                mViewRef?.get()?.requestLayout()
            }
        }
        get() = if (mPeekHeightAuto) PEEK_HEIGHT_AUTO else field

    private var callback: FullScreenCallback? = null
    public fun fullScreenCallback(init: FullScreenCallback.() -> Unit) {
        callback = FullScreenCallback()
        callback?.init()
    }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.FullScreenBehavior_layout)
        val value = array.peekValue(R.styleable.FullScreenBehavior_layout_fullscreen_peekHeight)
        peekHeight = if (value != null && value.data == PEEK_HEIGHT_AUTO) {
            value.data
        } else {
            array.getDimensionPixelSize(
                    R.styleable.FullScreenBehavior_layout_fullscreen_peekHeight, PEEK_HEIGHT_AUTO)
        }
        hideable = array.getBoolean(R.styleable.FullScreenBehavior_layout_fullscreen_hideable, false)
        skipCollapsed = array.getBoolean(R.styleable.FullScreenBehavior_layout_fullscreen_skipCollapsed, false)
        array.recycle()
        mMaximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable {
        return SavedState(super.onSaveInstanceState(parent, child)!!, mState)
    }

    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(parent, child, ss.superState)
        mState = if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            STATE_COLLAPSED
        } else {
            ss.state
        }
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        parent!!
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            child.fitsSystemWindows = true
        }
        val savedTop = child.top
        parent.onLayoutChild(child, layoutDirection)

        val peekHeight =
                if (mPeekHeightAuto) {
                    if (mPeekHeightMin == 0) {
                        mPeekHeightMin = parent.resources.getDimensionPixelSize(R.dimen.fullscreen_behavior_peek_height_min)
                    }
                    Math.max(mPeekHeightMin, mParentHeight - parent.height * 9 / 16)
                } else {
                    this.peekHeight
                }
        mMinOffset = Math.max(0, mParentHeight - child.height)
        mMaxOffset = Math.max(mParentHeight - peekHeight, mMinOffset)

        when {
            mState == STATE_EXPANDED ->
                ViewCompat.offsetTopAndBottom(child, mMinOffset)
            hideable && mState == STATE_HIDDEN ->
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

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            mIgnoreEvents = true
            return false
        }

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }

        mVelocityTracker?.addMovement(event)
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
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID &&
                        parent?.isPointInChildBounds(child, initialX, mInitialY) == false
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

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
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
        mVelocityTracker?.addMovement(event)
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
        mLastNestedScrollDy = 0
        mNestedScrolled = false
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View,
                                   dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val scrollChild = mNestedScrollingChildRef?.get()
        if (target != scrollChild) {
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
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
            }
        }
        dispatchOnSlide(child.top)
        mLastNestedScrollDy = dy
        mNestedScrolled = true
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V,
                                    target: View, type: Int) {
        if (child.top == mMinOffset) {
            setStateInternal(STATE_EXPANDED)
            return
        }
        if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef?.get()
                || !mNestedScrolled) {
            return
        }
        val top: Int
        val targetState: Int
        when {
            mLastNestedScrollDy > 0 -> {
                top = mMinOffset
                targetState = STATE_HIDDEN
            }
            hideable && shouldHide(child, getYVelocity()) -> {
                top = mParentHeight
                targetState = STATE_HIDDEN
            }
            mLastNestedScrollDy == 0 -> {
                val currentTop = child.top
                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
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
                            super.onNestedPreFling(coordinatorLayout, child, target,
                                    velocityX, velocityY))


    private fun setStateInternal(state: Int) {
        if (mState == state) return
        mState = state
        val fullScreen = mViewRef?.get()
        callback?.stateChange?.invoke(fullScreen ?: return, state)
    }

    private fun reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun shouldHide(child: View, yVel: Float): Boolean {
        if (skipCollapsed) return true
        if (child.top < mMaxOffset) {
            return false
        }
        val newTop = child.top + yVel * HIDE_FRICTION
        return Math.abs(newTop - mMaxOffset) / this.peekHeight.toFloat() > HIDE_THRESHOLD
    }

    private fun findScrollingChild(view: View): View? = when {
        ViewCompat.isNestedScrollingEnabled(view) -> view
        view is ViewGroup -> (0..view.childCount).find {
            ViewCompat.isNestedScrollingEnabled(view.getChildAt(it))
        }?.let { view.getChildAt(it) } ?: findScrollingChild(view)
        else -> null
    }

    private fun getYVelocity(): Float {
        mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity)
        return mVelocityTracker?.getYVelocity(mActivePointerId) ?: 0f
    }

    fun startSettlingAnimation(child: View, state: Int) {
        val top: Int =
                when {
                    state == STATE_COLLAPSED -> mMaxOffset
                    state == STATE_EXPANDED -> mMinOffset
                    hideable && state == STATE_HIDDEN -> mParentHeight
                    else -> throw IllegalArgumentException("Illegal state argument: $state")
                }
        if (mViewDragHelper?.smoothSlideViewTo(child, child.left, top) == true) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
        } else {
            setStateInternal(state)
        }
    }

    private val mDragCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (mState == STATE_DRAGGING) {
                return false
            }
            if (mTouchEventScrollingChild) {
                return false
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                val scroll = mNestedScrollingChildRef?.get()
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    return false
                }
            }
            return mViewRef != null && (mViewRef?.get() == child)
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dispatchOnSlide(top)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val top: Int
            val targetState: Int
            if (yvel < 0) {
                top = mMinOffset
                targetState = STATE_EXPANDED
            } else if (hideable && shouldHide(releasedChild, yvel)) {
                top = mParentHeight
                targetState = STATE_HIDDEN
            } else if (yvel == 0.0f) {
                val currentTop = releasedChild.top
                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                    top = mMinOffset
                    targetState = STATE_EXPANDED
                } else {
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                }
            } else {
                top = mMaxOffset
                targetState = STATE_COLLAPSED
            }
            if (mViewDragHelper?.settleCapturedViewAt(releasedChild.left, top) == true) {
                setStateInternal(STATE_SETTLING)
                ViewCompat.postOnAnimation(releasedChild,
                        SettleRunnable(releasedChild, targetState))
            } else {
                setStateInternal(targetState)
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(top, mMinOffset, if (hideable) mParentHeight else mMaxOffset)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left ?: 0
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (hideable) mParentHeight - mMinOffset else mMaxOffset - mMinOffset
        }
    }

    private fun dispatchOnSlide(top: Int) {
        val fullScreen = mViewRef!!.get()
        if (fullScreen != null && callback != null) {
            if (top > mMinOffset) {
                callback?.slide?.invoke(fullScreen, (mMaxOffset - top).toFloat() /
                        (mParentHeight - mMaxOffset))
            } else {
                callback?.slide?.invoke(fullScreen, (mMaxOffset - top).toFloat() /
                        (mMaxOffset - mMinOffset))
            }
        }
    }

    private inner class SettleRunnable(private val view: View, private val targetState: Int) : Runnable {
        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper?.continueSettling(true) == true) {
                ViewCompat.postOnAnimation(view, this)
            } else {
                setStateInternal(targetState)
            }
        }

    }

    private class SavedState : AbsSavedState {
        val state: Int

        constructor(source: Parcel, loader: ClassLoader? = null) : super(source) {
            state = source.readInt()
        }

        constructor(superState: Parcelable, state: Int) : super(superState) {
            this.state = state
        }

        constructor(parcel: Parcel) : super(parcel) {
            state = parcel.readInt()
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            super.writeToParcel(dest, flags)
            dest?.writeInt(state)
        }

//        public val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.ClassLoaderCreator<SavedState> {
//            override fun createFromParcel(source: Parcel?): SavedState {
//                return SavedState(source!!)
//            }
//
//            override fun createFromParcel(source: Parcel?, loader: ClassLoader?): SavedState {
//                return SavedState(source!!, loader)
//            }
//
//            override fun newArray(size: Int): Array<SavedState?> {
//                return arrayOfNulls(size)
//            }
//
//        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

}