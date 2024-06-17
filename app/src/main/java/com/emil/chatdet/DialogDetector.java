package com.emil.chatdet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class DialogDetector extends Dialog {
ImageView loading;
TextView result;
    @SuppressLint("MissingInflatedId")
    public DialogDetector(Context context) {
        super(context, R.style.RoundDialog);
        setContentView(R.layout.dialog_detector);
        loading = findViewById(R.id.iv_loading);
        result = findViewById(R.id.tv_det_result);
        setCancelable(false);
        Glide.with(context)
                .load(R.drawable.loading)
                .into(loading);
    }

}
