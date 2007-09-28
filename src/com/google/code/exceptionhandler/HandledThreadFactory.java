package com.google.code.exceptionhandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class HandledThreadFactory implements ThreadFactory {
    private static final ThreadFactory DEFAULT = new HandledThreadFactory();
    
    private final ThreadFactory factory;
    
    public HandledThreadFactory() {
        this.factory = Executors.defaultThreadFactory();
    }
    
    public HandledThreadFactory(ThreadFactory delegate) {
        this.factory = delegate;
    }
    
    @Override
    public Thread newThread(Runnable arg0) {
        Thread t = factory.newThread( arg0 );
        t.setUncaughtExceptionHandler( new ExceptionHandler() );
        return t;
    }
    
    public static ThreadFactory defaultThreadFactory() {
        return DEFAULT;
    }
}
