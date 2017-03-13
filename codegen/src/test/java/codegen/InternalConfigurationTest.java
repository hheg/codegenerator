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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.javaparser.ast.expr.AnnotationExpr;

public class InternalConfigurationTest {
	
	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test()
	public void testNullList() {
		Map<String,List<AnnotationExpr>> dummy = new HashMap<String,List<AnnotationExpr>>();
		expected.expectMessage("classAnnotations");
		new InternalConfiguration.ClazzContainer(null, dummy, dummy);		
	}
	
	@Test()
	public void testNullFieldMap() {
		Map<String,List<AnnotationExpr>> dummy = new HashMap<String,List<AnnotationExpr>>();
		List<AnnotationExpr> listDummy = new ArrayList<AnnotationExpr>();
		expected.expectMessage("fieldAnnotations");
		new InternalConfiguration.ClazzContainer(listDummy, null, dummy);		
	}
	
	@Test
	public void testNullMethodMap(){
		Map<String,List<AnnotationExpr>> dummy = new HashMap<String,List<AnnotationExpr>>();
		List<AnnotationExpr> listDummy = new ArrayList<AnnotationExpr>();
		expected.expectMessage("methodAnnotations");
		new InternalConfiguration.ClazzContainer(listDummy, dummy, null);
	}
}
