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
