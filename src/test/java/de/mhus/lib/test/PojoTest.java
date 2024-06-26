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

import de.mhus.commons.tools.MString;
import de.mhus.commons.pojo.AttributesStrategy;
import de.mhus.commons.pojo.DefaultFilter;
import de.mhus.commons.pojo.DefaultStrategy;
import de.mhus.commons.pojo.FunctionsStrategy;
import de.mhus.commons.pojo.PojoAction;
import de.mhus.commons.pojo.PojoAttribute;
import de.mhus.commons.pojo.PojoModel;
import de.mhus.commons.pojo.PojoParser;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PojoTest extends TestCase {

    @SuppressWarnings("unchecked")
    @Test
    public void testFunctionStrategy() throws Exception {

        PojoExample example = new PojoExample();
        PojoModel model = new PojoParser().parse(example, new FunctionsStrategy()).filter(new DefaultFilter())
                .getModel();

        System.out.println("Attributes: " + MString.join(model.getAttributeNames(), ','));
        System.out.println("Actions: " + MString.join(model.getActionNames(), ','));

        PojoAttribute<String> myString = model.getAttribute("mystring");
        assertNotNull(myString);

        myString.set(example, "aloha", false);
        assertEquals("aloha", example.getMyString());
        assertEquals("aloha", myString.get(example));

        PojoAction doClean = model.getAction("doclean");
        assertNotNull(doClean);
        doClean.doExecute(example);

        assertNull(example.getMyString());
        assertNull(myString.get(example));

        // Embedded
        PojoAttribute<String> line1 = model.getAttribute("myembedded.line1");
        assertNotNull(line1);
        line1.set(example, "cleopatra", false);
        assertEquals("cleopatra", example.getMyEmbedded().getLine1());

        // check hidden
        PojoAttribute<String> myhidden = model.getAttribute("myhidden");
        assertNull(myhidden);

        // check no action function
        PojoAction toString = model.getAction("tostring");
        assertNull(toString);

        // check read only
        PojoAttribute<String> myReadOnly = model.getAttribute("myreadonly");
        assertNotNull(myReadOnly);
        assertTrue(myReadOnly.canRead());
        assertFalse(myReadOnly.canWrite());

        PojoAttribute<Integer> myInt = model.getAttribute("myint");
        assertNotNull(myInt);
        myInt.set(example, 1, false);
        assertEquals(1, example.getMyInt());
        myInt.set(example, null, false);
        assertEquals(0, example.getMyInt());
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testFunctionUpper() {

        PojoExample example = new PojoExample();
        PojoModel model = new PojoParser().parse(example, new FunctionsStrategy(true, false, ".", false))
                .filter(new DefaultFilter()).getModel();

        System.out.println("Attributes: " + MString.join(model.getAttributeNames(), ','));
        System.out.println("Actions: " + MString.join(model.getActionNames(), ','));

        PojoAttribute<String> myString = model.getAttribute("MyString");
        assertNotNull(myString);

        PojoAction doClean = model.getAction("doClean");
        assertNotNull(doClean);

        PojoAttribute<String> line1 = model.getAttribute("MyEmbedded.Line1");
        assertNotNull(line1);

        PojoAttribute<String> myhidden = model.getAttribute("MyHidden");
        assertNull(myhidden);
    }

    @SuppressWarnings({ "unchecked", "unused" })
    @Test
    public void testAttributeStrategy() throws Exception {

        PojoExample example = new PojoExample();
        PojoModel model = new PojoParser().parse(example, new AttributesStrategy()).filter(new DefaultFilter())
                .getModel();

        System.out.println("Attributes: " + MString.join(model.getAttributeNames(), ','));
        System.out.println("Actions: " + MString.join(model.getActionNames(), ','));

        PojoAttribute<String> myString = model.getAttribute("mystring");
        assertNotNull(myString);

        myString.set(example, "aloha", false);
        assertEquals("aloha", example.getMyString());
        assertEquals("aloha", myString.get(example));

        PojoAttribute<String> myAttributeOnly = model.getAttribute("myattributeonly");
        assertNotNull(myString);

        // Embedded
        PojoAttribute<String> line1 = model.getAttribute("myembedded.line1");
        assertNotNull(line1);
        line1.set(example, "cleopatra", false);
        assertEquals("cleopatra", example.getMyEmbedded().getLine1());

        // check hidden
        PojoAttribute<String> myhidden = model.getAttribute("myhidden");
        assertNull(myhidden);

        // check read only
        PojoAttribute<String> myReadOnly = model.getAttribute("myreadonly");
        assertNotNull(myReadOnly);
        assertTrue(myReadOnly.canRead());
        assertTrue(myReadOnly.canWrite());

        PojoAttribute<Integer> myInt = model.getAttribute("myint");
        assertNotNull(myInt);
        myInt.set(example, 1, false);
        assertEquals(1, example.getMyInt());
        myInt.set(example, null, false);
        assertEquals(0, example.getMyInt());
    }

    @SuppressWarnings({ "unchecked", "unused" })
    @Test
    public void testDefaultStrategy() throws Exception {

        PojoExample example = new PojoExample();
        PojoModel model = new PojoParser().parse(example, new DefaultStrategy()).filter(new DefaultFilter()).getModel();

        System.out.println("Attributes: " + MString.join(model.getAttributeNames(), ','));
        System.out.println("Actions: " + MString.join(model.getActionNames(), ','));

        PojoAttribute<String> myString = model.getAttribute("mystring");
        assertNotNull(myString);

        myString.set(example, "aloha", false);
        assertEquals("aloha", example.getMyString());
        assertEquals("aloha", myString.get(example));

        PojoAttribute<String> myAttributeOnly = model.getAttribute("myattributeonly");
        assertNotNull(myString);

        PojoAction doClean = model.getAction("doclean");
        assertNotNull(doClean);
        doClean.doExecute(example);

        assertNull(example.getMyString());
        assertNull(myString.get(example));

        // Embedded
        PojoAttribute<String> line1 = model.getAttribute("myembedded.line1");
        assertNotNull(line1);
        line1.set(example, "cleopatra", false);
        assertEquals("cleopatra", example.getMyEmbedded().getLine1());

        // check hidden
        PojoAttribute<String> myhidden = model.getAttribute("myhidden");
        assertNull(myhidden);

        // check no action function
        PojoAction toString = model.getAction("tostring");
        assertNull(toString);

        // check read only
        PojoAttribute<String> myReadOnly = model.getAttribute("myreadonly");
        assertNotNull(myReadOnly);
        assertTrue(myReadOnly.canRead());
        assertFalse(myReadOnly.canWrite());
    }
}
