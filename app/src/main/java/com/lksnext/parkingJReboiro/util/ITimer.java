package com.lksnext.parkingJReboiro.util;

import android.os.CountDownTimer;

public interface ITimer {
    CountDownTimer start();
    void cancel();
}
