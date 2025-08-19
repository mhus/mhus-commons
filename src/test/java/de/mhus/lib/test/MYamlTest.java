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

import de.mhus.commons.tools.MJson;
import de.mhus.commons.yaml.MYaml;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class MYamlTest {

    @Test
    public void testJsonToYaml() throws IOException {
        var jsonStr = "{\"key\":\"value\", \"array\":[1,2,3], \"object\":{\"key\":\"value\"}}";
        var json = MJson.load(jsonStr);
        var yaml = MYaml.toYaml(json);
        var yamlStr = yaml.toString();
        System.out.println(yamlStr);
        assertThat(yamlStr).isEqualTo("key: value\narray:\n- 1\n- 2\n- 3\nobject:\n  key: value\n");
    }

}
