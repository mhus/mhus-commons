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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.TestInfo;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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