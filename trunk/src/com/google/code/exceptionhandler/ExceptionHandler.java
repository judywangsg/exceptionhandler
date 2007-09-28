package com.google.code.exceptionhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static ConcurrentMap<Channel, List<Throwable>> exceptions = new ConcurrentHashMap<Channel, List<Throwable>>();
    private final static ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private final static ConcurrentMap<Channel, CountDownLatch> waiters = new ConcurrentHashMap<Channel, CountDownLatch>();
    
    public static Channel channel(String name) {
        Channel c = channels.get(name);
        if (c == null) {
            c = new Channel(name);
            channels.putIfAbsent( name, c );
            waiters.putIfAbsent( c, new CountDownLatch(2) );
        }
        return c;
    }
    
    public static Collection<Channel> listChannels() {
        return channels.values();
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof HandledRuntimeException && e.getCause() != null) {
            Channel c = ((HandledRuntimeException)e).getChannel();
            Throwable cause = e.getCause();
            List<Throwable> list = exceptions.get( t );
            
            if ( list == null ) {
                list = new ArrayList<Throwable>();
                list.add( cause );
                list = exceptions.putIfAbsent( c, list );
                if (list != null) list.add( cause );
                
            } else {
                list.add( cause );
            }
            
            attach(t, c); //attach the count down for this channel to the thread
        } else {
            if (Thread.getDefaultUncaughtExceptionHandler() != null)
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException( t, e );
            else throw new RuntimeException(e);
        }
    }
    
    public static List<? extends Throwable> getExceptions(Channel target) throws InterruptedException {
        CountDownLatch latch = waiters.get( target );
        if (latch != null) latch.await();
        
        List<Throwable> list = exceptions.remove( target );
        waiters.remove(  target );
        
        if (list == null) return new ArrayList<Throwable>(0);
        return list;
    }
    
    private void attach(final Thread t, final Channel c) {
        final CountDownLatch latch = waiters.get( c );
        if (latch.getCount() == 2) { //the fresh state from registering a channel
            Thread waitThread = new Thread() {
                public void run() {
                    try {
                        if (t.isAlive()) t.join();
                    } catch (InterruptedException ie) {
                        //don't try to rejoin, just move on
                    } finally {
                        latch.countDown(); //second count down - everything is done
                    }
                }
            };
            
            latch.countDown(); //we are waiting for thread death
            waitThread.start();
        }
    }
       
    public static final class Channel {
        private final String name;
        protected Channel(String name) {
            this.name = name;
        }
        
        public boolean equals(Object o) {
            if (o instanceof Channel) {
                Channel c = (Channel)o;
                return name.equals(c.name);
            }
            return false;
        }
        
        public int hashCode() {
            return name.hashCode();
        }
        
        public String toString() {
            return "Channel[" + name + "]";
        }
    }
}
