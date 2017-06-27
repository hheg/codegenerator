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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CodeGeneratorTest {

	private static final List<Pair<String>> files = new ArrayList<Pair<String>>();
	{
		files.add(new Pair<String>("src/test/resources/codegen/TestClass.java", "target/test/codegen/TestClass.java"));
		files.add(new Pair<String>("src/test/resources/codegen/NestedTestClass.java","target/test/codegen/NestedTestClass.java"));
		files.add(new Pair<String>("src/test/resources/codegen/TestObject.java", "target/test/codegen/TestObject.java"));
		files.add(new Pair<String>("src/test/resources/codegen/CorruptJavaFile.java","target/test/codegen/CorruptJavaFile.java"));
	}

	@Before
	public void setup() throws IOException {
		for (Pair<String> p : files) {
			copy(p.left, p.right);
		}
	}

	private void copy(String source, String dest) throws IOException {
		File fileToCopy = new File(source).getAbsoluteFile();
		assertTrue("File " + fileToCopy.toString() + " doesn't exist", fileToCopy.exists());
		File destination = new File(dest).getAbsoluteFile();
		destination.getParentFile().mkdirs();
		FileUtils.copyFile(fileToCopy, destination);		
	}

	@Test
	public void testToAnnotateAClass() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_class.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		expectedSource.accept(new ClassAnnotationCreator("ClassAnnotation"), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));
	}

	@Test
	public void testToAnnotateAField() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_field.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		expectedSource.accept(new FieldAnnotationCreator("field",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("FieldAnnotation"))), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));
	}

	@Test
	public void testToAnnotateMethod() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_method.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		expectedSource.accept(new MethodAnnotationCreator("method()",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));
	}

	@Test
	public void testToAnnotateMethodWithTwoParameters() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_method2params.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		expectedSource.accept(new MethodAnnotationCreator("method(String,String)",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));

	}

	@Test
	public void testAnnotateAllMembers() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		expectedSource.accept(new MethodAnnotationCreator("method(String,String)",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);
		expectedSource.accept(new MethodAnnotationCreator("method()",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);
		expectedSource.accept(new FieldAnnotationCreator("field",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("FieldAnnotation"))), null);
		expectedSource.accept(new ClassAnnotationCreator("ClassAnnotation"), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));
	}

	@Test
	public void testAnnotateAllMembersWithTwoAnnotations() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_doubleannotation.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		expectedSource.accept(new MethodAnnotationCreator("method(String,String)",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);
		expectedSource.accept(new MethodAnnotationCreator("method(String,String)",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("SecondMethodAnnotation"))), null);
		expectedSource.accept(new MethodAnnotationCreator("method()",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);
		expectedSource.accept(new MethodAnnotationCreator("method()",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("SecondMethodAnnotation"))), null);
		expectedSource.accept(new FieldAnnotationCreator("field",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("FieldAnnotation"))), null);
		expectedSource.accept(new FieldAnnotationCreator("field",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("SecondFieldAnnotation"))), null);
		expectedSource.accept(new ClassAnnotationCreator("ClassAnnotation"), null);
		expectedSource.accept(new ClassAnnotationCreator("SecondClassAnnotation"), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));

	}

	@Test
	public void testAnnotateANestedClass() throws IOException, ParseException {
		File targetFile = getFile(files.get(1).right);
		assertTrue(targetFile.exists());
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfgnested.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedNestedSource();
		expectedSource.accept(new MethodAnnotationCreator("method()",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("MethodAnnotation"))), null);
		expectedSource.accept(new FieldAnnotationCreator("field",
				new MarkerAnnotationExpr(ASTHelper.createNameExpr("FieldAnnotation"))), null);
		expectedSource.accept(new ClassAnnotationCreator("ClassAnnotation"), null);
		assertEquals(expectedSource, JavaParser.parse(targetFile));
	}

	@Test
	public void testReadJsonWithFieldAnnotationWithValue() throws IOException, ParseException {
		File targetFile = getFile(files.get(0).right);
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_fieldannotationwithvalue.json"));
		assertTrue(cg.parse(targetFile));
		CompilationUnit expectedSource = getExpectedSource();
		List<MemberValuePair> pairs = new ArrayList<MemberValuePair>();
		pairs.add(new MemberValuePair("name", new StringLiteralExpr("value")));
		expectedSource.accept(new FieldAnnotationCreator("field",
				new NormalAnnotationExpr(ASTHelper.createNameExpr("FieldAnnotation"), pairs)), null);

		assertEquals(expectedSource, JavaParser.parse(targetFile));
	}

	@Test
	public void testSeveralAnnotationsBug() throws ParseException, IOException {
		File targetFile = getFile(files.get(2).right);
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_severalannotationsbug.json"));
		assertTrue(cg.parse(targetFile));
		File actualResult = getFile("src/test/resources/codegen/TestObjectResult.java");
		assertEquals(JavaParser.parse(actualResult), JavaParser.parse(targetFile));
	}
	
	@Test
	public void testAListOfFiles() throws IOException, ParseException, ParseExceptions {
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_severalannotationsbug.json"));
		List<File> parse = cg.parse(getFile(files.get(0).right), getFile(files.get(2).right));
		assertTrue(parse.size() == 1);
	}

	@Test(expected = NullPointerException.class)
	public void testToParseANotExistingFile() throws IOException, ParseException {
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_severalannotationsbug.json"));
		cg.parse((File) null);
	}
	
	@Test(expected = ParseExceptions.class)
	public void testParsingAMalformedFile() throws IOException, ParseException, ParseExceptions {
		CodeGenerator cg = new CodeGenerator(getFile("src/test/resources/codegen/cfg_severalannotationsbug.json"));
		try {
			cg.parse(new File[] { getFile(files.get(3).right), getFile(files.get(0).right) });
		} catch (ParseExceptions e) {
			assertEquals(1, e.getExceptions().size());
			throw e;
		}
	}

	private CompilationUnit getExpectedNestedSource() {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(CodeGeneratorTest.class.getPackage().getName())));

		ClassOrInterfaceDeclaration cid = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "NestedTestClass");
		ASTHelper.addTypeDeclaration(cu, cid);

		FieldDeclaration fd = ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC,
				ASTHelper.createReferenceType("String", 0), "field");
		ASTHelper.addMember(cid, fd);

		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "method");
		BlockStmt bstmt = new BlockStmt();
		method.setBody(bstmt);
		ASTHelper.addMember(cid, method);

		ClassOrInterfaceDeclaration nid = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC | ModifierSet.STATIC,
				false, "InternalClass");
		ASTHelper.addMember(cid, nid);

		FieldDeclaration nfd = ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC,
				ASTHelper.createReferenceType("String", 0), "field");
		ASTHelper.addMember(nid, nfd);

		MethodDeclaration nmethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "method");
		BlockStmt nbstmt = new BlockStmt();
		nmethod.setBody(nbstmt);
		ASTHelper.addMember(nid, nmethod);

		return cu;
	}

	private File getFile(String string) {
		return new File(string).getAbsoluteFile();
	}

	private CompilationUnit getExpectedSource() {
		CompilationUnit cu = new CompilationUnit();
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(CodeGeneratorTest.class.getPackage().getName())));
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

	private static class ClassAnnotationCreator extends VoidVisitorAdapter<Void> {
		private final String annotation;

		public ClassAnnotationCreator(String annotation) {
			this.annotation = annotation;
		}

		@Override
		public void visit(ClassOrInterfaceDeclaration n, Void arg) {
			n.getAnnotations().add(new MarkerAnnotationExpr(ASTHelper.createNameExpr(annotation)));
			super.visit(n, arg);
		}
	}

	private static class FieldAnnotationCreator extends VoidVisitorAdapter<Void> {
		private final AnnotationExpr annotation;
		private final String field;

		public FieldAnnotationCreator(String field, AnnotationExpr annotation) {
			this.annotation = annotation;
			this.field = field;
		}

		@Override
		public void visit(FieldDeclaration n, Void arg) {
			String fieldName = n.accept(new ClassAnnotationVisitor.FieldNameExtractor(), null);
			if (field.equals(fieldName)) {
				n.getAnnotations().add(annotation);
			}
			super.visit(n, arg);
		}
	}

	private static class MethodAnnotationCreator extends VoidVisitorAdapter<Void> {
		private final String method;
		private final AnnotationExpr annotationExpr;

		public MethodAnnotationCreator(String method, AnnotationExpr annotationExpr) {
			this.method = method;
			this.annotationExpr = annotationExpr;
		}

		@Override
		public void visit(MethodDeclaration n, Void arg) {
			if (method.equals(method(n))) {
				n.getAnnotations().add(annotationExpr);
			}
			super.visit(n, arg);
		}

		private String method(MethodDeclaration n) {
			return String.format("%s(%s)", n.getName(), join(n.getParameters()));
		}

		private Object join(List<Parameter> parameters) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < parameters.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(parameters.get(i).getType());
			}
			return sb.toString();
		}
	}

	private static class Pair<T> {
		final T left;
		final T right;

		public Pair(T left, T right) {
			this.left = left;
			this.right = right;
		}
	}

}
