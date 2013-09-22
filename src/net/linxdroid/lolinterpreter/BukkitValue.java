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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class BukkitValue extends LinkedHashMap<Value, Value> implements InMahBukkit, Bukkit {
	private static final long serialVersionUID = 0;
	private static final Bukkit DEFAULT_PARENT = new EmptyParent();

	private Bukkit parent;
	private int size;
	private Value[] values;

	public BukkitValue() {
		this(DEFAULT_PARENT);
	}

	public BukkitValue(Bukkit parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append('@').append(Integer.toHexString(hashCode()));

		if (values == null) {
			builder.append(super.toString());
		} else {
			builder.append('[').append(values[0]);
			for (int i = 1; i < size; i++) {
				builder.append(", ").append(values[i]);
			}
			builder.append(']');
		}

		return builder.toString();
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	public String getType() {
		return "BUKKIT";
	}

	public boolean isTroof() {
		return false;
	}

	public boolean getBoolean() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public Value castToTroof() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public boolean isNumeric() {
		return false;
	}

	public boolean isNumbr() {
		return false;
	}

	public int getInt() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public Value castToNumbr() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public Value castToMathNumbr() {
		throw new LOLCodeException(LOLCodeException.BAD_MATH_TYPE, getType());
	}

	public boolean isNumbar() {
		return false;
	}

	public boolean isMathNumbar() {
		return false;
	}

	public float getFloat() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public Value castToNumbar() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public boolean isYarn() {
		return false;
	}

	public String getString() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	public Value castToYarn() {
		throw new LOLCodeException(LOLCodeException.BAD_BUKKIT_USE);
	}

	private void convertValues() {
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				put(new NumbrValue(i), values[i]);
			}
		}

		values = null;
	}

	public Bukkit getParent() {
		return parent;
	}

	public boolean containsSlot(Value index) {
		return containsKey(index) || parent.containsSlot(index);
	}

	public void declareSlot(Value index, Value value) {
		if (values != null) {
			convertValues();
		}

		put(index, value);
	}

	private boolean setSlot(int index, Value value) {
		if (values == null) {
			if (!isEmpty() || index != 0) {
				return false;
			}

			values = new Value[8];
			values[0] = value;
			size = 1;
			return true;
		}

		if (index == size) {
			if (size == values.length) {
				Value[] newValues = new Value[size * 2];
				System.arraycopy(values, 0, newValues, 0, size);
				values = newValues;
			}

			values[size++] = value;
			return true;
		}

		convertValues();
		return false;
	}

	public void setSlot(Value index, Value value) {
		if (!index.isNumeric()) {
			if (!containsSlot(index)) {
				throw new LOLCodeException(LOLCodeException.BAD_SLOT);
			}

			put(index, value);
		} else if (!setSlot(index.getInt(), value)) {
			if (index.isNumbar()) {
				index = index.castToNumbr();
			}

			put(index, value);
		}
	}

	public Value getSlot(Value index) {
		if (values != null) {
			if (index.isNumeric()) {
				int indexValue = index.getInt();

				if (indexValue >= 0 && indexValue < size) {
					Value value = values[indexValue];

					if (value != null) {
						return value;
					}
				}
			} else if (index.isNumbar()) {
				index = index.castToNumbr();
			}
		} else {
			if (index.isNumbar()) {
				index = index.castToNumbr();
			}

			Value value = get(index);
			if (value != null) {
				return value;
			}
		}

		return parent.getSlot(index);
	}

	public int getNumSlots() {
		if (parent == DEFAULT_PARENT) {
			if (values != null) {
				return size;
			}

			return size();
		}

		return getSlots().getNumSlots();
	}

	public void getSlots(Set<Value> added, BukkitValue slots) {
		parent.getSlots(added, slots);

		if (values != null) {
			for (int i = 0; i < size; i++) {
				Value slot = new NumbrValue(i);
				if (added.add(slot)) {
					slots.setSlot(added.size() - 1, slot);
				}
			}
		} else {
			for (Map.Entry<Value, Value> entry : entrySet()) {
				Value slot = entry.getKey();
				if (added.add(slot)) {
					slots.setSlot(added.size() - 1, slot);
				}
			}
		}
	}

	public Value getSlots() {
		BukkitValue slots = new BukkitValue();
		getSlots(new HashSet<Value>(), slots);
		return slots;
	}

	public InMahBukkit getInMahBukkit(InMahBukkitFactory factory) {
		return this;
	}

	public Value assign(Value value) {
		return value;
	}

	public Value inMah(Value index) {
		return getSlot(index);
	}

	public void assignInMah(Value index, Value value) {
		declareSlot(index, value);
	}

	public InMahBukkit assignInMahBukkitInMah(Value index, InMahBukkitFactory factory) {
		Value value = getSlot(index);
		InMahBukkit bukkit;

		if (value == null) {
			bukkit = factory.create(NoobValue.INSTANCE);
		} else {
			bukkit = value.getInMahBukkit(factory);
		}

		declareSlot(index, bukkit);
		return bukkit;
	}

	private static class EmptyParent implements Bukkit {
		public EmptyParent() { }

		public Bukkit getParent() {
			return null;
		}

		public boolean containsSlot(Value index) {
			return false;
		}

		public Value getSlot(Value index) {
			throw new LOLCodeException(LOLCodeException.BAD_SLOT);
		}

		public void getSlots(Set<Value> added, BukkitValue slots) { }
	}

	public final Value call(Value target) {
		return this;
	}

	public final Value call(Value target, List<Value> arguments) {
		throw new LOLCodeException(LOLCodeException.BAD_CALL_TYPE, getType());
	}
}
