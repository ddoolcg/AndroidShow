package com.lcg.show;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * TODO
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 1.0
 * @since 2019/7/3 20:07
 */
public class TabEditText extends EditText {
    private String tip = "";
    private boolean init = true;
    private float baseLine = 5f;
    private float leftD = 0f;
    private Paint tipPaint;

    public TabEditText(Context context) {
        super(context);
    }

    public TabEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TabEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TabEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (init) {
            init = false;
            tipPaint = new Paint(getPaint());
            //
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabEditText);
            tip = typedArray.getString(R.styleable.TabEditText_tip);
            typedArray.recycle();
            CharSequence hint = getHint();
            if (TextUtils.isEmpty(hint)) {
                if (isEnabled()) {
                    setHint("请输入" + tip.replace("：", " ").replace(":", " "));
                } else {
                    setHint("无");
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //计算基线
        Paint.FontMetricsInt fontMetricsInt = getPaint().getFontMetricsInt();
        int dy = (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.bottom;
        baseLine = getHeight() / 2 + dy;
        if (tip == null) tip = "";
        tipPaint.setColor(getCurrentTextColor());
        // x: 开始的位置  y：基线
        canvas.drawText(tip, leftD, baseLine, tipPaint);
    }
}
