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

import de.mhus.commons.tools.MSystem;
import de.mhus.commons.errors.NotFoundException;
import de.mhus.lib.test.util.StringValue;
import de.mhus.lib.test.util.Template;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MSystemTest extends TestCase {

    @Test
    public void testDarkMode() {
        System.out.println("DarkMode: " + MSystem.isDarkMode());
    }

    @Test
    public void testSetEnv() throws Exception {
        String key;
        while (true) {
            key = "KEY_" + (int) (Math.random() * 10000);
            if (!System.getenv().containsKey(key))
                break;
        }
        var value = "VALUE_" + (int) (Math.random() * 10000);
        MSystem.setEnv(key, value);
        assertThat(System.getenv(key)).isEqualTo(value);
    }

    @Test
    public void testManifest() throws NotFoundException {
        Manifest manifest = MSystem.getManifest(MSystem.class);
        assertNotNull(manifest);
        String manifestVersion = manifest.getMainAttributes().getValue("Manifest-Version");
        System.out.println(manifestVersion);
        if (manifestVersion == null)
            System.out.println("Manifest-Version not found"); // could happen if running from IDE
        else
            assertEquals("1.0", manifestVersion);
    }

    @Test
    public void testTemplateNames() {
        Class<?> testy = StringValue.class;

        assertEquals("java.lang.String", MSystem.getTemplateCanonicalName(testy, 0));
        assertEquals("java.lang.Integer", MSystem.getTemplateCanonicalName((new Template<Integer>() {
        }).getClass(), 0));

        assertNull(MSystem.getTemplateCanonicalName(testy, 1));
        assertNull(MSystem.getTemplateCanonicalName(new Template<Integer>().getClass(), 0));
        assertNull(MSystem.getTemplateCanonicalName(String.class, 0));
    }

    @Test
    public void testCanonicalClassNames() {
        {
            String name = MSystem.getCanonicalClassName(String.class);
            assertEquals("java.lang.String", name);
        }
        {
            String name = MSystem.getCanonicalClassName(Map.Entry.class);
            assertEquals("java.util.Map.Entry", name);
        }
        {
            String name = MSystem.getCanonicalClassName(new Runnable() {
                @Override
                public void run() {
                }
            }.getClass());
            assertEquals("de.mhus.lib.test.MSystemTest$2", name);
        }
    }
}
