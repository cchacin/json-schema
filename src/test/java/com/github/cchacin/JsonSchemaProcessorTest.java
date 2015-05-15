package com.github.cchacin;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Arrays.asList;

public class JsonSchemaProcessorTest {

    @Test
    public void testSimple() throws Exception {
        final JavaFileObject source = forSourceLines(
                "test.Sample",
                "package fixtures;",
                "",
                "import com.github.cchacin.JsonSchema;",
                "",
                "@JsonSchema(path = \"simple.json\")",
                "public interface Sample {",
                "}"
        );
        final JavaFileObject expected = forSourceLines(
                "test.SampleJsonSchema",
                "package fixtures;",
                "",
                "public final class SampleJsonSchema {",
                "    private Integer id;",
                "    private String name;",
                "}"

        );
        assert_().about(javaSources())
                .that(asList(source))
                .processedWith(new JsonSchemaProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expected);
    }
}
