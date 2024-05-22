package com.dinghu.test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 1.0
 * @author huding
 * @Date: 2024/05/22 10:45
 * @Description:
 */
@SuppressWarnings("all")
@Component
public class MetricsCounter {

    private static Counter loginCounter = null;
    private static Counter registerCounter = null;

    private static AtomicInteger atomicInteger;

    public MetricsCounter(MeterRegistry registry) {
        loginCounter = registry.counter("login_nums");
        registerCounter = registry.counter("register_nums");
        atomicInteger = registry.gauge("ssl_expire_days", new AtomicInteger(10));
    }

    /**
     * 此方法可能会被多线程执行，需要考虑线程安全问题
     */
    public synchronized static void incrLogin() {
        loginCounter.increment();
    }

    public synchronized static void incrRegister() {
        registerCounter.increment();
    }

    public static void updateSslExpireDays(){
        atomicInteger.set(new Random().nextInt(100));
    }

}
