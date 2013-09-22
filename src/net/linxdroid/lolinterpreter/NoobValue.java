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

class NoobValue extends AbstractScalarValue {
	public static final Value INSTANCE = new NoobValue();

	private Value numbr = new NumbrValue(0);
	private Value numbar = new NumbarValue(0);
	private Value yarn = new YarnValue("");

	private NoobValue() { }

	public String getType() {
		return "NOOB";
	}

	public boolean isTroof() {
		return false;
	}

	public boolean getBoolean() {
		return false;
	}

	public Value castToTroof() {
		return TroofValue.FAIL;
	}

	public boolean isNumeric() {
		return false;
	}

	public boolean isNumbr() {
		return false;
	}

	public int getInt() {
		throw new LOLCodeException(LOLCodeException.BAD_NOOB_USE);
	}

	public Value castToNumbr() {
		return numbr;
	}

	public boolean isNumbar() {
		return false;
	}

	public boolean isMathNumbar() {
		return false;
	}

	public float getFloat() {
		throw new LOLCodeException(LOLCodeException.BAD_NOOB_USE);
	}

	public Value castToNumbar() {
		return numbar;
	}

	public boolean isYarn() {
		return false;
	}

	public String getString() {
		throw new LOLCodeException(LOLCodeException.BAD_NOOB_USE);
	}

	public Value castToYarn() {
		return yarn;
	}
}
