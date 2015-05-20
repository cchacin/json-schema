package com.github.cchacin;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"classpath:features"}, glue = {"com.github.cchacin"},
    format = {"pretty"})
public class CucumberTest {
}
