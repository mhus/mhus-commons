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

import de.mhus.commons.errors.TimeoutException;
import de.mhus.commons.errors.TimeoutRuntimeException;
import de.mhus.commons.errors.UsageException;
import de.mhus.commons.tools.MLang;
import de.mhus.commons.tools.MThread;
import de.mhus.commons.util.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
        }).onFailure(e -> exception.setValue(e));

        assertThat(exception.get()).isInstanceOf(UsageException.class);

    }

    @Test
    public void testSynchronizedTryThis() {
        assertThat(MLang.synchronizeAndTry(() -> 1, this).get()).isEqualTo(1);
        assertThat(MLang.synchronizeAndTry(() -> "a", this).get()).isEqualTo("a");
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).get()).isNull();
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).getException()).isInstanceOf(UsageException.class);
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).or("b")).isEqualTo("b");
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).orGet(() -> "c")).isEqualTo("c");
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).orTry(() -> "d").get()).isEqualTo("d");
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).orTry(() -> {
            throw new UsageException();
        }).get()).isNull();
        assertThat(MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).orTry(() -> {
            throw new UsageException();
        }).or("e")).isEqualTo("e");

        final Value<Exception> exception = new Value<>();
        MLang.synchronizeAndTry(() -> {
            throw new UsageException();
        }, this).onFailure(e -> exception.setValue(e));

        assertThat(exception.get()).isInstanceOf(UsageException.class);
    }

    @Test
    public void testSynchronize() {
        final var a = "a";
        final var b = "b";
        final var c = "c";

        // test for Dining philosophers problem
        List<Thread> threads = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            var thread = new Thread(() -> {
                for (int j = 0; j < 10000; j++)
                    switch (j % 3) {
                    case 0:
                        MLang.synchronize(() -> "r", a, b);
                        break;
                    case 1:
                        MLang.synchronize(() -> "r", b, c);
                        break;
                    case 2:
                        MLang.synchronize(() -> "r", c, a);
                        break;
                    }
            });
            threads.add(thread);
            thread.start();
        }
        var start = System.currentTimeMillis();
        while (true) {
            if (threads.stream().noneMatch(Thread::isAlive))
                break;
            MThread.sleep(100);
            if (System.currentTimeMillis() - start > 10000)
                break;
        }
        assertThat(threads.stream().noneMatch(Thread::isAlive)).isTrue();

    }

    @Test
    public void testAwait() {
        assertThat(MLang.await(() -> 1, 100)).isEqualTo(1);
        assertThat(MLang.await(() -> 1, 100, 10)).isEqualTo(1);
        assertThatThrownBy(() -> MLang.await(() -> null, 100)).isInstanceOf(TimeoutRuntimeException.class);
    }
}
