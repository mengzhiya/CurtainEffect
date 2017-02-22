package com.mare.curtaineffect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.test.TouchUtils;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mare.curtaineffect.velocity.FlingAnimationUtils;
import com.mare.curtaineffect.velocity.VelocityTrackerFactory;
import com.mare.curtaineffect.velocity.VelocityTrackerInterface;

public class CurtainView extends ImageView {
	private static String TAG = "CurtainView";

	private ValueAnimator mCurtainChangeAnimator;
	private float mDownY = -1, mDownOriginalY = -1, mCurtainOriginalH = 1300f;
	private boolean isOccupied = false;
	private ViewGroup mCurtain;
	private VelocityTrackerInterface mVelocityTracker;
	private FlingAnimationUtils mFlingAnimationUtils;
	private float mTouchSlop;
	private float mTheshold = 0;
	private Vibrator mVibrator;

	public CurtainView(Context context) {
		this(context, null);
	}

	public CurtainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CurtainView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	private void init(Context context) {
		mFlingAnimationUtils = new FlingAnimationUtils(context, 0.6f);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mTheshold = mCurtainOriginalH / 10;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null == mCurtain)
			return false;
		float diff;
		float downY;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownOriginalY = mDownY = event.getRawY();
			isOccupied = true;
			vibrate(true);
			initVelocityTracker();
			trackMovement(event);
			return true;
		case MotionEvent.ACTION_MOVE:
			trackMovement(event);
			downY = (int) event.getRawY();
			diff = downY - mDownY;
			if (Math.abs(diff) >= mTouchSlop) {
				mDownY = downY;
				updateCurtainParameters(diff);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			downY = (int) event.getRawY();
			trackMovement(event);
			float vel = 0f;
			float vectorVel = 0f;
			if (mVelocityTracker != null) {
				mVelocityTracker.computeCurrentVelocity(1000);
				vel = mVelocityTracker.getYVelocity();
				vectorVel = (float) Math.hypot(mVelocityTracker.getXVelocity(),
						mVelocityTracker.getYVelocity());
			}
			float expandedH = downY - mDownOriginalY;
			//boolean isFling = isFling(vel, vectorVel, expandedH);
			//Log.i(TAG, "vel : " + vel +",theshold : " + mFlingAnimationUtils.getMinVelocityPxPerSecond());
			float target = 0;
			if (Math.abs(expandedH) < mTheshold
					|| vel >= mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
				target = mCurtainOriginalH;
			} else {  
				target = -mCurtainOriginalH;
			}
			vibrate(false);
			startAnim(mCurtain.getY(), target);
			recyleMovement();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * @param vel
	 *            the current vertical velocity of the motion
	 * @param vectorVel
	 *            the length of the vectorial velocity
	 * @return
	 * @return whether a fling should expands the panel; contracts otherwise
	 */
	private boolean isFling(float vel, float vectorVel, float diffY) {
		boolean beyondThesold = Math.abs(vectorVel) >= mFlingAnimationUtils
				.getMinVelocityPxPerSecond();
		return beyondThesold || Math.abs(diffY) > mCurtainOriginalH / 4;
	}

	private void recyleMovement() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public void setCurtain(ViewGroup v) {
		this.mCurtain = v;
		updateCurtainParameters(mCurtainOriginalH);
	}

	private void updateCurtainParameters(float diff) {
		ViewGroup.LayoutParams params = mCurtain.getLayoutParams();
		int originalH = params.height;
		float target = originalH + diff;
		target = target <= 0 ? 0 : target;
		params.height = (int) (target >= mCurtainOriginalH ? mCurtainOriginalH
				: target);
		mCurtain.setLayoutParams(params);
	}

	private void startAnim(float oldHeight, final float newHeight) {
		if (mCurtainChangeAnimator != null) {
			oldHeight = (Float) mCurtainChangeAnimator.getAnimatedValue();
			mCurtainChangeAnimator.cancel();
		}
		mCurtainChangeAnimator = ValueAnimator.ofFloat(oldHeight, newHeight);
		mCurtainChangeAnimator.setDuration(200);
		mCurtainChangeAnimator.setInterpolator(AnimationUtils.loadInterpolator(
				getContext(), android.R.interpolator.fast_out_slow_in));
		mCurtainChangeAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float height = (Float) mCurtainChangeAnimator
								.getAnimatedValue();
						updateCurtainParameters(height);
						isOccupied = true;
					}
				});
		mCurtainChangeAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurtainChangeAnimator = null;
				isOccupied = false;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurtainChangeAnimator = null;
				isOccupied = false;
			}
		});
		mCurtainChangeAnimator.start();
	}

	private void trackMovement(MotionEvent event) {
		float deltaX = event.getRawX() - event.getX();
		float deltaY = event.getRawY() - event.getY();
		event.offsetLocation(deltaX, deltaY);
		if (mVelocityTracker != null)
			mVelocityTracker.addMovement(event);
		event.offsetLocation(-deltaX, -deltaY);
	}

	private void initVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
		}
		mVelocityTracker = VelocityTrackerFactory.obtain(getContext());
	}

	/**
	 * 动画 或者事件 正在处理...
	 * @return
	 */
	public boolean isOccupied() {
		return isOccupied;
	}
	
	private void vibrate(boolean start){
		if (start) {
			mVibrator.vibrate(500);
			mVibrator.vibrate(new long[]{100,300}, -1);
		}else {
			if (null != mVibrator) {
				mVibrator.cancel();
			}
		}
	}

}
