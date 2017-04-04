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

import com.github.javaparser.ast.Node;

public class FQNExtractor {

	private final Node startNode;

	public FQNExtractor(final Node startNode) {
		this.startNode = startNode;
	}

	public String find() {
		return startNode.accept(new FQNVisitor(startNode), null);
	}

}
