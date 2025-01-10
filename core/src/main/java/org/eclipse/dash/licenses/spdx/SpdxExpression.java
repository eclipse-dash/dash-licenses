/*************************************************************************
 * Copyright (c) 2020,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.spdx;

import java.util.Collection;
import java.util.function.Function;

public abstract class SpdxExpression {

	public abstract boolean matchesApproved(Collection<String> approved);

	public boolean isBinary() {
		return false;
	}

	public boolean isValid() {
		return true;
	}

	public boolean isIdentifier() {
		return false;
	}

	public SpdxExpression and(SpdxExpression expression) {
		if (this.equals(expression))
			return this;
		return SpdxBinaryOperation.create(SpdxBinaryOperation.AND, this, expression.asGroup());
	}

	/**
	 * Express the receiver as a group. We do this when joining two expressions to
	 * retain the meaning and integrity of the individual statements. The default
	 * case is that the receiver is a group. Subclasses that require actual grouping
	 * override.
	 * 
	 * @return
	 */
	protected SpdxExpression asGroup() {
		return this;
	}

	public String toPrecedenceString() {
		return toString();
	}

	public String toSpdxLinkedString() {
		return toAnnotatedString(
				identifier -> "<a href=\"https://spdx.org/licenses/" + identifier + ".html\">" + identifier + "</a>");
	}

	/**
	 * Renders the receiver with the SPDX license identifiers annotated as specified
	 * by a function. This was added to make it possible to render expressions with
	 * links to the identifiers embedded within the expression.
	 * 
	 * <p>
	 * For example, to render an expression as Markdown with links to the license
	 * pages on the SPDX website:
	 * 
	 * <pre>
	 * expression.toAnnotatedString(
	 * 	identifier -> "[" + identifier + "](https://spdx.org/licenses/" + identifier + ".html)");
	 * 
	 * >>> [EPL-2.0](https://spdx.org/licenses/EPL-2.0.html)
	 * </pre>
	 * 
	 * or to bold just the license identifiers in Markdown (or AsciiDoc):
	 * 
	 * <pre>
	 * expression.toAnnotatedString(
	 * 	identifier -> "**" + identifier + "**");
	 * 
	 * >>> **EPL-2.0** OR **Apache-2.0**
	 * </pre>
	 * 
	 * @param annotator a function that takes an SPDX Identifier {@link String}
	 *                  (e.g., <code>EPL-2.0</code>) and answers a {@link String}
	 *                  rendering that identifier for the output.
	 * 
	 * @return A {@link String} containing the SPDX expression with the identifiers
	 *         annotated as described by the function.
	 */
	public String toAnnotatedString(Function<String, String> annotator) {
		return toString();
	}

	public SpdxExpression collapse() {
		return this;
	}

	public boolean contains(SpdxExpression left) {
		return false;
	}

	public boolean isGroup() {
		return false;
	}

	public SpdxExpression simplified() {
		return this;
	}
}
