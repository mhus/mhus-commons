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

import de.mhus.commons.util.WeakList;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WeakListTest {

    @Test
    public void testWeak() {
        var list = new WeakList<Object>();
        Object a = new Object();
        Object b = new Object();
        assertThat(list.size()).isEqualTo(0);
        list.add(a);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(a);

        list.add(b);
        assertThat(list.size()).isEqualTo(2);

        a = null;
        System.gc();
        list.cleanupWeak();
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(b);
    }

    @Test
    public void testRemove() {
        var list = new WeakList<Object>();
        Object a = new Object();
        assertThat(list.size()).isEqualTo(0);
        list.add(a);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(a);

        list.remove(a);
        assertThat(list.size()).isEqualTo(0);
    }

    @Test
    public void testContains() {
        var list = new WeakList<Object>();
        Object a = new Object();
        Object b = new Object();
        assertThat(list.size()).isEqualTo(0);
        list.add(a);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(a);

        assertThat(list.contains(a)).isTrue();
        assertThat(list.contains(b)).isFalse();
    }

}
