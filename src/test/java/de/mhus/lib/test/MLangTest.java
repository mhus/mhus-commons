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
package de.mhus.lib.test;

import de.mhus.commons.errors.UsageException;
import de.mhus.commons.tools.MLang;
import de.mhus.commons.util.Value;
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
        }).or("b")).isEqualTo("b");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).orGet(() -> "c")).isEqualTo("c");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).orTry(() -> "d").get()).isEqualTo("d");
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).orTry(() -> {
            throw new UsageException();
        }).get()).isNull();
        assertThat(MLang.tryThis(() -> {
            throw new UsageException();
        }).orTry(() -> {
            throw new UsageException();
        }).or("e")).isEqualTo("e");

        final Value<Exception> exception = new Value<>();
        MLang.tryThis(() -> {
            throw new UsageException();
        }).onError(e -> exception.setValue(e));

        assertThat(exception.get()).isInstanceOf(UsageException.class);

    }
}
