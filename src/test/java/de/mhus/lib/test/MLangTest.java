package de.mhus.lib.test;

import de.mhus.commons.errors.UsageException;
import de.mhus.commons.tools.MLang;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MLangTest {

    @Test
    public void testTryThis() {
        assertThat(MLang.tryThis(() -> 1).get()).isEqualTo(1);
        assertThat(MLang.tryThis(() -> "a").get()).isEqualTo("a");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).get()).isNull();
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).getException()).isInstanceOf(UsageException.class);
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).onError("b")).isEqualTo("b");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).onErrorGet(() -> "c")).isEqualTo("c");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).onErrorTry(() -> "d").get()).isEqualTo("d");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).onErrorTry(() -> {
            throw new UsageException();
        }).get()).isNull();
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).onErrorTry(() -> {
            throw new UsageException();
        }).onError("e")).isEqualTo("e");
    }
}
