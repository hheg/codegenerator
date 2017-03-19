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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.AnnotationExpr;

class InternalConfiguration {

	public InternalConfiguration(Map<String, ClazzContainer> classes) {
		this.classes.putAll(classes);
	}

	private final Map<String, ClazzContainer> classes = new HashMap<String, ClazzContainer>();

	ClazzContainer getClass(String name) {
		return classes.get(name);
	}

	static class ClazzContainer {
		private final List<AnnotationExpr> classAnnotations = new ArrayList<AnnotationExpr>();
		private final Map<String, List<AnnotationExpr>> methodAnnotations = new HashMap<String, List<AnnotationExpr>>();
		private final Map<String, List<AnnotationExpr>> fieldAnnotations = new HashMap<String, List<AnnotationExpr>>();

		public ClazzContainer(final List<AnnotationExpr> classAnnotations,
				final Map<String, List<AnnotationExpr>> fieldAnnotations,
				final Map<String, List<AnnotationExpr>> methodAnnotations) {
			Utils.assertParamNotNull(classAnnotations, "classAnnotations");
			Utils.assertParamNotNull(fieldAnnotations, "fieldAnnotations");
			Utils.assertParamNotNull(methodAnnotations, "methodAnnotations");
			this.classAnnotations.addAll(classAnnotations);
			this.fieldAnnotations.putAll(fieldAnnotations);
			this.methodAnnotations.putAll(methodAnnotations);
		}

		public List<AnnotationExpr> getClassAnnotations() {
			return Collections.unmodifiableList(classAnnotations);
		}

		public List<AnnotationExpr> getFieldAnnotations(String field) {
			return getList(fieldAnnotations, field);
		}

		public List<AnnotationExpr> getMethodAnnotations(String method) {
			return getList(methodAnnotations, method);
		}

		private List<AnnotationExpr> getList(Map<String, List<AnnotationExpr>> map, String key) {
			List<AnnotationExpr> list = map.get(key);
			if (list == null) {
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(list);
		}

	}
}
