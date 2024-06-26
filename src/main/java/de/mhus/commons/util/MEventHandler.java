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
package de.mhus.commons.util;

import de.mhus.commons.lang.IRegistration;
import de.mhus.commons.lang.IRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.function.Consumer;

@Slf4j
public class MEventHandler<E> implements IRegistry<E> {

    private HashMap<Consumer<E>, String> listeners = new HashMap<>();
    private WeakHashMap<Consumer<E>, String> weak = new WeakHashMap<>();
    private boolean weakHandler = false;

    /** Create the handler in normal mode. */
    public MEventHandler() {
        this(false);
    }

    /**
     * Create the handler and specify if all listeners have to be weak.
     *
     * @param weakHandler
     *            If true handler is in weak mode. All registered handlers are weak.
     */
    public MEventHandler(boolean weakHandler) {
        this.weakHandler = weakHandler;
    }

    /**
     * Register a listener for this kind of events. If the handler is weak the listener will registered weak. A listener
     * can only register one time. A second registration will be ignored.
     *
     * @param listener
     *            Listener for the events.
     */
    @Override
    public IRegistration register(Consumer<E> listener) {
        if (weakHandler) {
            registerWeak(listener);
            return new Registration(listener);
        }
        synchronized (listeners) {
            if (!contains(listener))
                listeners.put(listener, "");
        }
        return new Registration(listener);
    }

    /**
     * Unregister normal or weak listener from the handler.
     *
     * @param listener
     *            Listener for the events.
     */
    @Override
    public void unregister(Consumer<E> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            weak.remove(listener);
        }
    }

    /**
     * Register the listener as a weak reference.
     *
     * @param listener
     *            Listener for the events.
     */
    @Override
    public IRegistration registerWeak(Consumer<E> listener) {
        synchronized (listeners) {
            weak.put(listener, "");
        }
        return new Registration(listener);
    }

    /**
     * Returns true if the listener instance is already registered for this event handler.
     *
     * @param listener
     *            Listener for the events.
     *
     * @return true if the listener is registered as normal or weak.
     */
    public boolean contains(Consumer<E> listener) {
        synchronized (this) {
            return listeners.containsKey(listener) || weak.containsKey(listener);
        }
    }

    /**
     * Returns a array of all registered (normal or weak) listeners. The list should not be cashed - this will prevent
     * weak listeners to be removed from the memory. The list should be used to fire events to the listeners if the
     * listeners are able to change the event handler (register / unregister).
     *
     * @return All registered listeners
     */
    public Object[] getListenersArray() {
        Object[] list = null;
        synchronized (listeners) {
            Object[] list1 = weakHandler ? null : listeners.keySet().toArray();
            Object[] list2 = weak.keySet().toArray();
            if (list1 == null)
                list = list2;
            else if (list2.length == 0)
                list = list1;
            else if (list1.length == 0)
                list = list2;
            else {
                list = new Object[list1.length + list2.length];
                System.arraycopy(list1, 0, list, 0, list1.length);
                System.arraycopy(list2, 0, list, list1.length, list2.length);
            }
        }
        return list;
    }

    /**
     * Use this to iterate into the listeners and if you be sure the listeners do not try to change the event handler
     * (register/unregister). This method will save resources if only normal or only weak listeners are registered.
     *
     * @return Iterable object for all listeners.
     */
    public Iterable<Consumer<E>> getListeners() {
        if (weakHandler || listeners.size() == 0)
            return weak.keySet();
        if (weak.size() == 0)
            return listeners.keySet();
        LinkedList<Consumer<E>> ll = new LinkedList<>(listeners.keySet());
        ll.addAll(weak.keySet());
        return ll;
    }

    /**
     * Amount of registered listeners.
     *
     * @return the size
     */
    public int size() {
        return listeners.size() + weak.size();
    }

    /**
     * Return true if the handler only store weak references (also by register()).
     *
     * @return flag
     */
    public boolean isWeakHandler() {
        return weakHandler;
    }

    public void fire() {
        fire(null);
    }

    @SuppressWarnings("unchecked")
    public void fire(E event) {
        for (Object obj : getListenersArray()) {
            // Thread.startVirtualThread(() -> {
            try {
                onFire((Consumer<E>) obj, event);
            } catch (Throwable t) {
                LOGGER.debug("fire of event {} with {} failed", obj, event, t);
            }
            // });
        }
    }

    public void onFire(Consumer<E> listener, E event) {
        listener.accept((E) event);
    }

    private class Registration implements IRegistration {
        private final Consumer<E> listener;

        public Registration(Consumer<E> listener) {
            this.listener = listener;
        }

        @Override
        public void unregister() {
            MEventHandler.this.unregister(listener);
        }
    }
}
