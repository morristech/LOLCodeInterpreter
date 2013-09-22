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

class NumbrValue extends AbstractTypedScalarValue {
	private int value;

	public NumbrValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return super.toString() + '[' + value + ']';
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		Value otherValue = (Value)other;

		if (otherValue.isNumbr()) {
			return value == otherValue.getInt();
		}

		if (otherValue.isNumbar()) {
			return value == otherValue.getFloat();
		}

		return false;
	}

	public String getType() {
		return "NUMBR";
	}

	public boolean getBoolean() {
		return value != 0;
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

	@Override
	public boolean isNumbr() {
		return true;
	}

	public int getInt() {
		return value;
	}

	@Override
	public Value castToNumbr() {
		return this;
	}

	@Override
	public Value castToMathNumbr() {
		return this;
	}

	public String getString() {
		return String.valueOf(value);
	}
}
