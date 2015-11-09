package ru.noties.ccf;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

/**
 * Created by Dimitry Ivanov on 27.10.2015.
 */
public abstract class CCFAnimator {

    public interface OnNewColorListener {
        void onNewColor(@ColorInt int color);
    }

    /**
     * Concats specified array of {@link CCFAnimator} into a {@link ru.noties.ccf.CCFAnimator.ConcatAnimator}. Maybe be used to
     * mix hsv, arg, argb CCFAnimators
     *
     * @see #hsv(int[])
     * @see #rgb(int[])
     * @see #argb(int[])
     *
     * @param animators array of {@link CCFAnimator} to include in returned {@link ru.noties.ccf.CCFAnimator.ConcatAnimator}
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator concat(final CCFAnimator... animators) {
        return new ConcatAnimator(animators);
    }

    /**
     * Creates a {@link CCFAnimator} animate alpha of specified color
     *
     * @see #argb(int, int)
     *
     * @param color starting color
     * @param toAlpha alpha amount to animate specified color
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator alpha(
            @ColorInt int color,
            @ColorInt int toAlpha
    ) {
        return argb(color, applyAlpha(color, toAlpha));
    }

    /**
     * Creates a {@link CCFAnimator} to animate `fromColor` color to `toColor`. Alpha property will be ignored
     *
     * @see #argb(int, int)
     * @see #rgb(int[])
     *
     * @param fromColor starting color
     * @param toColor end color
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator rgb(
            @ColorInt int fromColor,
            @ColorInt int toColor
    ) {
        return new RGBAnimator(null, fromColor, toColor);
    }

    /**
     * Creates a {@link CCFAnimator} to animate between array of colors.
     * For each pair of colors RGB CCFAnimator will be created
     *
     * @see #rgb(int, int)
     *
     * @param colors to animate
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator rgb(@Size(min = 2) int[] colors) {
        final CCFAnimator[] animators = new CCFAnimator[colors.length - 1];
        for (int i = 0, length = animators.length; i < length; i++) {
            animators[i] = CCFAnimator.rgb(colors[i], colors[i + 1]);
        }
        return concat(animators);
    }

    /**
     * Creates a {@link CCFAnimator} to animate `fromColor` color to `toColor`
     *
     * @see #rgb(int, int)
     * @see #argb(int[])
     *
     * @param fromColor starting color
     * @param toColor end color
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator argb(
            @ColorInt int fromColor,
            @ColorInt int toColor
    ) {

        final int fromAlpha = extractAlpha(fromColor);
        final int toAlpha   = extractAlpha(toColor);

        final AlphaEvaluator alphaEvaluator;
        if (fromAlpha != toAlpha) {
            alphaEvaluator = new AlphaEvaluatorImpl(fromAlpha, toAlpha);
        } else {
            alphaEvaluator = null;
        }

        return new RGBAnimator(alphaEvaluator, fromColor, toColor);
    }

    /**
     * Constructs a {@link CCFAnimator} from specified array of colors
     *
     * @see #argb(int, int)
     *
     * @param colors colors to cross-fade (minimum length is 2)
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator argb(@Size(min = 2) int[] colors) {
        final CCFAnimator[] animators = new CCFAnimator[colors.length - 1];
        for (int i = 0, length = animators.length; i < length; i++) {
            animators[i] = CCFAnimator.argb(colors[i], colors[i + 1]);
        }
        return concat(animators);
    }

    /**
     * Creates a {@link CCFAnimator} to animate HSV of specified colors. Alpha property will be ignored
     *
     * @see #hsv(int, int, int, int)
     * @see #hsv(int[])
     *
     * @param fromColor starting color
     * @param toColor end color
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator hsv(
            @ColorInt int fromColor,
            @ColorInt int toColor
    ) {
        return hsv(fromColor, toColor, 0, 0);
    }

    /**
     * Creates a {@link CCFAnimator} to animate HSV of specified colors
     *
     * @see #hsv(int, int)
     * @see #hsv(int[])
     *
     * @param fromColor starting color
     * @param toColor end color
     * @param fromAlpha start alpha
     * @param toAlpha end alpha
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator hsv(
            @ColorInt int fromColor,
            @ColorInt int toColor,
            @IntRange(from = 0, to = 255) int fromAlpha,
            @IntRange(from = 0, to = 255) int toAlpha
    ) {

        final AlphaEvaluator alphaEvaluator;
        if (fromAlpha != toAlpha) {
            alphaEvaluator = new AlphaEvaluatorImpl(fromAlpha, toAlpha);
        } else {
            alphaEvaluator = null;
        }

        final float[] from  = buildHSV(fromColor);
        final float[] to    = buildHSV(toColor);

        // determine whether we are backwards
        if (isHSVBackwards(from[0], to[0])) {
            return new HSVBackwardsAnimator(alphaEvaluator, fromColor, toColor, from, to);
        }

        return new HSVAnimator(alphaEvaluator, fromColor, toColor, from, to);
    }

    /**
     * Constructs a {@link CCFAnimator} from specified array of colors
     *
     * @see #hsv(int, int)
     * @see #hsv(int, int, int, int)
     *
     * @param colors colors to animate
     * @return {@link CCFAnimator}
     */
    public static CCFAnimator hsv(@Size(min = 2) int[] colors) {

        final CCFAnimator[] animators = new CCFAnimator[colors.length - 1];
        for (int i = 0, length = animators.length; i < length; i++) {
            animators[i] = CCFAnimator.hsv(colors[i], colors[i + 1]);
        }
        return concat(animators);
    }

    protected static boolean isHSVBackwards(float fromH, float toH) {
        return Math.abs(toH - fromH) > 180.F;
    }

    protected static float[] buildHSV(@ColorInt int color) {
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv;
    }

    protected static int[] buildRGB(@ColorInt int color) {
        final int[] rgb = new int[3];
        rgb[0] = (color >> 16) & 0xFF;
        rgb[1] = (color >> 8) & 0xFF;
        rgb[2] = color & 0xFF;
        return rgb;
    }

    @ColorInt
    protected static int rgbToColor(int[] rgb) {
        return (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    @ColorInt
    protected static int argbToColor(int alpha, int[] rgb) {
        return (alpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    @ColorInt
    protected static int applyAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    protected static int extractAlpha(@ColorInt int color) {
        return color >>> 24;
    }


    protected CCFAnimator(int fromColor, int toColor) {

    }

    /**
     * Returns a color depending on fraction
     * @param fraction current animation fraction
     * @return color
     */
    public abstract int getColor(@FloatRange(from = .0F, to = 1.F) float fraction);


    public ValueAnimator asValueAnimator(@NonNull final OnNewColorListener onNewColorListener) {
        final ValueAnimator animator = ValueAnimator.ofFloat(.0F, 1.F);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();
                onNewColorListener.onNewColor(getColor(fraction));
            }
        });
        return animator;
    }

    protected interface AlphaEvaluator {
        int evaluate(float fraction);
    }

    protected static class AlphaEvaluatorImpl implements AlphaEvaluator {

        private final int mFromAlpha;
        private final int mToAlpha;

        protected AlphaEvaluatorImpl(int fromAlpha, int toAlpha) {
            mFromAlpha = fromAlpha;
            mToAlpha = toAlpha;
        }

        @Override
        public int evaluate(float fraction) {
            return (int) (mFromAlpha + (mToAlpha - mFromAlpha) * fraction + .5F);
        }
    }

    protected static class RGBAnimator extends CCFAnimator {

        private final AlphaEvaluator mAlphaEvaluator;
        private final int[] mFromColor;
        private final int[] mToColor;
        private final int[] mOut;

        protected RGBAnimator(
                @Nullable AlphaEvaluator alphaEvaluator,
                @ColorInt int fromColor,
                @ColorInt int toColor
        ) {
            super(fromColor, toColor);

            this.mAlphaEvaluator = alphaEvaluator;
            this.mFromColor = buildRGB(fromColor);
            this.mToColor   = buildRGB(toColor);
            this.mOut = new int[3];
        }

        @Override
        public int getColor(float fraction) {

            mOut[0] = (int) (mFromColor[0] + ((mToColor[0] - mFromColor[0]) * fraction + .5F));
            mOut[1] = (int) (mFromColor[1] + ((mToColor[1] - mFromColor[1]) * fraction + .5F));
            mOut[2] = (int) (mFromColor[2] + ((mToColor[2] - mFromColor[2]) * fraction + .5F));

            if (mAlphaEvaluator != null) {
                return argbToColor(mAlphaEvaluator.evaluate(fraction), mOut);
            }

            return rgbToColor(mOut);
        }
    }

    protected abstract static class AbsHSVAnimator extends CCFAnimator {

        private final AlphaEvaluator mAlphaEvaluator;

        private final float[] mFrom;
        private final float[] mTo;
        private final float[] mOut;

        protected AbsHSVAnimator(
                @Nullable AlphaEvaluator alphaEvaluator,
                @ColorInt int fromColor,
                @ColorInt int toColor,
                @Size(3) float[] fromHSV,
                @Size(3) float[] toHSV
        ) {
            super(fromColor, toColor);
            this.mAlphaEvaluator = alphaEvaluator;
            this.mFrom = fromHSV;
            this.mTo = toHSV;
            this.mOut = new float[3];
        }

        @Override
        public int getColor(float fraction) {

            mOut[0] = getHue(fraction);
            mOut[1] = mFrom[1] + ((mTo[1] - mFrom[1]) * fraction);
            mOut[2] = mFrom[2] + ((mTo[2] - mFrom[2]) * fraction);

            if (mAlphaEvaluator != null) {
                return Color.HSVToColor(mAlphaEvaluator.evaluate(fraction), mOut);
            }

            return Color.HSVToColor(mOut);
        }

        protected abstract float getHue(float fraction);
    }

    protected static class HSVAnimator extends AbsHSVAnimator {

        private final float mFromH;
        private final float mDiff;

        protected HSVAnimator(AlphaEvaluator alphaEvaluator, int fromColor, int toColor, float[] fromHSV, float[] toHSV) {
            super(alphaEvaluator, fromColor, toColor, fromHSV, toHSV);
            mFromH = fromHSV[0];
            mDiff = toHSV[0] - fromHSV[0];
        }

        @Override
        protected float getHue(float fraction) {
            return mFromH + (mDiff * fraction);
        }
    }

    protected static class HSVBackwardsAnimator extends AbsHSVAnimator {

        private final float mFromH;
        private final float mDiff;
        private final boolean mFromIsBigger;

        protected HSVBackwardsAnimator(AlphaEvaluator alphaEvaluator, int fromColor, int toColor, float[] fromHSV, float[] toHSV) {
            super(alphaEvaluator, fromColor, toColor, fromHSV, toHSV);
            mFromH = fromHSV[0];
            mDiff = 360.F - (Math.abs(toHSV[0] - fromHSV[0]));
            mFromIsBigger = Float.compare(mFromH, toHSV[0]) > 0;
        }

        @Override
        protected float getHue(float fraction) {

            final float evaluated = mDiff * fraction;

            if (mFromIsBigger) {
                final float left = mFromH + evaluated;
                if (Float.compare(left, 360.F) > 0) {
                    return left - 360.F;
                }
                return left;
            }

            final float left = mFromH - evaluated;
            if (Float.compare(left, .0F) < 0) {
                return 360.F - Math.abs(left);
            }

            return left;
        }
    }

    protected static class ConcatAnimator extends CCFAnimator {

        private final CCFAnimator[] mAnimators;
        private final int mLength;
        private final float mFractionStep;

        protected ConcatAnimator(@NonNull CCFAnimator[] animators) {
            super(0, 0);
            this.mAnimators = animators;
            this.mLength = mAnimators.length;
            this.mFractionStep = 1.F / mLength;
        }

        @Override
        public int getColor(float fraction) {

            final int index;
            final float stepFraction;

            {
                final int i = (int) (fraction / mFractionStep);

                if (i >= mLength) {
                    index = mLength - 1;
                    stepFraction = 1.F;
                } else {
                    index = i;
                    stepFraction = (fraction % mFractionStep) / mFractionStep;
                }
            }

            return mAnimators[index].getColor(stepFraction);
        }
    }
}
