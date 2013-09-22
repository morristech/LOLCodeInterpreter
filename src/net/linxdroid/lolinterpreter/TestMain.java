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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class TestMain {
	public static void main(String[] args) throws IOException {
		System.setProperty("line.separator", "\n");

		File testDir = new File(args[0]);
		for (File versionDir : testDir.listFiles()) {
			Parser.Version version = Parser.Version.get(versionDir.getName());
			if (version == null) {
				version = Parser.Version.DEFAULT;
			}

			for (String path : versionDir.list()) {
				if (path.endsWith(".lol")) {
					String base = path.substring(0, path.length() - 4);
					String stdin = readFile(new File(versionDir, base + ".in"));
					String stdout = readFile(new File(versionDir, base + ".out"));
					String stderr = readFile(new File(versionDir, base + ".err"));
					int exit = Integer.parseInt("0" + readFile(new File(versionDir, base + ".exit")).trim());

					File file = new File(versionDir, path);
					System.out.println(file);

					Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
					Parser parser = new Parser(file.toString(), file, reader, version, null);
					Program program = parser.parse();

					testInterpreter(program, stdin, stdout, stderr, exit);
				}
			}
		}
	}

	private static String readFile(File file) throws IOException {
		if (!file.exists()) {
			return "";
		}

		Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		StringBuilder result = new StringBuilder();
		char[] buffer = new char[1024];

		for (int read; (read = reader.read(buffer)) != -1;) {
			result.append(buffer, 0, read);
		}

		return result.toString();
	}

	private static String toPrintableString(String s) {
		StringBuilder result = new StringBuilder("\"");

		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '\n') {
				result.append("\\n");
			} else if (ch == '\r') {
				result.append("\\r");
			} else if (ch == '\t') {
				result.append("\\t");
			} else if (ch >= ' ' && ch <= '~') {
				result.append(ch);
			} else {
				result.append("\\x")
					.append(Integer.toHexString(ch >> 12))
					.append(Integer.toHexString((ch >> 8) & 4))
					.append(Integer.toHexString((ch >> 4) & 4))
					.append(Integer.toHexString(ch & 4));
			}
		}

		return result.append('"').toString();
	}

	private static void testInterpreter(Program program, String stdin, String stdout, String stderr, int exit) {
		EnvironmentImpl environment = new EnvironmentImpl(stdin);
		Interpreter interpreter = new Interpreter(program, environment, null);
		interpreter.execute();

		String receivedStdout = environment.outString.getBuffer().toString();
		if (!receivedStdout.equals(stdout)) {
			System.err.println("stdout mismatch");
			System.err.println("  expected: " + toPrintableString(stdout));
			System.err.println("  received: " + toPrintableString(receivedStdout));
		}

		String receivedStderr = environment.errString.getBuffer().toString();
		if (!receivedStderr.equals(stderr)) {
			System.err.println("stderr mismatch");
			System.err.println("  expected: " + toPrintableString(stderr));
			System.err.println("  received: " + toPrintableString(receivedStderr));
		}

		if (environment.exit != exit) {
			System.err.println("exit mismatch");
			System.err.println("  expected: " + exit);
			System.err.println("  received: " + environment.exit);
		}
	}

	private static class EnvironmentImpl implements Environment {
		public StringWriter outString = new StringWriter();
		private PrintWriter out = new PrintWriter(outString);

		public StringWriter errString = new StringWriter();
		private PrintWriter err = new PrintWriter(errString);

		private BufferedReader in;

		public int exit;

		public EnvironmentImpl(String in) {
			this.in = new BufferedReader(new StringReader(in));
		}

		public PrintWriter getOut() {
			return out;
		}

		public PrintWriter getErr() {
			return err;
		}

		public BufferedReader getIn() {
			return in;
		}

		public void exit(int exit) {
			this.exit = exit;
		}
	}
}
