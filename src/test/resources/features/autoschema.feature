Feature: Annotation Processor to generate Java POJO's from Json file example
  
  Scenario: Simple
    Given I have the following json in "simple.json":
    """
    {
      "id": 1,
      "name": "name"
    }
    """
    And I have the following class "test.Sample":
    """
    package test;
    
    import com.github.cchacin.JsonSchema;
    
    @JsonSchema(path = "simple.json")
    public interface Sample {
    }
    """
    When I run the annotation processor
    Then should compiles without errors
    And the result classes in "test.SampleJsonSchema" should be:
    """
    package test;

    public final class SampleJsonSchema {
        public Integer id;
        public String name;
    }
    """
