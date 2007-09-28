package com.google.code.test;

import com.google.code.exceptionhandler.HandledRuntimeException;
import com.google.code.exceptionhandler.HandledThreadFactory;
import com.google.code.exceptionhandler.ExceptionHandler;

public class TestAsync {
    public static void main(String... args) throws Exception {
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
                Thread dbThread = HandledThreadFactory.defaultThreadFactory().newThread( dbLayer );
                dbThread.start();
            }
        };
        
        Thread middleThread = new Thread( middleLayer );
        middleThread.start();
        
        System.out.println(
            ExceptionHandler.getExceptions( ExceptionHandler.channel( "DBLayer" ) ).toString()
        );
    }
}
