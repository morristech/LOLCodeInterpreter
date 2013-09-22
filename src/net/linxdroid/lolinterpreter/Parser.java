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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.ibm.icu.lang.UCharacter;

class Parser {
	private TokenSource tokenSource;
	private Tokenizer tokenizer;
	private ProgramBlock programBlock = new ProgramBlock();
	private StatementBlock statementBlock = new StatementBlock();
	private List<FunctionBlock> functions = new ArrayList<FunctionBlock>();
	public static Cmd cmd;

	public Parser(String path, File file, Reader reader, Version version, Cmd cmmd) {
		cmd = cmmd;
		tokenSource = new TokenSource(null, path, file, reader, statementBlock);
		tokenizer = new Tokenizer(tokenSource);
		tokenizer.addVersion(version.module());
	}

	private int readToken() throws IOException {
		return tokenizer.readToken();
	}

	private void error(String message) throws IOException {
		tokenizer.error(message);
	}

	private void error(String message, Throwable cause) throws IOException {
		tokenizer.error(message, cause);
	}

	private void errorExpected(String what) throws IOException {
		tokenizer.errorExpected(what);
	}

	private void errorRedeclared(Identifier ident) throws IOException {
	}

	private void readEOL() throws IOException {
		if (readToken() != Tokenizer.TOKEN_EOL) {
			errorExpected("end of line");
		}
	}

	private void checkIdentifier(String name) throws IOException {
		Identifier ident = programBlock.identifiers.get(name);
		if (ident != null) {
			if (ident.getVariable() != null) {
				error("variable already declared: " + name);
			} else if (ident.getImInYrIndex() != -1) {
				error("loop label already defined: " + name);
			} else {
				error("function already defined: " + name);
			}
		}
	}

	private void declareVariable(String name, Program.Expression value) throws IOException {
		checkIdentifier(name);
		statementBlock.identifiers.add(name);

		Program.Expression variableExpr;

		if (programBlock.outer == null || programBlock instanceof FunctionBlock) {
			int index = programBlock.variables.size();

			programBlock.variables.add(name);
			if (value != null) {
				statementBlock.statements.add(new Program.DeclareVariableStatement(index, value));
			}
			variableExpr = new Program.VariableExpression(index);
		} else {
			if (value != null) {
				statementBlock.statements.add(new Program.DeclareSlotStatement(Program.ObjectExpression.THIS, name, value));
			}
			variableExpr = new Program.SlotExpression(Program.ObjectExpression.THIS, new Program.YarnExpression(name));
		}

		programBlock.identifiers.put(name, new Variable(variableExpr));
	}

	private Program.Expression getVariable(String name) throws IOException {
		Identifier ident = programBlock.identifiers.get(name);
		if (ident == null && programBlock.isOuterScopeVisible() && tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
			ident = programBlock.outer.identifiers.get(name);
		}

		Program.Expression variable;

		if (ident == null || (variable = ident.getVariable()) == null) {
			error("undeclared variable: " + name);
			return null;
		}

		return variable;
	}

	public void addVersion(float numbar) throws IOException {
		for (Version version : Version.values()) {
			if (numbar == version.numbar()) {
				tokenizer.addVersion(version.module());
				return;
			}
		}

		error("unsupported language version: " + numbar);
	}

	private List<Program.Statement> closeStatementBlock() throws IOException {
		if (statementBlock == tokenSource.statementBlock) {
			errorExpected("KTHXBYE");
		}

		for (String name : statementBlock.identifiers) {
			programBlock.identifiers.remove(name);
		}

		for (String name : statementBlock.mahVariables) {
			programBlock.mahVariables.remove(name);
		}

		List<Program.Statement> statements = statementBlock.statements;
		statementBlock = statementBlock.outer;

		return statements;
	}

	private void closeImInYrStatementBlock(ImInYrStatementBlock block) throws IOException {
		statementBlock.statements.addAll(block.update);

		if (block.variableName != null) {
			programBlock.identifiers.remove(block.variableName);
		}

		programBlock.identifiers.remove(block.label);
		programBlock.gtfoDepth--;
		programBlock.imInYr = block.outerImInYr;

		List<Program.Statement> statements = closeStatementBlock();
		statementBlock.statements.add(new Program.ImInYrStatement(statements, block.variable, block.til, block.expr));
	}

	private void closeONoesStatementBlock() throws IOException {
		Program.Expression expr = ((ONoesStatementBlock)statementBlock).expr;
		List<Program.Statement> stmts = closeStatementBlock();

		((PlzStatementBlock)statementBlock).oNoes.add(new Program.PlzStatement.ONoes(expr, stmts));
	}

	private void closePlzStatementBlock(List<Program.Statement> awsumThx) throws IOException {
		PlzStatementBlock block = (PlzStatementBlock)statementBlock;
		List<Program.Statement> stmts = closeStatementBlock();

		statementBlock.statements.add(new Program.PlzStatement(stmts, block.oNoes, awsumThx));
	}

	public void parseHai() throws IOException {
		int token;

		while ((token = readToken()) == Tokenizer.TOKEN_EOL) { }

		if (token != Tokenizer.KW_HAI) {
			errorExpected("HAI");
		}

		token = readToken();
		if (token != Tokenizer.TOKEN_EOL) {
			if (tokenizer.hasMinimumVersion(Tokenizer.VERSION_1_1)) {
				if (token == Tokenizer.TOKEN_FLOAT) {
					float version = tokenizer.getFloat();
					readEOL();

					addVersion(version);
				} else if (token == Tokenizer.TOKEN_IDENTIFIER) {
					if (tokenizer.getString().equals("GINGER")) {
						tokenizer.addVersion(Tokenizer.VERSION_1_1);
					} else {
						error("unsupported language version: " + tokenizer.getString());
					}
				} else {
					errorExpected("language version after HAI");
				}
			} else {
				errorExpected("end of line");
			}
		}
	}

	public Program parse() throws IOException {
		parseHai();

		for (int token = readToken();; token = readToken()) {
			switch (token) {
				case Tokenizer.TOKEN_EOF:
					errorExpected(statementBlock.expected);
					break;

				case Tokenizer.TOKEN_EOL:
					continue;

				case Tokenizer.KW_AWSUM: {
					if (readToken() != Tokenizer.KW_THX) {
						errorExpected("AWSUM THX");
					}

					if (statementBlock.token == Tokenizer.KW_NOES) {
						closeONoesStatementBlock();
					} else if (statementBlock.token != Tokenizer.KW_PLZ) {
						error("unexpected AWSUM THX");
					}

					statementBlock = new StatementBlock(statementBlock, Tokenizer.KW_AWSUM, "KTHX");
					break;
				}

				case Tokenizer.KW_BYES:
				case Tokenizer.KW_DIAF: {
					Program.Expression exitCode;
					Program.Expression message;
					int exprToken;

					if ((exprToken = readToken()) == Tokenizer.TOKEN_EOL) {
						exitCode = new Program.NumbrExpression(token == Tokenizer.KW_BYES ? 0 : 1);
						message = null;
					} else {
						exitCode = readExpression(exprToken);

						if ((token = readToken()) == Tokenizer.TOKEN_EOL) {
							message = null;
						} else {
							message = readExpression(token);
							readEOL();
						}
					}

					statementBlock.statements.add(new Program.ByesStatement(exitCode, message));
					break;
				}

				case Tokenizer.KW_CAN:
					if (readToken() != Tokenizer.KW_HAS) {
						errorExpected("CAN HAS");
					}

					String moduleName = null;
					float version = 0;

					if ((token = readToken()) == Tokenizer.TOKEN_IDENTIFIER) {
						moduleName = tokenizer.getString();
					} else if (token == Tokenizer.TOKEN_FLOAT) {
						version = tokenizer.getFloat();
					} else if (token == Tokenizer.TOKEN_STRING) {
						String path = tokenizer.getString();
						if (readToken() != Tokenizer.TOKEN_QUESTION) {
							errorExpected("? after CAN HAS");
						}

						if (tokenSource.file == null) {
							error("unexpected CAN HAS with file");
						}

						File file = new File(tokenSource.file.getParent(), path);
						InputStream input;

						try {
							input = new FileInputStream(file);
						} catch (FileNotFoundException ex) {
							error(path + ": file not found", ex);
							input = null;
						}

						Reader reader = new InputStreamReader(input, "UTF-8");
						tokenSource = new TokenSource(tokenSource, path, file, reader, statementBlock);
						tokenizer.setTokenSource(tokenSource);
						parseHai();
						break;
					} else {
						errorExpected("module identifier or language version");
					}

					if (readToken() != Tokenizer.TOKEN_QUESTION) {
						errorExpected("? after CAN HAS");
					}
					readEOL();

					if (token == Tokenizer.TOKEN_IDENTIFIER) {
						if (moduleName.equals("GINGER")) {
							tokenizer.addVersion(Tokenizer.VERSION_1_1);
						} else if (moduleName.equals("JAVA")) {
							tokenizer.addModule(Tokenizer.MODULE_JAVA);
						} else if (!moduleName.equals("STDIO")) {
							error("unknown module: " + moduleName);
						}
					} else {
						addVersion(version);
					}
					break;

				case Tokenizer.KW_FOUND:
					if (readToken() != Tokenizer.KW_YR) {
						errorExpected("FOUND YR");
					}

					statementBlock.statements.add(new Program.FoundYrStatement(readExpression()));
					readEOL();
					break;

				case Tokenizer.KW_GIMMEH: {
					int what;
					switch (readToken()) {
						case Tokenizer.KW_LINE:
							what = Program.GimmehExpression.LINE;
							break;

						case Tokenizer.KW_WORD:
							what = Program.GimmehExpression.WORD;
							break;

						case Tokenizer.KW_LETTAR:
							what = Program.GimmehExpression.LETTAR;
							break;

						default:
							tokenizer.unreadToken();
							what = Program.GimmehExpression.LINE;
							break;
					}

					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						errorExpected("variable");
					}

					Program.Expression var = getVariable(tokenizer.getString());

					if (tokenizer.hasVersion(Tokenizer.VERSION_1_0) || tokenizer.hasVersion(Tokenizer.VERSION_1_1)) {
						if (readToken() != Tokenizer.KW_OUTTA) {
							tokenizer.unreadToken();
						} else {
							if (readToken() != Tokenizer.KW_STDIN) {
								errorExpected("OUTTA STDIN for GIMMEH");
							}
						}
					}

					readEOL();

					statementBlock.statements.add(createAssignStatement(var, new Program.GimmehExpression(what)));
					break;
				}

				case Tokenizer.KW_GTFO: {
					if (programBlock.gtfoDepth == 0 && programBlock.outer == null) {
						error("unexpected GTFO");
					}

					if ((token = readToken()) == Tokenizer.TOKEN_IDENTIFIER) {
						String name = tokenizer.getString();
						readEOL();

						Identifier ident = programBlock.identifiers.get(name);
						if (ident == null || ident.getImInYrIndex() == -1) {
							error("undeclared IM IN YR label: " + name);
						}

						statementBlock.statements.add(new Program.GTFOStatement(programBlock.gtfoDepth - ident.getImInYrIndex() - 1));
					} else {
						tokenizer.unreadToken();
						readEOL();
						statementBlock.statements.add(Program.GTFOStatement.INSTANCE);
					}
					break;
				}

				case Tokenizer.KW_HOW: {
					if (readToken() != Tokenizer.KW_DUZ) {
						errorExpected("HOW DUZ");
					}

					Program.Expression object;
					boolean outerVisible;

					if (readToken() == Tokenizer.KW_I) {
						if (programBlock.object == null) {
							object = null;
						} else {
							if (statementBlock.outer != null && statementBlock.token != Tokenizer.KW_O) {
								error("unexpected HOW DUZ I");
							}

							object = Program.ObjectExpression.THIS;
						}

						outerVisible = true;
					} else {
						if (!tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
							errorExpected("HOW DUZ I");
						}

						tokenizer.unreadToken();
						object = readExpression();
						outerVisible = false;
					}

					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						errorExpected("identifier after HOW DUZ");
					}

					String name = tokenizer.getString();

					FunctionBlock block = new FunctionBlock(programBlock, name, object);
					functions.add(block);
					programBlock = block;
					statementBlock = new StatementBlock(statementBlock, Tokenizer.KW_HOW, "IF U SAY SO");

					if ((token = readToken()) == Tokenizer.KW_YR) {
						for (;;) {
							if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
								errorExpected("identifier after YR for HOW DUZ");
							}

							String argName = tokenizer.getString();
							checkIdentifier(argName);
							programBlock.identifiers.put(argName, new Variable(new Program.VariableExpression(programBlock.variables.size())));
							programBlock.variables.add(argName);

							if ((token = readToken()) != Tokenizer.KW_AN) {
								break;
							}

							if (readToken() != Tokenizer.KW_YR) {
								errorExpected("AN YR for HOW DUZ");
							}
						}
					}

					block.numArguments = block.variables.size();

					if (token == Tokenizer.TOKEN_QUESTION) {
						token = readToken();
					}

					if (token != Tokenizer.TOKEN_EOL) {
						errorExpected("end of line");
					}

					if (outerVisible) {
						checkIdentifier(name);
						programBlock.outer.identifiers.put(name, block);
					}
					break;
				}

				case Tokenizer.KW_I: {
					if (readToken() != Tokenizer.KW_HAS || readToken() != Tokenizer.KW_A) {
						errorExpected("I HAS A");
					}

					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						errorExpected("identifier after I HAS A");
					}

					String name = tokenizer.getString();
					Program.Expression value;

					if (readToken() != Tokenizer.KW_ITZ) {
						tokenizer.unreadToken();
						value = Program.NoobExpression.INSTANCE;
					} else {
						if (readToken() != Tokenizer.KW_LIEK) {
							tokenizer.unreadToken();
							value = readExpression();
						} else {
							if (readToken() != Tokenizer.KW_A) {
								errorExpected("ITZ LIEK A");
							}

							value = new Program.BukkitExpression(readExpression());
						}
					}

					readEOL();

					declareVariable(name, value);
					break;
				}

				case Tokenizer.KW_IF: {
					if (readToken() != Tokenizer.KW_U || readToken() != Tokenizer.KW_SAY || readToken() != Tokenizer.KW_SO) {
						errorExpected("IF U SAY SO");
					}
					readEOL();

					if (statementBlock.token != Tokenizer.KW_HOW) {
						error("unexpected IF U SAY SO");
					}

					FunctionBlock block = (FunctionBlock)programBlock;
					block.setStatements(statementBlock.statements);

					closeStatementBlock();
					programBlock = programBlock.outer;

					if (block.object != null) {
						statementBlock.statements.add(new Program.DeclareSlotStatement(block.object, block.name, new Program.FunctionExpression(block.getFunction())));
					}
					break;
				}

				case Tokenizer.KW_IM: {
					if ((token = readToken()) == Tokenizer.KW_IN) {
						if (readToken() != Tokenizer.KW_YR) {
							errorExpected("IM IN YR");
						}

						if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
							errorExpected("label for IM IN YR");
						}

						String label = tokenizer.getString();
						checkIdentifier(label);

						List<Program.Statement> watchinStmts = Collections.emptyList();
						String variableName;
						Program.Expression variable;
						boolean til = false;
						Program.Expression expr = null;
						List<Program.Statement> update;

						if (!tokenizer.hasMinimumVersion(Tokenizer.VERSION_1_2)) {
							watchinStmts = Collections.emptyList();
							variableName = null;
							variable = null;
							til = false;
							expr = Program.TroofExpression.WIN;
							update = Collections.emptyList();
						} else {
							Operation operation;
							if ((token = readToken()) == Tokenizer.KW_TIL
									|| token == Tokenizer.KW_WILE
									|| token == Tokenizer.TOKEN_EOL) {
								tokenizer.unreadToken();
								operation = null;
							} else {
								operation = readOperation(token);
							}

							if (operation == null) {
								tokenizer.unreadToken();
								watchinStmts = Collections.emptyList();
								variableName = null;
								variable = null;
								update = Collections.emptyList();
							} else {
								boolean yr;

								if ((token = readToken()) == Tokenizer.KW_YR) {
									yr = true;
									token = readToken();
								} else {
									if (!tokenizer.hasVersion(Tokenizer.VERSION_1_3)) {
										errorExpected("YR for IM IN YR");
									}

									yr = false;
								}

								if (token != Tokenizer.TOKEN_IDENTIFIER) {
									errorExpected("variable for IM IN YR");
								}

								String name = tokenizer.getString();

								Program.Expression from;
								if (tokenizer.hasVersion(Tokenizer.VERSION_1_3)) {
									if (readToken() == Tokenizer.KW_FROM) {
										from = readExpression();
									} else {
										if (operation == WatchinOperation.INSTANCE) {
											errorExpected("FROM for IM IN YR WATCHIN");
										}

										tokenizer.unreadToken();
										from = null;
									}
								} else {
									from = null;
								}

								if (yr) {
									declareVariable(name, from != null ? from : new Program.NumbrExpression(0));
									variableName = name;
									variable = getVariable(name);
								} else {
									variableName = null;
									variable = getVariable(name);
									if (from != null) {
										statementBlock.statements.add(createAssignStatement(variable, from));
									}
								}

								if (operation == WatchinOperation.INSTANCE) {
									int slotsIndex = programBlock.variables.size();
									programBlock.variables.add("<watchin-slots>");
									Program.Expression slotsExpr = new Program.VariableExpression(slotsIndex);

									int indexIndex = programBlock.variables.size();
									programBlock.variables.add("<watchin-index>");
									Program.Expression indexExpr = new Program.VariableExpression(indexIndex);

									statementBlock.statements.add(
										new Program.DeclareVariableStatement(slotsIndex, new Program.BukkitSlotsExpression(from)));
									statementBlock.statements.add(
										new Program.DeclareVariableStatement(indexIndex, new Program.NumbrExpression(0)));

									watchinStmts = new ArrayList<Program.Statement>(2);
									watchinStmts.add(createAssignStatement(
										variable, new Program.SlotExpression(slotsExpr, indexExpr)));
									watchinStmts.add(new Program.DeclareVariableStatement(indexIndex,
											new Program.SumExpression(indexExpr, new Program.NumbrExpression(1))));

									expr = new Program.SmalrThanExpression(indexExpr, new Program.HowBigIzExpression(slotsExpr));
									update = Collections.emptyList();
								} else {
									watchinStmts = Collections.emptyList();
									update = Collections.singletonList(createAssignStatement(variable, operation.createExpression(variable)));
								}
							}

							if (operation != WatchinOperation.INSTANCE) {
								if ((token = readToken()) == Tokenizer.KW_TIL) {
									expr = readExpression();
									til = true;
								} else {
									if (token == Tokenizer.KW_WILE) {
										expr = readExpression();
									} else {
										tokenizer.unreadToken();
										expr = Program.TroofExpression.WIN;
									}

									til = false;
								}
							}
						}

						readEOL();

						ImInYrStatementBlock block = new ImInYrStatementBlock(
							statementBlock, programBlock, label, variableName, variable, til, expr, update);
						statementBlock = block;
						statementBlock.statements.addAll(watchinStmts);
						programBlock.gtfoDepth++;
						programBlock.imInYr = block;
						programBlock.identifiers.put(label, block);
					} else if (token == Tokenizer.KW_OUTTA) {
						if (readToken() != Tokenizer.KW_YR) {
							errorExpected("IM OUTTA YR");
						}

						if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
							errorExpected("label for IM OUTTA YR");
						}

						String name = tokenizer.getString();
						readEOL();

						if (statementBlock.token != Tokenizer.KW_IM) {
							error("unexpected IM OUTTA YR");
						}

						ImInYrStatementBlock block = (ImInYrStatementBlock)statementBlock;

						if (!block.label.equals(name)) {
							errorExpected("unexpected label for IM OUTTA YR: " + name);
						}

						closeImInYrStatementBlock(block);
					} else {
						errorExpected("IM IN YR or IM OUTTA YR");
					}
					break;
				}

				case Tokenizer.KW_IZ: {
					Program.Expression expr = readExpression();

					if (readToken() == Tokenizer.TOKEN_QUESTION) {
						if (readToken() != Tokenizer.TOKEN_EOL) {
							tokenizer.unreadToken();
						}
					} else {
						tokenizer.unreadToken();
					}

					while ((token = readToken()) == Tokenizer.TOKEN_EOL) { }

					if (token != Tokenizer.KW_YARLY) {
						tokenizer.unreadToken();
					}

					statementBlock = new ORlyStatementBlock(statementBlock, Tokenizer.KW_YARLY, expr, "KTHX");
					break;
				}

				case Tokenizer.KW_KTHX: {
					readEOL();

					switch (statementBlock.token) {
						case Tokenizer.KW_AWSUM: {
							closePlzStatementBlock(closeStatementBlock());
							break;
						}

						case Tokenizer.KW_IM:
							closeImInYrStatementBlock((ImInYrStatementBlock)statementBlock);
							break;

						case Tokenizer.KW_NOES: {
							closeONoesStatementBlock();
							closePlzStatementBlock(Collections.<Program.Statement>emptyList());
							break;
						}

						case Tokenizer.KW_NOWAI: {
							List<Program.Statement> noWai = closeStatementBlock();
							ORlyStatementBlock block = (ORlyStatementBlock)statementBlock;
							List<Program.Statement> statements = closeStatementBlock();

							statementBlock.statements.add(new Program.ORlyStatement(block.expr, statements, noWai));
							break;
						}

						case Tokenizer.KW_O: {
							List<Program.Statement> statements = closeStatementBlock();

							statementBlock.statements.add(new Program.OHaiStatement(programBlock.object, statements));
							programBlock = programBlock.outer;
							break;
						}

						case Tokenizer.KW_PLZ: {
							List<Program.Statement> stmts = closeStatementBlock();

							statementBlock.statements.addAll(stmts);
							break;
						}

						case Tokenizer.KW_YARLY: {
							ORlyStatementBlock block = (ORlyStatementBlock)statementBlock;
							List<Program.Statement> statements = closeStatementBlock();
							List<Program.Statement> noWai = Collections.emptyList();

							statementBlock.statements.add(new Program.ORlyStatement(block.expr, statements, noWai));
							break;
						}

						default:
							error("unexpected KTHX");
					}
					break;
				}

				case Tokenizer.KW_KTHXBYE: {
					if (statementBlock != tokenSource.statementBlock) {
						errorExpected(statementBlock.expected);
					}

					while ((token = readToken()) != Tokenizer.TOKEN_EOF) {
						if (token != Tokenizer.TOKEN_EOL) {
							errorExpected("end of file after KTHXBYE");
						}
					}

					if (tokenSource.outer != null) {
						statementBlock.expected = tokenSource.expected;
						tokenSource = tokenSource.outer;
						tokenizer.setTokenSource(tokenSource);
						break;
					}

					Program.Block mainBlock = new Program.Block(programBlock.variables.size(), statementBlock.statements);
					return new Program(tokenizer.hasVersion(Tokenizer.VERSION_1_1), mainBlock);
				}

				case Tokenizer.KW_LOL: {
					Program.Expression var = readExpression();

					if (readToken() != Tokenizer.KW_R) {
						errorExpected("R for LOL");
					}

					Program.Expression expr = readExpression();
					readEOL();

					statementBlock.statements.add(createAssignStatement(var, expr));
					break;
				}

				case Tokenizer.KW_MAH: {
					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						error("expected identifier after MAH");
					}

					if (!programBlock.isOuterScopeVisible() || !tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
						error("unexpected MAH");
					}

					for (;;) {
						String name = tokenizer.getString();
						checkIdentifier(name);

						Identifier ident = programBlock.outer.identifiers.get(name);
						if (ident == null || ident.getVariable() == null) {
							error("undeclared variable: " + name);
							continue;
						}

						if (!programBlock.mahVariables.add(name)) {
							error("variable already declared as MAH: " + name);
						}

						statementBlock.mahVariables.add(name);

						if ((token = readToken()) == Tokenizer.KW_AN) {
							token = readToken();
						}

						if (token != Tokenizer.TOKEN_IDENTIFIER) {
							tokenizer.unreadToken();
							break;
						}
					}

					readEOL();
					break;
				}

				case Tokenizer.KW_MEBBE: {
					if (statementBlock.token != Tokenizer.KW_YA && statementBlock.token != Tokenizer.KW_MEBBE) {
						error("unexpected MEBBE");
					}

					statementBlock = new ORlyStatementBlock(statementBlock, Tokenizer.KW_MEBBE, readExpression(), "OIC");
					readEOL();
					break;
				}

				case Tokenizer.KW_NERFZ: {
					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						error("expected identifier after NERFZ");
					}

					String name = tokenizer.getString();

					if (readToken() != Tokenizer.TOKEN_EXCLAMATION_EXCLAMATION) {
						error("expected !! for NERFZ");
					}

					int value = tokenizer.getInt() < 0 ? 1 : tokenizer.getInt();

					readEOL();

					Program.Expression var = getVariable(name);
					statementBlock.statements.add(createAssignStatement(
						var, new Program.DiffExpression(new Program.NumbrCastExpression(var), new Program.NumbrExpression(value))));
					break;
				}

				case Tokenizer.KW_NO: {
					if (readToken() != Tokenizer.KW_WAI) {
						errorExpected("NO WAI");
					}

					readEOL();

					if (statementBlock.token != Tokenizer.KW_YA && statementBlock.token != Tokenizer.KW_MEBBE) {
						error("unexpected NO WAI");
					}

					statementBlock = new StatementBlock(statementBlock, Tokenizer.KW_NO, "OIC");
					break;
				}

				case Tokenizer.KW_NOWAI: {
					readEOL();

					if (statementBlock.token != Tokenizer.KW_YARLY) {
						error("unexpected NOWAI");
					}

					statementBlock = new StatementBlock(statementBlock, Tokenizer.KW_NOWAI, "KTHX");
					break;
				}

				case Tokenizer.KW_O: {
					if ((token = readToken()) == Tokenizer.KW_RLY) {
						if (readToken() != Tokenizer.TOKEN_QUESTION) {
							errorExpected("O RLY?");
						}

						readEOL();
						while ((token = readToken()) == Tokenizer.TOKEN_EOL) { }

						if (token != Tokenizer.KW_YA || readToken() != Tokenizer.KW_RLY) {
							errorExpected("YA RLY");
						}

						readEOL();

						statementBlock = new ORlyStatementBlock(statementBlock, Tokenizer.KW_YA, Program.ItExpression.INSTANCE, "OIC");
					} else if (tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
						if (token == Tokenizer.KW_HAI) {
							if (readToken() != Tokenizer.KW_IM) {
								errorExpected("O HAI IM");
							}

							if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
								errorExpected("identifier for O HAI IM");
							}

							String name = tokenizer.getString();
							Program.Expression liek;

							if (readToken() != Tokenizer.KW_IM) {
								tokenizer.unreadToken();
								liek = null;
							} else {
								if (readToken() != Tokenizer.KW_LIEK) {
									errorExpected("IM LIEK for O HAI IM");
								}

								liek = readExpression();
							}

							readEOL();

							declareVariable(name, new Program.BukkitExpression(liek));
							programBlock = new ProgramBlock(programBlock, getVariable(name));
							statementBlock = new StatementBlock(statementBlock, Tokenizer.KW_O, "KTHX");
						} else if (token == Tokenizer.KW_NOES) {
							if (statementBlock.token != Tokenizer.KW_PLZ && statementBlock.token != Tokenizer.KW_NOES) {
								error("unexpected O NOES");
							}

							if (statementBlock.token == Tokenizer.KW_NOES) {
								closeONoesStatementBlock();
							} else if (statementBlock.token != Tokenizer.KW_PLZ) {
								error("unexpected O NOES");
							}

							Program.Expression expr = readExpression();
							readEOL();

							statementBlock = new ONoesStatementBlock(statementBlock, expr);
						} else {
							errorExpected("O RLY?, O HAI IM, or O NOES");
						}
					} else {
						errorExpected("O RLY?");
					}
					break;
				}

				case Tokenizer.KW_OBTW:
					tokenizer.readOBTW();
					readEOL();
					break;

				case Tokenizer.KW_OIC: {
					readEOL();

					Program.Statement stmt;

					if (statementBlock.token == Tokenizer.KW_YA
							|| statementBlock.token == Tokenizer.KW_MEBBE
							|| statementBlock.token == Tokenizer.KW_NO) {
						List<Program.Statement> noWai;

						if (statementBlock.token == Tokenizer.KW_NO) {
							noWai = closeStatementBlock();
						} else {
							noWai = Collections.emptyList();
						}

						while (statementBlock.token == Tokenizer.KW_MEBBE) {
							Program.Expression expr = ((ORlyStatementBlock)statementBlock).expr;
							noWai = Collections.singletonList((Program.Statement)new Program.ORlyStatement(expr, closeStatementBlock(), noWai));
						}

						Program.Expression expr = ((ORlyStatementBlock)statementBlock).expr;
						stmt = new Program.ORlyStatement(expr, closeStatementBlock(), noWai);
					} else if (statementBlock.token == Tokenizer.KW_WTF) {
						WTFStatementBlock block = (WTFStatementBlock)statementBlock;
						stmt = new Program.WTFStatement(closeStatementBlock(), block.labels, block.omgWTFIndex, block.expr);
						programBlock.gtfoDepth--;
					} else {
						error("unexpected OIC");
						stmt = null;
					}

					statementBlock.statements.add(stmt);
					break;
				}

				case Tokenizer.KW_OMG: {
					if (statementBlock.token != Tokenizer.KW_WTF) {
						error("unexpected OMG");
					}

					WTFStatementBlock block = (WTFStatementBlock)statementBlock;
					if (block.omgWTFIndex != -1) {
						error("unexpected OMG after OMGWTF");
					}

					Value literal;
					if (tokenizer.hasMinimumVersion(Tokenizer.VERSION_1_2)) {
						switch (readToken()) {
							case Tokenizer.TOKEN_IDENTIFIER:
								if (!tokenizer.hasVersion(Tokenizer.VERSION_1_1)) {
									errorExpected("value literal");
								}

								literal = new YarnValue(tokenizer.getString());
								break;

							case Tokenizer.TOKEN_INTEGER:
								literal = new NumbrValue(tokenizer.getInt());
								break;

							case Tokenizer.TOKEN_FLOAT:
								literal = new NumbarValue(tokenizer.getFloat());
								break;

							case Tokenizer.TOKEN_STRING:
								literal = new YarnValue(tokenizer.getString());
								break;

							case Tokenizer.KW_FAIL:
								literal = TroofValue.FAIL;
								break;

							case Tokenizer.KW_WIN:
								literal = TroofValue.WIN;
								break;

							default:
								errorExpected("value literal");
								literal = null;
								break;
						}
					} else {
						if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
							errorExpected("identifier for OMG");
						}

						literal = new YarnValue(tokenizer.getString());
					}

					if (block.labels.put(literal, statementBlock.statements.size()) != null) {
						error("duplicate OMG literal");
					}
					break;
				}

				case Tokenizer.KW_OMGWTF: {
					if (statementBlock.token != Tokenizer.KW_WTF) {
						error("unexpected OMGWTF");
					}

					WTFStatementBlock block = (WTFStatementBlock)statementBlock;
					if (block.omgWTFIndex != -1) {
						error("duplicate OMGWTF");
					}

					block.omgWTFIndex = statementBlock.statements.size();
					break;
				}

				case Tokenizer.KW_OVARZ: {
					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						error("expected identifier after OVARZ");
					}

					String name = tokenizer.getString();

					if (readToken() != Tokenizer.TOKEN_EXCLAMATION_EXCLAMATION) {
						error("expected !! for OVARZ");
					}

					int value = tokenizer.getInt() < 0 ? 1 : tokenizer.getInt();

					readEOL();

					Program.Expression var = getVariable(name);
					statementBlock.statements.add(createAssignStatement(
						var, new Program.QuoshuntExpression(new Program.NumbrCastExpression(var), new Program.NumbrExpression(value))));
					break;
				}

				case Tokenizer.KW_PLZ: {
					readEOL();

					statementBlock = new PlzStatementBlock(statementBlock);
					break;
				}

				case Tokenizer.KW_RTFM: {
					Program.Expression expr = readExpression();
					readEOL();

					statementBlock.statements.add(new Program.RTFMStatement(expr));
					break;
				}

				case Tokenizer.KW_TIEMZD: {
					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						error("expected identifier after TIEMZD");
					}

					String name = tokenizer.getString();

					if (readToken() != Tokenizer.TOKEN_EXCLAMATION_EXCLAMATION) {
						error("expected !! for TIEMZD");
					}

					int value = tokenizer.getInt() < 0 ? 1 : tokenizer.getInt();

					readEOL();

					Program.Expression var = getVariable(name);
					statementBlock.statements.add(createAssignStatement(
						var, new Program.ProduktExpression(new Program.NumbrCastExpression(var), new Program.NumbrExpression(value))));
					break;
				}

				case Tokenizer.KW_UPZ: {
					if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
						error("expected identifier after UPZ");
					}

					String name = tokenizer.getString();

					if (readToken() != Tokenizer.TOKEN_EXCLAMATION_EXCLAMATION) {
						error("expected !! for UPZ");
					}

					int value = tokenizer.getInt() < 0 ? 1 : tokenizer.getInt();

					readEOL();

					Program.Expression var = getVariable(name);
					statementBlock.statements.add(createAssignStatement(
						var, new Program.SumExpression(new Program.NumbrCastExpression(var), new Program.NumbrExpression(value))));
					break;
				}

				case Tokenizer.KW_VISIBLE:
				case Tokenizer.KW_INVISIBLE: {
					boolean invisible = token == Tokenizer.KW_INVISIBLE;
					List<Program.Expression> exprs = new ArrayList<Program.Expression>();
					boolean suppressNewLine = false;

					token = readToken();
					do {
						exprs.add(new Program.YarnCastExpression(readExpression(token)));

						if ((token = readToken()) == Tokenizer.TOKEN_EXCLAMATION) {
							suppressNewLine = true;
							readEOL();
							break;
						}
					} while (token != Tokenizer.TOKEN_EOL);

					statementBlock.statements.add(new Program.VisibleStatement(invisible, exprs, suppressNewLine));
					break;
				}

				case Tokenizer.KW_WHATEVER: {
					if (programBlock.imInYr == null) {
						error("unexpected WHATEVER");
					}

					readEOL();

					statementBlock.statements.add(new Program.WhateverStatement(programBlock.imInYr.update));
					break;
				}

				case Tokenizer.KW_WTF: {
					Program.Expression expr = null;

					if ((token = readToken()) != Tokenizer.TOKEN_QUESTION) {
						if (tokenizer.hasVersion(Tokenizer.VERSION_1_1)) {
							if (token == Tokenizer.KW_IZ) {
								token = readToken();
							}

							expr = readExpression(token);

							if (readToken() != Tokenizer.TOKEN_QUESTION) {
								tokenizer.unreadToken();
							}
						} else {
							errorExpected("WTF?");
						}
					} else {
						expr = Program.ItExpression.INSTANCE;
					}

					readEOL();
					while ((token = readToken()) == Tokenizer.TOKEN_EOL) { }

					if (token != Tokenizer.KW_OMG) {
						errorExpected("OMG");
					}

					tokenizer.unreadToken();

					statementBlock = new WTFStatementBlock(statementBlock, expr);
					programBlock.gtfoDepth++;
					break;
				}

				default: {
					Program.Expression expr = readExpression(token);
					Program.Statement stmt;

					if (tokenizer.hasMinimumVersion(Tokenizer.VERSION_1_2)) {
						switch (readToken()) {
							case Tokenizer.KW_HAS:
								if (!tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
									stmt = null;
								} else {
									if (readToken() != Tokenizer.KW_A) {
										errorExpected("HAS A");
									}

									if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
										errorExpected("identifier for HAS A");
									}

									String name = tokenizer.getString();
									Program.Expression value;

									if (readToken() != Tokenizer.KW_ITZ) {
										tokenizer.unreadToken();
										value = Program.NoobExpression.INSTANCE;
									} else {
										if (readToken() != Tokenizer.KW_LIEK) {
											tokenizer.unreadToken();
											value = readExpression();
										} else {
											if (readToken() != Tokenizer.KW_A) {
												errorExpected("ITZ LIEK A");
											}

											value = new Program.BukkitExpression(readExpression());
										}
									}

									readEOL();
									stmt = new Program.DeclareSlotStatement(expr, name, value);
								}
								break;

							case Tokenizer.KW_IS:
								if (readToken() != Tokenizer.KW_NOW || readToken() != Tokenizer.KW_A) {
									errorExpected("IS NOW A");
								}

								stmt = createAssignStatement(expr, readCastExpression(expr, readToken()));
								break;

							case Tokenizer.KW_R:
								stmt = createAssignStatement(expr, readExpression());
								readEOL();
								break;

							default:
								stmt = null;
								break;
						}

						if (stmt == null) {
							tokenizer.unreadToken();
							readEOL();
							stmt = new Program.AssignItStatement(expr);
						}
					} else {
						readEOL();
						stmt = new Program.AssignItStatement(expr);
					}

					statementBlock.statements.add(stmt);
					break;
				}
			}
		}
	}

	private Operation readOperation(int token) throws IOException {
		if (token == Tokenizer.KW_UPPIN) {
			return SumOperation.UPPIN;
		}

		if (token == Tokenizer.KW_NERFIN) {
			return SumOperation.NERFIN;
		}

		if (!tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // loop2
			errorExpected("UPPIN OR NERFIN");
		}

		if (token == Tokenizer.KW_WATCHIN) {
			return WatchinOperation.INSTANCE;
		}

		if (token != Tokenizer.TOKEN_IDENTIFIER) {
			errorExpected("operation");
		}

		return (Operation)readPrefixExpression(token, true);
	}

	private Program.Expression readExpression() throws IOException {
		return readExpression(readToken());
	}

	private Program.Expression readCastExpression(Program.Expression expr, int token) throws IOException {
		switch (token) {
			case Tokenizer.KW_NOOB:
				return new Program.NoobCastExpression(expr);

			case Tokenizer.KW_TROOF:
				return new Program.TroofCastExpression(expr);

			case Tokenizer.KW_YARN:
				return new Program.YarnCastExpression(expr);

			case Tokenizer.KW_NUMBR:
				return new Program.NumbrCastExpression(expr);

			case Tokenizer.KW_NUMBAR:
				return new Program.NumbarCastExpression(expr);
		}

		errorExpected("type name");
		return null;
	}

	private Program.Expression readOfExpression(String name) throws IOException {
		if (readToken() != Tokenizer.KW_OF) {
			errorExpected(name + " OF");
		}

		return readExpression();
	}

	private Program.Expression readAnExpression() throws IOException {
		if (readToken() != Tokenizer.KW_AN) {
			tokenizer.unreadToken();
		}

		return readExpression();
	}

	private List<Program.Expression> readExpressions() throws IOException {
		List<Program.Expression> exprs = new ArrayList<Program.Expression>();

		for (;;) {
			int token;

			if ((token = readToken()) == Tokenizer.KW_MKAY) {
				break;
			}

			if (token == Tokenizer.TOKEN_EOL || token == Tokenizer.TOKEN_EOF) {
				tokenizer.unreadToken();
				break;
			}

			if (token == Tokenizer.KW_AN) {
				token = readToken();
			}

			exprs.add(readExpression(token));
		}

		return exprs;
	}

	private List<Program.Expression> readOfExpressions(String name) throws IOException {
		if (readToken() != Tokenizer.KW_OF) {
			errorExpected(name + " OF");
		}

		return readExpressions();
	}

	private List<Program.Expression> readOfBinaryExpressions(String name) throws IOException {
		List<Program.Expression> exprs = new ArrayList<Program.Expression>();
		exprs.add(readOfExpression(name));
		exprs.add(readAnExpression());

		return exprs;
	}

	private static final int PRECEDENCE_TIEMZ_OVARZ = 1 << 3;
	private static final int PRECEDENCE_UP_NERF = 2 << 3;
	private static final int PRECEDENCE_IN_MAH = 3 << 3;
	private static final int PRECEDENCE_CONDITIONAL = 4 << 3;
	private static final int PRECEDENCE_LOGICAL = 5 << 3;
	private static final int PRECEDENCE_MASK = ~7;

	private static final int OPERATOR_END = 0;

	private static final int OPERATOR_IN_MAH = PRECEDENCE_IN_MAH | 0;

	private static final int OPERATOR_BIGR_THAN = PRECEDENCE_CONDITIONAL | 0;
	private static final int OPERATOR_NOT_BIGR_THAN = PRECEDENCE_CONDITIONAL | 1;
	private static final int OPERATOR_SMALR_THAN = PRECEDENCE_CONDITIONAL | 2;
	private static final int OPERATOR_NOT_SMALR_THAN = PRECEDENCE_CONDITIONAL | 3;
	private static final int OPERATOR_LIEK = PRECEDENCE_CONDITIONAL | 4;
	private static final int OPERATOR_NOT_LIEK = PRECEDENCE_CONDITIONAL | 5;

	private static final int OPERATOR_AND = PRECEDENCE_LOGICAL | 1;
	private static final int OPERATOR_OR = PRECEDENCE_LOGICAL | 2;
	private static final int OPERATOR_XOR = PRECEDENCE_LOGICAL | 3;

	private static final int OPERATOR_UP = PRECEDENCE_UP_NERF | 0;
	private static final int OPERATOR_NERF = PRECEDENCE_UP_NERF | 1;

	private static final int OPERATOR_TIEMZ = PRECEDENCE_TIEMZ_OVARZ | 0;
	private static final int OPERATOR_OVAR = PRECEDENCE_TIEMZ_OVARZ | 1;

	private int readOperator() throws IOException {
		int t;
		switch (t = readToken()) {
			case Tokenizer.KW_AND:
				return OPERATOR_AND;

			case Tokenizer.KW_BIGR:
				if (readToken() != Tokenizer.KW_THAN) {
					errorExpected("BIGR THAN");
				}

				return OPERATOR_BIGR_THAN;

			case Tokenizer.KW_IN:
				if (readToken() != Tokenizer.KW_MAH) {
					errorExpected("IN MAH");
				}

				return OPERATOR_IN_MAH;

			case Tokenizer.KW_LIEK:
				return OPERATOR_LIEK;

			case Tokenizer.KW_NERF:
				return OPERATOR_NERF;

			case Tokenizer.KW_NOT:
				switch (readToken()) {
					case Tokenizer.KW_BIGR:
						if (readToken() != Tokenizer.KW_THAN) {
							errorExpected("NOT BIGR THAN");
						}

						return OPERATOR_NOT_BIGR_THAN;

					case Tokenizer.KW_LIEK:
						return OPERATOR_NOT_LIEK;

					case Tokenizer.KW_SMALR:
						if (readToken() != Tokenizer.KW_THAN) {
							errorExpected("NOT SMALR THAN");
						}

						return OPERATOR_NOT_SMALR_THAN;
				}

				errorExpected("NOT BIGR THAN, NOT SMALR THAN, or NOT LIEK");
				return 0;

			case Tokenizer.KW_OR:
				return OPERATOR_OR;

			case Tokenizer.KW_OVAR:
				return OPERATOR_OVAR;

			case Tokenizer.KW_SMALR:
				if (readToken() != Tokenizer.KW_THAN) {
					errorExpected("SMALR THAN");
				}

				return OPERATOR_SMALR_THAN;

			case Tokenizer.KW_TIEMZ:
				return OPERATOR_TIEMZ;

			case Tokenizer.KW_UP:
				return OPERATOR_UP;

			case Tokenizer.KW_XOR:
				return OPERATOR_XOR;

			default:
				tokenizer.unreadToken();
				return OPERATOR_END;
		}
	}

	private List<Program.Expression> createList(Program.Expression left, Program.Expression right) {
		List<Program.Expression> exprs = new ArrayList<Program.Expression>(2);
		exprs.add(left);
		exprs.add(right);
		return exprs;
	}

	private Program.Expression readExpression(int token) throws IOException {
		Program.Expression expr = readPrefixExpression(token, false);

		if (!tokenizer.hasVersion(Tokenizer.VERSION_1_0) && !tokenizer.hasVersion(Tokenizer.VERSION_1_1)) {
			return expr;
		}

		Stack<Program.Expression> exprs = new Stack<Program.Expression>();
		Stack<Integer> ops = new Stack<Integer>();

		exprs.push(null);
		exprs.push(expr);
		ops.add(OPERATOR_END);

		for (;;) {
			int op = readOperator();

			while ((op & PRECEDENCE_MASK) <= (ops.peek() & PRECEDENCE_MASK)) {
				Program.Expression right = exprs.pop();
				Program.Expression left = exprs.pop();

				switch (ops.pop()) {
					case OPERATOR_END:
						return right;

					case OPERATOR_IN_MAH:
						exprs.push(new Program.InMahExpression(right, left));
						break;

					case OPERATOR_BIGR_THAN:
						exprs.push(new Program.BigrThanExpression(left, right));
						break;

					case OPERATOR_NOT_BIGR_THAN:
						exprs.push(new Program.NotExpression(new Program.BigrThanExpression(left, right)));
						break;

					case OPERATOR_SMALR_THAN:
						exprs.push(new Program.SmalrThanExpression(left, right));
						break;

					case OPERATOR_NOT_SMALR_THAN:
						exprs.push(new Program.NotExpression(new Program.SmalrThanExpression(left, right)));
						break;

					case OPERATOR_LIEK:
						exprs.push(new Program.BothSaemExpression(left, right));
						break;

					case OPERATOR_NOT_LIEK:
						exprs.push(new Program.NotExpression(new Program.BothSaemExpression(left, right)));
						break;

					case OPERATOR_AND:
						exprs.push(new Program.AllExpression(createList(left, right)));
						break;

					case OPERATOR_OR:
						exprs.push(new Program.AnyExpression(createList(left, right)));
						break;

					case OPERATOR_XOR:
						exprs.push(new Program.WonExpression(left, right));
						break;

					case OPERATOR_UP:
						exprs.push(new Program.SumExpression(
							new Program.MathNumbrExpression(left), new Program.MathNumbrExpression(right)));
						break;

					case OPERATOR_NERF:
						exprs.push(new Program.DiffExpression(
							new Program.MathNumbrExpression(left), new Program.MathNumbrExpression(right)));
						break;

					case OPERATOR_TIEMZ:
						exprs.push(new Program.ProduktExpression(
							new Program.MathNumbrExpression(left), new Program.MathNumbrExpression(right)));
						break;

					case OPERATOR_OVAR:
						exprs.push(new Program.QuoshuntExpression(
							new Program.MathNumbrExpression(left), new Program.MathNumbrExpression(right)));
						break;

					default:
						throw new IllegalStateException();
				}
			}

			exprs.push(readExpression());
			ops.add(op);
		}
	}

	private FunctionBlock getFunction(ProgramBlock block, String name) {
		Identifier ident = programBlock.identifiers.get(name);
		if (ident == null) {
			return null;
		}

		return ident.getFunctionBlock();
	}

	private Program.Expression readPrefixExpression(int token, boolean operation) throws IOException {
		switch (token) {
			case Tokenizer.TOKEN_IDENTIFIER: {
				String name = tokenizer.getString();
				boolean outer = false;

				Identifier ident = programBlock.identifiers.get(name);
				if (ident == null && programBlock.isOuterScopeVisible() && tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
					ident = programBlock.outer.identifiers.get(name);
					outer = true;
				}

				if (ident == null) {
					error("undeclared variable: " + name);
				}

				FunctionBlock function = ident.getFunctionBlock();
				if (function != null) {
					if (operation && function.numArguments != 1) {
						errorExpected("unary function");
					}

					if (function.object != null) {
						Program.Expression target;
						if (outer) {
							target = programBlock.getOuterObject();
						} else {
							target = Program.ObjectExpression.THIS;
						}

						if (operation) {
							return new SlotFunctionCallOperation(target, new Program.YarnExpression(name));
						}

						List<Program.Expression> arguments = new ArrayList<Program.Expression>();

						for (int i = 0; i < function.numArguments; i++) {
							arguments.add(readExpression());
						}

						return new Program.SlotFunctionCallExpression(target, new Program.YarnExpression(name), arguments);
					}

					if (operation) {
						return new FunctionCallOperation(function.getFunction());
					}

					Program.Expression[] arguments = new Program.Expression[function.numArguments];

					for (int i = 0; i < function.numArguments; i++) {
						arguments[i] = readExpression();
					}

					return new Program.FunctionCallExpression(function.getFunction(), arguments);
				}

				Program.Expression value;
				if (outer) {
					value = ident.getVariable();
					if (value != null) {
						if (value instanceof Program.VariableExpression) {
							value = new Program.GlobalVariableExpression(((Program.VariableExpression)value).getIndex());
						} else {
							value = new Program.SlotExpression(programBlock.getOuterObject(), ((Program.SlotExpression)value).getIndex());
						}
					}
				} else {
					value = ident.getVariable();
				}

				if (value == null) {
					error("undeclared variable: " + name);
				}

				if (!tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
					return value;
				}

				for (token = readToken();;) {
					Program.Expression index;

					if (token == Tokenizer.TOKEN_EXCLAMATION_EXCLAMATION) {
						int argument = tokenizer.getInt();

						if (argument >= 0) {
							index = new Program.NumbrExpression(argument);
						} else {
							if (argument == -1 || readToken() != Tokenizer.TOKEN_IDENTIFIER) {
								errorExpected("slot");
							}

							index = new Program.YarnExpression(tokenizer.getString());
						}
					} else if (token == Tokenizer.TOKEN_EXCLAMATION_QUESTION) {
						if (tokenizer.getInt() >= -1) {
							readToken();
							errorExpected("variable");
						} else if (readToken() != Tokenizer.TOKEN_IDENTIFIER) {
							errorExpected("variable");
						}

						index = getVariable(tokenizer.getString());
					} else {
						tokenizer.unreadToken();

						if (operation) {
							if (!(value instanceof Program.SlotExpression)) {
								errorExpected("operation");
							}

							Program.SlotExpression slotExpr = (Program.SlotExpression)value;
							return new SlotFunctionCallOperation(slotExpr.getBukkit(), slotExpr.getIndex());
						}

						return value;
					}

					token = readToken();

					if (!operation && (token == Tokenizer.KW_WIF || token == Tokenizer.KW_OF)) {
						List<Program.Expression> arguments = new ArrayList<Program.Expression>();
						arguments.add(readExpression());

						for (;;) {
							if ((token = readToken()) == Tokenizer.KW_MKAY || token == Tokenizer.TOKEN_EXCLAMATION) {
								break;
							}

							if (token == Tokenizer.TOKEN_EOL || token == Tokenizer.TOKEN_EOF) {
								tokenizer.unreadToken();
								break;
							}

							if (token != Tokenizer.KW_AN) {
								tokenizer.unreadToken();
							}

							arguments.add(readExpression());
						}

						return new Program.SlotFunctionCallExpression(value, index, arguments);
					}

					value = new Program.SlotExpression(value, index);
				}
			}

			case Tokenizer.TOKEN_INTEGER:
				return new Program.NumbrExpression(tokenizer.getInt());

			case Tokenizer.TOKEN_FLOAT:
				return new Program.NumbarExpression(tokenizer.getFloat());

			case Tokenizer.TOKEN_STRING:
				return new Program.YarnExpression(tokenizer.getString());

			case Tokenizer.TOKEN_INTERPOLATED_STRING: {
				List<String> strings = tokenizer.getInterpolatedString();
				List<Program.Expression> exprs = new ArrayList<Program.Expression>();

				String string = strings.get(0);
				if (string.length() != 0) {
					exprs.add(new Program.YarnExpression(string));
				}

				for (int i = 1, size = strings.size(); i < size; i += 2) {
					string = strings.get(i);
					exprs.add(new Program.YarnCastExpression(getVariable(string)));

					string = strings.get(i + 1);
					if (string.length() != 0) {
						exprs.add(new Program.YarnExpression(string));
					}
				}

				return new Program.SmooshExpression(exprs);
			}

			case Tokenizer.KW_A:
				if (tokenizer.hasVersion(Tokenizer.VERSION_1_3)) { // bukkit
					switch (readToken()) {
						case Tokenizer.KW_NOOB:
							return Program.NoobExpression.INSTANCE;

						case Tokenizer.KW_TROOF:
							return Program.TroofExpression.FAIL;

						case Tokenizer.KW_NUMBR:
							return new Program.NumbrExpression(0);

						case Tokenizer.KW_NUMBAR:
							return new Program.NumbarExpression(0);

						case Tokenizer.KW_YARN:
							return new Program.YarnExpression("");

						case Tokenizer.KW_BUKKIT:
							return Program.BukkitExpression.INSTANCE;

						default:
							errorExpected("type after A");
					}
				}
				break;

			case Tokenizer.KW_ALL:
				return new Program.AllExpression(readOfExpressions("ANY"));

			case Tokenizer.KW_ANY:
				return new Program.AnyExpression(readOfExpressions("ANY"));

			case Tokenizer.KW_BIGGR:
				return new Program.BiggrExpression(readOfExpression("BIGGR"), readAnExpression());

			case Tokenizer.KW_BOTH: {
				if ((token = readToken()) == Tokenizer.KW_OF) {
					return new Program.AllExpression(readExpressions());
				}

				if (token == Tokenizer.KW_SAEM) {
					return new Program.BothSaemExpression(readExpression(), readAnExpression());
				}

				errorExpected("BOTH OF or BOTH SAEM");
				return null;
			}

			case Tokenizer.KW_DIFF:
				return new Program.DiffExpression(readOfExpression("DIFF"), readAnExpression());

			case Tokenizer.KW_DIFFRINT:
				return new Program.DiffrintExpression(readExpression(), readAnExpression());

			case Tokenizer.KW_EITHER:
				return new Program.AnyExpression(readOfBinaryExpressions("EITHER"));

			case Tokenizer.KW_FAIL:
				return Program.TroofExpression.FAIL;

			case Tokenizer.KW_IT:
				return Program.ItExpression.INSTANCE;

			case Tokenizer.KW_JAVA:
				return new Program.JavaExpression(readExpression());

			case Tokenizer.KW_MAEK: {
				Program.Expression expr = readExpression();

				if ((token = readToken()) == Tokenizer.KW_A) {
					token = readToken();
				}

				return readCastExpression(expr, token);
			}

			case Tokenizer.KW_MOD:
				return new Program.ModExpression(readOfExpression("MOD"), readAnExpression());

			case Tokenizer.KW_NOT:
				return new Program.NotExpression(readExpression());

			case Tokenizer.KW_PRODUKT:
				return new Program.ProduktExpression(readOfExpression("PRODUKT"), readAnExpression());

			case Tokenizer.KW_QUOSHUNT:
				return new Program.QuoshuntExpression(readOfExpression("QUOSHUNT"), readAnExpression());

			case Tokenizer.KW_SMALLR:
				return new Program.SmallrExpression(readOfExpression("SMALLR"), readAnExpression());

			case Tokenizer.KW_SMOOSH:
				return new Program.SmooshExpression(readExpressions());

			case Tokenizer.KW_SUM:
				return new Program.SumExpression(readOfExpression("SUM"), readAnExpression());

			case Tokenizer.KW_WON:
				return new Program.WonExpression(readOfExpression("WON"), readAnExpression());

			case Tokenizer.KW_WIN:
				return Program.TroofExpression.WIN;
		}

		errorExpected("expression");
		return null;
	}

	private Program.Statement createAssignStatement(Program.InMahExpression expr) throws IOException {
		Program.Expression bukkit = expr.getBukkit();
		if (bukkit instanceof Program.InMahExpression) {
			return createAssignStatement((Program.InMahExpression)bukkit);
		}

		return createAssignStatement(bukkit, new Program.GetInMahBukkitExpression(bukkit));
	}

	private Program.Expression createAssignInMahBukkitInMahExpression(Program.Expression expr) {
		if (expr instanceof Program.InMahExpression) {
			Program.InMahExpression inMahExpr = (Program.InMahExpression)expr;
			return new Program.AssignInMahBukkitInMahExpression(inMahExpr.getBukkit(), inMahExpr.getIndex());
		}

		return expr;
	}

	private Program.Statement createAssignStatement(Program.Expression expr, Program.Expression value) throws IOException {
		if (expr instanceof Program.VariableExpression) {
			return new Program.AssignVariableStatement(((Program.VariableExpression)expr).getIndex(), value);
		}

		if (expr instanceof Program.GlobalVariableExpression) {
			int index = ((Program.GlobalVariableExpression)expr).getIndex();
			String name = programBlock.outer.variables.get(index);

			if (!programBlock.mahVariables.contains(name)) {
				error("undeclared MAH variable: " + name);
			}

			return new Program.AssignGlobalVariableStatement(index, value);
		}

		if (expr instanceof Program.SlotExpression) {
			Program.SlotExpression slotExpr = (Program.SlotExpression)expr;
			return new Program.AssignSlotStatement(slotExpr.getBukkit(), slotExpr.getIndex(), value);
		}

		if (expr instanceof Program.InMahExpression) {
			Program.InMahExpression inMahExpr = (Program.InMahExpression)expr;
			return new Program.AssignInMahStatement(createAssignStatement(inMahExpr), createAssignInMahBukkitInMahExpression(inMahExpr.getBukkit()), inMahExpr.getIndex(), value);
		}

		error("invalid assignment target");
		return null;
	}

	public enum Version {
		VERSION_1_0("1.0", 1.0f, Tokenizer.VERSION_1_0),
		VERSION_1_1("1.1", 1.1f, Tokenizer.VERSION_1_1),
		VERSION_1_2("1.2", 1.2f, Tokenizer.VERSION_1_2),
		VERSION_1_3("1.3", 1.3f, Tokenizer.VERSION_1_3);

		public static final Version DEFAULT = VERSION_1_3;

		private int module;
		private float numbar;
		private String string;

		Version(String string, float numbar, int module) {
			this.string = string;
			this.numbar = numbar;
			this.module = module;
		}

		public String string() {
			return string;
		}

		public float numbar() {
			return numbar;
		}

		public int module() {
			return module;
		}

		public static Version get(String string) {
			for (Version version : values()) {
				if (version.string().equals(string)) {
					return version;
				}
			}

			return null;
		}
	}

	private static class TokenSource {
		public String path;
		public File file;
		public Reader reader;
		public int last = -1;
		public int next = -2;
		public int line;
		public int column;

		public TokenSource outer;
		public StatementBlock statementBlock;
		public String expected;

		public TokenSource(TokenSource outer, String path, File file, Reader reader, StatementBlock statementBlock) {
			this.outer = outer;
			this.path = path;
			this.file = file;
			this.line = 1;
			this.column = 1;
			this.reader = reader;
			this.statementBlock = statementBlock;
			this.expected = statementBlock.expected;
		}
	}

	private interface Identifier {
		Program.Expression getVariable();
		int getImInYrIndex();
		FunctionBlock getFunctionBlock();
	}

	private static class Variable implements Identifier {
		public Program.Expression variable;

		public Variable(Program.Expression variable) {
			this.variable = variable;
		}

		public Program.Expression getVariable() {
			return variable;
		}

		public int getImInYrIndex() {
			return -1;
		}

		public FunctionBlock getFunctionBlock() {
			return null;
		}
	}

	private interface Operation {
		Program.Expression createExpression(Program.Expression arg);
	}

	private static class SumOperation implements Operation {
		public static final Operation UPPIN = new SumOperation(1);
		public static final Operation NERFIN = new SumOperation(-1);

		private Program.Expression increment;

		private SumOperation(int increment) {
			this.increment = new Program.NumbrExpression(increment);
		}

		public Program.Expression createExpression(Program.Expression arg) {
			return new Program.SumExpression(arg, increment);
		}
	}

	private static class WatchinOperation implements Operation {
		public static final Operation INSTANCE = new WatchinOperation();

		private WatchinOperation() { }

		public Program.Expression createExpression(Program.Expression arg) {
			throw new UnsupportedOperationException();
		}
	}

	private static class FunctionCallOperation implements Program.Expression, Operation {
		private Program.Function function;

		public FunctionCallOperation(Program.Function function) {
			this.function = function;
		}

		public void visit(Program.ExpressionVisitor visitor) {
			throw new UnsupportedOperationException();
		}

		public Program.Expression createExpression(Program.Expression arg) {
			return new Program.FunctionCallExpression(function, new Program.Expression[] { arg });
		}
	}

	private static class SlotFunctionCallOperation implements Program.Expression, Operation {
		private Program.Expression bukkit;
		private Program.Expression index;

		public SlotFunctionCallOperation(Program.Expression bukkit, Program.Expression index) {
			this.bukkit = bukkit;
			this.index = index;
		}

		public void visit(Program.ExpressionVisitor visitor) {
			throw new UnsupportedOperationException();
		}

		public Program.Expression createExpression(Program.Expression arg) {
			return new Program.SlotFunctionCallExpression(bukkit, index, Collections.singletonList(arg));
		}
	}

	private static class StatementBlock {
		public StatementBlock outer;
		public List<Program.Statement> statements = new ArrayList<Program.Statement>();
		public List<String> identifiers = new ArrayList<String>();
		public List<String> mahVariables = new ArrayList<String>();
		public int token;
		public String expected;

		public StatementBlock() {
			token = Tokenizer.KW_HAI;
			expected = "KTHXBYE";
		}

		public StatementBlock(StatementBlock outer, int token, String expected) {
			this.outer = outer;
			this.token = token;
			this.expected = expected;
		}
	}

	private static class ORlyStatementBlock extends StatementBlock {
		public Program.Expression expr;

		public ORlyStatementBlock(StatementBlock outer, int token, Program.Expression expr, String expected) {
			super(outer, token, expected);
			this.expr = expr;
		}
	}

	private static class WTFStatementBlock extends StatementBlock {
		public Map<Value, Integer> labels = new HashMap<Value, Integer>();
		public int omgWTFIndex = -1;
		public Program.Expression expr;

		public WTFStatementBlock(StatementBlock outer, Program.Expression expr) {
			super(outer, Tokenizer.KW_WTF, "OIC");
			this.expr = expr;
		}
	}

	private static class ImInYrStatementBlock extends StatementBlock implements Identifier {
		public ImInYrStatementBlock outerImInYr;
		public int index;
		public String label;
		public String variableName;
		public Program.Expression variable;
		public boolean til;
		public Program.Expression expr;
		public List<Program.Statement> update;

		public ImInYrStatementBlock(StatementBlock outer, ProgramBlock programBlock, String label, String variableName, Program.Expression variable, boolean til, Program.Expression expr, List<Program.Statement> update) {
			super(outer, Tokenizer.KW_IM, "IM OUTTA YR " + label);
			this.outerImInYr = programBlock.imInYr;
			this.index = programBlock.gtfoDepth;
			this.label = label;
			this.variableName = variableName;
			this.variable = variable;
			this.til = til;
			this.expr = expr;
			this.update = update;
		}

		public Program.Expression getVariable() {
			return null;
		}

		public int getImInYrIndex() {
			return index;
		}

		public FunctionBlock getFunctionBlock() {
			return null;
		}
	}

	private static class PlzStatementBlock extends StatementBlock {
		public List<Program.Statement> plzStatements;
		public List<Program.PlzStatement.ONoes> oNoes = new ArrayList<Program.PlzStatement.ONoes>();

		public PlzStatementBlock(StatementBlock outer) {
			super(outer, Tokenizer.KW_PLZ, "KTHX");
		}
	}

	private static class ONoesStatementBlock extends StatementBlock {
		public Program.Expression expr;

		public ONoesStatementBlock(StatementBlock outer, Program.Expression expr) {
			super(outer, Tokenizer.KW_NOES, "KTHX");
			this.expr = expr;
		}
	}

	private static class ProgramBlock {
		public ProgramBlock outer;
		public Map<String, Identifier> identifiers = new HashMap<String, Identifier>();
		public List<String> variables = new ArrayList<String>();
		public Set<String> mahVariables = new HashSet<String>();
		public int gtfoDepth;
		public ImInYrStatementBlock imInYr;
		public Program.Expression object;

		public ProgramBlock() { }

		public ProgramBlock(ProgramBlock outer, Program.Expression object) {
			this.outer = outer;
			this.object = object;
		}

		public boolean isOuterScopeVisible() {
			return outer != null;
		}

		public Program.Expression getOuterObject() {
			return Program.ObjectExpression.OUTER;
		}
	}

	private static class FunctionBlock extends ProgramBlock implements Identifier {
		public String name;
		public int numArguments;
		private Program.Function function;

		public FunctionBlock(ProgramBlock outer, String name, Program.Expression object) {
			super(outer, object);
			this.name = name;
		}

		@Override
		public boolean isOuterScopeVisible() {
			return outer != null && (object == null || object == Program.ObjectExpression.THIS);
		}

		public void setStatements(List<Program.Statement> statements) {
			function = new Program.Function(variables.size(), statements, numArguments);
		}

		public Program.Function getFunction() {
			return function;
		}

		public Program.Expression getOuterObject() {
			return Program.ObjectExpression.THIS;
		}

		public Program.Expression getVariable() {
			return null;
		}

		public int getImInYrIndex() {
			return -1;
		}

		public FunctionBlock getFunctionBlock() {
			return this;
		}
	}

	private static class Tokenizer {
		public static final int KW_A = 0;
		public static final int KW_ALL = 1;
		public static final int KW_AN = 2;
		public static final int KW_AND = 3;
		public static final int KW_ANY = 4;
		public static final int KW_AWSUM = 5;
		public static final int KW_BIGGR = 6;
		public static final int KW_BIGR = 7;
		public static final int KW_BOTH = 8;
		private static final int RAW_KW_BTW = 9;
		public static final int KW_BUKKIT = 10;
		public static final int KW_BYES = 11;
		public static final int KW_CAN = 12;
		public static final int KW_DIAF = 13;
		public static final int KW_DIFF = 14;
		public static final int KW_DIFFRINT = 15;
		public static final int KW_DUZ = 16;
		public static final int KW_EITHER = 17;
		public static final int KW_FAIL = 18;
		public static final int KW_FOUND = 19;
		public static final int KW_FROM = 20;
		public static final int KW_GIMMEH = 21;
		public static final int KW_GTFO = 22;
		public static final int KW_HAI = 23;
		public static final int KW_HAS = 24;
		public static final int KW_HOW = 25;
		public static final int KW_I = 26;
		public static final int KW_IF = 27;
		public static final int KW_IM = 28;
		public static final int KW_IN = 29;
		public static final int KW_INVISIBLE = 30;
		public static final int KW_IS = 31;
		public static final int KW_IT = 32;
		public static final int KW_ITZ = 33;
		public static final int KW_IZ = 34;
		public static final int KW_JAVA = 35;
		public static final int KW_KTHX = 36;
		public static final int KW_KTHXBYE = 37;
		public static final int KW_LETTAR = 38;
		public static final int KW_LIEK = 39;
		public static final int KW_LINE = 40;
		public static final int KW_LOL = 41;
		public static final int KW_MAEK = 42;
		public static final int KW_MAH = 43;
		public static final int KW_MEBBE = 44;
		public static final int KW_MKAY = 45;
		public static final int KW_MOD = 46;
		public static final int KW_NERF = 47;
		public static final int KW_NERFIN = 48;
		public static final int KW_NERFZ = 49;
		public static final int KW_NO = 50;
		public static final int KW_NOES = 51;
		public static final int KW_NOOB = 52;
		public static final int KW_NOT = 53;
		public static final int KW_NOW = 54;
		public static final int KW_NOWAI = 55;
		public static final int KW_NUMBAR = 56;
		public static final int KW_NUMBR = 57;
		public static final int KW_O = 58;
		public static final int KW_OBTW = 59;
		public static final int KW_OF = 60;
		public static final int KW_OIC = 61;
		public static final int KW_OMG = 62;
		public static final int KW_OMGWTF = 63;
		public static final int KW_OR = 64;
		public static final int KW_OUTTA = 65;
		public static final int KW_OVAR = 66;
		public static final int KW_OVARZ = 67;
		public static final int KW_PLZ = 68;
		public static final int KW_PRODUKT = 69;
		public static final int KW_QUOSHUNT = 70;
		public static final int KW_R = 71;
		public static final int KW_RLY = 72;
		public static final int KW_RTFM = 73;
		public static final int KW_SAEM = 74;
		public static final int KW_SAY = 75;
		public static final int KW_SMALLR = 76;
		public static final int KW_SMALR = 77;
		public static final int KW_SMOOSH = 78;
		public static final int KW_SO = 79;
		public static final int KW_STDIN = 80;
		public static final int KW_SUM = 81;
		public static final int KW_THAN = 82;
		public static final int KW_THX = 83;
		public static final int KW_TIEMZ = 84;
		public static final int KW_TIEMZD = 85;
		public static final int KW_TIL = 86;
		public static final int KW_TLDR = 87;
		public static final int KW_TROOF = 88;
		public static final int KW_U = 89;
		public static final int KW_UP = 90;
		public static final int KW_UPPIN = 91;
		public static final int KW_UPZ = 92;
		public static final int KW_VISIBLE = 93;
		public static final int KW_WAI = 94;
		public static final int KW_WATCHIN = 95;
		public static final int KW_WHATEVER = 96;
		public static final int KW_WIF = 97;
		public static final int KW_WILE = 98;
		public static final int KW_WIN = 99;
		public static final int KW_WON = 100;
		public static final int KW_WORD = 101;
		public static final int KW_WTF = 102;
		public static final int KW_XOR = 103;
		public static final int KW_YA = 104;
		public static final int KW_YARLY = 105;
		public static final int KW_YARN = 106;
		public static final int KW_YR = 107;

		private static final String[] KEYWORDS = {
			"A",
			"ALL",
			"AN",
			"AND",
			"ANY",
			"AWSUM",
			"BIGGR",
			"BIGR",
			"BOTH",
			"BTW",
			"BUKKIT",
			"BYES",
			"CAN",
			"DIAF",
			"DIFF",
			"DIFFRINT",
			"DUZ",
			"EITHER",
			"FAIL",
			"FOUND",
			"FROM",
			"GIMMEH",
			"GTFO",
			"HAI",
			"HAS",
			"HOW",
			"I",
			"IF",
			"IM",
			"IN",
			"INVISIBLE",
			"IS",
			"IT",
			"ITZ",
			"IZ",
			"JAVA",
			"KTHX",
			"KTHXBYE",
			"LETTAR",
			"LIEK",
			"LINE",
			"LOL",
			"MAEK",
			"MAH",
			"MEBBE",
			"MKAY",
			"MOD",
			"NERF",
			"NERFIN",
			"NERFZ",
			"NO",
			"NOES",
			"NOOB",
			"NOT",
			"NOW",
			"NOWAI",
			"NUMBAR",
			"NUMBR",
			"O",
			"OBTW",
			"OF",
			"OIC",
			"OMG",
			"OMGWTF",
			"OR",
			"OUTTA",
			"OVAR",
			"OVARZ",
			"PLZ",
			"PRODUKT",
			"QUOSHUNT",
			"R",
			"RLY",
			"RTFM",
			"SAEM",
			"SAY",
			"SMALLR",
			"SMALR",
			"SMOOSH",
			"SO",
			"STDIN",
			"SUM",
			"THAN",
			"THX",
			"TIEMZ",
			"TIEMZD",
			"TIL",
			"TLDR",
			"TROOF",
			"U",
			"UP",
			"UPPIN",
			"UPZ",
			"VISIBLE",
			"WAI",
			"WATCHIN",
			"WHATEVER",
			"WIF",
			"WILE",
			"WIN",
			"WON",
			"WORD",
			"WTF",
			"XOR",
			"YA",
			"YARLY",
			"YARN",
			"YR",
		};

		public static final int MODULE_JAVA = 1 << 0;
		public static final int VERSION_1_0 = 1 << 1;
		public static final int VERSION_1_1 = 1 << 2;
		public static final int VERSION_1_2 = 1 << 3;
		public static final int VERSION_1_3 = 1 << 4;

		private static final int MIN_VERSION_1_2 = VERSION_1_2 | VERSION_1_3;
		private static final int MIN_VERSION_1_1 = VERSION_1_1 | MIN_VERSION_1_2;
		private static final int ALL_VERSIONS = VERSION_1_0 | VERSION_1_1 | VERSION_1_2 | VERSION_1_3;

		private static final int[] KEYWORD_FLAGS = new int[KEYWORDS.length];
		private static final boolean CHARACTER_NAME_ESCAPE_SUPPORTED;

		public static final int TOKEN_EOF = -1;
		public static final int TOKEN_EOL = -2;
		private static final int RAW_TOKEN_CONTINUE = -3;
		public static final int TOKEN_IDENTIFIER = -4;
		public static final int TOKEN_INTEGER = -5;
		public static final int TOKEN_FLOAT = -6;
		public static final int TOKEN_STRING = -7;
		public static final int TOKEN_INTERPOLATED_STRING = -8;
		public static final int TOKEN_QUESTION = -9;
		public static final int TOKEN_EXCLAMATION = -10;
		public static final int TOKEN_EXCLAMATION_EXCLAMATION = -11;
		public static final int TOKEN_EXCLAMATION_QUESTION = -12;

		static {
			boolean charNameEscapeSupported = false;
			try {
				charNameEscapeSupported = UCharacter.class != null;
			} catch (NoClassDefFoundError ex) {
				charNameEscapeSupported = false;
			}

			CHARACTER_NAME_ESCAPE_SUPPORTED = charNameEscapeSupported;

			KEYWORD_FLAGS[KW_A] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_ALL] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_AN] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_AND] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_ANY] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_AWSUM] = VERSION_1_3;
			KEYWORD_FLAGS[KW_BIGGR] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_BIGR] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_BOTH] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[RAW_KW_BTW] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_BUKKIT] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_BYES] = VERSION_1_0;
			KEYWORD_FLAGS[KW_CAN] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_DIAF] = VERSION_1_0;
			KEYWORD_FLAGS[KW_DIFF] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_DIFFRINT] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_DUZ] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_EITHER] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_FAIL] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_FOUND] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_FROM] = VERSION_1_3;
			KEYWORD_FLAGS[KW_GIMMEH] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_GTFO] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_HAI] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_HAS] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_HOW] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_I] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_IF] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_IM] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_IN] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_INVISIBLE] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_IS] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_IT] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_ITZ] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_IZ] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_JAVA] = MODULE_JAVA;
			KEYWORD_FLAGS[KW_KTHX] = VERSION_1_0 | VERSION_1_3;
			KEYWORD_FLAGS[KW_KTHXBYE] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_LETTAR] = VERSION_1_0;
			KEYWORD_FLAGS[KW_LIEK] = VERSION_1_0 | VERSION_1_3;
			KEYWORD_FLAGS[KW_LINE] = VERSION_1_0;
			KEYWORD_FLAGS[KW_LOL] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_MAEK] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_MAH] = VERSION_1_0 | VERSION_1_1 | VERSION_1_3;
			KEYWORD_FLAGS[KW_MEBBE] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_MKAY] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_MOD] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_NERF] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_NERFIN] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_NERFZ] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_NO] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_NOES] = VERSION_1_3;
			KEYWORD_FLAGS[KW_NOOB] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_NOT] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_NOW] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_NOWAI] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_NUMBAR] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_NUMBR] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_O] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_OBTW] = MIN_VERSION_1_1;
			KEYWORD_FLAGS[KW_OF] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_OIC] = MIN_VERSION_1_1;
			KEYWORD_FLAGS[KW_OMG] = MIN_VERSION_1_1;
			KEYWORD_FLAGS[KW_OMGWTF] = MIN_VERSION_1_1;
			KEYWORD_FLAGS[KW_OR] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_OUTTA] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_OVAR] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_OVARZ] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_PLZ] = VERSION_1_3;
			KEYWORD_FLAGS[KW_PRODUKT] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_QUOSHUNT] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_R] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_RLY] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_RTFM] = VERSION_1_3;
			KEYWORD_FLAGS[KW_SAEM] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_SAY] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_SMALLR] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_SMALR] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_SMOOSH] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_SO] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_STDIN] = VERSION_1_0;
			KEYWORD_FLAGS[KW_SUM] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_THAN] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_THX] = VERSION_1_3;
			KEYWORD_FLAGS[KW_TIEMZ] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_TIEMZD] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_TIL] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_TLDR] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_TROOF] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_U] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_UP] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_UPPIN] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_UPZ] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_VISIBLE] = ALL_VERSIONS;
			KEYWORD_FLAGS[KW_WAI] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_WATCHIN] = VERSION_1_3;
			KEYWORD_FLAGS[KW_WHATEVER] = VERSION_1_3;
			KEYWORD_FLAGS[KW_WIF] = VERSION_1_3;
			KEYWORD_FLAGS[KW_WILE] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_WIN] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_WON] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_WORD] = VERSION_1_0;
			KEYWORD_FLAGS[KW_WTF] = MIN_VERSION_1_1;
			KEYWORD_FLAGS[KW_XOR] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_YA] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_YARLY] = VERSION_1_0 | VERSION_1_1;
			KEYWORD_FLAGS[KW_YARN] = MIN_VERSION_1_2;
			KEYWORD_FLAGS[KW_YR] = ALL_VERSIONS;
		}

		private TokenSource tokenSource;
		private int modules;
		private int lastToken;
		private boolean rereadToken;
		private StringBuilder builder = new StringBuilder();
		private StringBuilder builder2 = new StringBuilder();
		private char[] chars = new char[2];

		private int intValue;
		private float floatValue;
		private String stringValue;
		private List<String> interpolatedStringValue;

		public Tokenizer(TokenSource tokenSource) {
			this.tokenSource = tokenSource;
		}

		public void setTokenSource(TokenSource tokenSource) {
			this.tokenSource = tokenSource;
		}

		public void addModule(int module) {
			modules |= module;
		}

		public boolean hasModule(int module) {
			return (modules & module) != 0;
		}

		public boolean hasMinimumVersion(int version) {
			return (modules & -version) != 0;
		}

		public void addVersion(int version) {
			addModule(version);
		}

		public boolean hasVersion(int version) {
			return hasModule(version);
		}

		public void error(String message)/* throws IOException*/ {
			//throw new ParseException(tokenSource.path, tokenSource.line, tokenSource.column, message);
			cmd.stdOut.print(tokenSource.path + ":" + tokenSource.line + ":" + tokenSource.column);
		}

		public void error(String message, Throwable throwable) /*throws IOException*/ {
			//ParseException ex = new ParseException(tokenSource.path, tokenSource.line, tokenSource.column, message);
			//ex.initCause(throwable);
			//throw ex;
			cmd.stdOut.print(tokenSource.path + ":" + tokenSource.line + ":" + tokenSource.column);
		}

		public void errorExpected(String what) throws IOException {
			error("expected " + what + ", got " + getTokenName(lastToken));
		}

		private String getTokenName(int token) {
			switch (token) {
				case TOKEN_EOF:
					return "end of file";

				case TOKEN_EOL:
					return "end of line";

				case TOKEN_IDENTIFIER:
					return "identifier " + stringValue;

				case TOKEN_INTEGER:
					return "NUMBR";

				case TOKEN_FLOAT:
					return "NUMBAR";

				case TOKEN_STRING:
				case TOKEN_INTERPOLATED_STRING:
					return "YARN";

				case TOKEN_QUESTION:
					return "?";

				case TOKEN_EXCLAMATION:
					return "!";

				case TOKEN_EXCLAMATION_EXCLAMATION:
					return "!!";

				case TOKEN_EXCLAMATION_QUESTION:
					return "!?";

				default:
					return "keyword " + KEYWORDS[token];
			}
		}

		private int peek() throws IOException {
			if (tokenSource.next == -2) {
				tokenSource.next = tokenSource.reader.read();
			}

			return tokenSource.next;
		}

		private int read() throws IOException {
			int ch;

			if (tokenSource.next != -2) {
				ch = tokenSource.next;
				tokenSource.next = -2;
			} else {
				ch = tokenSource.reader.read();
			}

			if (ch == '\r') {
				tokenSource.line++;
				tokenSource.column = 1;

				tokenSource.next = tokenSource.reader.read();
				if (tokenSource.next == '\n') {
					tokenSource.next = -2;
				}
			} else if (ch == '\n') {
				tokenSource.line++;
				tokenSource.column = 1;
			} else {
				tokenSource.column++;
			}

			tokenSource.last = ch;
			return ch;
		}

		public void unreadToken() {
			rereadToken = true;
		}

		private boolean tryRead(char ch) throws IOException {
			if (peek() != ch) {
				return false;
			}

			read();
			return true;
		}

		public void readOBTW() throws IOException {
			boolean ok = false;

			for (int ch = read();; ch = read()) {
				if (ch == '\r' || ch == '\n') {
					ok = true;
				}

				if (ok) {
					if (!Character.isWhitespace(ch)) {
						ok = false;
					} else if (tryRead('T')
							&& tryRead('L')
							&& tryRead('D')
							&& tryRead('R')
							&& ((ch = peek()) == -1
								|| Character.isWhitespace(ch)
								|| ch == ','
								|| (ch == '.' && hasMinimumVersion(VERSION_1_0)))) {
						return;
					}
				}

				if (ch == -1) {
					error("unterminated OBTW");
				}
			}
		}

		public int readToken() throws IOException {
			if (rereadToken) {
				rereadToken = false;
				return lastToken;
			}

			int token = readRawToken();

			if (token == RAW_KW_BTW) {
				for (int ch = read();; ch = read()) {
					if (ch == -1 || ch == '\r' || ch == '\n') {
						token = TOKEN_EOL;
						break;
					}
				}
			} else if (token == RAW_TOKEN_CONTINUE) {
				do {
					token = readRawToken();
					if (token != TOKEN_EOL) {
						lastToken = token;
						errorExpected("newline after ...");
					}

					token = readRawToken();
					if (token == TOKEN_EOL) {
						lastToken = token;
						errorExpected("command after ...");
					}
				} while (token == RAW_TOKEN_CONTINUE);

				if (token == RAW_KW_BTW) {
					token = TOKEN_EOL;
				}
			}

			lastToken = token;
			return token;
		}

		public int readRawToken() throws IOException {
			int ch = read();

			while (Character.isWhitespace(ch)) {
				if (ch == '\r' || ch == '\n') {
					return TOKEN_EOL;
				}

				ch = read();
			}

			if (ch == -1) {
				return -1;
			}

			if (ch == ',' && hasMinimumVersion(VERSION_1_1)) {
				return TOKEN_EOL;
			}

			if (ch == '\u2026') {
				return RAW_TOKEN_CONTINUE;
			}

			if ((ch == '\u203d' || ch == '\u2049') && hasVersion(VERSION_1_3)) {
				intValue = Character.isWhitespace(peek()) ? -1 : -2;
				return TOKEN_EXCLAMATION_QUESTION;
			}

			if (ch == '?') {
				if (Character.isWhitespace(tokenSource.last)) {
					error("unexpected whitespace before ?");
				}

				return TOKEN_QUESTION;
			}

			if (ch == '!') {
				if (Character.isWhitespace(tokenSource.last)) {
					error("unexpected whitespace before !");
				}

				if ((ch = peek()) == '!') {
					read();

					if ((ch = peek()) >= '0' && ch <= '9') {
						builder.setLength(0);
						builder.append((char)read());

						while ((ch = peek()) >= '0' && ch <= '9') {
							builder.append((char)read());
						}

						try {
							intValue = Integer.parseInt(builder.toString());
						} catch (NumberFormatException ex) {
							error("NUMBR out of range", ex);
						}
					} else if (Character.isWhitespace(ch)) {
						intValue = -1;
					} else {
						intValue = -2;
					}

					return TOKEN_EXCLAMATION_EXCLAMATION;
				}

				if (hasVersion(VERSION_1_3) && ch == '?') { // bukkit
					read();
					intValue = Character.isWhitespace(peek()) ? -1 : -2;
					return TOKEN_EXCLAMATION_QUESTION;
				}

				return TOKEN_EXCLAMATION;
			}

			if (ch == '.') {
				if (hasMinimumVersion(VERSION_1_2)) {
					if (peek() == '.') {
						read();
						if (read() != '.') {
							error("expected ...");
						}

						return RAW_TOKEN_CONTINUE;
					}

					if (hasVersion(VERSION_1_0) && ((ch = peek()) < '0' || ch > '9')) {
						return TOKEN_EOL;
					}

					builder.setLength(0);
					builder.append('.');
					readFloat();
					return TOKEN_FLOAT;
				}

				if (hasVersion(VERSION_1_0)) {
					return TOKEN_EOL;
				}
			}

			if (ch == '-') {
				builder.setLength(0);
				builder.append('-');

				if ((ch = read()) == '.' && hasMinimumVersion(VERSION_1_1)) {
					builder.append('.');
					readFloat();
					return TOKEN_FLOAT;
				}

				if (ch < '0' || ch > '9') {
					error("expected NUMBR");
				}

				builder.append((char)ch);
				return readNumeric();
			}

			if (ch >= '0' && ch <= '9') {
				builder.setLength(0);
				builder.append((char)ch);
				return readNumeric();
			}

			if (ch == '"') {
				builder.setLength(0);
				interpolatedStringValue = null;

				for (ch = read(); ch != '"'; ch = read()) {
					boolean escape = ch == ':' && hasMinimumVersion(VERSION_1_2);

					if (escape) {
						ch = read();
					}

					if (ch == -1 || ch == '\r' || ch == '\n') {
						error("unterminated string");
					}

					if (!escape) {
						builder.append((char)ch);
					} else {
						switch (ch) {
							case ')':
								builder.append('\n');
								break;

							case '>':
								builder.append('\t');
								break;

							case 'o':
								builder.append('\007');
								break;

							case '"':
								builder.append('"');
								break;

							case ':':
								builder.append(':');
								break;

							case '(': {
								int value = 0;

								switch (ch = read()) {
									case '0': case '1': case '2': case '3': case '4':
									case '5': case '6': case '7': case '8': case '9':
										value = ch - '0';
										break;

									case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
										value = ch - 'a' + 10;
										break;

									case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
										value = ch - 'A' + 10;
										break;

									default:
										error("expected hex string");
										break;
								}

								loop: for (;;) {
									switch (peek()) {
										case '0': case '1': case '2': case '3': case '4':
										case '5': case '6': case '7': case '8': case '9':
											value = (value << 4) + read() - '0';
											break;

										case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
											value = (value << 4) + read() - 'a' + 10;
											break;

										case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
											value = (value << 4) + read() - 'A' + 10;
											break;

										case ')':
											read();
											break loop;

										default:
											error("expected hex string");
											break;
									}
								}

								if (Character.toChars(value, chars, 0) == 1) {
									builder.append(chars[0]);
								} else {
									builder.append(chars[0]);
									builder.append(chars[1]);
								}
								break;
							}

							case '{': {
								if (interpolatedStringValue == null) {
									interpolatedStringValue = new ArrayList<String>();
								}

								interpolatedStringValue.add(builder.toString());
								interpolatedStringValue.add(readIdentifier(read()));
								if (read() != '}') {
									error("expected end of interpolated variable escape sequence");
								}
								builder.setLength(0);
								break;
							}

							case '[': {
								if (!CHARACTER_NAME_ESCAPE_SUPPORTED) {
									error("unsupported character name escape sequence");
								}

								builder2.setLength(0);

								while ((ch = read()) != -1 && ch != ']' && ch != '\r' && ch != '\n') {
									builder2.append((char)ch);
								}

								if (ch != ']') {
									error("expected end of character name escape sequence");
								}

								String name = builder2.toString();
								int value = UCharacter.getCharFromName(name);

								if (value == -1) {
									error("unknown character name: " + name);
								}

								if (Character.toChars(value, chars, 0) == 1) {
									builder.append(chars[0]);
								} else {
									builder.append(chars[0]);
									builder.append(chars[1]);
								}
								break;
							}

							default:
								error("invalid escape character");
						}
					}
				}

				if (interpolatedStringValue != null) {
					interpolatedStringValue.add(builder.toString());
					return TOKEN_INTERPOLATED_STRING;
				}

				stringValue = builder.toString();
				return TOKEN_STRING;
			}

			stringValue = readIdentifier(ch);

			int keyword = Arrays.binarySearch(KEYWORDS, stringValue);
			if (keyword >= 0 && (KEYWORD_FLAGS[keyword] & modules) != 0) {
				return keyword;
			}

			return TOKEN_IDENTIFIER;
		}

		private int readNumeric() throws IOException {
			for (int ch = peek();; ch = peek()) {
				if (ch == '.' && hasMinimumVersion(VERSION_1_2)) {
					builder.append((char)read());
					readFloat();
					return TOKEN_FLOAT;
				}

				if (ch < '0' || ch > '9') {
					try {
						intValue = Integer.parseInt(builder.toString());
					} catch (NumberFormatException ex) {
						error("NUMBR out of range", ex);
					}

					return TOKEN_INTEGER;
				}

				builder.append((char)read());
			}
		}

		private void readFloat() throws IOException {
			int ch = read();
			if (ch < '0' || ch > '9') {
				error("expected NUMBAR");
			}

			builder.append((char)ch);

			for (ch = peek(); ch >= '0' && ch <= '9'; ch = peek()) {
				builder.append((char)read());
			}

			try {
				floatValue = Float.parseFloat(builder.toString());
			} catch (NumberFormatException ex) {
				error("NUMBAR out of range", ex);
			}
		}

		private static boolean isIdentifierStart(int ch) {
			return (ch >= 'A' && ch <= 'Z')
				|| (ch >= 'a' && ch <= 'z');
		}

		private static boolean isIdentifierPart(int ch) {
			return (ch >= 'A' && ch <= 'Z')
				|| (ch >= 'a' && ch <= 'z')
				|| (ch >= '0' && ch <= '9')
				|| ch == '_';
		}

		private String readIdentifier(int ch) throws IOException {
			if (!isIdentifierStart(ch)) {
				error("invalid character: " + ch + " ('" + (char)ch + "')");
			}

			builder.setLength(0);
			builder.append((char)ch);

			for (ch = peek(); isIdentifierPart(ch); ch = peek()) {
				builder.append((char)read());
			}

			return builder.toString();
		}

		public int getInt() {
			return intValue;
		}

		public float getFloat() {
			return floatValue;
		}

		public String getString() {
			return stringValue;
		}

		public List<String> getInterpolatedString() {
			return interpolatedStringValue;
		}
	}
}
