/**
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
package com.github.cchacin;

import com.google.testing.compile.CompileTester;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.CompileTester.SuccessfulCompilationClause;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Arrays.asList;

public class JsonSchemaProcessorStepDefs {

  private JavaFileObject actual;
  private CompileTester compileTester;
  private SuccessfulCompilationClause successfulCompilationClause;

  @Given("^I have the following json in \"(.*?)\":$")
  public void i_have_the_following_json_in(final String jsonFilename, final String jsonContent)
      throws Throwable {
    try (final Writer writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream("target/test-classes/"
            + jsonFilename), Charset.defaultCharset()))) {
      writer.write(jsonContent);
    }
  }

  @Given("^I have the following class \"(.*?)\":$")
  public void i_have_the_following_class(final String classname, final String classContent)
      throws Throwable {
    actual = forSourceString(classname, classContent);
  }

  @When("^I run the annotation processor$")
  public void i_run_the_annotation_processor() throws Throwable {
    compileTester =
        assert_().about(javaSources()).that(asList(actual))
            .processedWith(new JsonSchemaProcessor());
  }

  @Then("^should compiles without errors$")
  public void should_compiles_without_errors() throws Throwable {
    successfulCompilationClause = compileTester.compilesWithoutError();
  }

  @Then("^the result classes in \"(.*?)\" should be:$")
  public void the_result_classes_in_should_be(final String classname, final String classContent)
      throws Throwable {
    successfulCompilationClause.and().generatesSources(forSourceLines(classname, classContent));
  }
}
