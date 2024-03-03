package com.anzi.rpc.spi;


/**
 * 对象的获取器，保证对象对各个线程的可见性
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
