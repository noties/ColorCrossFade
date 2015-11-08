package ru.noties.ccf.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import ru.noties.ccf.CCFAnimator;
import ru.noties.debug.Debug;
import ru.noties.debug.out.AndroidLogDebugOutput;

public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorPickedListener {

    private enum CCFType {
        RGB, ARGB, HSV, HSV_WITH_ALPHA
    }

    private static final List<Integer> DURATIONS;
    static {
        DURATIONS = Arrays.asList(
                200,
                400,
                1000,
                2000,
                4000,
                8000,
                15000
        );
    }

    private static final List<TimeInterpolator> INTERPOLATORS;
    private static final String[] INTERPOLATORS_TEXT;
    static {
        INTERPOLATORS = Arrays.asList(
                (TimeInterpolator) new LinearInterpolator(),
                new AccelerateInterpolator(),
                new AccelerateDecelerateInterpolator(),
                new DecelerateInterpolator(),
                new BounceInterpolator(),
                new AnticipateInterpolator(),
                new AnticipateOvershootInterpolator(),
                new OvershootInterpolator()
        );
        INTERPOLATORS_TEXT = new String[INTERPOLATORS.size()];
        for (int i = 0, length = INTERPOLATORS.size(); i < length; i++) {
            INTERPOLATORS_TEXT[i] = INTERPOLATORS.get(i).getClass().getSimpleName();
        }
    }

    private static final String TAG_FROM = "tag.From";
    private static final String TAG_TO = "tag.To";
    private static final long DEFAULT_DURATION = 1000L;

    private PendingAnimationConfig mPendingAnimationConfig;
    private ValueAnimator mAnimator;
    private CCFType mType;
    private StatusBarColor mStatusBarColor;

    private View mColorCanvas;
    private View mFromColorIndicator;
    private View mToColorIndicator;
    private TextView mColorIndication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Debug.init(new AndroidLogDebugOutput(true));

        setContentView(R.layout.activity_main);

        mType = CCFType.HSV;
        mStatusBarColor = StatusBarColor.newInstance(getWindow());

        mPendingAnimationConfig = new PendingAnimationConfig();
        mPendingAnimationConfig.fromColor = 0xFF03a9f4;
        mPendingAnimationConfig.toColor = 0xFF009688;

        mColorIndication = (TextView) findViewById(R.id.color_indication);

        mColorCanvas = findViewById(R.id.color_canvas);
        mColorCanvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }

                final CCFAnimator ccfAnimator = animator();
                mAnimator = ccfAnimator.asValueAnimator(new CCFAnimator.OnNewColorListener() {
                    @Override
                    public void onNewColor(@ColorInt int color) {
                        mColorCanvas.setBackgroundColor(color);
                        mColorIndication.setText(Integer.toHexString(color));
                        mStatusBarColor.setStatusBarColor(color);
                    }
                });
                mAnimator.setDuration(mPendingAnimationConfig.duration > 0L ? mPendingAnimationConfig.duration : DEFAULT_DURATION);
                if (mPendingAnimationConfig.interpolator != null) {
                    mAnimator.setInterpolator(mPendingAnimationConfig.interpolator);
                }
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swapColors();
                    }
                });
                mAnimator.start();
            }
        });

        mFromColorIndicator = findViewById(R.id.color_from);
        mToColorIndicator = findViewById(R.id.color_to);
        final View swapColors = findViewById(R.id.colors_swap);

        mFromColorIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickColor(TAG_FROM, mPendingAnimationConfig.fromColor);
            }
        });

        mToColorIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickColor(TAG_TO, mPendingAnimationConfig.toColor);
            }
        });

        swapColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapColors();
            }
        });

        final View typeItem = findViewById(R.id.type_item);
        final TextView typeItemText = (TextView) typeItem.findViewById(R.id.type_selected_text);
        typeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ListAdapter adapter = new ArrayAdapter<CCFType>(MainActivity.this, android.R.layout.simple_list_item_1, CCFType.values());

                new AlertDialog.Builder(MainActivity.this)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which != mType.ordinal()) {
                                    mType = CCFType.values()[which];
                                    typeItemText.setText(mType.name());
                                }
                            }
                        }).create().show();
            }
        });
        typeItemText.setText(mType.name());

        final View durationItem = findViewById(R.id.duration_item);
        final TextView durationItemText = (TextView) findViewById(R.id.duration_item_text);
        durationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListAdapter adapter = new ArrayAdapter<Integer>(MainActivity.this, android.R.layout.simple_list_item_1, DURATIONS);

                new AlertDialog.Builder(MainActivity.this)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPendingAnimationConfig.duration = DURATIONS.get(which);
                                durationItemText.setText(String.valueOf(mPendingAnimationConfig.duration));
                            }
                        }).create().show();
            }
        });
        durationItemText.setText(String.valueOf(DEFAULT_DURATION));

        final View interpolatorItem = findViewById(R.id.interpolator_item);
        final TextView interpolatorItemText = (TextView) findViewById(R.id.interpolator_item_text);
        interpolatorItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ListAdapter adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, INTERPOLATORS_TEXT);

                new AlertDialog.Builder(MainActivity.this)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPendingAnimationConfig.interpolator = INTERPOLATORS.get(which);
                                interpolatorItemText.setText(String.valueOf(mPendingAnimationConfig.interpolator.getClass().getSimpleName()));
                            }
                        }).create().show();
            }
        });
        interpolatorItemText.setText(INTERPOLATORS.get(0).getClass().getSimpleName());

        updateColorsVisuals();
    }

    private CCFAnimator animator() {

        final int fromColor = mPendingAnimationConfig.fromColor;
        final int toColor = mPendingAnimationConfig.toColor;

        switch (mType) {

            case HSV:
                return CCFAnimator.hsv(fromColor, toColor);

            case HSV_WITH_ALPHA:
                final int fromAlpha = Color.alpha(fromColor);
                final int toAlpha = Color.alpha(toColor);
                return CCFAnimator.hsv(fromColor, toColor, fromAlpha, toAlpha);

            case RGB:
                return CCFAnimator.rgb(fromColor, toColor);

            case ARGB:
                return CCFAnimator.argb(fromColor, toColor);

            default:
                throw new IllegalStateException("Unknown CCFType: " + mType);
        }

    }

    private void pickColor(String tag, int color) {
        final ColorPickerDialog dialog = ColorPickerDialog.newInstance(tag, color);
        dialog.show(getSupportFragmentManager(), "dialog_tag");
    }

    private void updateColorsVisuals() {
        mColorCanvas.setBackgroundColor(mPendingAnimationConfig.fromColor);
        mFromColorIndicator.setBackgroundColor(mPendingAnimationConfig.fromColor);
        mToColorIndicator.setBackgroundColor(mPendingAnimationConfig.toColor);
        mStatusBarColor.setStatusBarColor(mPendingAnimationConfig.fromColor);
        mColorIndication.setText(Integer.toHexString(mPendingAnimationConfig.fromColor));
    }

    @Override
    public void onColorPicked(String tag, int color) {
        if (TAG_FROM.equals(tag)) {
            mPendingAnimationConfig.fromColor = color;
        } else if (TAG_TO.equals(tag)) {
            mPendingAnimationConfig.toColor = color;
        }

        updateColorsVisuals();
    }

    private void swapColors() {
        final int tmp = mPendingAnimationConfig.fromColor;
        mPendingAnimationConfig.fromColor = mPendingAnimationConfig.toColor;
        mPendingAnimationConfig.toColor = tmp;
        updateColorsVisuals();
    }

    private static class PendingAnimationConfig {
        int fromColor;
        int toColor;
        long duration;
        TimeInterpolator interpolator;
    }
}
