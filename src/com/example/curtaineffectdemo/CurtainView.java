package com.example.curtaineffectdemo;

import com.example.curtaineffectdemo.velocity.FlingAnimationUtils;
import com.example.curtaineffectdemo.velocity.VelocityTrackerFactory;
import com.example.curtaineffectdemo.velocity.VelocityTrackerInterface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class CurtainView extends ImageView {
	private static String TAG = "CurtainView";
	private int curtainHeigh = -1;

	private ValueAnimator mCurtainChangeAnimator;
	private final int ORIGINAL_HEIGHT = getResources().getDisplayMetrics().heightPixels -100;
	private float mDdownY = -1, mDownOriginalY = -1;
	private boolean isMove = false;
	private View img_curtain_ad;
	private VelocityTrackerInterface mVelocityTracker;
	private FlingAnimationUtils mFlingAnimationUtils;
	float mTouchSlop;

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
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null == img_curtain_ad)
			return false;
		float diff;
		float downY;
		if (!isMove) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownOriginalY = (int) event.getRawY();
				mDdownY = mDownOriginalY;
				initVelocityTracker();
				trackMovement(event);
				return true;
			case MotionEvent.ACTION_MOVE:
				trackMovement(event);
				downY = (int) event.getRawY();
				diff = downY - mDownOriginalY;
				if (Math.abs(diff) > mTouchSlop) {
					updateCurtainParameters((int) (curtainHeigh + diff));
					mDdownY = downY;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				downY = (int) event.getRawY();
				diff = mDdownY - mDownOriginalY;
				trackMovement(event);
				float vel = 0f;
				float vectorVel = 0f;
				if (mVelocityTracker != null) {
					mVelocityTracker.computeCurrentVelocity(1000);
					vel = mVelocityTracker.getYVelocity();
					vectorVel = (float) Math.hypot(
							mVelocityTracker.getXVelocity(),
							mVelocityTracker.getYVelocity());
				}
				boolean isFling = isFling(vel, vectorVel, diff);
				Log.e(TAG, " diff : " + diff + ", vel = " + vel
						+ ", vectorVel= " + vectorVel);
				if (isFling) {
					startAnim(curtainHeigh, vectorVel > 0 ? ORIGINAL_HEIGHT : 0);
				} else {
					updateCurtainParameters((int) (curtainHeigh + diff));
				}
				recyleMovement();
				break;
			default:
				break;
			}
		}
		return false;
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
		if (Math.abs(vectorVel) < mFlingAnimationUtils
				.getMinVelocityPxPerSecond()) {
			return diffY > getHeight();
		}
		return false;
	}

	private void recyleMovement() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public void setCurtain(View v) {
		this.img_curtain_ad = v;
	};

	private void updateCurtainParameters(int y) {
		ViewGroup.LayoutParams params = img_curtain_ad.getLayoutParams();
		Log.e(TAG, " y : " + y);
		y = (y >= ORIGINAL_HEIGHT ? ORIGINAL_HEIGHT : y <= 0 ? 0 : y);
		Log.e(TAG, " y2 : " + y);
		params.height = y;
		curtainHeigh = y;
		img_curtain_ad.setLayoutParams(params);
		float curY = curtainHeigh + getHeight();
		setY(curY <= 0 ? 0 : curY);
	}

	private void startAnim(int oldHeight, final int newHeight) {
		if (mCurtainChangeAnimator != null) {
			oldHeight = (Integer) mCurtainChangeAnimator.getAnimatedValue();
			mCurtainChangeAnimator.cancel();
		}
		mCurtainChangeAnimator = ValueAnimator.ofInt(oldHeight, newHeight);
		mCurtainChangeAnimator.setDuration(1000);
		mCurtainChangeAnimator.setInterpolator(AnimationUtils.loadInterpolator(
				getContext(), android.R.interpolator.fast_out_slow_in));
		mCurtainChangeAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						int height = (Integer) mCurtainChangeAnimator
								.getAnimatedValue();
						updateCurtainParameters(height);
						isMove = true;
					}
				});
		mCurtainChangeAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurtainChangeAnimator = null;
				isMove = false;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurtainChangeAnimator = null;
				isMove = false;
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

}
