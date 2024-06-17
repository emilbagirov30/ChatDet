package com.emil.chatdet;

import android.text.InputFilter;
import android.text.Spanned;

public class IpAddressInputFilter implements InputFilter {
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (!Character.isDigit(source.charAt(i)) && source.charAt(i) != '.') {
                return "";
            }
        }
        return null;
    }
}