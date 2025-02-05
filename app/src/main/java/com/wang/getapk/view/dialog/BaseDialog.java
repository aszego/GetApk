package com.wang.getapk.view.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.wang.getapk.R;
import com.wang.getapk.util.AutoGrayThemeHelper;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewbinding.ViewBinding;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Author: wangxiaojie6
 * Date: 2017/12/28
 */

public abstract class BaseDialog<Builder extends BaseBuilder<?>, VB extends ViewBinding> extends
        AppCompatDialog {

    public static final int POSITIVE = 1;
    public static final int NEUTRAL = 2;
    public static final int NEGATIVE = 3;

    @IntDef({POSITIVE, NEUTRAL, NEGATIVE})
    public @interface DialogAction {
    }

    @Nullable
    private AppCompatTextView mTitleTV;
    @Nullable
    private AppCompatButton mNeutralBtn;
    @Nullable
    private AppCompatButton mNegativeBtn;
    @Nullable
    private AppCompatButton mPositiveBtn;

    private CompositeDisposable mCompositeDisposable;
    Builder mBuilder;

    VB viewBinding;

    @Nullable
    protected AutoGrayThemeHelper mGrayThemeHelper;

    BaseDialog(Builder builder) {
        super(builder.context);
        mBuilder = builder;
        mCompositeDisposable = new CompositeDisposable();
        setCancelable(builder.cancelable);
        setCanceledOnTouchOutside(builder.canceledOnTouchOutside);
        setOnShowListener(builder.showListener);
        setOnDismissListener(builder.dismissListener);
        setOnCancelListener(builder.cancelListener);
        setOnKeyListener(builder.keyListener);
        getWindow().setSoftInputMode(mBuilder.softInputMode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = getViewBinding();
        setContentView(viewBinding.getRoot());
        mGrayThemeHelper = createGrayThemeHelper(getWindow().getDecorView(), getContext());
        initCommonView();
        initViewListener();
        afterView(mBuilder.context, mBuilder);
    }

    private void initViews(View view) {
        mTitleTV = view.findViewById(R.id.title_tv);
        mNeutralBtn = view.findViewById(R.id.neutral_btn);
        mNegativeBtn = view.findViewById(R.id.negative_btn);
        mPositiveBtn = view.findViewById(R.id.positive_btn);
        if (mNeutralBtn != null) {
            mNeutralBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNeutral();
                }
            });
        }
        if (mNegativeBtn != null) {
            mNegativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNegative();
                }
            });
        }
        if (mPositiveBtn != null) {
            mPositiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPositive();
                }
            });
        }
    }

    @Nullable
    protected AutoGrayThemeHelper createGrayThemeHelper(View view, Context context) {
        return new AutoGrayThemeHelper(view, context, AutoGrayThemeHelper.QINGMING_CHECKER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.bindDateChangedReceiver();
            mGrayThemeHelper.applyOrRemoveGrayTheme();
        }
    }

    private void initCommonView() {
        if (mTitleTV != null) {
            if (mBuilder.titleColorSet) {
                mTitleTV.setTextColor(mBuilder.titleColor);
            }
            mTitleTV.setText(mBuilder.title);
        }
        setButton(mPositiveBtn, mBuilder.positive);
        setButton(mNegativeBtn, mBuilder.negative);
        setButton(mNeutralBtn, mBuilder.neutral);
    }

    private void setButton(Button button, CharSequence name) {
        if (button != null) {
            if (TextUtils.isEmpty(name)) {
                button.setVisibility(View.GONE);
            } else {
                button.setVisibility(View.VISIBLE);
                button.setText(name);
            }
        }
    }

    protected abstract VB getViewBinding();

    protected void initViewListener() {

    }

    protected abstract void afterView(Context context, Builder builder);


    public void onNeutral() {
        if (mBuilder.autoDismiss) {
            dismiss();
        }
        if (mBuilder.onNeutralListener != null) {
            mBuilder.onNeutralListener.onClick(this, NEUTRAL);
        }
    }


    public void onNegative() {
        if (mBuilder.autoDismiss) {
            dismiss();
        }
        if (mBuilder.onNegativeListener != null) {
            mBuilder.onNegativeListener.onClick(this, NEGATIVE);
        }
    }


    public void onPositive() {
        if (mBuilder.autoDismiss) {
            dismiss();
        }
        if (mBuilder.onPositiveListener != null) {
            mBuilder.onPositiveListener.onClick(this, POSITIVE);
        }
    }

    protected final void applyOrRemoveGrayTheme() {
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.applyOrRemoveGrayTheme();
        }
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    /**
     * 插入到观察者集合
     *
     * @param disposable
     */
    public void addDisposable(Disposable disposable) {
        if (disposable != null) {
            if (mCompositeDisposable != null) {
                mCompositeDisposable.add(disposable);
            } else {
                disposable.dispose();
            }
        }
    }

    public void removeDisposable(Disposable disposable) {
        if (disposable != null) {
            if (mCompositeDisposable != null) {
                mCompositeDisposable.remove(disposable);
            } else if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    /**
     * 获取观察者集合
     *
     * @return
     */
    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGrayThemeHelper != null) {
            mGrayThemeHelper.unbindDateChangedReceiver();
        }
    }

    public interface OnButtonClickListener {

        void onClick(@NonNull BaseDialog<?, ?> dialog, @DialogAction int which);

    }
}
