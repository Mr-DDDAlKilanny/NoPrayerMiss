/*
 * Copyright (c) 2013-2019 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayer.times.utils;

import android.content.Context;
import android.icu.util.Measure;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;


public class AutoResizeTextView extends AppCompatTextView {


    public AutoResizeTextView(Context context) {
        super(context);
        init();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i = 13;
        do {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, (MeasureSpec.getSize(heightMeasureSpec) * i) / 20f - getPaddingTop() - getPaddingBottom());
            i--;
        } while (getPaint().measureText(getText().toString()) * 1.1f > MeasureSpec.getSize(widthMeasureSpec) && i > 0);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }
}
