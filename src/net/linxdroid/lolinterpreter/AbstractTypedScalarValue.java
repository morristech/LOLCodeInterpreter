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

abstract class AbstractTypedScalarValue extends AbstractScalarValue {
	public Value castToTroof() {
		return TroofValue.getInstance(getBoolean());
	}

	public boolean isTroof() {
		return false;
	}

	public boolean isNumeric() {
		return false;
	}

	public boolean isNumbr() {
		return false;
	}

	public Value castToNumbr() {
		return new NumbrValue(getInt());
	}

	public boolean isNumbar() {
		return false;
	}

	public boolean isMathNumbar() {
		return false;
	}

	public float getFloat() {
		return getInt();
	}

	public Value castToNumbar() {
		return new NumbarValue(getFloat());
	}

	public boolean isYarn() {
		return false;
	}

	public Value castToYarn() {
		return new YarnValue(getString());
	}
}