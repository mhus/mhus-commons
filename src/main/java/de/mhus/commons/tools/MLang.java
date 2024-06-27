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

import de.mhus.commons.errors.InternalRuntimeException;
import de.mhus.commons.errors.TimeoutRuntimeException;
import de.mhus.commons.lang.Consumer0;
import de.mhus.commons.lang.Consumer1;
import de.mhus.commons.lang.Function0;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

public class MLang {

    private static final Object GLOBAL_LOCK = new Object();

    /**
     * Try to execute the action and return an TryResult with the exception or void. Will not throw an exception.
     *
     * @param action
     *            The code to execute
     *
     * @return The result of execution
     */
    public static TryResult<Void> tryThis(Consumer0 action) {
        try {
            action.accept();
            return new TryResult<Void>((Exception) null);
        } catch (Exception e) {
            return new TryResult<Void>(e);
        }

    }

    /**
     * Try to execute the supplier and return the result or an exception. Will not throw an exception.
     *
     * @param supplier
     *            The code to execute
     *
     * @return The result of execution
     *
     * @param <T>
     */
    public static <T> TryResult<T> tryThis(Function0<T> supplier) {
        try {
            T res = supplier.apply();
            return new TryResult<T>(res);
        } catch (Exception e) {
            return new TryResult<T>(e);
        }
    }

    /**
     * Wait until the action returns a non-null value or the timeout is reached. If timeout is reach a
     * TimeoutRuntimeException is thrown.
     *
     * @param action
     *            The action to execute
     * @param timeout
     *            The timeout in milliseconds
     *
     * @return The result of the action
     *
     * @param <T>
     *            The result type
     */
    public static <T> T await(Supplier<T> action, long timeout) {
        return await(action, timeout, 100);
    }

    /**
     * Wait until the action returns a non-null value or the timeout is reached. If timeout is reach a
     * TimeoutRuntimeException is thrown.
     *
     * @param action
     *            The action to execute
     * @param timeout
     *            The timeout in milliseconds
     * @param delay
     *            The delay between checks
     *
     * @return The result of the action
     *
     * @param <T>
     *            The result type
     */
    public static <T> T await(Supplier<T> action, long timeout, long delay) {
        var start = System.currentTimeMillis();
        while (true) {
            try {
                var result = action.get();
                if (result != null)
                    return result;
            } catch (Exception e) {
                throw new InternalRuntimeException(e);
            }
            if (System.currentTimeMillis() - start > timeout)
                throw new TimeoutRuntimeException("Timeout");
            MThread.sleep(delay);
        }
    }

    /**
     * Wait until the action returns a true value or the timeout is reached. If timeout is reach a
     * TimeoutRuntimeException is thrown.
     *
     * @param action
     *            The action to execute
     * @param timeout
     *            The timeout in milliseconds
     */
    public static void awaitTrue(Supplier<Boolean> action, long timeout) {
        awaitTrue(action, timeout, 100);
    }

    /**
     * Wait until the action returns a true value or the timeout is reached. If timeout is reach a
     * TimeoutRuntimeException is thrown.
     *
     * @param action
     *            The action to execute
     * @param timeout
     *            The timeout in milliseconds
     * @param delay
     *            The delay between checks
     */
    public static void awaitTrue(Supplier<Boolean> action, long timeout, long delay) {
        var start = System.currentTimeMillis();
        while (true) {
            try {
                var result = action.get();
                if (result != null && result)
                    return;
            } catch (Exception e) {
                throw new InternalRuntimeException(e);
            }
            if (System.currentTimeMillis() - start > timeout)
                throw new TimeoutRuntimeException("Timeout");
            MThread.sleep(delay);
        }
    }

    /**
     * Synchronize the execution of the function with the given locks. The locks are sorted by their identity hash code
     * to avoid deadlocks. It will throw an InternalRuntimeException if an exception is thrown. If no lock is given the
     * global lock is used.
     *
     * @param function
     *            The function to execute
     * @param lock
     *            The locks to use
     *
     * @return The result of the function
     *
     * @param <T>
     *            The result type
     */
    public static <T> T synchronize(Function0<T> function, Object... lock) {
        // reorder lock
        if (lock == null || lock.length == 0)
            lock = new Object[] { GLOBAL_LOCK };
        if (lock.length > 1)
            Arrays.sort(lock, Comparator.comparingInt(System::identityHashCode));
        // lock and execute in correct order
        var locker = new LockNextAndExecute<T>(0, lock, function);
        var res = locker.execute(null);
        if (locker.error != null)
            throw new InternalRuntimeException(locker.error);
        return res;
    }

    /**
     * Synchronize the execution of the function with the given locks. The locks are sorted by their identity hash code
     * to avoid deadlocks. If no lock is given the global lock is used.
     *
     * @param function
     *            The function to execute
     * @param lock
     *            The locks to use
     *
     * @return The result of the function as TryResult
     *
     * @param <T>
     *            The result type
     */
    public static <T> TryResult<T> synchronizeAndTry(Function0<T> function, Object... lock) {
        // reorder lock
        if (lock == null || lock.length == 0)
            lock = new Object[] { GLOBAL_LOCK };
        if (lock.length > 1)
            Arrays.sort(lock, Comparator.comparingInt(System::identityHashCode));
        Arrays.sort(lock, Comparator.comparingInt(System::identityHashCode));
        // lock and execute in correct order
        var locker = new LockNextAndExecute<T>(0, lock, function);
        var res = locker.execute(null);
        if (locker.error != null)
            return new TryResult<>(locker.error);
        return new TryResult<>(res);
    }

    private static class LockNextAndExecute<T> {

        private final int index;
        private final Object[] lock;
        private final Function0<T> function;
        private Exception error;

        public LockNextAndExecute(int index, Object[] lock, Function0<T> function) {
            this.index = index;
            this.lock = lock;
            this.function = function;
        }

        public T execute(LockNextAndExecute<T> parent) {
            if (index < lock.length - 1) {
                synchronized (lock[index]) {
                    return new LockNextAndExecute<T>(index + 1, lock, function).execute(this);
                }
            } else {
                try {
                    synchronized (lock[index]) {
                        return function.apply();
                    }
                } catch (Exception e) {
                    this.error = e;
                    if (parent != null)
                        parent.error = e;
                    return null;
                }
            }
        }
    }

    public static class TryResult<T> {

        private final T result;
        private final Exception exception;

        public TryResult(T result) {
            this.result = result;
            this.exception = null;
        }

        public TryResult(Exception exception) {
            this.exception = exception;
            this.result = null;
        }

        public T get() {
            return result;
        }

        public Exception getException() {
            return exception;
        }

        public boolean isFailure() {
            return exception != null;
        }

        public T orElse(T def) {
            if (exception != null)
                return def;
            return result;
        }

        public TryResult<T> onFailure(Consumer1<Exception> action) {
            if (exception != null) {
                try {
                    action.accept(exception);
                } catch (Exception e) {
                    // ignore
                }
            }
            return this;
        }

        public TryResult<T> onSuccess(Consumer1<T> action) {
            if (exception == null) {
                try {
                    action.accept(result);
                } catch (Exception e) {
                    return new TryResult<>(e);
                }
            }
            return this;
        }

        public T orElseGet(Supplier<T> def) {
            if (exception != null)
                return def.get();
            return result;
        }

        public TryResult<T> orElseTry(Function0<T> def) {
            if (exception != null)
                return tryThis(def);
            return this;
        }

        public TryResult<T> orElseThrow() {
            if (exception != null)
                throw new RuntimeException(exception);
            return this;
        }

        public <E extends Exception> TryResult<T> orElseThrow(Class<E> exceptionClass) throws E {
            if (exception != null && exceptionClass.isInstance(exception))
                throw (E) exception;
            return this;
        }

        public <E extends Exception> T getOrThrow(Supplier<E> supplier) throws E {
            if (exception != null)
                throw supplier.get();
            return result;
        }

    }

}
