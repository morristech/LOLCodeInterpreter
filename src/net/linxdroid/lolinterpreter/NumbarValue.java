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

import java.util.Formatter;
import java.util.Locale;

class NumbarValue extends AbstractTypedScalarValue {
	private float value;

	public NumbarValue(float value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return super.toString() + '[' + value + ']';
	}

	@Override
	public int hashCode() {
		return (int)value;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		Value otherValue = (Value)other;
		return otherValue.isNumeric() && value == otherValue.getFloat();
	}

	public String getType() {
		return "NUMBAR";
	}

	public boolean getBoolean() {
		return value != 0;
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

	public int getInt() {
		return (int)value;
	}

	@Override
	public boolean isNumbar() {
		return true;
	}

	@Override
	public boolean isMathNumbar() {
		return true;
	}

	public float getFloat() {
		return value;
	}

	@Override
	public Value castToNumbar() {
		return this;
	}

	public String getString() {
		String string = new Formatter(Locale.US).format("%f", value).toString();
		int length = string.length();

		if (length >= 7 && string.charAt(length - 7) == '.') {
			return string.substring(0, length - 4);
		}

		return string;
	}
}
