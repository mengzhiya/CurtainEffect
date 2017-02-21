package com.mare.curtaineffectdemo.velocity;

import android.content.Context;

/**
 * A class to generate {@link VelocityTrackerInterface}, depending on the configuration.
 */
public class VelocityTrackerFactory {

    public static final String PLATFORM_IMPL = "platform";
    public static final String NOISY_IMPL = "noisy";

    public static VelocityTrackerInterface obtain(Context ctx) {
        String tracker = "platform";
        switch (tracker) {
            case NOISY_IMPL:
                return NoisyVelocityTracker.obtain();
            case PLATFORM_IMPL:
                return PlatformVelocityTracker.obtain();
            default:
                throw new IllegalStateException("Invalid tracker: " + tracker);
        }
    }
}
