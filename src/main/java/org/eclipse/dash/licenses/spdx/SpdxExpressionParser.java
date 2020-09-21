/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.spdx;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * Simple SPDX License expression parser. This is a very simple hand-written
 * parser that supports the SPDX expression syntax.
 */
public class SpdxExpressionParser {
	public SpdxExpression parse(String expression) {
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(expression));

		tokenizer.resetSyntax();

		tokenizer.wordChars('a', 'z');
		tokenizer.wordChars('A', 'Z');
		tokenizer.wordChars(128 + 32, 255);
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('.', '.');
		tokenizer.whitespaceChars(0, ' ');

		tokenizer.ordinaryChar('(');
		tokenizer.ordinaryChar(')');
		tokenizer.ordinaryChar('+');

		return parse(tokenizer, null);
	}

	private SpdxExpression parse(StreamTokenizer tokenizer, SpdxExpression expression) {
		try {
			int token = tokenizer.nextToken();
			switch (token) {
			case StreamTokenizer.TT_WORD:
				String symbol = tokenizer.sval.toLowerCase();
				switch (symbol) {
				case "and": {
					SpdxExpression right = parse(tokenizer, null);
					return parse(tokenizer, SpdxBinaryOperation.create(SpdxBinaryOperation.AND, expression, right));
				}
				case "or": {
					SpdxExpression right = parse(tokenizer, null);
					return parse(tokenizer, SpdxBinaryOperation.create(SpdxBinaryOperation.OR, expression, right));
				}
				case "with": {
					SpdxExpression right = parse(tokenizer, null);
					return parse(tokenizer, SpdxBinaryOperation.create(SpdxBinaryOperation.WITH, expression, right));
				}
				default:
					// Assume that the token is an SPDX identifier.

					// The expression should be empty when we find an
					// identifier; we should be at the beginning of either
					// the entire expression, or of an operand for a binary
					// expression. If we've previously read anything, then
					// the expression is invalid.
					if (expression != null)
						return new SpdxInvalidExpression();

					// Note that we grab the original form from the tokenizer
					// and note the converted (lowercase) version that we used
					// in the switch.
					return parse(tokenizer, new SpdxIdentifier(tokenizer.sval));
				}
			case '+':
				// We must have previously read an identifier when we encounter
				// a plus. If we've seen nothing or something other than an identifer,
				// then the expression is invalid.
				if (expression == null || !expression.isIdentifier())
					return new SpdxInvalidExpression();

				return parse(tokenizer, SpdxPlus.create((SpdxIdentifier) expression));
			case '(':
				SpdxGroup group = new SpdxGroup(parse(tokenizer, expression));

				// If we do not encounter a closing parenthesis after
				// unwinding the recursion, then the expression is invalid.
				if (tokenizer.nextToken() != ')')
					return new SpdxInvalidExpression();

				return parse(tokenizer, group);
			case ')':
				// Put the token back as we collapse the recursion. When
				// we get back to the frame that started this parenthetical,
				// it will re-consume the the token.
				tokenizer.pushBack();
				return expression;
			case StreamTokenizer.TT_EOF:
				return expression;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
