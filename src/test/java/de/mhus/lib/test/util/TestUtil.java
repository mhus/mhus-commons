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
package de.mhus.lib.test.util;

import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;
import java.util.Optional;

public class TestUtil {

    public static void start(TestInfo testInfo) {
        if (testInfo == null) {
            System.out.println(">>> unknown");
            return;
        }
        Optional<Class<?>> clazz = testInfo.getTestClass();
        Optional<Method> method = testInfo.getTestMethod();
        System.out.println(
                ">>> "
                        + (clazz == null || clazz.isEmpty() ? "?" : clazz.get().getCanonicalName())
                        + "::"
                        + (method == null || method.isEmpty() ? "?" : method.get().getName()));
    }

    public static void stop(TestInfo testInfo) {
        if (testInfo == null) {
            System.out.println("<<< unknown");
            return;
        }
        Optional<Class<?>> clazz = testInfo.getTestClass();
        Optional<Method> method = testInfo.getTestMethod();
        System.out.println(
                "<<< "
                        + (clazz == null || clazz.isEmpty() ? "?" : clazz.get().getCanonicalName())
                        + "::"
                        + (method == null || method.isEmpty() ? "?" : method.get().getName()));
    }

}