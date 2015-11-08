package ru.noties.ccf.sample;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

/**
 * Created by Dimitry Ivanov on 04.11.2015.
 */
public class ColorPickerDialog extends DialogFragment {

    public interface OnColorPickedListener {
        void onColorPicked(String tag, int color);
    }

    private static final String ARG_TAG = "arg.Tag";
    private static final String ARG_COLOR = "arg.Color";

    public static ColorPickerDialog newInstance(String tag, int color) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_TAG, tag);
        bundle.putInt(ARG_COLOR, color);

        final ColorPickerDialog fragment = new ColorPickerDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    private OnColorPickedListener mOnColorPickedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof OnColorPickedListener)) {
            throw new IllegalStateException("Activity must implement `OnColorPickedListener`");
        }

        mOnColorPickedListener = (OnColorPickedListener) activity;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle sis) {

        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_color_picker, null);

        final int color = getArguments().getInt(ARG_COLOR, 0);

        final ColorPicker picker = (ColorPicker) view.findViewById(R.id.dialog_color_picker);
        final SVBar svBar = (SVBar) view.findViewById(R.id.dialog_color_sv_bar);
        final OpacityBar alphaBar = (OpacityBar) view.findViewById(R.id.dialog_color_alpha_bar);

        picker.addSVBar(svBar);
        picker.addOpacityBar(alphaBar);

        picker.setOldCenterColor(color);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final int pickerColor = picker.getColor();
                        if (pickerColor != color) {
                            mOnColorPickedListener.onColorPicked(
                                    getArguments().getString(ARG_TAG),
                                    pickerColor
                            );
                        }

                    }
                }).create();
    }
}
