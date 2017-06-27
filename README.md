A maven plugin for generating annotations on classes, methods and fields on generated files.

Example configuration:
```xml
  <plugin>
    <groupId>codegen</groupId>
    <artifactId>codegen-maven-plugin</artifactId>
    <version>${codegen.version}</version>
    <executions>
       <execution>
          <id>annotate</id>
          <phase>generate-sources</phase>
          <goals>
             <goal>codegen</goal>
          </goals>
          <inherited>false</inherited>
          <configuration>
             <srcDirectory>${project.build.directory}/generated-sources/xjc</srcDirectory>
             <configFile>src/main/resources/annotations.json</configFile>
          </configuration>
       </execution>
    </executions>
  </plugin>
``` 
Example configuration file
```json
{
"classes":{
	"codegen.NestedTestClass":
		{
			"classAnnotations":["@ClassAnnotation"],
			"methodAnnotations":{"method()":["@MethodAnnotation"]},
			"fieldAnnotations":{"field":["@FieldAnnotation"]}
		},
	"codegen.NestedTestClass.InternalClass":
		{
			"classAnnotations":["@ClassAnnotation"],
			"methodAnnotations":{"method()":["@MethodAnnotation"]},
			"fieldAnnotations":{"field":["@FieldAnnotation"]}
		}
	}
}
```