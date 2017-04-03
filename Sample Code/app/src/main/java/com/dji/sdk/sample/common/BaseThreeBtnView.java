package com.dji.sdk.sample.common;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dji.sdk.sample.R;

/**
 * Created by dji on 15/12/20.
 */
public abstract class BaseThreeBtnView extends RelativeLayout implements View.OnClickListener {
    protected TextView mTexInfo;

    protected Button middleBtn;
    protected Button leftBtn;
    protected Button rightBtn;

    public BaseThreeBtnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context, attrs);
    }

    private void initUI(Context context, AttributeSet attrs) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        mTexInfo.setText(getString(getInfoResourceId()));

        middleBtn.setText(getString(getMiddleBtnTextResourceId()));
        leftBtn.setText(getString(getLeftBtnTextResourceId()));
        rightBtn.setText(getString(getRightBtnTextResourceId()));

        middleBtn.setOnClickListener(this);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);

    }

    private String getString(int id) {
        return getResources().getString(id);
    }


    protected abstract int getMiddleBtnTextResourceId();
    protected abstract int getLeftBtnTextResourceId();
    protected abstract int getRightBtnTextResourceId();

    protected abstract int getInfoResourceId();

    protected abstract void getMiddleBtnMethod();
    protected abstract void getLeftBtnMethod();
    protected abstract void getRightBtnMethod();

}
