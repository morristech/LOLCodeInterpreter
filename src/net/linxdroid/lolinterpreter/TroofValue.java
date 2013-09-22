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

class TroofValue extends AbstractTypedScalarValue {
	public static final Value WIN = new TroofValue(true);
	public static final Value FAIL = new TroofValue(false);

	private boolean value;
	private int intValue;
	private Value numbr;
	private Value numbar;
	private String stringValue;
	private Value yarn;

	public static Value getInstance(boolean value) {
		return value ? WIN : FAIL;
	}

	private TroofValue(boolean value) {
		this.value = value;

		if (value) {
			intValue = 1;
			stringValue = "WIN";
		} else {
			intValue = 0;
			stringValue = "";
		}

		numbr = new NumbrValue(intValue);
		numbar = new NumbarValue(intValue);
		yarn = new YarnValue(stringValue);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + value + ']';
	}

	public String getType() {
		return "TROOF";
	}

	@Override
	public boolean isTroof() {
		return true;
	}

	public boolean getBoolean() {
		return value;
	}

	@Override
	public Value castToTroof() {
		return this;
	}

	public int getInt() {
		return intValue;
	}

	@Override
	public Value castToNumbr() {
		return numbr;
	}

	public float getFloat() {
		return intValue;
	}

	@Override
	public Value castToNumbar() {
		return numbar;
	}

	public String getString() {
		return stringValue;
	}

	@Override
	public Value castToYarn() {
		return yarn;
	}
}
