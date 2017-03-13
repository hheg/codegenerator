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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Configuration {
	@JsonProperty
	private final Map<String, ClazzContainer> classes = new HashMap<String, ClazzContainer>();
	@JsonProperty
	private String encoding;
		
	public ClazzContainer getClass(String name) {
		return classes.get(name);
	}	

	Map<String,ClazzContainer> getClasses(){
		return Collections.unmodifiableMap(classes);
	}
	
	void setClass(String name, ClazzContainer clazz){
		classes.put(name, clazz);
	}

	public static class ClazzContainer {
		@JsonProperty
		private List<String> classAnnotations;
		@JsonProperty
		private Map<String, List<String>> methodAnnotations;
		@JsonProperty
		private Map<String, List<String>> fieldAnnotations;

		public ClazzContainer() {
			this.setClassAnnotations(new ArrayList<String>());
			this.setMethodAnnotations(new HashMap<String,List<String>>());
			this.setFieldAnnotations(new HashMap<String,List<String>>());
		}
		
		public List<String> getClassAnnotations() {
			return classAnnotations;
		}

		public List<String> getMethodAnnotations(String method) {
			return methodAnnotations.get(method);
		}

		public List<String> getFieldAnnotations(String field) {
			return fieldAnnotations.get(field);
		}

		public void setClassAnnotations(List<String> classAnnotations) {
			this.classAnnotations = classAnnotations;
		}

		public Map<String, List<String>> getMethodAnnotations() {
			return methodAnnotations;
		}

		public void setMethodAnnotations(Map<String, List<String>> methodAnnotations) {
			this.methodAnnotations = methodAnnotations;
		}

		public Map<String, List<String>> getFieldAnnotations() {
			return fieldAnnotations;
		}

		public void setFieldAnnotations(Map<String, List<String>> fieldAnnotations) {
			this.fieldAnnotations = fieldAnnotations;
		}
		
		List<String> putMethodAnnotations(String key, List<String> annotations){
			return getMethodAnnotations().put(key, annotations);
		}
		
		List<String> putFieldAnnotations(String key, List<String> annotations){
			return getFieldAnnotations().put(key, annotations);
		}

	}

	public static Configuration parse(File file) throws IOException {
		Utils.assertParamNotNull(file, "file");
		ObjectMapper mapper = new ObjectMapper();
		InputStream is = null;
		try {
			is = Files.newInputStream(file.toPath());
			return mapper.readValue(is, Configuration.class);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
