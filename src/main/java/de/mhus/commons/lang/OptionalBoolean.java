package de.mhus.commons.lang;

import de.mhus.commons.util.ObjectStream;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public final class OptionalBoolean {

    private static final OptionalBoolean EMPTY = new OptionalBoolean();


    private final boolean isPresent;
    private final boolean value;

    private OptionalBoolean() {
        this.isPresent = false;
        this.value = false;
    }

    public static OptionalBoolean empty() {
        return EMPTY;
    }

    private OptionalBoolean(boolean value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalBoolean of(boolean value) {
        return new OptionalBoolean(value);
    }

    public boolean isPresent() {
        return isPresent;
    }

    public boolean isEmpty() {
        return !isPresent;
    }

    public void ifPresentOrElse(BooleanConsumer action, Runnable emptyAction) {
        if (isPresent) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public Stream<Boolean> stream() {
        if (isPresent) {
            return Stream.of(value);
        } else {
            return Stream.empty();
        }
    }

    public boolean getOrTrue() {
        return isPresent ? value : true;
    }

    public boolean getOrFalse() {
        return isPresent ? value : false;
    }

    public boolean orElse(boolean other) {
        return isPresent ? value : other;
    }

    public Boolean orElseBoolean(Boolean other) {
        return isPresent ? value : other;
    }

    public boolean orElseGet(BooleanSupplier supplier) {
        return isPresent ? value : supplier.getAsBoolean();
    }

    public boolean orElseThrow() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public<X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof OptionalBoolean other
                && (isPresent && other.isPresent
                ? Boolean.compare(value, other.value) == 0
                : isPresent == other.isPresent);
    }

    @Override
    public int hashCode() {
        return isPresent ? Boolean.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? ("OptionalBoolean[" + value + "]")
                : "OptionalBoolean.empty";
    }

    public interface BooleanConsumer {

        void accept(boolean value);

        default BooleanConsumer andThen(BooleanConsumer after) {
            Objects.requireNonNull(after);
            return (boolean t) -> { accept(t); after.accept(t); };
        }
    }

}
