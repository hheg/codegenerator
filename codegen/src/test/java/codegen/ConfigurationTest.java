/*
 * Copyright 2017 Henrik Hegardt
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
*/
package codegen;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codegen.Configuration.ClazzContainer;

public class ConfigurationTest {

	@Test
	public void outputJsonConfigFile() throws JsonGenerationException, JsonMappingException, IOException{
		Configuration cfg = new Configuration();
		ClazzContainer clazz = new ClazzContainer();
		clazz.getClassAnnotations().add("@ClassAnnotation");
		clazz.putFieldAnnotations("field", Arrays.asList("@FieldAnnotation"));
		clazz.putMethodAnnotations("method()", Arrays.asList("@MethodAnnotation"));
		cfg.setClass("TestClass",clazz);
		ObjectMapper mapper = new ObjectMapper();
		StringWriter sw = new StringWriter();
		mapper.writeValue(sw, cfg);
		System.out.println(sw);
	}
	
	@Test
	public void testReadJsonConfigFile() throws IOException {
		Configuration cfg = Configuration.parse(getFile("src/test/resources/codegen/cfg.json"));
		ClazzContainer clazz = cfg.getClass("codegen.TestClass");
		assertTrue(clazz != null);
		assertTrue(!clazz.getFieldAnnotations("field").isEmpty());
		assertTrue(!clazz.getClassAnnotations().isEmpty());
		assertTrue(!clazz.getMethodAnnotations("method()").isEmpty());
	}

	private File getFile(String fileName) {
		return FileUtils.getFile(fileName);
	}

}
