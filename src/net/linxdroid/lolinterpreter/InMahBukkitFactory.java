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

abstract class InMahBukkitFactory {
	public static final InMahBukkitFactory VERSION_1_0_FACTORY = new InMahBukkitFactoryVersion1_0();
	public static final InMahBukkitFactory VERSION_1_1_FACTORY = new InMahBukkitFactoryVersion1_1();

	public abstract InMahBukkit create(Value value);

	private static class InMahBukkitFactoryVersion1_0 extends InMahBukkitFactory {
		public InMahBukkitFactoryVersion1_0() { }

		public InMahBukkit create(Value value) {
			return new InMahBukkitValue(value);
		}
	}

	private static class InMahBukkitFactoryVersion1_1 extends InMahBukkitFactory {
		public InMahBukkitFactoryVersion1_1() { }

		public InMahBukkit create(Value value) {
			InMahBukkit bukkit = new BukkitValue();
			bukkit.assignInMah(new NumbrValue(0), value);
			return bukkit;
		}
	}
}
