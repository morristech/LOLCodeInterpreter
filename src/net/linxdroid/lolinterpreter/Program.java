/*
 * Java LOLCODE - LOLCODE parser and interpreter (http://lolcode.com/)
 * Copyright (C) 2007-2011  Brett Kail (bkail@iastate.edu)
 * http://bkail.public.iastate.edu/lolcode/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.linxdroid.lolinterpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Program {
	private boolean version1_1;
	private Block mainBlock;

	public Program(boolean version1_1, Block mainBlock) {
		this.version1_1 = version1_1;
		this.mainBlock = mainBlock;
	}

	public boolean isVersion1_1() {
		return version1_1;
	}

	public Block getMainBlock() {
		return mainBlock;
	}

	public static class Block {
		private int numVariables;
		private List<Statement> statements;

		public Block(int numVariables, List<Statement> statements) {
			this.numVariables = numVariables;
			this.statements = statements;
		}

		public int getNumVariables() {
			return numVariables;
		}

		public List<Statement> getStatements() {
			return statements;
		}
	}

	public static class Function extends Block {
		private int numArguments;

		public Function(int numVariables, List<Statement> statements, int numArguments) {
			super(numVariables, statements);
			this.numArguments = numArguments;
		}

		public int getNumArguments() {
			return numArguments;
		}
	}

	public interface StatementVisitor {
		// assignment
		public void visit(DeclareVariableStatement stmt);
		public void visit(DeclareSlotStatement stmt);
		public void visit(AssignItStatement stmt);
		public void visit(AssignVariableStatement stmt);
		public void visit(AssignGlobalVariableStatement stmt);
		public void visit(AssignSlotStatement stmt);
		public void visit(AssignInMahStatement stmt);

		// terminal-based
		public void visit(ByesStatement stmt);
		public void visit(VisibleStatement stmt);

		// flow control
		public void visit(ORlyStatement stmt);
		public void visit(WTFStatement stmt);
		public void visit(GTFOStatement stmt);
		public void visit(ImInYrStatement stmt);
		public void visit(FoundYrStatement stmt);

		// 1.3 bukkit
		public void visit(OHaiStatement stmt);

		// 1.3 exception
		public void visit(PlzStatement stmt);
		public void visit(RTFMStatement stmt);

		// 1.3 loop2
		public void visit(WhateverStatement stmt);
	}

	public interface ExpressionVisitor {
		// types
		public void visit(NoobExpression expr);
		public void visit(TroofExpression expr);
		public void visit(NumbrExpression expr);
		public void visit(NumbarExpression expr);
		public void visit(YarnExpression expr);
		public void visit(BukkitExpression expr);
		public void visit(FunctionExpression expr);

		// variable/function
		public void visit(ItExpression expr);
		public void visit(VariableExpression expr);
		public void visit(GlobalVariableExpression expr);
		public void visit(FunctionCallExpression expr);
		public void visit(ObjectExpression expr);
		public void visit(SlotExpression expr);
		public void visit(SlotFunctionCallExpression expr);

		// in mah
		public void visit(InMahExpression expr);
		public void visit(GetInMahBukkitExpression expr);
		public void visit(AssignInMahBukkitInMahExpression expr);

		// math
		public void visit(SumExpression expr);
		public void visit(DiffExpression expr);
		public void visit(ProduktExpression expr);
		public void visit(QuoshuntExpression expr);
		public void visit(ModExpression expr);
		public void visit(BiggrExpression expr);
		public void visit(SmallrExpression expr);

		// boolean
		public void visit(WonExpression expr);
		public void visit(NotExpression expr);
		public void visit(AllExpression expr);
		public void visit(AnyExpression expr);

		// comparison
		public void visit(BothSaemExpression expr);
		public void visit(DiffrintExpression expr);

		// concatenation
		public void visit(SmooshExpression expr);

		// casting
		public void visit(NoobCastExpression expr);
		public void visit(TroofCastExpression expr);
		public void visit(NumbrCastExpression expr);
		public void visit(NumbarCastExpression expr);
		public void visit(YarnCastExpression expr);

		// terminal-based
		public void visit(GimmehExpression expr);

		// 1.0/1.1 operators
		public void visit(MathNumbrExpression expr);
		public void visit(BigrThanExpression expr);
		public void visit(SmalrThanExpression expr);

		// 1.3 loop2
		public void visit(BukkitSlotsExpression expr);
		public void visit(HowBigIzExpression expr);

		// java
		public void visit(JavaExpression expr);
	}

	public interface Statement {
		public abstract void visit(StatementVisitor visitor);
	}

	public static class DeclareVariableStatement implements Statement {
		private int index;
		private Expression value;

		public DeclareVariableStatement(int index, Expression value) {
			this.index = index;
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public int getVariableIndex() {
			return index;
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class DeclareSlotStatement implements Statement {
		private Expression bukkit;
		private String name;
		private Expression value;

		public DeclareSlotStatement(Expression bukkit, String name, Expression value) {
			this.bukkit = bukkit;
			this.name = name;
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public String getName() {
			return name;
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class AssignItStatement implements Statement {
		private Expression value;

		public AssignItStatement(Expression value) {
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class AssignVariableStatement implements Statement {
		private int index;
		private Expression value;

		public AssignVariableStatement(int index, Expression value) {
			this.index = index;
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public int getVariableIndex() {
			return index;
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class AssignGlobalVariableStatement implements Statement {
		private int index;
		private Expression value;

		public AssignGlobalVariableStatement(int index, Expression value) {
			this.index = index;
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public int getVariableIndex() {
			return index;
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class AssignSlotStatement implements Statement {
		private Expression bukkit;
		private Expression index;
		private Expression value;

		public AssignSlotStatement(Expression bukkit, Expression index, Expression value) {
			this.bukkit = bukkit;
			this.index = index;
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public Expression getIndex() {
			return index;
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class AssignInMahStatement implements Statement {
		private Statement stmt;
		private Expression bukkit;
		private Expression index;
		private Expression value;

		public AssignInMahStatement(Statement stmt, Expression bukkit, Expression index, Expression value) {
			this.stmt = stmt;
			this.bukkit = bukkit;
			this.index = index;
			this.value = value;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Statement getAssignStatement() {
			return stmt;
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public Expression getIndex() {
			return index;
		}

		public Expression getValue() {
			return value;
		}
	}

	public static class ByesStatement implements Statement {
		private Expression exitCode;
		private Expression message;

		public ByesStatement(Expression exitCode, Expression message) {
			this.exitCode = exitCode;
			this.message = message;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getExitCode() {
			return exitCode;
		}

		public Expression getMessage() {
			return message;
		}
	}

	public static class VisibleStatement implements Statement {
		private boolean invisible;
		private List<Expression> exprs;
		private boolean suppressNewLine;

		public VisibleStatement(boolean invisible, List<Expression> exprs, boolean suppressNewLine) {
			this.invisible = invisible;
			this.exprs = exprs;
			this.suppressNewLine = suppressNewLine;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public boolean isInvisible() {
			return invisible;
		}

		public List<Expression> getExpressions() {
			return exprs;
		}

		public boolean isSuppressNewLine() {
			return suppressNewLine;
		}
	}

	public static class ORlyStatement implements Statement {
		private Expression expr;
		private List<Statement> yaRly;
		private List<Statement> noWai;

		public ORlyStatement(Expression expr, List<Statement> yaRly, List<Statement> noWai) {
			this.expr = expr;
			this.yaRly = yaRly;
			this.noWai = noWai;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getExpression() {
			return expr;
		}

		public List<Statement> getYaRly() {
			return yaRly;
		}

		public List<Statement> getNoWai() {
			return noWai;
		}
	}

	public static class WTFStatement implements Statement {
		private List<Statement> statements;
		private Map<Value, Integer> labels;
		private int omgWTFIndex;
		private Expression expr;

		public WTFStatement(List<Statement> statements, Map<Value, Integer> labels, int omgWTFIndex, Expression expr) {
			this.statements = statements;
			this.labels = labels;
			this.omgWTFIndex = omgWTFIndex;
			this.expr = expr;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public List<Statement> getStatements() {
			return statements;
		}

		public Map<Value, Integer> getLabels() {
			return labels;
		}

		public int getOMGWTFIndex() {
			return omgWTFIndex;
		}

		public Expression getExpression() {
			return expr;
		}
	}

	public static class GTFOStatement implements Statement {
		public static final Statement INSTANCE = new GTFOStatement(0);

		private int depth;

		private GTFOStatement() { }

		public GTFOStatement(int depth) {
			this.depth = depth;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public int getDepth() {
			return depth;
		}
	}

	public static class ImInYrStatement implements Statement {
		private List<Statement> stmts;
		private Expression variable;
		private boolean til;
		private Expression expr;

		public ImInYrStatement(List<Statement> stmts, Expression variable, boolean til, Expression expr) {
			this.stmts = stmts;
			this.variable = variable;
			this.til = til;
			this.expr = expr;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public List<Statement> getStatements() {
			return stmts;
		}

		public Expression getVariable() {
			return variable;
		}

		public boolean isTil() {
			return til;
		}

		public Expression getExpression() {
			return expr;
		}
	}

	public static class FoundYrStatement implements Statement {
		private Expression expr;

		public FoundYrStatement(Expression expr) {
			this.expr = expr;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getExpression() {
			return expr;
		}
	}

	public static class OHaiStatement implements Statement {
		private Expression expr;
		private List<Statement> stmts;

		public OHaiStatement(Expression expr, List<Statement> stmts) {
			this.expr = expr;
			this.stmts = stmts;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getExpression() {
			return expr;
		}

		public List<Statement> getStatements() {
			return stmts;
		}
	}

	public static class PlzStatement implements Statement {
		private List<Statement> stmts;
		private List<ONoes> oNoes;
		private List<Statement> awsumThx;

		public PlzStatement(List<Statement> stmts, List<ONoes> oNoes, List<Statement> awsumThx) {
			this.stmts = stmts;
			this.oNoes = oNoes;
			this.awsumThx = awsumThx;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public List<Statement> getStatements() {
			return stmts;
		}

		public List<ONoes> getONoes() {
			return oNoes;
		}

		public List<Statement> getAwsumThx() {
			return awsumThx;
		}

		public static class ONoes {
			private Expression expr;
			private List<Statement> stmts;

			public ONoes(Expression expr, List<Statement> stmts) {
				this.expr = expr;
				this.stmts = stmts;
			}

			public Expression getExpression() {
				return expr;
			}

			public List<Statement> getStatements() {
				return stmts;
			}
		}
	}

	public static class RTFMStatement implements Statement {
		private Expression expr;

		public RTFMStatement(Expression expr) {
			this.expr = expr;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getExpression() {
			return expr;
		}
	}

	public static class WhateverStatement implements Statement {
		private List<Statement> updateStmts;

		public WhateverStatement(List<Statement> updateStmts) {
			this.updateStmts = updateStmts;
		}

		public void visit(StatementVisitor visitor) {
			visitor.visit(this);
		}

		public List<Statement> getUpdateStatements() {
			return updateStmts;
		}
	}

	public interface Expression {
		void visit(ExpressionVisitor visitor);
	}

	public static abstract class UnaryExpression implements Expression {
		private Expression expression;

		public UnaryExpression(Expression expression) {
			this.expression = expression;
		}

		public Expression getExpression() {
			return expression;
		}
	}

	public static abstract class BinaryExpression implements Expression {
		private Expression left;
		private Expression right;

		public BinaryExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return super.toString() + '[' + left + ", " + right + ']';
		}

		public Expression getLeftExpression() {
			return left;
		}

		public Expression getRightExpression() {
			return right;
		}
	}

	public static abstract class InfiniteArityExpression implements Expression {
		private List<Expression> exprs;

		public InfiniteArityExpression(List<Expression> exprs) {
			this.exprs = exprs;
		}

		@Override
		public String toString() {
			return super.toString() + exprs;
		}

		public List<Expression> getExpressions() {
			return exprs;
		}
	}

	public static class NoobExpression implements Expression {
		public static final Expression INSTANCE = new NoobExpression();

		private NoobExpression() { }

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class TroofExpression implements Expression {
		public static final Expression FAIL = new TroofExpression(false);
		public static final Expression WIN = new TroofExpression(true);

		private boolean value;

		private TroofExpression(boolean value) {
			this.value = value;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public boolean getValue() {
			return value;
		}
	}

	public static class NumbrExpression implements Expression {
		private int value;

		public NumbrExpression(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return super.toString() + '[' + value + ']';
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public int getValue() {
			return value;
		}
	}

	public static class NumbarExpression implements Expression {
		private float value;

		public NumbarExpression(float value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return super.toString() + '[' + value + ']';
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public float getValue() {
			return value;
		}
	}

	public static class YarnExpression implements Expression {
		private String value;

		public YarnExpression(String value) {
			this.value = value;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public String getValue() {
			return value;
		}
	}

	public static class BukkitExpression implements Expression {
		public static Expression INSTANCE = new BukkitExpression();

		private Expression liek;

		private BukkitExpression() { }

		public BukkitExpression(Expression liek) {
			this.liek = liek;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getLiek() {
			return liek;
		}
	}

	public static class FunctionExpression implements Expression {
		private Function function;

		public FunctionExpression(Function function) {
			this.function = function;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Function getFunction() {
			return function;
		}
	}

	public static class ItExpression implements Expression {
		public static Expression INSTANCE = new ItExpression();

		private ItExpression() { }

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class VariableExpression implements Expression {
		private int index;

		public VariableExpression(int index) {
			this.index = index;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public int getIndex() {
			return index;
		}
	}

	public static class GlobalVariableExpression implements Expression {
		private int index;

		public GlobalVariableExpression(int index) {
			this.index = index;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public int getIndex() {
			return index;
		}
	}

	public static class FunctionCallExpression implements Expression {
		private Function function;
		private Expression[] arguments;

		public FunctionCallExpression(Function function, Expression[] arguments) {
			this.function = function;
			this.arguments = arguments;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Function getFunction() {
			return function;
		}

		public Expression[] getArguments() {
			return arguments;
		}
	}

	public static class ObjectExpression implements Expression {
		public static Expression THIS = new ObjectExpression(false);
		public static Expression OUTER = new ObjectExpression(true);

		private boolean outer;

		private ObjectExpression(boolean outer) {
			this.outer = outer;
		}

		@Override
		public String toString() {
			return super.toString() + (outer ? "[OUTER]" : "[THIS]");
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public boolean isOuter() {
			return outer;
		}
	}

	public static class SlotExpression implements Expression {
		private Expression bukkit;
		private Expression index;

		public SlotExpression(Expression bukkit, Expression index) {
			this.bukkit = bukkit;
			this.index = index;
		}

		@Override
		public String toString() {
			return super.toString() + '[' + bukkit + ", " + index + ']';
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public Expression getIndex() {
			return index;
		}
	}

	public static class SlotFunctionCallExpression implements Expression {
		private Expression bukkit;
		private Expression index;
		private List<Expression> arguments;

		public SlotFunctionCallExpression(Expression bukkit, Expression index, List<Expression> arguments) {
			this.bukkit = bukkit;
			this.index = index;
			this.arguments = arguments;
		}

		@Override
		public String toString() {
			return super.toString() + '[' + bukkit + ", " + index + ", " + arguments + ']';
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public Expression getIndex() {
			return index;
		}

		public List<Expression> getArguments() {
			return arguments;
		}
	}

	public static class InMahExpression implements Expression {
		private Expression bukkit;
		private Expression index;

		public InMahExpression(Expression bukkit, Expression index) {
			this.bukkit = bukkit;
			this.index = index;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public Expression getIndex() {
			return index;
		}
	}

	public static class GetInMahBukkitExpression extends UnaryExpression {
		public GetInMahBukkitExpression(Expression expr) {
			super(expr);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class AssignInMahBukkitInMahExpression implements Expression {
		private Expression bukkit;
		private Expression index;

		public AssignInMahBukkitInMahExpression(Expression bukkit, Expression index) {
			this.bukkit = bukkit;
			this.index = index;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public Expression getBukkit() {
			return bukkit;
		}

		public Expression getIndex() {
			return index;
		}
	}

	public static class SumExpression extends BinaryExpression {
		public SumExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class DiffExpression extends BinaryExpression {
		public DiffExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class ProduktExpression extends BinaryExpression {
		public ProduktExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class QuoshuntExpression extends BinaryExpression {
		public QuoshuntExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class ModExpression extends BinaryExpression {
		public ModExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class BiggrExpression extends BinaryExpression {
		public BiggrExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class SmallrExpression extends BinaryExpression {
		public SmallrExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class WonExpression extends BinaryExpression {
		public WonExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NotExpression extends UnaryExpression {
		public NotExpression(Expression expr) {
			super(expr);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class AllExpression extends InfiniteArityExpression {
		public AllExpression(List<Expression> exprs) {
			super(exprs);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class AnyExpression extends InfiniteArityExpression {
		public AnyExpression(List<Expression> exprs) {
			super(exprs);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class BothSaemExpression extends BinaryExpression {
		public BothSaemExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class DiffrintExpression extends BinaryExpression {
		public DiffrintExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class SmooshExpression extends InfiniteArityExpression {
		public SmooshExpression(List<Expression> exprs) {
			super(exprs);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NoobCastExpression extends UnaryExpression {
		public NoobCastExpression(Expression expression) {
			super(expression);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class TroofCastExpression extends UnaryExpression {
		public TroofCastExpression(Expression expression) {
			super(expression);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NumbrCastExpression extends UnaryExpression {
		public NumbrCastExpression(Expression expression) {
			super(expression);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class NumbarCastExpression extends UnaryExpression {
		public NumbarCastExpression(Expression expression) {
			super(expression);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class YarnCastExpression extends UnaryExpression {
		public YarnCastExpression(Expression expression) {
			super(expression);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class GimmehExpression implements Expression {
		public static final int LINE = 0;
		public static final int WORD = 1;
		public static final int LETTAR = 2;

		private int what;

		public GimmehExpression(int what) {
			this.what = what;
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}

		public int getWhat() {
			return what;
		}
	}

	public static class MathNumbrExpression extends UnaryExpression {
		public MathNumbrExpression(Expression expr) {
			super(expr);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class BigrThanExpression extends BinaryExpression {
		public BigrThanExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class SmalrThanExpression extends BinaryExpression {
		public SmalrThanExpression(Expression left, Expression right) {
			super(left, right);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class BukkitSlotsExpression extends UnaryExpression {
		public BukkitSlotsExpression(Expression expr) {
			super(expr);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class HowBigIzExpression extends UnaryExpression {
		public HowBigIzExpression(Expression expr) {
			super(expr);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}

	public static class JavaExpression extends UnaryExpression {
		public JavaExpression(Expression expr) {
			super(expr);
		}

		public void visit(ExpressionVisitor visitor) {
			visitor.visit(this);
		}
	}
}
