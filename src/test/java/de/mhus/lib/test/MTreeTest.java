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

import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MFile;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MSystem;
import de.mhus.commons.tools.MXml;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.NotFoundException;
import de.mhus.commons.tree.*;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MTreeTest extends TestCase {

    @Test
    public void testPropertiesParser() {
        {
            IProperties p = IProperties.toProperties("a=b");
            assertThat(p.getString("a").get()).isEqualTo("b");
        }
        {
            IProperties p = IProperties.toProperties("a=b c=d");
            assertThat(p.getString("a").get()).isEqualTo("b");
            assertThat(p.getString("c").get()).isEqualTo("d");
        }
        {
            IProperties p = IProperties.toProperties(" a = b c = d ");
            assertThat(p.getString("a").get()).isEqualTo("");
            assertThat(p.getString("b c").get()).isEqualTo("");
        }
        {
            IProperties p = IProperties.toProperties(" a =b c =d ");
            assertThat(p.getString("a").get()).isEqualTo("b");
            assertThat(p.getString("c").get()).isEqualTo("d");
        }
        {
            IProperties p = IProperties.toProperties(" a = \"b\" c =\"d\" ");
            assertThat(p.getString("a").get()).isEqualTo("b");
            assertThat(p.getString("c").get()).isEqualTo("d");
        }
        {
            IProperties p = IProperties.toProperties(" a =\" b \" c =\" d \" ");
            assertThat(p.getString("a").get()).isEqualTo(" b ");
            assertThat(p.getString("c").get()).isEqualTo(" d ");
        }
        {
            IProperties p = IProperties.toProperties(" a =\" b\\\" \" c =\" d\\\\ \" ");
            assertThat(p.getString("a").get()).isEqualTo(" b\" ");
            assertThat(p.getString("c").get()).isEqualTo(" d\\ ");
        }
        {
            IProperties p = IProperties.toProperties(" a =\"\\u2202\" c =\"d\" ");
            assertThat(p.getString("a").get()).isEqualTo("\u2202");
            assertThat(p.getString("c").get()).isEqualTo("d");
        }
        {
            IProperties p = IProperties.toProperties(" a =\\u2202 c =\"d'\" ");
            assertThat(p.getString("a").get()).isEqualTo("\u2202");
            assertThat(p.getString("c").get()).isEqualTo("d'");
        }
        {
            IProperties p = IProperties.toProperties(" a = 'b' c =' d ' ");
            assertThat(p.getString("a").get()).isEqualTo("b");
            assertThat(p.getString("c").get()).isEqualTo(" d ");
        }
        {
            IProperties p = IProperties.toProperties(" a = 'b\\'' c =' d \\' \"' ");
            assertThat(p.getString("a").get()).isEqualTo("b'");
            assertThat(p.getString("c").get()).isEqualTo(" d ' \"");
        }
        {
            IProperties p = IProperties.toProperties(" a = 'b'\n\rc ='d' ");
            assertThat(p.getString("a").get()).isEqualTo("b");
            assertThat(p.getString("c").get()).isEqualTo("d");
        }
        {
            IProperties p = IProperties.toProperties(" a = 'b'\tc ='d' ");
            assertThat(p.getString("a").get()).isEqualTo("b");
            assertThat(p.getString("c").get()).isEqualTo("d");
        }

    }

    @Test
    public void testOptionals() {
        {
            IProperties p = new MProperties();
            assertThat(p.getString("test1").orElse("oh")).isEqualTo("oh");
            assertThat(p.getString("test1").orElse(null)).isNull();

            assertThat(p.getInt("test1").orElse(1)).isEqualTo(1);
            assertThat(p.getInt("test1").orElse(1)).isEqualTo(1);

            assertThat(p.getDouble("test1").orElse(1.0)).isEqualTo(1.0);
            assertThat(p.getDouble("test1").orElse(1.0)).isEqualTo(1.0);

            assertThat(p.getLong("test1").orElse(1)).isEqualTo(1);
            assertThat(p.getLong("test1").orElse(1)).isEqualTo(1);
        }
        {
            IReadonly p = new MProperties();
            assertThat(p.getString("test1").orElse("oh")).isEqualTo("oh");
            assertThat(p.getString("test1").orElse(null)).isNull();

            assertThat(p.getInt("test1").orElse(1)).isEqualTo(1);
            assertThat(p.getInt("test1").orElse(1)).isEqualTo(1);

            assertThat(p.getDouble("test1").orElse(1.0)).isEqualTo(1.0);
            assertThat(p.getDouble("test1").orElse(1.0)).isEqualTo(1.0);

            assertThat(p.getLong("test1").orElse(1)).isEqualTo(1);
            assertThat(p.getLong("test1").orElse(1)).isEqualTo(1);
        }
    }

    @Test
    public void testPropertiesWithUTF8() throws IOException, NotFoundException {
        InputStream is = MSystem.locateResource(this, "utf8.properties").openStream();

        MProperties props = MProperties.load(is);
        System.out.println(props);
        assertTrue(props.containsKey("utf8key_u_\u2022"));
        assertTrue(props.containsKey("test1\u00b0"));
        // assertTrue(props.containsKey("utf8key•"));
        assertEquals("360\u2022", props.getString("utf8key_u_\u2022").get());
        assertEquals("test\u00b0", props.getString("test1\u00b0").get());
        // assertEquals("360\u2022", props.getString("utf8key\u2022"));
    }

    @Test
    public void testSerializable() throws MException, IOException, ClassNotFoundException {

        String serialized = null;

        {
            ITreeNode c = new TreeNode();
            c.setString("test1", "wow");
            c.setString("test2", "alf");

            ITreeNode c1 = new TreeNode();
            c1.setString("test1", "wow");
            c1.setString("test2", "alf");

            ITreeNode c2 = new TreeNode();
            c2.setString("test1", "wow");
            c2.setString("test2", "alf");

            ITreeNode c3 = new TreeNode();
            c3.setString("test1", "wow");
            c3.setString("test2", "alf");

            c.setObject("c1", c1);
            TreeNodeList array = c.createArray("array");
            array.add(c2);
            array.add(c3);

            validateTree(c, false);
            validateTree(c1, false);
            validateTree(c2, false);
            validateTree(c3, false);

            serialized = MCast.serializeToString(c);
        }

        {
            ITreeNode c = (ITreeNode) MCast.unserializeFromString(serialized, null);
            validateTree(c, false);

            ITreeNode c1 = c.getObject("c1").get();
            validateTree(c1, false);

            TreeNodeList array = c.getArray("array").get();
            ITreeNode c2 = array.get(0);
            validateTree(c2, false);
            ITreeNode c3 = array.get(0);
            validateTree(c3, false);
        }
    }

    @Test
    public void testProperties() throws MException {
        {
            ITreeNode c = new TreeNode();
            c.setString("test1", "wow");
            c.setString("test2", "alf");

            validateTree(c, false);
        }
        {
            ITreeNode c = new TreeNode();
            c.setString("test1", "wow");
            c.setString("test2", "alf");

            // save
            File file = new File("target/config.properties");
            DefaultNodeFactory dcf = new DefaultNodeFactory();
            System.out.println("C1: " + c);
            dcf.write(c, file);

            System.out.println("---");
            System.out.println(MFile.readFile(file));
            System.out.println("---");
            // read
            ITreeNode c2 = dcf.read(file);
            System.out.println("C2: " + c2);
            validateTree(c2, false);
        }

    }

    @Test
    public void testXml() throws Exception {

        String xml = "<start test1='wow' test2='alf'><sub test1='wow1' test2='alf1'/><sub test1='wow2' test2='alf2'/><sub test1='wow3' test2='alf3'/></start>";
        {
            Document doc = MXml.loadXml(xml);

            ITreeNode c = MTree.readFromXmlString(doc.getDocumentElement());

            validateTree(c, true);
        }
        {
            Document doc = MXml.loadXml(xml);
            ITreeNode c = MTree.readFromXmlString(doc.getDocumentElement());

            // save
            File file = new File("target/config.xml");
            DefaultNodeFactory dcf = new DefaultNodeFactory();
            System.out.println("C1: " + c);
            dcf.write(c, file);

            // read
            ITreeNode c2 = dcf.read(file);
            System.out.println("C2: " + c2);
            validateTree(c2, true);
        }
    }

    @Test
    public void testYaml() throws Exception {
        String yaml = "test1: wow\n" + "test2: alf\n" + "sub:\n" + "- test1: wow1\n" + "  test2: alf1\n"
                + "- test1: wow2\n" + "  test2: alf2\n" + "- test1: wow3\n" + "  test2: alf3\n" + "projects:\n"
                + "- properties:\n" + "    name: name\n" + "    url: http://test.de";

        {
            ITreeNode c = MTree.readFromYamlString(yaml);
            validateTree(c, true);
        }
        {
            ITreeNode c = MTree.readFromYamlString(yaml);
            File file = new File("target/config.yaml");
            DefaultNodeFactory dcf = new DefaultNodeFactory();
            System.out.println("C1: " + c);
            dcf.write(c, file);

            // read
            ITreeNode c2 = dcf.read(file);
            System.out.println("C2: " + c2);
            validateTree(c2, true);

            // check file
            String content = MFile.readFile(file);
            System.out.println(content);
            assertFalse(content.contains("{"));
        }
    }

    @Test
    public void testJsonStream() throws Exception {

        String json = MString.replaceAll("{'test1':'wow','test2':'alf','boolon':true,'booloff':false," + "'sub': [  "
                + "{'test1':'wow1','test2':'alf1'} , " + "{'test1':'wow2','test2':'alf2'} , "
                + "{'test1':'wow3','test2':'alf3'}  " + "] }", "'", "\"");
        {
            ITreeNode c = MTree.readFromJsonString(json);
            validateTree(c, true);
            assertTrue(c.getBoolean("boolon").getOrFalse());
            assertFalse(c.getBoolean("booloff").getOrTrue());
        }
        {
            ITreeNode c = MTree.readFromJsonString(json);
            File file = new File("target/config.json");
            DefaultNodeFactory dcf = new DefaultNodeFactory();
            System.out.println("C1: " + c);
            dcf.write(c, file);
            System.out.println("File: " + MFile.readFile(file));
            // read
            ITreeNode c2 = dcf.read(file);
            System.out.println("C2: " + c2);
            validateTree(c2, true);
            assertTrue(c.getBoolean("boolon").getOrFalse());
            assertFalse(c.getBoolean("booloff").getOrTrue());
        }
    }

    @Test
    public void testHash() throws Exception {

        ITreeNode c = new TreeNode();
        c.setString("test1", "wow");
        c.setString("test2", "alf");
        TreeNodeList a = c.createArray("sub");
        ITreeNode s = a.createObject();
        s.setString("test1", "wow1");
        s.setString("test2", "alf1");
        s = a.createObject();
        s.setString("test1", "wow2");
        s.setString("test2", "alf2");
        s = a.createObject();
        s.setString("test1", "wow3");
        s.setString("test2", "alf3");

        validateTree(c, true);
    }

    // @Test
    // public void testClone() throws Exception {
    //
    // String xml =
    // "<start test1='wow' test2='alf'><sub test1='wow1' test2='alf1'/><sub
    // test1='wow2' test2='alf2'/><sub test1='wow3' test2='alf3'/></start>";
    // Document doc = MXml.loadXml(xml);
    //
    // IConfig src = IConfig.createFromXml(doc.getDocumentElement());
    //
    // IConfig tar1 = new IConfig();
    // JsonConfig tar2 = new JsonConfig();
    // XmlConfig tar3 = new XmlConfig();
    //
    // builder.cloneConfig(src, tar1);
    // builder.cloneConfig(src, tar2);
    // builder.cloneConfig(src, tar3);
    //
    // derTeschd(src, true);
    // derTeschd(tar1, true);
    // derTeschd(tar2, true);
    // derTeschd(tar3, true);
    // }

    // @Test
    private void validateTree(ITreeNode c, boolean testsub) throws MException {
        System.out.println(MSystem.findCallingMethod(3) + ": " + c);
        assertEquals("wow", c.getString("test1", "no"));
        assertEquals("alf", c.getString("test2", "no"));
        assertEquals("no", c.getString("test3", "no"));

        assertNull(c.getObject("test4").orElse(null));

        if (!testsub)
            return;

        // sub config tests

        Collection<ITreeNode> list = c.getArray("sub").get();
        assertEquals(3, list.size());

        Iterator<ITreeNode> listIter = list.iterator();
        ITreeNode sub = listIter.next();
        assertEquals("wow1", sub.getString("test1", "no"));
        assertEquals("alf1", sub.getString("test2", "no"));
        assertEquals("no", sub.getString("test3", "no"));

        sub = listIter.next();
        assertEquals("wow2", sub.getString("test1", "no"));
        assertEquals("alf2", sub.getString("test2", "no"));
        assertEquals("no", sub.getString("test3", "no"));

        sub = listIter.next();
        assertEquals("wow3", sub.getString("test1", "no"));
        assertEquals("alf3", sub.getString("test2", "no"));
        assertEquals("no", sub.getString("test3", "no"));

        // change properties

        c.setString("test1", "aloa");
        c.setString("test3", "nix");
        assertEquals("aloa", c.getString("test1", "no"));
        assertEquals("alf", c.getString("test2", "no"));
        assertEquals("nix", c.getString("test3", "no"));

        // change config

        sub = c.createObject("sub");
        sub.setString("test1", "aloa4");
        sub.setString("test2", "alf4");
        assertEquals("aloa4", sub.getString("test1", "no"));
        assertEquals("alf4", sub.getString("test2", "no"));
        assertEquals("no", sub.getString("test3", "no"));

        // assertEquals( 2, c.moveConfig(sub, WritableResourceNode.MOVE_UP) );
        // assertEquals( 3, c.moveConfig(sub, WritableResourceNode.MOVE_DOWN) );
        // assertEquals( 0, c.moveConfig(sub, WritableResourceNode.MOVE_FIRST) );
        // assertEquals( 3, c.moveConfig(sub, WritableResourceNode.MOVE_LAST) );

    }
}
