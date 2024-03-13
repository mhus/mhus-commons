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

import de.mhus.commons.lang.IRegistration;
import de.mhus.commons.util.MEventHandler;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit test for simple App. */
public class EventHandlerTest extends TestCase {

    @Test
    public void testRegistration() {
        MEventHandler<MyEvent> eh = new MEventHandler<MyEvent>();
        MyListener l1 = new MyListener();
        MyListener l2 = new MyListener();

        IRegistration reg1 = eh.register(l1);
        IRegistration reg2 = eh.registerWeak(l2);

        assertTrue(eh.getListenersArray().length == 2);

        reg1.unregister();
        reg2.unregister();

        assertTrue(eh.getListenersArray().length == 0);
    }

    /**
     * Test registration of normal and weak listeners. Test if weak listener will be removed after full gc().
     */
    @Test
    public void testListeners() {
        MEventHandler<MyEvent> eh = new MEventHandler<MyEvent>();
        MyListener l1 = new MyListener();
        MyListener l2 = new MyListener();

        eh.register(l1);
        eh.registerWeak(l2);

        assertTrue(eh.getListenersArray().length == 2);

        l1 = null;
        l2 = null;
        System.gc();

        assertTrue(eh.getListenersArray().length == 1);
    }

    /** Test unregister for normal and weak listeners */
    @Test
    public void testUnregister() {
        MEventHandler<MyEvent> eh = new MEventHandler<MyEvent>();
        MyListener l1 = new MyListener();
        MyListener l2 = new MyListener();

        eh.register(l1);
        eh.registerWeak(l2);

        assertTrue(eh.getListenersArray().length == 2);

        eh.unregister(l1);
        eh.unregister(l2);

        assertTrue(eh.getListenersArray().length == 0);
    }

    /** Test if weak mode is supported. */
    @Test
    public void testWeakMode() {
        MEventHandler<MyEvent> eh = new MEventHandler<MyEvent>(true);
        MyListener l1 = new MyListener();
        MyListener l2 = new MyListener();

        eh.register(l1);
        eh.registerWeak(l2);

        assertTrue(eh.getListenersArray().length == 2);

        l1 = null;
        l2 = null;
        System.gc();

        assertTrue(eh.getListenersArray().length == 0);
    }

    @Test
    public void testIterator() {
        MEventHandler<MyEvent> eh = new MEventHandler<MyEvent>(true);
        MyListener l1 = new MyListener();
        MyListener l2 = new MyListener();

        eh.register(l1);
        eh.registerWeak(l2);

        int cnt = 0;
        for (Consumer<MyEvent> cur : eh.getListeners()) {
            ((MyListener) cur).accept(null);
            cnt++;
        }

        assertTrue(cnt == 2);
    }

    @Test
    public void testFireMethod() throws SecurityException, NoSuchMethodException {

        MEventHandler<MyEvent> eh = new MEventHandler<MyEvent>(true);
        MyListener l1 = new MyListener();
        eh.register(l1);

        eh.fire();

        assertTrue(l1.done);
    }

    public record MyEvent(String id) {
    }

    public static class MyListener implements Consumer<MyEvent> {
        private boolean done = false;

        @Override
        public void accept(MyEvent myEvent) {
            done = true;
        }
    }

}
