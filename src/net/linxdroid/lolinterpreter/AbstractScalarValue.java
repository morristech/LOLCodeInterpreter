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

import java.util.List;

abstract class AbstractScalarValue implements Value {
	public Value castToMathNumbr() {
		throw new LOLCodeException(LOLCodeException.BAD_MATH_TYPE, getType());
	}

	public final void declareSlot(Value index, Value value) {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_TYPE, getType());
	}

	public void setSlot(Value index, Value value) {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_TYPE, getType());
	}

	public Value getSlot(Value index) {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_TYPE, getType());
	}

	public int getNumSlots() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_TYPE, getType());
	}

	public Value getSlots() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_TYPE, getType());
	}

	public final InMahBukkit getInMahBukkit(InMahBukkitFactory factory) {
		return factory.create(this);
	}

	public final Value assign(Value value) {
		return value;
	}

	public final Value inMah(Value index) {
		if (!index.isNumeric()) {
			throw new LOLCodeException(LOLCodeException.BAD_IN_MAH_TYPE, index.getType());
		}

		int indexValue = index.getInt();
		if (indexValue == 0) {
			return this;
		}

		throw new LOLCodeException(LOLCodeException.BAD_INDEX, "Index: " + indexValue + ", Size: 1");
	}

	public Value call(Value target) {
		return this;
	}

	public Value call(Value target, List<Value> arguments) {
		throw new LOLCodeException(LOLCodeException.BAD_CALL_TYPE, getType());
	}
}
