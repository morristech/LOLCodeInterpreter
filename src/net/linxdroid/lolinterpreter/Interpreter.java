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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class Interpreter implements CompiledProgram, Program.StatementVisitor, Program.ExpressionVisitor {
	private static int EXIT_BYES = 0;
	private static int EXIT_GTFO = 1;
	private static int EXIT_FOUND_YR = 2;
	private static int EXIT_WHATEVER = 3;

	private Program program;
	private Environment environment;
	private InMahBukkitFactory inMahBukkitFactory;

	private Value value;
	private Scope globalScope;
	private Scope scope;
	private int exit;
	private int exitDepth;
	private int exitCode;
	Cmd cmd;

	public Interpreter(Program program, Environment environment, Cmd cmdarg) {
		cmd = cmdarg;
		this.program = program;
		this.environment = environment;
		this.inMahBukkitFactory = program.isVersion1_1() ? InMahBukkitFactory.VERSION_1_1_FACTORY : InMahBukkitFactory.VERSION_1_0_FACTORY;
	}

	public Value execute() {
		Program.Block block = program.getMainBlock();

		globalScope = scope = new Scope(null, block);
		for (Program.Statement stmt : block.getStatements()) {
			stmt.visit(this);
			if (exit != 0) {
				return null;
			}
		}

		Value result = scope.getIt();
		scope = scope.getOuterScope();

		return result;
	}

	public int getExitCode() {
		return exitCode;
	}

	private Value evaluate(Program.Expression expr) {
		expr.visit(this);
		return value;
	}

	public void visit(Program.DeclareVariableStatement stmt) {
		scope.declareVariable(stmt.getVariableIndex(), evaluate(stmt.getValue()));
	}

	public void visit(Program.DeclareSlotStatement stmt) {
		evaluate(stmt.getBukkit()).declareSlot(new YarnValue(stmt.getName()), evaluate(stmt.getValue()));
	}

	public void visit(Program.AssignItStatement stmt) {
		scope.setIt(evaluate(stmt.getValue()));
	}

	public void visit(Program.AssignVariableStatement stmt) {
		scope.setVariable(stmt.getVariableIndex(), evaluate(stmt.getValue()));
	}

	public void visit(Program.AssignGlobalVariableStatement stmt) {
		globalScope.setVariable(stmt.getVariableIndex(), evaluate(stmt.getValue()));
	}

	public void visit(Program.AssignSlotStatement stmt) {
		evaluate(stmt.getBukkit()).setSlot(evaluate(stmt.getIndex()), evaluate(stmt.getValue()));
	}

	public void visit(Program.AssignInMahStatement stmt) {
		stmt.getAssignStatement().visit(this);
		((InMahBukkit)evaluate(stmt.getBukkit())).assignInMah(evaluate(stmt.getIndex()), evaluate(stmt.getValue()));
	}

	public void visit(Program.ByesStatement stmt) {
		Program.Expression message = stmt.getMessage();
		if (message != null) {
			PrintWriter err = environment.getErr();
			err.println(evaluate(message).getString());
			err.flush();
		}

		exitCode = evaluate(stmt.getExitCode()).getInt();
		environment.exit(exitCode);
		exit = EXIT_BYES;
	}

	public void visit(Program.VisibleStatement stmt) {
		PrintWriter out = stmt.isInvisible() ? environment.getErr() : environment.getOut();

		for (Program.Expression expr : stmt.getExpressions()) {
			cmd.stdOut.print(evaluate(expr).getString());
		}

		if (!stmt.isSuppressNewLine()) {
			cmd.stdOut.println();
		}
		out.flush();
	}

	public void visit(Program.ORlyStatement stmt) {
		for (Program.Statement child : evaluate(stmt.getExpression()).getBoolean() ? stmt.getYaRly() : stmt.getNoWai()) {
			child.visit(this);
			if (exit != 0) {
				return;
			}
		}
	}

	public void visit(Program.WTFStatement stmt) {
		int index;

		Integer indexValue = stmt.getLabels().get(evaluate(stmt.getExpression()));
		if (indexValue != null) {
			index = indexValue.intValue();
		} else {
			index = stmt.getOMGWTFIndex() & 0x7fffffff;
		}

		List<Program.Statement> stmts = stmt.getStatements();
		for (int size = stmts.size(); index < size; index++) {
			stmts.get(index).visit(this);
			if (exit != 0) {
				if (exit == EXIT_GTFO && exitDepth-- == 0) {
					exit = 0;
				}
				return;
			}
		}
	}

	public void visit(Program.GTFOStatement stmt) {
		exit = EXIT_GTFO;
		exitDepth = stmt.getDepth();
	}

	private boolean evaluateImInYrCondition(Program.ImInYrStatement stmt) {
		Value exprValue = evaluate(stmt.getExpression());
		boolean result;

		Program.Expression variable = stmt.getVariable();
		if (variable == null) {
			result = exprValue.getBoolean();
		} else {
			if (exprValue.isTroof()) {
				result = exprValue.getBoolean();
			} else {
				result = evaluate(variable).equals(exprValue);
			}
		}

		return result != stmt.isTil();
	}

	public void visit(Program.ImInYrStatement stmt) {
		List<Program.Statement> stmts = stmt.getStatements();

		while (evaluateImInYrCondition(stmt)) {
			for (Program.Statement child : stmts) {
				child.visit(this);
				if (exit != 0) {
					if (exit == EXIT_WHATEVER) {
						exit = 0;
						break;
					} else {
						if (exit == EXIT_GTFO && exitDepth-- == 0) {
							exit = 0;
						}
						return;
					}
				}
			}
		} 
	}

	public void visit(Program.FoundYrStatement stmt) {
		value = evaluate(stmt.getExpression());
		exit = EXIT_FOUND_YR;
	}

	public void visit(Program.OHaiStatement stmt) {
		Value save = scope.pushObject(evaluate(stmt.getExpression()));
		try {
			for (Program.Statement child : stmt.getStatements()) {
				child.visit(this);
				if (exit != 0) {
					return;
				}
			}
		} finally {
			scope.popObject(save);
		}
	}

	public void visit(Program.PlzStatement stmt) {
		try {
			for (Program.Statement child : stmt.getStatements()) {
				child.visit(this);
				if (exit != 0) {
					return;
				}
			}
		} catch (RuntimeException ex) {
			String type;
			if (ex instanceof LOLCodeException) {
				type = ((LOLCodeException)ex).getType();
			} else {
				type = ex.getClass().getName();
			}

			scope.setIt(new YarnValue(type));

			for (Program.PlzStatement.ONoes oNoes : stmt.getONoes()) {
				if (evaluate(oNoes.getExpression()).getBoolean()) {
					for (Program.Statement child : oNoes.getStatements()) {
						child.visit(this);
						if (exit != 0) {
							return;
						}
					}

					return;
				}
			}

			throw ex;
		} finally {
			for (Program.Statement child : stmt.getAwsumThx()) {
				child.visit(this);
				if (exit != 0) {
					return;
				}
			}
		}
	}

	public void visit(Program.RTFMStatement stmt) {
		throw new LOLCodeException(evaluate(stmt.getExpression()).getString());
	}

	public void visit(Program.WhateverStatement stmt) {
		for (Program.Statement child : stmt.getUpdateStatements()) {
			child.visit(this);
		}

		exit = EXIT_WHATEVER;
	}

	public void visit(Program.NoobExpression expr) {
		value = NoobValue.INSTANCE;
	}

	public void visit(Program.TroofExpression expr) {
		value = TroofValue.getInstance(expr.getValue());
	}

	public void visit(Program.NumbrExpression expr) {
		value = new NumbrValue(expr.getValue());
	}

	public void visit(Program.NumbarExpression expr) {
		value = new NumbarValue(expr.getValue());
	}

	public void visit(Program.YarnExpression expr) {
		value = new YarnValue(expr.getValue());
	}

	public void visit(Program.BukkitExpression expr) {
		Program.Expression liekExpr = expr.getLiek();

		if (liekExpr == null) {
			value = new BukkitValue();
		} else {
			Value liek = evaluate(liekExpr);

			if (!(liek instanceof BukkitValue)) {
				throw new LOLCodeException(LOLCodeException.BAD_LIEK_TYPE, liek.getType());
			}

			value = new BukkitValue((BukkitValue)liek);
		}
	}

	public void visit(Program.FunctionExpression expr) {
		value = new FunctionValue(new FunctionImpl(expr.getFunction()));
	}

	public void visit(Program.ItExpression expr) {
		value = scope.getIt();
	}

	public void visit(Program.VariableExpression expr) {
		value = scope.getVariable(expr.getIndex());
	}

	public void visit(Program.GlobalVariableExpression expr) {
		value = globalScope.getVariable(expr.getIndex());
	}

	private void callFunction(Program.Function function, Scope newScope) {
		scope = newScope;

		try {
			for (Program.Statement stmt : function.getStatements()) {
				stmt.visit(this);
				if (exit != 0) {
					if (exit == EXIT_FOUND_YR) {
						exit = 0;
					} else if (exit == EXIT_GTFO) {
						value = NoobValue.INSTANCE;
						exit = 0;
					}
					return;
				}
			}

			value = newScope.getIt();
		} finally {
			scope = scope.getOuterScope();
		}
	}

	Value callFunction(Program.Function function, Value target, List<Value> arguments) {
		int numArguments = function.getNumArguments();
		if (numArguments != arguments.size()) {
			throw new LOLCodeException(LOLCodeException.BAD_ARGUMENT_COUNT, "Received: " + arguments.size() + ", Expected: " + numArguments);
		}

		Scope newScope = new Scope(scope, function);
		newScope.pushObject(target);

		for (int i = 0; i < numArguments; i++) {
			newScope.declareVariable(0, arguments.get(i));
		}

		callFunction(function, newScope);
		return value;
	}

	public void visit(Program.FunctionCallExpression expr) {
		Program.Function function = expr.getFunction();
		Scope newScope = new Scope(scope, function);

		Program.Expression[] arguments = expr.getArguments();
		for (int i = 0; i < arguments.length; i++) {
			newScope.declareVariable(i, evaluate(arguments[i]));
		}

		callFunction(function, newScope);
	}

	public void visit(Program.ObjectExpression expr) {
		if (expr.isOuter()) {
			value = scope.getOuterObject();
		} else {
			value = scope.getObject();
		}
	}

	public void visit(Program.SlotExpression expr) {
		Value bukkit = evaluate(expr.getBukkit());
		value = bukkit.getSlot(evaluate(expr.getIndex())).call(bukkit);
	}

	public void visit(Program.SlotFunctionCallExpression expr) {
		Value bukkit = evaluate(expr.getBukkit());
		Value slot = bukkit.getSlot(evaluate(expr.getIndex()));

		List<Program.Expression> argumentExprs = expr.getArguments();
		int numArguments = argumentExprs.size();
		List<Value> arguments = new ArrayList<Value>(numArguments);

		for (int i = 0; i < numArguments; i++) {
			arguments.add(evaluate(argumentExprs.get(i)));
		}

		value = slot.call(bukkit, arguments);
	}

	public void visit(Program.InMahExpression expr) {
		value = evaluate(expr.getBukkit()).inMah(evaluate(expr.getIndex()));
	}

	public void visit(Program.GetInMahBukkitExpression expr) {
		value = evaluate(expr.getExpression()).getInMahBukkit(inMahBukkitFactory);
	}

	public void visit(Program.AssignInMahBukkitInMahExpression expr) {
		value = ((InMahBukkit)evaluate(expr.getBukkit())).assignInMahBukkitInMah(evaluate(expr.getIndex()), inMahBukkitFactory);
	}

	public void visit(Program.SumExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(left.getFloat() + right.getFloat());
		} else {
			value = new NumbrValue(left.getInt() + right.getInt());
		}
	}

	public void visit(Program.DiffExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(left.getFloat() - right.getFloat());
		} else {
			value = new NumbrValue(left.getInt() - right.getInt());
		}
	}

	public void visit(Program.ProduktExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());


		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(left.getFloat() * right.getFloat());
		} else {
			value = new NumbrValue(left.getInt() * right.getInt());
		}
	}

	public void visit(Program.QuoshuntExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(left.getFloat() / right.getFloat());
		} else {
			try {
				value = new NumbrValue(left.getInt() / right.getInt());
			} catch (ArithmeticException ex) {
				throw new LOLCodeException(LOLCodeException.BAD_QUOSHUNT, ex);
			}
		}
	}

	public void visit(Program.ModExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(left.getFloat() % right.getFloat());
		} else {
			try {
				value = new NumbrValue(left.getInt() % right.getInt());
			} catch (ArithmeticException ex) {
				throw new LOLCodeException(LOLCodeException.BAD_MOD, ex);
			}
		}
	}

	public void visit(Program.BiggrExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(Math.max(left.getFloat(), right.getFloat()));
		} else {
			value = new NumbrValue(Math.max(left.getInt(), right.getInt()));
		}
	}

	public void visit(Program.SmallrExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		if (left.isMathNumbar() | right.isMathNumbar()) {
			value = new NumbarValue(Math.min(left.getFloat(), right.getFloat()));
		} else {
			value = new NumbrValue(Math.min(left.getInt(), right.getInt()));
		}
	}

	public void visit(Program.WonExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		value = TroofValue.getInstance(left.getBoolean() ^ right.getBoolean());
	}

	public void visit(Program.NotExpression expr) {
		value = TroofValue.getInstance(!evaluate(expr.getExpression()).getBoolean());
	}

	public void visit(Program.AllExpression expr) {
		boolean result = true;

		for (Program.Expression arg : expr.getExpressions()) {
			result &= evaluate(arg).getBoolean();
		}

		value = TroofValue.getInstance(result);
	}

	public void visit(Program.AnyExpression expr) {
		boolean result = false;

		for (Program.Expression arg : expr.getExpressions()) {
			result |= evaluate(arg).getBoolean();
		}

		value = TroofValue.getInstance(result);
	}

	public void visit(Program.BothSaemExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		value = TroofValue.getInstance(left.equals(right));
	}

	public void visit(Program.DiffrintExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());

		value = TroofValue.getInstance(!left.equals(right));
	}

	public void visit(Program.SmooshExpression expr) {
		StringBuilder builder = new StringBuilder();

		for (Program.Expression arg : expr.getExpressions()) {
			builder.append(evaluate(arg).getString());
		}

		value = new YarnValue(builder.toString());
	}

	public void visit(Program.NoobCastExpression expr) {
		evaluate(expr.getExpression());
		value = NoobValue.INSTANCE;
	}

	public void visit(Program.TroofCastExpression expr) {
		value = evaluate(expr.getExpression()).castToTroof();
	}

	public void visit(Program.NumbrCastExpression expr) {
		value = evaluate(expr.getExpression()).castToNumbr();
	}

	public void visit(Program.NumbarCastExpression expr) {
		value = evaluate(expr.getExpression()).castToNumbar();
	}

	public void visit(Program.YarnCastExpression expr) {
		value = evaluate(expr.getExpression()).castToYarn();
	}

	public void visit(Program.GimmehExpression expr) {
		try {
			what: switch (expr.getWhat()) {
				case Program.GimmehExpression.LINE: {
					String string = cmd.readln();
					if (string == null) {
						value = NoobValue.INSTANCE;
					} else {
						value = new YarnValue(string);
					}
					break;
				}

				case Program.GimmehExpression.WORD: {
					BufferedReader reader = environment.getIn();
					StringBuilder builder = new StringBuilder();

					int ch;
					for (;;) {
						if ((ch = reader.read()) == -1) {
							value = NoobValue.INSTANCE;
							break what;
						}

						if (!Character.isWhitespace(ch)) {
							builder.append((char)ch);
							break;
						}
					}

					for (;;) {
						reader.mark(1);

						if ((ch = reader.read()) == -1) {
							break;
						}

						if (Character.isWhitespace(ch)) {
							reader.reset();
							break;
						}

						builder.append((char)ch);
					}

					value = new YarnValue(builder.toString());
					break;
				}

				case Program.GimmehExpression.LETTAR: {
					int ch = Integer.parseInt(cmd.readln());
					if (ch == -1) {
						value = NoobValue.INSTANCE;
					} else {
						value = new YarnValue(String.valueOf((char)ch));
					}
					break;
				}

				default:
					throw new IllegalStateException();
			}
		} catch (IOException ex) {
			throw new LOLCodeException(LOLCodeException.BAD_IO, ex);
		}
	}

	public void visit(Program.MathNumbrExpression expr) {
		value = evaluate(expr.getExpression()).castToMathNumbr();
	}

	public void visit(Program.BigrThanExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());
		value = TroofValue.getInstance(left.castToMathNumbr().getInt() > right.castToMathNumbr().getInt());
	}

	public void visit(Program.SmalrThanExpression expr) {
		Value left = evaluate(expr.getLeftExpression());
		Value right = evaluate(expr.getRightExpression());
		value = TroofValue.getInstance(left.castToMathNumbr().getInt() < right.castToMathNumbr().getInt());
	}

	public void visit(Program.BukkitSlotsExpression expr) {
		value = evaluate(expr.getExpression()).getSlots();
	}

	public void visit(Program.HowBigIzExpression expr) {
		value = new NumbrValue(evaluate(expr.getExpression()).getNumSlots());
	}

	public void visit(Program.JavaExpression expr) {
		String name = evaluate(expr.getExpression()).getString();
		Class klass;

		try {
			klass = Class.forName(name);
		} catch (ClassNotFoundException ex) {
			throw new LOLCodeException(LOLCodeException.BAD_JAVA_CLASS, ex);
		}

		value = JavaValue.create(klass);
	}

	private static class Scope {
		private Scope outer;
		private Value it = NoobValue.INSTANCE;
		private Value object;
		private Value outerObject;
		private Value[] variables;

		public Scope(Scope outer, Program.Block block) {
			this.outer = outer;

			int numVariables = block.getNumVariables();
			this.variables = new Value[numVariables];
		}

		public Scope getOuterScope() {
			return outer;
		}

		public void setIt(Value value) {
			this.it = value;
		}

		public Value getIt() {
			return it;
		}

		public Value pushObject(Value value) {
			Value save = outerObject;
			outerObject = object;
			object = value;
			return save;
		}

		public void popObject(Value value) {
			object = outerObject;
			outerObject = value;
		}

		public Value getObject() {
			return object;
		}

		public Value getOuterObject() {
			return outerObject;
		}

		public void declareVariable(int index, Value value) {
			variables[index] = variables[index] = value;
		}

		public void setVariable(int index, Value value) {
			variables[index] = variables[index].assign(value);
		}

		public InMahBukkit getInMahBukkitVariable(int index, InMahBukkitFactory factory) {
			InMahBukkit bukkit = variables[index].getInMahBukkit(factory);
			variables[index] = bukkit;
			return bukkit;
		}

		public Value getVariable(int index) {
			return variables[index];
		}
	}

	private class FunctionImpl implements Function {
		private Program.Function function;

		public FunctionImpl(Program.Function function) {
			this.function = function;
		}

		public Value call(Value target, List<Value> arguments) {
			return callFunction(function, target, arguments);
		}
	}
}
