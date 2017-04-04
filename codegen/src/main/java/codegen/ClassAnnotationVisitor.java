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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import codegen.InternalConfiguration.ClazzContainer;

public class ClassAnnotationVisitor extends VoidVisitorAdapter<Void> {

	private final InternalConfiguration config;
	private String packageName;
	private String FQN;
	private boolean hasChanged = false;

	private ClassAnnotationVisitor(final InternalConfiguration config, final String packageName) {
		this(config);
		Utils.assertParamNotNull(packageName, "packageName");
		this.packageName = packageName;
	}

	public ClassAnnotationVisitor(InternalConfiguration config) {
		Utils.assertParamNotNull(config, "config");
		this.config = config;
	}

	@Override
	public void visit(PackageDeclaration n, Void arg) {
		this.packageName = n.getName().toString();
		super.visit(n, arg);
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, Void arg) {
		if (this.FQN == null) {
			this.FQN = packageName + "." + n.getName();
			final ClazzContainer clazz = this.config.getClass(this.FQN);
			if (clazz != null && !clazz.getClassAnnotations().isEmpty()) {
				hasChanged |= normalize(n.getAnnotations(), clazz.getClassAnnotations());
			}
			super.visit(n, arg);
		} else {
			new ClassAnnotationVisitor(config, this.FQN).visit(n, arg);
		}
	}

	@Override
	public void visit(EnumDeclaration n, Void arg) {
		if (this.FQN == null) {
			this.FQN = packageName + "." + n.getName();
			final ClazzContainer clazz = this.config.getClass(this.FQN);
			if (clazz != null && !clazz.getClassAnnotations().isEmpty()) {
				hasChanged |= normalize(n.getAnnotations(), clazz.getClassAnnotations());
			}
			super.visit(n, arg);
		} else {
			new ClassAnnotationVisitor(config, this.FQN).visit(n, arg);
		}
	}

	@Override
	public void visit(FieldDeclaration n, Void arg) {
		if (checkEnclosingEquality(n)) {
			ClazzContainer clazz = this.config.getClass(this.FQN);
			if (clazz != null) {
				String fieldName = n.accept(new FieldNameExtractor(), null);
				if (fieldName != null) {
					List<AnnotationExpr> fieldAnnotations = clazz.getFieldAnnotations(fieldName);
					if (!fieldAnnotations.isEmpty()) {
						hasChanged |= normalize(n.getAnnotations(), fieldAnnotations);
					}
				}
			}
		}
		super.visit(n, arg);
	}

	@Override
	public void visit(final MethodDeclaration n, final Void arg) {
		if (checkEnclosingEquality(n)) {
			ClazzContainer clazz = this.config.getClass(this.FQN);
			if (clazz != null) {
				String methodSignature = getMethodSignature(n);
				List<AnnotationExpr> methodAnnotations = clazz.getMethodAnnotations(methodSignature);
				if (!methodAnnotations.isEmpty()) {
					hasChanged |= normalize(n.getAnnotations(), methodAnnotations);
				}
			}
		}
		super.visit(n, arg);
	}

	boolean normalize(final List<AnnotationExpr> source, final List<AnnotationExpr> newAnnotations) {
		boolean hasChanged = false;
		final Map<String, AnnotationExpr> sourceMap = new HashMap<String, AnnotationExpr>();
		for (AnnotationExpr annotationExpr : source) {
			String accept = annotationExpr.accept(new AnnotationDeclarationBaseExtractor(), null);
			sourceMap.put(accept, annotationExpr);
		}
		for (AnnotationExpr newAnnotation : newAnnotations) {
			String name = newAnnotation.accept(new AnnotationDeclarationBaseExtractor(), null);
			if (sourceMap.containsKey(name)) {
				AnnotationExpr oldAnnotation = sourceMap.put(name, newAnnotation);
				source.remove(oldAnnotation);
				hasChanged |= source.add(newAnnotation);
			} else {
				hasChanged |= source.add(newAnnotation);
			}
		}
		return hasChanged;
	}

	String getMethodSignature(final MethodDeclaration n) {
		return String.format("%s(%s)", n.getName(), join(n.getParameters()));
	}

	private String join(final List<Parameter> parameters) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parameters.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(parameters.get(i).getType());
		}
		return sb.toString();
	}

	static class FieldNameExtractor extends GenericVisitorAdapter<String, Void> {
		@Override
		public String visit(VariableDeclaratorId n, Void arg) {
			return n.getName();
		}
	}

	private class AnnotationDeclarationBaseExtractor extends GenericVisitorAdapter<String, Void> {

		@Override
		public String visit(MarkerAnnotationExpr n, Void arg) {
			return n.getName().getName();
		}

		@Override
		public String visit(NormalAnnotationExpr n, Void arg) {
			return n.getName().toString();
		}

		@Override
		public String visit(SingleMemberAnnotationExpr n, Void arg) {
			return n.getName().getName();
		}

	}

	boolean checkEnclosingEquality(Node node) {
		if (FQN == null) {
			throw new IllegalStateException("FQN not initialized");
		}
		String found = new FQNExtractor(node).find();
		return found.equals(this.FQN);
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	String getFQN() {
		return this.FQN;
	}
}
