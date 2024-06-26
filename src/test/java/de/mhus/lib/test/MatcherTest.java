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

import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.SyntaxError;
import de.mhus.commons.matcher.Condition;
import de.mhus.commons.matcher.Matcher;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MatcherTest extends TestCase {

    @Test
    public void testSimple() throws MException {
        {
            Matcher filter = new Matcher(".*aaa.*");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("xyz"));
        }

        {
            Matcher filter = new Matcher("'.*aaa.*'");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("xyz"));
        }

        {
            Matcher filter = new Matcher("'.*a\\'aa.*'");
            System.out.println(filter);
            assertEquals(true, filter.matches("a'aa"));
            assertEquals(false, filter.matches("xyz"));
        }

        {
            Matcher filter = new Matcher(".*aaa.* or .*bbb.*");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(true, filter.matches("bbb"));
            assertEquals(true, filter.matches("blablabbbla"));
            assertEquals(false, filter.matches("xyz"));
        }

        {
            Matcher filter = new Matcher(".*aaa.* and .*bbb.*");
            System.out.println(filter);
            assertEquals(false, filter.matches("aaa"));
            assertEquals(false, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("bbb"));
            assertEquals(false, filter.matches("blablabbbla"));
            assertEquals(false, filter.matches("xyz"));
            assertEquals(true, filter.matches("blablaaabbbla"));
        }

        {
            Matcher filter = new Matcher(".*aaa.* and not .*bbb.*");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("bbb"));
            assertEquals(false, filter.matches("blablabbbla"));
            assertEquals(false, filter.matches("xyz"));
            assertEquals(false, filter.matches("blablaaabbbla"));
        }

        {
            Matcher filter = new Matcher("not .*aaa.* and .*bbb.*");
            System.out.println(filter);
            assertEquals(false, filter.matches("aaa"));
            assertEquals(false, filter.matches("blablaaabla"));
            assertEquals(true, filter.matches("bbb"));
            assertEquals(true, filter.matches("blablabbbla"));
            assertEquals(false, filter.matches("xyz"));
            assertEquals(false, filter.matches("blablaaabbbla"));
        }

        {
            Matcher filter = new Matcher("not .*aaa.* and not .*bbb.*");
            System.out.println(filter);
            assertEquals(false, filter.matches("aaa"));
            assertEquals(false, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("bbb"));
            assertEquals(false, filter.matches("blablabbbla"));
            assertEquals(true, filter.matches("xyz"));
            assertEquals(false, filter.matches("blablaaabbbla"));
        }
    }

    @Test
    public void testBrackets() throws MException {
        {
            Matcher filter = new Matcher("not (.*aaa.* or .*bbb.*)");
            System.out.println(filter);
            assertEquals(false, filter.matches("aaa"));
            assertEquals(false, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("bbb"));
            assertEquals(false, filter.matches("blablabbbla"));
            assertEquals(true, filter.matches("xyz"));
            assertEquals(false, filter.matches("blablaaabbbla"));
        }

        {
            Matcher filter = new Matcher("not (.*aaa.* and .*bbb.*)");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(true, filter.matches("bbb"));
            assertEquals(true, filter.matches("blablabbbla"));
            assertEquals(true, filter.matches("xyz"));
            assertEquals(false, filter.matches("blablaaabbbla"));
        }
        {
            Matcher filter = new Matcher(".*xyz.* or (.*aaa.* and .*bbb.*)");
            System.out.println(filter);
            assertEquals(false, filter.matches("aaa"));
            assertEquals(false, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("bbb"));
            assertEquals(false, filter.matches("blablabbbla"));
            assertEquals(true, filter.matches("xyz"));
            assertEquals(true, filter.matches("blablaaabbbla"));
        }

        {
            Matcher filter = new Matcher("ext_.* or exec_.* or ro_vp_msg");
            System.out.println(filter);
            assertEquals(true, filter.matches("ro_vp_msg"));
            assertEquals(false, filter.matches("int_msg"));
        }
    }

    @Test
    public void testPatterns() throws MException {
        {
            Matcher filter = new Matcher("fs *aaa*");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("xyz"));
        }
        {
            Matcher filter = new Matcher("sql %aaa%");
            System.out.println(filter);
            assertEquals(true, filter.matches("aaa"));
            assertEquals(true, filter.matches("blablaaabla"));
            assertEquals(false, filter.matches("xyz"));
        }
    }

    @Test
    public void testCondition() throws MException {
        HashMap<String, Object> val = new HashMap<String, Object>();
        val.put("param1", "aloa");
        val.put("param2", "nix");
        val.put("param3", "round cube");
        val.put("param4", "round cube");
        val.put("param5", "5.123");
        val.put("param6", "5.123");
        val.put("param7", "4.123");

        {
            Condition cond = new Condition("$param1 aloa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 aloax");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        // fs
        {
            Condition cond = new Condition("$param1 fs aloa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 fs al*");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 fs *oa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 fs *o*");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 fs *x*");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        // sql
        {
            Condition cond = new Condition("$param1 sql aloa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 sql al%");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 sql %oa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 sql %o%");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 sql %x%");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        // regex
        {
            Condition cond = new Condition("$param1 regex aloa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 regex al.*");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 regex .*oa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 regex .*o.*");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 regex .*x.*");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        // regex 2
        {
            Condition cond = new Condition("$param1 =~ aloa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 =~ al.*");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 =~ .*oa");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 =~ .*o.*");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param1 =~ .*x.*");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        // more ...
        {
            Condition cond = new Condition("($param1 bla or $param1 aloa)");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("($param1 bla or $param1 blub)");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        {
            Condition cond = new Condition("($param1 aloa and $param2 nix)");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("$param3 'round cube'");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param3} 'round cube'");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }

        {
            Condition cond = new Condition("${param3} ${param4}");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }

        {
            Condition cond = new Condition("${param3} ${param4}");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }

        {
            Condition cond = new Condition("${param3} ${param2}");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }

        {
            Condition cond = new Condition("${param5} 5");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }

        {
            Condition cond = new Condition("${param5} < 5");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param7} < 5");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param7} <= 4.123");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param7} > 3");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param7} > 5");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }

        {
            Condition cond = new Condition("${param7} <= ${param6}");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param7} >= ${param6}");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param5} == ${param6}");
            System.out.println(cond);
            assertEquals(true, cond.matches(val));
        }
        {
            Condition cond = new Condition("${param5} == 2");
            System.out.println(cond);
            assertEquals(false, cond.matches(val));
        }
        try {
            new Condition("1 == 2");
            fail();
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        }
    }
}
