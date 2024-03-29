package de.mhus.commons.tools;

import de.mhus.commons.lang.Consumer0;
import de.mhus.commons.lang.Consumer1;

import java.util.function.Supplier;

public class MLang {

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
    public static <T> TryResult<T> tryThis(Supplier<T> supplier) {
        try {
            T res = supplier.get();
            return new TryResult<T>(res);
        } catch (Exception e) {
            return new TryResult<T>(e);
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

        public boolean isError() {
            return exception != null;
        }

        public T or(T def) {
            if (exception != null)
                return def;
            return result;
        }

        public TryResult<T> onError(Consumer1<Exception> action) {
            if (exception != null) {
                try {
                    action.accept(exception);
                } catch (Exception e) {
                    // ignore
                }
            }
            return this;
        }

        public T orGet(Supplier<T> def) {
            if (exception != null)
                return def.get();
            return result;
        }

        public TryResult<T> orTry(Supplier<T> def) {
            if (exception != null)
                return tryThis(def);
            return this;
        }

        public TryResult<T> orThrow() {
            if (exception != null)
                throw new RuntimeException(exception);
            return this;
        }

        public <E extends Exception> TryResult<T> orThrow(Class<E> exceptionClass) throws E {
            if (exception != null && exceptionClass.isInstance(exception))
                throw (E) exception;
            return this;
        }

    }

}
