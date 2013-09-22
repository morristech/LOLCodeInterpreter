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

class YarnValue extends AbstractTypedScalarValue {
	private String value;

	public YarnValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return super.toString() + '[' + value + ']';
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		Value otherValue = (Value)other;
		return otherValue.isYarn() && value.equals(otherValue.getString());
	}

	public String getType() {
		return "YARN";
	}

	public boolean getBoolean() {
		return value.length() != 0;
	}

	public int getInt() {
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);

			if ((ch < '0' || ch > '9') && ch != '-') {
				throw new LOLCodeException(LOLCodeException.BAD_NUMBR_FORMAT, value);
			}
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new LOLCodeException(LOLCodeException.BAD_NUMBR_FORMAT, value);
		}
	}

	@Override
	public Value castToMathNumbr() {
		return castToNumbr();
	}

	@Override
	public boolean isMathNumbar() {
		return value.indexOf('.') != -1;
	}

	public float getFloat() {
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);

			if ((ch < '0' || ch > '9') && ch != '-' && ch != '.') {
				throw new LOLCodeException(LOLCodeException.BAD_NUMBAR_FORMAT, value);
			}
		}

		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			throw new LOLCodeException(LOLCodeException.BAD_NUMBAR_FORMAT, value);
		}
	}

	@Override
	public boolean isYarn() {
		return true;
	}

	public String getString() {
		return value;
	}

	@Override
	public Value castToYarn() {
		return this;
	}
}
