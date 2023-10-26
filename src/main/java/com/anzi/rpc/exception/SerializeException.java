package com.anzi.rpc.exception;

/**
 * 序列化异常
 *
 * @author anzi
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String msg) {
        super(msg);
    }
}
