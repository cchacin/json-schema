package com.github.cchacin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DisplayTool;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class JsonSchemaProcessor extends AbstractProcessor {

  private Filer filer;

  private final VelocityContext vc = new VelocityContext();

  private final Template vt;

  public JsonSchemaProcessor() {
    super();
    final Properties props = new Properties();
    final URL url = getClass().getClassLoader().getResource("velocity.properties");
    try {
      props.load(url != null ? url.openStream() : null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    final VelocityEngine ve = new VelocityEngine(props);
    vt = ve.getTemplate("template.vm");
    ve.init();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(JsonSchema.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    filer = processingEnv.getFiler();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    for (final Element element : roundEnv.getElementsAnnotatedWith(JsonSchema.class)) {
      final TypeElement classElement = (TypeElement) element;
      final PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
          "annotated class: " + classElement.getQualifiedName(), element);

      final String fqClassName = classElement.getQualifiedName().toString();
      final String className = classElement.getSimpleName().toString();
      final String packageName = packageElement.getQualifiedName().toString();

      final JsonSchema jsonSchema = element.getAnnotation(JsonSchema.class);

      try {
        final JsonNode node =
            new ObjectMapper().readTree(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(jsonSchema.path()));
        vc.put("display",new DisplayTool());
        vc.put("json", node);

        vc.put("className", className);
        vc.put("packageName", packageName);

        final JavaFileObject jfo = filer.createSourceFile(fqClassName + "JsonSchema");

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
            "creating source file: " + jfo.toUri());
        final Writer writer = jfo.openWriter();

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
            "applying velocity template: " + vt.getName());

        vt.merge(vc, writer);

        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return false;
  }
}
