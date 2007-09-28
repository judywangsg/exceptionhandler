package com.google.code.test;

import com.google.code.exceptionhandler.ExceptionHandler;
import com.google.code.exceptionhandler.HandledRuntimeException;

public class TestSingleThreaded {
    public static void main(String... args) throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler( new ExceptionHandler() );
        Thread t = new Thread() { public void run() {
            try { System.out.println(
                    ExceptionHandler.getExceptions(
                            ExceptionHandler.channel( "DBLayer" ) ).toString());
            } catch (InterruptedException ie) {}
        }};
        t.start();
        
        run();
    }
    
    public static void run() {
        //Pretend that this is my GUI thread.
        System.out.println("Hello, I'm running some subtask.");
       
        final Runnable dbLayer = new Runnable() {
            public void run() {
                System.out.println("Throwing DB exception...");
                //imagine a try/catch block here dealing with
                //some system operation you need to deal with
                //elsewhere
                throw new HandledRuntimeException(new Exception(), ExceptionHandler.channel( "DBLayer" ));
            }
        };
        
        
        Runnable middleLayer = new Runnable() {
            public void run() {
                dbLayer.run();
            }
        };
        
        middleLayer.run();
    }
}
