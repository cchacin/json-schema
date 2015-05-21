Feature: Annotation Processor to generate Java POJO's from Json file example
  Background:
    Given I have the following class "test.Sample":
    """
    package test;

    import com.github.cchacin.JsonSchema;

    @JsonSchema(path = "simple.json")
    public interface Sample {
    }
    """

  Scenario: Simple
    Given I have the following json in "simple.json":
    """
    {
      "id": 1,
      "name": "name"
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

  Scenario: Inner object
    Given I have the following json in "simple.json":
    """
    {
      "id": 1,
      "name": "name",
      "otherObject": {
        "a": 1,
        "b": "b"
      }
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

      public final class otherObjectDTO {
        public Integer a;
        public String b;
      }
      public otherObjectDTO otherObject;
    }
    """
