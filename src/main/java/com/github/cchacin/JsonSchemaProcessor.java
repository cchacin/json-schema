package com.github.cchacin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;

@AutoService(Processor.class)
public class JsonSchemaProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    final VelocityContext vc = new VelocityContext();

    final Template vt;

    final VelocityEngine ve;

    final List<String> fields = newLinkedList();

    protected JsonSchemaProcessor() {
        super();
        final Properties props = new Properties();
        final URL url = getClass().getClassLoader().getResource("velocity.properties");
        try {
            props.load(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ve = new VelocityEngine(props);
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
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();

    }

    private void print(final JsonNode node) throws IOException {
        final Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();

        while (fieldsIterator.hasNext()) {
            final Map.Entry<String, JsonNode> field = fieldsIterator.next();
            final String key = field.getKey();
            System.out.println("Key: " + key);
            final JsonNode value = field.getValue();
            if (value.isContainerNode()) {
                print(value); // RECURSIVE CALL
            } else {
                fields.add(field.getKey());
                System.out.println("Type: " + field.getValue().getClass().getSimpleName());
                System.out.println("Value: " + value);
                System.out.println(vc.get("fields"));
            }
            vc.put("fields", fields);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        String fqClassName;
        String className;
        String packageName;

        for (final Element element : roundEnv.getElementsAnnotatedWith(JsonSchema.class)) {
            final TypeElement classElement = (TypeElement) element;
            final PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "annotated class: " + classElement.getQualifiedName(), element);

            fqClassName = classElement.getQualifiedName().toString();
            className = classElement.getSimpleName().toString();
            packageName = packageElement.getQualifiedName().toString();

            System.out.println(element);
            final JsonSchema jsonSchema = element.getAnnotation(JsonSchema.class);
            try {
                final JsonNode node = new ObjectMapper().readTree(Thread.currentThread().getContextClassLoader().getResourceAsStream(jsonSchema.path()));
                print(node);


                vc.put("className", className);
                vc.put("packageName", packageName);

                final JavaFileObject jfo = filer.createSourceFile(
                        fqClassName + "JsonSchema");

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "creating source file: " + jfo.toUri());

                final Writer writer = jfo.openWriter();

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "applying velocity template: " + vt.getName());

                vt.merge(vc, writer);

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//        final MethodSpec main = MethodSpec.methodBuilder("main")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(void.class)
//                .addParameter(String[].class, "args")
//                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
//                .build();
//
//        final TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .addMethod(main)
//                .build();
//
//        final JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
//                .build();
//
//        try {
//            javaFile.writeTo(new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()));
//            javaFile.writeTo(new File(helloWorld.name));
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            final PrintWriter out;
//            out = new PrintWriter(filer.createSourceFile(helloWorld.name).openWriter());
//            out.append(javaFile.toString());
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        System.out.println(roundEnv.toString());
        return true;
    }
}
