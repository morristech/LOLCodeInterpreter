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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

public class Main implements Environment {
	private PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
	private PrintWriter err = new PrintWriter(new OutputStreamWriter(System.err));
	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	static Cmd cmd;

	public static void main(HashMap<Integer, Object> args) 
	{
		cmd = new Cmd(args);
		
		if(cmd.argv[1].toString() != null){
			main(cmd.argv[1].toString());
		}else{
			
		}
	}
	
	public static void main(String args) {
		boolean debugExceptions = false;
		Parser.Version version = Parser.Version.DEFAULT;

		String path = args;

		try {
			File file = new File(path);
			InputStream input = new FileInputStream(file);
			Reader reader = new InputStreamReader(input, "UTF-8");
			Parser parser = new Parser(path, file, new BufferedReader(reader), version, cmd);
			Program program = parser.parse();
			Interpreter interpreter = new Interpreter(program, new Main(), cmd);
			interpreter.execute();
		} catch (FileNotFoundException ex) {
			if (debugExceptions) {
				ex.printStackTrace();
			} else {
				error(path + ": file not found");
			}

			System.exit(1);
		} catch (ParseException ex) {
			if (debugExceptions) {
				ex.printStackTrace();
			} else {
				System.err.println(ex.getMessage());
			}

			System.exit(3);
		} catch (IOException ex) {
			if (debugExceptions) {
				ex.printStackTrace();
			} else {
				String message = ex.getMessage();

				if (message == null) {
					error(path + ": I/O error");
				} else {
					error(path + ": " + message);
				}
			}

			System.exit(2);
		}
	}

	private static void error(String s) {
		System.err.println("lolcode: " + s);
	}

	private static void help(PrintStream out) {
		out.println("Java LOLCODE  Copyright (C) 2007-2011  Brett Kail");
		out.println("This program comes with ABSOLUTELY NO WARRANTY");
		out.println("This is free software, and you are welcome to redistribute it");
		out.println("under certain conditions.");
		out.println();
		out.println("Usage: java -jar lolcode.jar [OPTION]... FILE");
		out.println("Read and execute a lolcode program");
		out.println();
		out.println("  -ge              debug user exceptions");
		out.println("  -h, -?, --help   print this help text");
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

	public void exit(int code) {
		System.exit(code);
	}
}
