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
import java.util.Stack;

/**
 * Simple SPDX License expression parser.
 */
public class SpdxExpressionParser {
	// TODO Add rudimentary error recovery
	public SpdxExpression parse(String expression) {
		StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(expression));
		tokenizer.ordinaryChar('(');
		tokenizer.ordinaryChar(')');

		Stack<SpdxExpression> stack = new Stack<>();
		parse(tokenizer, stack);
		return stack.pop();
	}

	private boolean parse(StreamTokenizer tokenizer, Stack<SpdxExpression> stack) {
		try {
			while (true) {
				int token = tokenizer.nextToken();
				switch (token) {
				case StreamTokenizer.TT_EOF:
					return false;
				case StreamTokenizer.TT_WORD:
					String symbol = tokenizer.sval.toLowerCase();
					switch (symbol) {
					case "and": {
						boolean endGroup = parse(tokenizer, stack);
						stack.push(new SpdxConjunction(stack.pop(), stack.pop()));
						if (endGroup)
							return false;
						break;
					}
					case "or": {
						boolean endGroup = parse(tokenizer, stack);
						stack.push(new SpdxDisjunction(stack.pop(), stack.pop()));
						if (endGroup)
							return false;
						break;
					}
					case "with": {
						boolean endGroup = parse(tokenizer, stack);
						stack.push(new SpdxException(stack.pop(), stack.pop()));
						if (endGroup)
							return false;
						break;
					}
					default:
						// Assume that the token is an SPDX identifier
						// Note that we grab the original form from the tokenizer
						// and note the converted (lowercase) version that we used
						// in the switch.
						stack.push(new SpdxIdentifier(tokenizer.sval));
					}
					break;
				case '(':
					parse(tokenizer, stack);
					stack.push(new SpdxGroup(stack.pop()));
					break;
				case ')':
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
