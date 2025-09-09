/*
 * Copyright (c) 2013 by Malte Isberner (https://github.com/misberner).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.kv;

/**
 * The visibility of a builder option attribute.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public enum Visibility {
	/**
	 * Public visibility.
	 */
	PUBLIC("public "),
	/**
	 * Default (package-level) visibility.
	 */
	DEFAULT(""),
	/**
	 * Protected visibility.
	 */
	PROTECTED("protected "),
	/**
	 * Private visibility.
	 */
	PRIVATE("private "),
	/**
	 * This is not a regular Java visibility. It is used to specify that the visibility of an option
	 * should be the same as the value declared in the {@link GenerateBuilder} annotation.
	 */
	INHERIT("!");
	
	private final String declPrefix;
	private Visibility(String declPrefix) {
		this.declPrefix = declPrefix;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return this.declPrefix;
	}

}