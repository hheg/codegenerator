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

import java.util.Collections;
import java.util.List;

import com.github.javaparser.ParseException;

public class ParseExceptions extends Exception {

	private static final long serialVersionUID = -3237176459897867344L;

	private final List<ParseException> exceptions;

	public ParseExceptions(List<ParseException> exceptions) {
		this.exceptions = exceptions;
	}

	public List<ParseException> getExceptions() {
		return Collections.unmodifiableList(exceptions);
	}
}