package com.afollestad.aesthetic;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

import static com.afollestad.aesthetic.Rx.onErrorLogAndRethrow;

/**
 * @author Aidan Follestad (afollestad)
 * Modified for ToolbarTitleTextView by r4sh33d
 */
public class ToolbarTitleTextView extends android.support.v7.widget.AppCompatTextView {

    private BgIconColorState lastState;
    private Disposable subscription;
    private PublishSubject<Integer> onColorUpdated;

    public ToolbarTitleTextView(Context context) {
        super(context);
    }

    public ToolbarTitleTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolbarTitleTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void invalidateColors(BgIconColorState state) {
        lastState = state;
        setBackgroundColor(state.bgColor());
        final ActiveInactiveColors iconTitleColors = state.iconTitleColor();
        if (iconTitleColors != null) {
            setTextColor(iconTitleColors.activeColor());
        }

        onColorUpdated.onNext(state.bgColor());
    }

    public Observable<Integer> colorUpdated() {
        return onColorUpdated;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        onColorUpdated = PublishSubject.create();

        // Need to invalidate the colors as early as possible. When subscribing to the continuous observable
        // below (subscription = ...), we're using distinctToMainThread(), which introduces a slight delay. During
        // this delay, we see the original colors, which are then swapped once the emission is consumed.
        // So, we'll just do a take(1), and since we're calling from the main thread, we don't need to worry
        // about distinctToMainThread() for this call. This prevents the 'flickering' of colors.

        Observable.combineLatest(
                Aesthetic.get().colorPrimary(),
                Aesthetic.get().colorIconTitle(null),
                BgIconColorState.creator())
                .take(1)
                .subscribe(new Consumer<BgIconColorState>() {
                    @Override
                    public void accept(BgIconColorState bgIconColorState) throws Exception {
                        invalidateColors(bgIconColorState);
                    }
                });


        subscription =
                Observable.combineLatest(
                        Aesthetic.get().colorPrimary(),
                        Aesthetic.get().colorIconTitle(null),
                        BgIconColorState.creator())
                        .compose(Rx.<BgIconColorState>distinctToMainThread())
                        .subscribe(
                                new Consumer<BgIconColorState>() {
                                    @Override
                                    public void accept(@NonNull BgIconColorState bgIconColorState) {
                                        invalidateColors(bgIconColorState);
                                    }
                                },
                                onErrorLogAndRethrow());
    }

    @Override
    protected void onDetachedFromWindow() {
        lastState = null;
        onColorUpdated = null;
        subscription.dispose();
        super.onDetachedFromWindow();
    }
}
