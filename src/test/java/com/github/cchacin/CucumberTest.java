package com.github.cchacin;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "classpath:features"
        },
        glue = {
                "com.github.cchacin"
        },
        format = {
                "pretty"
        }
)
public class CucumberTest {
}
