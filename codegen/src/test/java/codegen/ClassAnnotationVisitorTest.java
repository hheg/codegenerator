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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ReferenceType;

public class ClassAnnotationVisitorTest {

	@Test
	public void testMethodSignature() {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));
		MethodDeclaration md = new MethodDeclaration(ModifierSet.PUBLIC, new ArrayList<AnnotationExpr>(),
				new ArrayList<TypeParameter>(), ASTHelper.VOID_TYPE, "method", new ArrayList<Parameter>(), 0,
				new ArrayList<ReferenceType>(), new BlockStmt());
		String methodSignature = visitor.getMethodSignature(md);
		assertEquals("method()", methodSignature);
	}

	@Test
	public void testMethodSignatureWithOneParameter() {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));
		List<Parameter> parameterList = new ArrayList<Parameter>();
		parameterList.add(ASTHelper.createParameter(ASTHelper.createReferenceType("java.lang.String", 0), "string"));
		MethodDeclaration md = new MethodDeclaration(ModifierSet.PUBLIC, new ArrayList<AnnotationExpr>(),
				new ArrayList<TypeParameter>(), ASTHelper.VOID_TYPE, "method", parameterList, 0,
				new ArrayList<ReferenceType>(), new BlockStmt());
		String methodSignature = visitor.getMethodSignature(md);
		assertEquals("method(java.lang.String)", methodSignature);
	}

	@Test
	public void testMethodSignatureWithTwoParameters() {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));
		List<Parameter> parameterList = new ArrayList<Parameter>();
		parameterList.add(ASTHelper.createParameter(ASTHelper.createReferenceType("java.lang.String", 0), "string1"));
		parameterList.add(ASTHelper.createParameter(ASTHelper.createReferenceType("java.lang.String", 0), "string2"));
		MethodDeclaration md = new MethodDeclaration(ModifierSet.PUBLIC, new ArrayList<AnnotationExpr>(),
				new ArrayList<TypeParameter>(), ASTHelper.VOID_TYPE, "method", parameterList, 0,
				new ArrayList<ReferenceType>(), new BlockStmt());
		String methodSignature = visitor.getMethodSignature(md);
		assertEquals("method(java.lang.String,java.lang.String)", methodSignature);
	}

	@Test
	public void testNestedPackageDeclaration() {
		String packageName = "com.test.next";
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));
		CompilationUnit cu = getExpectedSource(packageName);
		visitor.visit(cu, null);
		assertEquals("com.test.next.TestClass", visitor.getFQN());
	}

	@Test
	@Ignore // Ignoring because we overwrite the annotation for now
	public void testCheckAlreadyExistingAnnotationsClassShouldNotChangeExistingAnnotation() throws ParseException {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		List<AnnotationExpr> classAnnotations = new ArrayList<AnnotationExpr>();
		MarkerAnnotationExpr markerAnnotationExpr = new MarkerAnnotationExpr(ASTHelper.createNameExpr("Duplicate"));
		classAnnotations.add(markerAnnotationExpr);
		CompilationUnit cu = getParsedCompilationUnit();
		ClassOrInterfaceDeclaration ciod = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
		ciod.getAnnotations().add(markerAnnotationExpr);
		InternalConfiguration.ClazzContainer clazz = new InternalConfiguration.ClazzContainer(classAnnotations,
				new HashMap<String, List<AnnotationExpr>>(), new HashMap<String, List<AnnotationExpr>>());
		map.put("test.TestClass", clazz);
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));
		cu.accept(visitor, null);
		assertFalse(visitor.hasChanged());
		assertTrue(ciod.getAnnotations().toString(), ciod.getAnnotations().size() == 1);
	}

	@Test
	@Ignore // Ignoring because we overwrite the annotation for now
	public void testCheckAlreadyExistingAnnotationsFieldShouldNotChangeExistingAnnotation() throws ParseException {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		List<AnnotationExpr> fieldAnnotations = new ArrayList<AnnotationExpr>();
		MarkerAnnotationExpr markerAnnotationExpr = new MarkerAnnotationExpr(ASTHelper.createNameExpr("Duplicate"));
		fieldAnnotations.add(markerAnnotationExpr);
		Map<String, List<AnnotationExpr>> fieldMap = new HashMap<String, List<AnnotationExpr>>();
		fieldMap.put("field", fieldAnnotations);
		CompilationUnit cu = getParsedCompilationUnit();
		ClassOrInterfaceDeclaration ciod = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
		BodyDeclaration bodyDeclaration = ciod.getMembers().get(0);
		bodyDeclaration.getAnnotations().add(markerAnnotationExpr);

		InternalConfiguration.ClazzContainer clazz = new InternalConfiguration.ClazzContainer(
				new ArrayList<AnnotationExpr>(), fieldMap, new HashMap<String, List<AnnotationExpr>>());
		map.put("test.TestClass", clazz);

		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));
		cu.accept(visitor, null);

		assertFalse(visitor.hasChanged());
		assertTrue(bodyDeclaration.getAnnotations().toString(), bodyDeclaration.getAnnotations().size() == 1);
	}

	@Test
	@Ignore // Ignoring because we overwrite the annotation for now
	public void testCheckAlreadyExistingAnnotationsMethodShouldNotChangeCurrentAnnotation() throws ParseException {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		List<AnnotationExpr> methodAnnotations = new ArrayList<AnnotationExpr>();
		MarkerAnnotationExpr markerAnnotationExpr = new MarkerAnnotationExpr(ASTHelper.createNameExpr("Duplicate"));
		methodAnnotations.add(markerAnnotationExpr);
		Map<String, List<AnnotationExpr>> methodMap = new HashMap<String, List<AnnotationExpr>>();
		methodMap.put("method()", methodAnnotations);
		CompilationUnit cu = getParsedCompilationUnit();
		ClassOrInterfaceDeclaration ciod = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
		BodyDeclaration bodyDeclaration = ciod.getMembers().get(1);
		bodyDeclaration.getAnnotations().add(markerAnnotationExpr);

		InternalConfiguration.ClazzContainer clazz = new InternalConfiguration.ClazzContainer(
				new ArrayList<AnnotationExpr>(), new HashMap<String, List<AnnotationExpr>>(), methodMap);
		map.put("test.TestClass", clazz);
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));

		cu.accept(visitor, null);

		assertFalse(visitor.hasChanged());
		assertTrue(bodyDeclaration.getAnnotations().toString(), bodyDeclaration.getAnnotations().size() == 1);
	}

	@Test
	@Ignore // Ignoring because we overwrite the annotation for now
	public void testCheckAlreadyExistingAnnotationsMethodWithAnnotationBodyShouldNotChangeExistingAnnotationWithBody()
			throws ParseException {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		List<AnnotationExpr> methodAnnotations = new ArrayList<AnnotationExpr>();
		NameExpr createNameExpr = ASTHelper.createNameExpr("Duplicate");
		MarkerAnnotationExpr markerAnnotation = new MarkerAnnotationExpr(createNameExpr);
		methodAnnotations.add(markerAnnotation);
		Map<String, List<AnnotationExpr>> methodMap = new HashMap<String, List<AnnotationExpr>>();
		methodMap.put("method()", methodAnnotations);

		InternalConfiguration.ClazzContainer clazz = new InternalConfiguration.ClazzContainer(
				new ArrayList<AnnotationExpr>(), new HashMap<String, List<AnnotationExpr>>(), methodMap);
		map.put("test.TestClass", clazz);
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));

		CompilationUnit cu = getParsedCompilationUnit();
		ClassOrInterfaceDeclaration ciod = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
		BodyDeclaration bodyDeclaration = ciod.getMembers().get(1);

		List<MemberValuePair> mvp = new ArrayList<MemberValuePair>();
		mvp.add(new MemberValuePair("name", new StringLiteralExpr("value")));
		NormalAnnotationExpr markerAnnotationExprWithBody = new NormalAnnotationExpr(createNameExpr, mvp);
		bodyDeclaration.getAnnotations().add(markerAnnotationExprWithBody);

		cu.accept(visitor, null);

		assertFalse(visitor.hasChanged());
		assertTrue(bodyDeclaration.getAnnotations().toString(), bodyDeclaration.getAnnotations().size() == 1);
		assertEquals(bodyDeclaration.getAnnotations().get(0), markerAnnotationExprWithBody);
	}

	@Test
	public void testCheckAlreadyExistingAnnotationsWithoutAnnotationBody() throws ParseException {
		Map<String, InternalConfiguration.ClazzContainer> map = new HashMap<String, InternalConfiguration.ClazzContainer>();
		NameExpr createNameExpr = ASTHelper.createNameExpr("Duplicate");
		List<AnnotationExpr> methodAnnotations = new ArrayList<AnnotationExpr>();
		List<MemberValuePair> mvp = new ArrayList<MemberValuePair>();
		mvp.add(new MemberValuePair("name", new StringLiteralExpr("value")));
		NormalAnnotationExpr markerAnnotationExprWithBody = new NormalAnnotationExpr(createNameExpr, mvp);

		methodAnnotations.add(markerAnnotationExprWithBody);
		Map<String, List<AnnotationExpr>> methodMap = new HashMap<String, List<AnnotationExpr>>();
		methodMap.put("method()", methodAnnotations);

		InternalConfiguration.ClazzContainer clazz = new InternalConfiguration.ClazzContainer(
				new ArrayList<AnnotationExpr>(), new HashMap<String, List<AnnotationExpr>>(), methodMap);
		map.put("test.TestClass", clazz);
		ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(new InternalConfiguration(map));

		CompilationUnit cu = getParsedCompilationUnit();
		ClassOrInterfaceDeclaration ciod = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
		BodyDeclaration bodyDeclaration = ciod.getMembers().get(1);
		MarkerAnnotationExpr markerAnnotation = new MarkerAnnotationExpr(createNameExpr);
		bodyDeclaration.getAnnotations().add(markerAnnotation);

		cu.accept(visitor, null);

		assertTrue(visitor.hasChanged());
		assertTrue(bodyDeclaration.getAnnotations().toString(), bodyDeclaration.getAnnotations().size() == 1);
		assertEquals(bodyDeclaration.getAnnotations().get(0), markerAnnotationExprWithBody);
	}

	private CompilationUnit getParsedCompilationUnit() throws ParseException {
		/* @formatter:off */
		String file = "package test;" + "public class TestClass {" + "	public String field;"
				+ " 	public void method(){}" + "}";
		/* @formatter:on */
		InputStream is = new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
		return JavaParser.parse(is);
	}

	private CompilationUnit getExpectedSource(String packageName) {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(packageName)));
		ClassOrInterfaceDeclaration cid = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "TestClass");
		ASTHelper.addTypeDeclaration(cu, cid);

		FieldDeclaration fd = ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC,
				ASTHelper.createReferenceType("String", 0), "field");
		ASTHelper.addMember(cid, fd);

		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "method");
		BlockStmt bstmt = new BlockStmt();
		method.setBody(bstmt);
		ASTHelper.addMember(cid, method);

		List<Parameter> params = new ArrayList<Parameter>();
		params.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "param1"));
		params.add(ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "param2"));
		MethodDeclaration methodTwoArgs = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "method",
				params);
		methodTwoArgs.setBody(new BlockStmt());
		ASTHelper.addMember(cid, methodTwoArgs);

		return cu;

	}
}
