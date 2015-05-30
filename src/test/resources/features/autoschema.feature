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
      public OtherObjectDTO otherObject;

      public final class OtherObjectDTO {
        public Integer a;
        public String b;
      }
    }
    """

  Scenario: Array root object
    Given I have the following json in "simple.json":
    """
    [
      {
        "id": 1,
        "name": "name"
      }
    ]
    """
    When I run the annotation processor
    Then should compiles without errors
    And the result classes in "test.SampleJsonSchema" should be:
    """
    package test;

    public final class SampleJsonSchema {
      public java.util.Collection<Wrapper> wrapper;
      public final class Wrapper {
        public Integer id;
        public String name;
      }
    }
    """

  Scenario: Array object
    Given I have the following json in "simple.json":
    """
    {
      "id": "123",
      "books": [
        {
          "id": 1,
          "name": "name"
        }
      ]
    }
    """
    When I run the annotation processor
    Then should compiles without errors
    And the result classes in "test.SampleJsonSchema" should be:
    """
    package test;

    public final class SampleJsonSchema {
      public String id;
      public java.util.Collection<BooksDTO> booksDTO;

      public final class BooksDTO {
        public Integer id;
        public String name;
      }
    }
    """
