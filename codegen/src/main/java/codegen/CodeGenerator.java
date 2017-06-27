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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;

import codegen.Configuration.ClazzContainer;

public class CodeGenerator {

	private final InternalConfiguration config;
	private final Charset encoding;

	public CodeGenerator(final File configFile) throws IOException, ParseException {
		Utils.assertParamNotNull(configFile, "configFile");
		final Configuration parsedConfig = Configuration.parse(configFile);		
		this.encoding = Charset.forName(parsedConfig.getEncoding() != null ? parsedConfig.getEncoding() : "UTF-8");		
		config = validate(parsedConfig.getClasses());
	}

	private InternalConfiguration validate(final Map<String, ClazzContainer> classes) throws ParseException {
		final Map<String, InternalConfiguration.ClazzContainer> internalClasses = new HashMap<String, InternalConfiguration.ClazzContainer>();
		for (Entry<String, ClazzContainer> clazzContainer : classes.entrySet()) {
			final List<AnnotationExpr> classAnnotations = new ArrayList<AnnotationExpr>();

			for (String string : clazzContainer.getValue().getClassAnnotations()) {
				classAnnotations.add(JavaParser.parseAnnotation(string));
			}

			final Map<String, List<AnnotationExpr>> fieldAnnotations = parseAnnotations(
					clazzContainer.getValue().getFieldAnnotations().entrySet());
			final Map<String, List<AnnotationExpr>> methodAnnotations = parseAnnotations(
					clazzContainer.getValue().getMethodAnnotations().entrySet());

			internalClasses.put(clazzContainer.getKey(),
					new InternalConfiguration.ClazzContainer(classAnnotations, fieldAnnotations, methodAnnotations));
		}
		return new InternalConfiguration(internalClasses);
	}

	private Map<String, List<AnnotationExpr>> parseAnnotations(final Set<Entry<String, List<String>>> entrySet)
			throws ParseException {
		final Map<String, List<AnnotationExpr>> map = new HashMap<String, List<AnnotationExpr>>();
		for (Entry<String, List<String>> entry : entrySet) {
			final String key = entry.getKey();
			final List<String> annotationStrings = entry.getValue();
			final List<AnnotationExpr> list = new ArrayList<AnnotationExpr>(annotationStrings.size());
			for (String annotation : annotationStrings) {
				list.add(JavaParser.parseAnnotation(annotation));
			}
			map.put(key, list);
		}
		return map;
	}

	public List<File> parse(final List<File> files) throws IOException, ParseExceptions {
		final List<FileParseFaultResult> list = new ArrayList<FileParseFaultResult>();
		final List<File> parsedFiles = new ArrayList<File>();
		for (File file : files) {
			try {
				if (parse(file)) {
					parsedFiles.add(file);
				}
			} catch (ParseException e) {
				list.add(new FileParseFaultResult(e, file.toString()));
			}
		}
		if (!list.isEmpty()) {
			throw new ParseExceptions(list);
		}
		return parsedFiles;
	}

	public List<File> parse(final File... files) throws IOException, ParseExceptions {
		return parse(Arrays.asList(files));
	}

	public boolean parse(final File targetFile) throws IOException, ParseException {
		if (targetFile == null) {
			throw new NullPointerException();
		}
		if (!targetFile.exists()) {
			throw new FileNotFoundException(targetFile.toString());
		}
		final CompilationUnit cu = JavaParser.parse(targetFile, encoding.name());
		final ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(config);
		visitor.visit(cu, null);
		if (visitor.hasChanged()) {
			writeFile(cu, targetFile);
		}
		return visitor.hasChanged();
	}

	private void writeFile(final CompilationUnit cu, final File targetFile) throws IOException {
		if (targetFile.exists()) {
			if (!targetFile.delete()) {
				throw new IOException("Couldn't delete file " + targetFile.getAbsolutePath());
			}
		}
		final BufferedWriter fileOutput = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(targetFile), encoding));
		try {
			fileOutput.write(cu.toString());
		} finally {
			fileOutput.close();
		}
	}
}
