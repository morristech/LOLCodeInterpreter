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

import java.util.Collections;
import java.util.List;

class FunctionValue extends AbstractTypedScalarValue {
	private Function function;

	public FunctionValue(Function function) {
		this.function = function;
	}

	public String getType() {
		return "FUNCTION";
	}

	public boolean getBoolean() {
		throw new UnsupportedOperationException();
	}

	public int getInt() {
		throw new UnsupportedOperationException();
	}

	public float getFloat() {
		throw new UnsupportedOperationException();
	}

	public String getString() {
		throw new UnsupportedOperationException();
	}

	public Value call(Value target) {
		return function.call(target, Collections.<Value>emptyList());
	}

	public Value call(Value target, List<Value> arguments) {
		return function.call(target, arguments);
	}
}
