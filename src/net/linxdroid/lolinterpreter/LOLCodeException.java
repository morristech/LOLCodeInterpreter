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

public class LOLCodeException extends RuntimeException {
	private static final long serialVersionUID = 0;

	static final String BAD_ARGUMENT_COUNT = "BadArgumentCount";
	static final String BAD_BUKKIT_TYPE = "BadBUKKITType";
	static final String BAD_BUKKIT_USE = "BadBUKKITUse";
	static final String BAD_CALL_TYPE = "BadCallType";
	static final String BAD_IN_MAH_TYPE = "BadINMAHType";
	static final String BAD_INDEX = "BadIndex";
	static final String BAD_JAVA_CLASS = "BadJavaClass";
	static final String BAD_JAVA_TYPE = "BadJavaType";
	static final String BAD_JAVA_USE = "BadJavaUse";
	static final String BAD_IO = "BadIO";
	static final String BAD_LIEK_TYPE = "BadLIEKType";
	static final String BAD_MATH_TYPE = "BadMathType";
	static final String BAD_MOD = "BadMOD";
	static final String BAD_NOOB_USE = "BadNOOBUse";
	static final String BAD_NUMBR_FORMAT = "BadNUMBRFormat";
	static final String BAD_NUMBAR_FORMAT = "BadNUMBARFormat";
	static final String BAD_QUOSHUNT = "BadQUOSHUNT";
	static final String BAD_SLOT = "BadSlot";

	private String type;

	public LOLCodeException() { }

	public LOLCodeException(Throwable throwable) {
		super(throwable);
		this.type = throwable.getClass().getName();
	}

	public LOLCodeException(String type) {
		super(type);
		this.type = type;
	}

	public LOLCodeException(String type, String message) {
		super(type + ": " + message);
		this.type = type;
	}

	public LOLCodeException(String type, Throwable throwable) {
		super(type, throwable);
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
