package com.google.code.exceptionhandler;

import com.google.code.exceptionhandler.ExceptionHandler.Channel;

public class HandledRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final Channel channel;

    public HandledRuntimeException(Throwable original, Channel channel) {
        super(original);
        this.channel = channel;
    }
    
    public Channel getChannel() {
        return channel;
    }
}
