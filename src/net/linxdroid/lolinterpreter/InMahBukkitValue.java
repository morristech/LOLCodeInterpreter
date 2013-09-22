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

import java.util.ArrayList;
import java.util.List;

class InMahBukkitValue extends ArrayList<Value> implements InMahBukkit {
	private static final long serialVersionUID = 0;

	public InMahBukkitValue(Value value) {
		add(value);
	}

	private InMahBukkitValue(InMahBukkitValue parent) {
		super(parent);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(hashCode()) + super.toString();
	}

	@Override
	public int hashCode() {
		return get(0).hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return get(0).equals(other);
	}

	public String getType() {
		return "BUKKIT";
	}

	public boolean isTroof() {
		return get(0).isTroof();
	}

	public boolean getBoolean() {
		return get(0).getBoolean();
	}

	public Value castToTroof() {
		return get(0).castToTroof();
	}

	public boolean isNumeric() {
		return get(0).isNumeric();
	}

	public boolean isNumbr() {
		return get(0).isNumbr();
	}

	public int getInt() {
		return get(0).getInt();
	}

	public Value castToNumbr() {
		return get(0).castToNumbr();
	}

	public Value castToMathNumbr() {
		return get(0).castToMathNumbr();
	}

	public boolean isNumbar() {
		return get(0).isNumbar();
	}

	public float getFloat() {
		return get(0).getFloat();
	}

	public Value castToNumbar() {
		return get(0).castToNumbar();
	}

	public boolean isMathNumbar() {
		return get(0).isMathNumbar();
	}

	public boolean isYarn() {
		return get(0).isYarn();
	}

	public String getString() {
		return get(0).getString();
	}

	public Value castToYarn() {
		return get(0).castToYarn();
	}

	public void declareSlot(Value index, Value value) {
		setSlot(index, value);
	}

	public void setSlot(Value index, Value value) {
		if (!index.isNumeric()) {
			throw new LOLCodeException(LOLCodeException.BAD_IN_MAH_TYPE, index.getType());
		}

		assignInMah(index, value);
	}

	public Value getSlot(Value index) {
		if (index.isNumeric()) {
			int indexValue = index.getInt();

			if (indexValue >= 0 && indexValue < size()) {
				return get(indexValue);
			}
		}

		return NoobValue.INSTANCE;
	}

	public int getNumSlots() {
		return size();
	}

	public Value getSlots() {
		InMahBukkitValue slots = new InMahBukkitValue(new NumbrValue(0));
		for (int i = size(); i > 1; i--) {
			slots.add(new NumbrValue(1));
		}

		return slots;
	}

	public InMahBukkit getInMahBukkit(InMahBukkitFactory factory) {
		return this;
	}

	public Value assign(Value value) {
		set(0, value);
		return this;
	}

	private int getIndex(Value index) {
		if (!index.isNumeric()) {
			throw new LOLCodeException(LOLCodeException.BAD_IN_MAH_TYPE, index.getType());
		}

		return index.getInt();
	}

	public Value inMah(Value index) {
		return get(getIndex(index));
	}

	public void assignInMah(Value index, Value value) {
		int indexValue = getIndex(index);
		if (indexValue < 0) {
			throw new LOLCodeException(LOLCodeException.BAD_INDEX, "Index: " + indexValue);
		}

		ensureCapacity(indexValue + 1);

		for (int i = indexValue - size(); i-- >= 0;) {
			add(NoobValue.INSTANCE);
		}

		set(indexValue, value);
	}

	public InMahBukkit assignInMahBukkitInMah(Value index, InMahBukkitFactory factory) {
		InMahBukkit bukkit;
		int indexValue = getIndex(index);

		if (indexValue < size()) {
			bukkit = get(indexValue).getInMahBukkit(factory);
			set(indexValue, bukkit);
		} else {
			ensureCapacity(indexValue + 1);

			for (int i = indexValue - size(); i-- > 0;) {
				add(NoobValue.INSTANCE);
			}

			bukkit = factory.create(NoobValue.INSTANCE);
			add(bukkit);
		}

		return bukkit;
	}

	public final Value call(Value target) {
		return this;
	}

	public final Value call(Value target, List<Value> arguments) {
		throw new LOLCodeException(LOLCodeException.BAD_CALL_TYPE, getType());
	}
}
