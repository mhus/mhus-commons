/**
 * Copyright (C) 2002 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.commons.tools;

import de.mhus.commons.errors.RuntimeInterruptedException;
import de.mhus.commons.errors.TimeoutRuntimeException;
import de.mhus.commons.util.Checker;
import de.mhus.commons.util.ValueProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * @author hummel
 *         <p>
 *         To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code
 *         Generation&gt;Code and Comments
 */
@Slf4j
public class MThread {

    /**
     * Sleeps _millisec milliseconds. On Interruption it will throw an RuntimeInterruptedException
     *
     * @param _millisec
     */
    public static void sleep(long _millisec) {
        try {
            Thread.sleep(_millisec);
        } catch (InterruptedException e) {
            throw new RuntimeInterruptedException(e);
        }
    }

    /**
     * Sleeps _millisec milliseconds. On Interruption it will throw an InterruptedException. If thread is already
     * interrupted, it will throw the exception directly.
     *
     * <p>
     * This can be used in loops if a interrupt should be able to stop the loop.
     *
     * @param _millisec
     *
     * @throws InterruptedException
     *             on interrupt
     */
    public static void sleepInLoop(long _millisec) throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        Thread.sleep(_millisec);
    }

    /**
     * Sleeps _millisec milliseconds. On Interruption it will print a debug stack trace but not break. It will leave the
     * Thread.interrupted state to false see
     * https://docs.oracle.com/javase/tutorial/essential/concurrency/interrupt.html
     *
     * @param _millisec
     *
     * @return true if the thread was interrupted in the sleep time
     */
    public static boolean sleepForSure(long _millisec) {
        boolean interrupted = false;
        while (true) {
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(_millisec);
                return interrupted;
            } catch (InterruptedException e) {
                interrupted = true;
                try {
                    Thread.sleep(1); // clear interrupted state
                } catch (InterruptedException e1) {
                }
                LOGGER.debug("Error", e);
                long done = System.currentTimeMillis() - start;
                _millisec = _millisec - done;
                if (_millisec <= 0)
                    return interrupted;
            }
        }
    }

    /**
     * Try every 200ms to get the value. If the provider throws an error or return null the try will be repeated. If the
     * time out is reached a TimeoutRuntimeException will be thrown.
     *
     * @param provider
     * @param timeout
     * @param nullAllowed
     *
     * @return The requested value
     */
    public static <T> T getWithTimeout(final ValueProvider<T> provider, long timeout, boolean nullAllowed) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                T val = provider.getValue();
                if (nullAllowed || val != null)
                    return val;
            } catch (Throwable t) {
            }
            if (System.currentTimeMillis() - start > timeout)
                throw new TimeoutRuntimeException();
            sleep(200);
        }
    }

    /**
     * Wait for the checker to return true or throw an TimeoutRuntimeException on timeout. A exception in the checker
     * will be ignored.
     *
     * @param checker
     * @param timeout
     */
    public static void waitFor(final Checker checker, long timeout) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                if (checker.check())
                    return;
            } catch (Throwable t) {
            }
            if (System.currentTimeMillis() - start > timeout)
                throw new TimeoutRuntimeException();
            sleep(200);
        }
    }

    /**
     * Wait for the checker to return true or throw an TimeoutRuntimeException on timeout.
     *
     * @param checker
     * @param timeout
     *
     * @throws Exception
     *             Thrown if checker throws an exception
     */
    public static void waitForWithException(final Checker checker, long timeout) throws Exception {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                if (checker.check())
                    return;
            } catch (Throwable t) {
                throw t;
            }
            if (System.currentTimeMillis() - start > timeout)
                throw new TimeoutRuntimeException();
            sleep(200);
        }
    }

    /**
     * Check if the thread was interrupted an throws the InterruptedException exception.
     *
     * @throws InterruptedException
     *             Throw if the thread was interrupted in the meantime.
     */
    public static void checkInterruptedException() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    }

    public static void run(Runnable task) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }).start();
    }

    public static void run(Consumer<Thread> consumer) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    consumer.accept(Thread.currentThread());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }).start();
    }

    public static void cleanup() {

    }
}
