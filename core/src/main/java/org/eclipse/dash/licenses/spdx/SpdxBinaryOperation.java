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

import java.util.Collection;
import java.util.function.Function;

public class SpdxBinaryOperation extends SpdxExpression {

	private interface Operator {

		int getPrecedence();

		boolean matchesApproved(SpdxBinaryOperation spdxBinaryOperation, Collection<String> approved);

		boolean checkEqual(SpdxBinaryOperation left, SpdxBinaryOperation right);

	}

	public static Operator AND = new Operator() {

		@Override
		public int getPrecedence() {
			return 2;
		}

		@Override
		public boolean matchesApproved(SpdxBinaryOperation operation, Collection<String> approved) {
			return operation.left.matchesApproved(approved) && operation.right.matchesApproved(approved);
		}

		@Override
		public String toString() {
			return "AND";
		}

		@Override
		public boolean checkEqual(SpdxBinaryOperation left, SpdxBinaryOperation right) {
			if (left.operator != right.operator)
				return false;

			// Order is not important.
			if (left.left.equals(right.left) && left.right.equals(right.right))
				return true;
			if (left.left.equals(right.right) && left.right.equals(right.left))
				return true;

			return false;
		};
	};

	public static Operator OR = new Operator() {

		@Override
		public int getPrecedence() {
			return 1;
		}

		@Override
		public boolean matchesApproved(SpdxBinaryOperation operation, Collection<String> approved) {
			return operation.left.matchesApproved(approved) || operation.right.matchesApproved(approved);
		}

		@Override
		public String toString() {
			return "OR";
		}

		@Override
		public boolean checkEqual(SpdxBinaryOperation left, SpdxBinaryOperation right) {
			if (left.operator != right.operator)
				return false;

			// Order is not important.
			if (left.left.equals(right.left) && left.right.equals(right.right))
				return true;
			if (left.left.equals(right.right) && left.right.equals(right.left))
				return true;

			return false;
		};
	};

	public static Operator WITH = new Operator() {

		@Override
		public int getPrecedence() {
			return 3;
		}

		@Override
		public boolean matchesApproved(SpdxBinaryOperation operation, Collection<String> approved) {
			// TODO Implement this.
			return false;
		}

		@Override
		public String toString() {
			return "WITH";
		}

		@Override
		public boolean checkEqual(SpdxBinaryOperation left, SpdxBinaryOperation right) {
			if (left.operator != right.operator)
				return false;

			// Order is important for a WITH expression.
			if (left.left.equals(right.left) && left.right.equals(right.right))
				return true;

			return false;
		};
	};

	private Operator operator;
	private SpdxExpression left;
	private SpdxExpression right;

	private SpdxBinaryOperation(Operator operator, SpdxExpression left, SpdxExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	/**
	 * Create an instance by joining the left side of an expression with the right
	 * side of an expression using an operator. If the right side is a binary
	 * expression, then we compare the precedence of the operator of that expression
	 * with the operator that we're given. If the given operator has higher
	 * precedence, then we rework the relationship so that the higher precedence
	 * operator is given precedence.
	 * <p>
	 * For example, if we need to join "X" to "Y OR Z" with "AND", we would naively
	 * end up with "X AND (Y OR Z)" which violates the precedence rules. So, we
	 * break apart the right expression so that it instead manifests as "(X AND Y)
	 * OR Z".
	 * <p>
	 * The parser is depth first and greedy, so we only need to check the right
	 * side.
	 *
	 * @param operator AND, OR, or WITH
	 * @param left     the left side of the expression
	 * @param right    the right side of the expression
	 * @return an instance representing the left and right expressions joined with
	 *         the operator, or some variant thereof based on precedence.
	 */
	public static SpdxExpression create(Operator operator, SpdxExpression left, SpdxExpression right) {
		if (left == null || right == null)
			return new SpdxInvalidExpression();

		if (left == SpdxNone.INSTANCE)
			return right;
		if (right == SpdxNone.INSTANCE)
			return left;

		if (right.isBinary()) {
			return create(operator, left, (SpdxBinaryOperation) right);
		}
		return new SpdxBinaryOperation(operator, left, right);
	}

	static SpdxExpression create(Operator operator, SpdxExpression left, SpdxBinaryOperation right) {
		if (operator.getPrecedence() >= right.operator.getPrecedence()) {
			SpdxExpression primary = create(operator, left, right.left);
			return create(right.operator, primary, right.right);
		}
		return new SpdxBinaryOperation(operator, left, right);
	}

	@Override
	public boolean isBinary() {
		return true;
	}

	@Override
	public boolean isValid() {
		return left.isValid() && right.isValid();
	}

	@Override
	public String toString() {
		return left.toString() + " " + operator.toString() + " " + right.toString();
	}

	@Override
	public String toPrecedenceString() {
		return "(" + left.toPrecedenceString() + " " + operator.toString() + " " + right.toPrecedenceString() + ")";
	}

	@Override
	public String toAnnotatedString(Function<String, String> annotator) {
		return left.toAnnotatedString(annotator) + " " + operator.toString() + " " + right.toAnnotatedString(annotator);
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		return operator.matchesApproved(this, approved);
	}

	@Override
	protected SpdxExpression asGroup() {
		return new SpdxGroup(this);
	}

	@Override
	public SpdxExpression collapse() {
		SpdxExpression left = this.left.collapse();
		SpdxExpression right = this.right.collapse();

		if (right.contains(left))
			return right;
		if (left.contains(right))
			return left;

		if (left.equals(right))
			return left;

		return new SpdxBinaryOperation(operator, left, right);
	}

	@Override
	public boolean contains(SpdxExpression value) {
		return left.contains(value) || right.contains(value);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof SpdxExpression) {
			// Do a little double dispatching...
			return ((SpdxExpression) object).equalsBinaryOperation(this);
		}
		return false;
	}

	@Override
	boolean equalsBinaryOperation(SpdxBinaryOperation operand) {
		return this.operator.checkEqual(this, operand);
	}

	@Override
	public int hashCode() {
		return this.operator.hashCode() & this.left.hashCode() & this.right.hashCode();
	}
}
