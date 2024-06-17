package com.emil.chatdet;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.motion.widget.OnSwipe;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import java.io.File;

public class DialogBigPicture extends DialogFragment {
    private ImageView bigPicture;
  File picture;

    public DialogBigPicture(File picture) {
        this.picture = picture;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_big_picture, container, false);
        bigPicture = (ImageView) view.findViewById(R.id.iv_big_picture);

        Glide.with(getActivity()).load(picture).into(bigPicture);
        view.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getRawX();
                        float endY = event.getRawY();
                        float dx = endX - startX;
                        float dy = endY - startY;
                        float distance = Math.abs(dx) + Math.abs(dy);
                        if (distance > 200) {
                            dismiss();
                        }
                        break;
                }
                return true;
            }
        });
        return view;
    }

}