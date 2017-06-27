A maven plugin for generating annotations on classes, methods and fields on generated files.

Example configuration:
```xml
  <plugin>
    <groupId>codegen</groupId>
    <artifactId>codegen-maven-plugin</artifactId>
    <version>${codegen.verison}</version>
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