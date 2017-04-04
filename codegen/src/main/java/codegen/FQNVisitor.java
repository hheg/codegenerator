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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EmptyMemberDeclaration;
import com.github.javaparser.ast.body.EmptyTypeDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.MultiTypeParameter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.GenericVisitor;

class FQNVisitor implements GenericVisitor<String, Void> {

	public FQNVisitor(final Node startNode) {
		if (startNode instanceof CompilationUnit) {
			throw new IllegalStateException("Can't find out the FQN at the root node");
		}
	}

	private String noop(Node n, Void arg) {
		final Node parentNode = n.getParentNode();
		if (parentNode == null) {
			return "";
		}
		return parentNode.accept(this, arg);
	}

	public String visit(CompilationUnit n, Void arg) {
		return n.getPackage().accept(this, null);
	}

	public String visit(PackageDeclaration n, Void arg) {
		return n.getName().toString();
	}

	public String visit(ClassOrInterfaceDeclaration n, Void arg) {
		final String parent = n.getParentNode().accept(this, arg);
		return parent + "." + n.getName();
	}

	public String visit(EnumDeclaration n, Void arg) {
		final String parent = n.getParentNode().accept(this, arg);
		return parent + "." + n.getName();
	}

	public String visit(ImportDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(TypeParameter n, Void arg) {
		return noop(n, arg);
	}

	public String visit(LineComment n, Void arg) {
		return noop(n, arg);
	}

	public String visit(BlockComment n, Void arg) {
		return noop(n, arg);
	}

	public String visit(EmptyTypeDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(EnumConstantDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(AnnotationDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(AnnotationMemberDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(FieldDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(VariableDeclarator n, Void arg) {
		return noop(n, arg);
	}

	public String visit(VariableDeclaratorId n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ConstructorDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(MethodDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(Parameter n, Void arg) {
		return noop(n, arg);
	}

	public String visit(MultiTypeParameter n, Void arg) {
		return noop(n, arg);
	}

	public String visit(EmptyMemberDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(InitializerDeclaration n, Void arg) {
		return noop(n, arg);
	}

	public String visit(JavadocComment n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ClassOrInterfaceType n, Void arg) {
		return noop(n, arg);
	}

	public String visit(PrimitiveType n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ReferenceType n, Void arg) {
		return noop(n, arg);
	}

	public String visit(VoidType n, Void arg) {
		return noop(n, arg);
	}

	public String visit(WildcardType n, Void arg) {
		return noop(n, arg);
	}

	public String visit(UnknownType n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ArrayAccessExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ArrayCreationExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ArrayInitializerExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(AssignExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(BinaryExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(CastExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ClassExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ConditionalExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(EnclosedExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(FieldAccessExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(InstanceOfExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(StringLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(IntegerLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(LongLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(IntegerLiteralMinValueExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(LongLiteralMinValueExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(CharLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(DoubleLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(BooleanLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(NullLiteralExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(MethodCallExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(NameExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ObjectCreationExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(QualifiedNameExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ThisExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(SuperExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(UnaryExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(VariableDeclarationExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(MarkerAnnotationExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(SingleMemberAnnotationExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(NormalAnnotationExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(MemberValuePair n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ExplicitConstructorInvocationStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(TypeDeclarationStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(AssertStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(BlockStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(LabeledStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(EmptyStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ExpressionStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(SwitchStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(SwitchEntryStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(BreakStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ReturnStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(IfStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(WhileStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ContinueStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(DoStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ForeachStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ForStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(ThrowStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(SynchronizedStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(TryStmt n, Void arg) {
		return noop(n, arg);
	}

	public String visit(CatchClause n, Void arg) {
		return noop(n, arg);
	}

	public String visit(LambdaExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(MethodReferenceExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(TypeExpr n, Void arg) {
		return noop(n, arg);
	}

	public String visit(IntersectionType arg0, Void arg1) {
		return noop(arg0, arg1);
	}

	public String visit(UnionType arg0, Void arg1) {
		return noop(arg0, arg1);
	}

}
