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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Ignore;
import org.junit.Test;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

public class FQNVisitorTest {
	
	@Test
	public void testFQNNestedPackage() throws ParseException{
		String packageName = "com.test.next";
		CompilationUnit parsedCompilationUnit = getParsedCompilationUnit();
		parsedCompilationUnit.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(packageName)));
		TypeDeclaration typeDeclaration = parsedCompilationUnit.getTypes().get(0);
		ClassOrInterfaceDeclaration ciod = (ClassOrInterfaceDeclaration) typeDeclaration;
		String FQN = ciod.accept(new FQNVisitor(ciod), null);
		assertEquals(packageName+".TestClass",FQN);		
	}
	
	
	@Test
	public void testFQNOfSingleClass() throws ParseException {
		String packageName = "test";
		CompilationUnit parsedCompilationUnit = getParsedCompilationUnit();		
		TypeDeclaration typeDeclaration = parsedCompilationUnit.getTypes().get(0);
		BodyDeclaration bodyDeclaration = typeDeclaration.getMembers().get(0);
		BodyDeclaration bodyDeclaration2 = typeDeclaration.getMembers().get(1);
		
		String FQN1 = bodyDeclaration.accept(new FQNVisitor(bodyDeclaration), null);
		String FQN2 = bodyDeclaration2.accept(new FQNVisitor(bodyDeclaration2), null);
		assertEquals(packageName + "." + "TestClass", FQN1);
		assertEquals(packageName + "." + "TestClass", FQN2);
	}

	/* This test shows that when you set the parent of a ClassOrInterfaceDeclaration the assertion fails, unless you add members to the class.*/
	@Test
	@Ignore
	public void testProbableBugWithParentsNotBeingSet_SettingCompilationUnitAsClassNodeParent() throws ParseException {
		//As Reference
		CompilationUnit parsedCompilationUnit = getParsedCompilationUnit();
		assertTrue(hasUnbrokenParentsLink(parsedCompilationUnit));
		
		//Trying to create a CU manually
		CompilationUnit createdCU = new CompilationUnit();
		PackageDeclaration createdPackageDeclaration = new PackageDeclaration(ASTHelper.createNameExpr("test"));
		createdCU.setPackage(createdPackageDeclaration);
		assertEquals(createdPackageDeclaration.getParentNode(),createdCU);
		
		//public class TestClass {
		ClassOrInterfaceDeclaration createdClassOrInterfaceDeclaration = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC,
		        false, "TestClass");
		ASTHelper.addTypeDeclaration(createdCU, createdClassOrInterfaceDeclaration);
		createdClassOrInterfaceDeclaration.setParentNode(createdCU); // below assertion still fails, but it works when the others are set
//		assertEquals(createdCU,createdClassOrInterfaceDeclaration);
		
		// public String field;
		FieldDeclaration createdFieldDeclaration = createField(createdClassOrInterfaceDeclaration);
		assertEquals(createdClassOrInterfaceDeclaration,createdFieldDeclaration.getParentNode());
		
		// public void method(){}
		MethodDeclaration createdMethodDeclaratoin = createMethod(createdClassOrInterfaceDeclaration);
		assertEquals(createdClassOrInterfaceDeclaration,createdMethodDeclaratoin.getParentNode());
		
		assertTrue(hasUnbrokenParentsLink(createdCU));
		assertEquals(parsedCompilationUnit,createdCU);		
	}
	
	/* This shows that if you don't set a ClassOrInterfaceDeclaration parent it still fails later on in the reference check */
	@Test
	@Ignore
	public void testProbableBugWithParentsNotBeingSet_NotSettingCompilationUnitAsClassNodeParent() throws ParseException {
		//As Reference
		CompilationUnit parsedCompilationUnit = getParsedCompilationUnit();
		assertTrue(hasUnbrokenParentsLink(parsedCompilationUnit));
		
		//Trying to create a CU manually
		CompilationUnit createdCU = new CompilationUnit();
		PackageDeclaration createdPackageDeclaration = new PackageDeclaration(ASTHelper.createNameExpr("test"));
		createdCU.setPackage(createdPackageDeclaration);
		assertEquals(createdPackageDeclaration.getParentNode(),createdCU);
		
		//public class TestClass {
		ClassOrInterfaceDeclaration createdClassOrInterfaceDeclaration = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC,
		        false, "TestClass");
		ASTHelper.addTypeDeclaration(createdCU, createdClassOrInterfaceDeclaration);
//		createdClassOrInterfaceDeclaration.setParentNode(createdCU);
//		assertEquals(createdCU,createdClassOrInterfaceDeclaration);
		
		// public String field;
		FieldDeclaration createdFieldDeclaration = createField(createdClassOrInterfaceDeclaration);
		assertEquals(createdClassOrInterfaceDeclaration,createdFieldDeclaration.getParentNode());
		
		// public void method(){}
		MethodDeclaration createdMethodDeclaratoin = createMethod(createdClassOrInterfaceDeclaration);
		assertEquals(createdClassOrInterfaceDeclaration,createdMethodDeclaratoin.getParentNode());
		
		assertTrue(hasUnbrokenParentsLink(createdCU));
		assertEquals(parsedCompilationUnit,createdCU);		
	}
	
	/* This test shows that it the assertion will fail when checking the parent of a ClassOrInterfaceDeclaration even if it's set explicitly */	
	@Test
	@Ignore
	public void testProbableBugWithParentsNotBeingSet() throws ParseException {
		//As Reference
		CompilationUnit parsedCompilationUnit = getParsedCompilationUnit();
		assertTrue(hasUnbrokenParentsLink(parsedCompilationUnit));
		
		//Trying to create a CU manually
		CompilationUnit createdCU = new CompilationUnit();
		PackageDeclaration createdPackageDeclaration = new PackageDeclaration(ASTHelper.createNameExpr("test"));
		createdCU.setPackage(createdPackageDeclaration);
		assertEquals(createdPackageDeclaration.getParentNode(),createdCU);
		
		//public class TestClass {
		ClassOrInterfaceDeclaration createdClassOrInterfaceDeclaration = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC,
		        false, "TestClass");
		ASTHelper.addTypeDeclaration(createdCU, createdClassOrInterfaceDeclaration);
		createdClassOrInterfaceDeclaration.setParentNode(createdCU); // below assertion still fails, should it?
		assertEquals(createdCU,createdClassOrInterfaceDeclaration);
		
		// public String field;
		FieldDeclaration createdFieldDeclaration = createField(createdClassOrInterfaceDeclaration);
		assertEquals(createdClassOrInterfaceDeclaration,createdFieldDeclaration.getParentNode());
		
		// public void method(){}
		MethodDeclaration createdMethodDeclaratoin = createMethod(createdClassOrInterfaceDeclaration);
		assertEquals(createdClassOrInterfaceDeclaration,createdMethodDeclaratoin.getParentNode());
		
		assertTrue(hasUnbrokenParentsLink(createdCU));
		assertEquals(parsedCompilationUnit,createdCU);		
	}
	
	
	
	private CompilationUnit getParsedCompilationUnit() throws ParseException {
		/* @formatter:off */
		String file = 
				"package test;"+
				"public class TestClass {"+
				"	public String field;"+
				" 	public void method(){}"+
				"}";
		/* @formatter:on */
		InputStream is = new ByteArrayInputStream(file.getBytes(Charset.forName("UTF-8")));
		return JavaParser.parse(is);
	}
	
	private FieldDeclaration createField(ClassOrInterfaceDeclaration createdClassOrInterfaceDeclaration){
		FieldDeclaration createdFieldDeclaration = ASTHelper.createFieldDeclaration(ModifierSet.PUBLIC,
		        ASTHelper.createReferenceType("String", 0), "field");
		ASTHelper.addMember(createdClassOrInterfaceDeclaration, createdFieldDeclaration);
		createdFieldDeclaration.setParentNode(createdClassOrInterfaceDeclaration);
		return createdFieldDeclaration;
	}
	
	private MethodDeclaration createMethod(ClassOrInterfaceDeclaration createdClassOrInterfaceDeclaration) {
		MethodDeclaration createdMethodDeclaration = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "method");
		BlockStmt bstmt = new BlockStmt();
		createdMethodDeclaration.setBody(bstmt);
		ASTHelper.addMember(createdClassOrInterfaceDeclaration, createdMethodDeclaration);
		createdMethodDeclaration.setParentNode(createdClassOrInterfaceDeclaration);
		return createdMethodDeclaration;
	}

	private boolean hasUnbrokenParentsLink(CompilationUnit cu){
		PackageDeclaration package1 = cu.getPackage();
		assertEquals(package1.getParentNode(),cu);
		
		TypeDeclaration classTypeDeclaration = cu.getTypes().get(0);
		assertEquals(cu,classTypeDeclaration.getParentNode());
		assertTrue(classTypeDeclaration instanceof ClassOrInterfaceDeclaration);
		ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) classTypeDeclaration;
		assertEquals("TestClass",classOrInterfaceDeclaration.getName());
		
		BodyDeclaration fieldBodyDeclaration = classOrInterfaceDeclaration.getMembers().get(0);
		assertEquals(classOrInterfaceDeclaration, fieldBodyDeclaration.getParentNode());
		assertTrue(fieldBodyDeclaration instanceof FieldDeclaration);
		
		BodyDeclaration methodBodyDeclaration = classOrInterfaceDeclaration.getMembers().get(1);
		assertEquals(classOrInterfaceDeclaration,methodBodyDeclaration.getParentNode());
		assertTrue(methodBodyDeclaration instanceof MethodDeclaration);
		return true;
	}
	
}
