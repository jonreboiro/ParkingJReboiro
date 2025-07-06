package com.lksnext.parkingJReboiro.util;

import android.os.CountDownTimer;
import java.util.function.LongConsumer;

public class AndroidCountDownTimer extends CountDownTimer implements ITimer {
    private final Runnable onFinish;
    private final LongConsumer onTick;

    public AndroidCountDownTimer(long millisInFuture, long countDownInterval, LongConsumer onTick, Runnable onFinish) {
        super(millisInFuture, countDownInterval);
        this.onTick = onTick;
        this.onFinish = onFinish;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        onTick.accept(millisUntilFinished);
    }

    @Override
    public void onFinish() {
        onFinish.run();
    }
}