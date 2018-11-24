/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package java.lang;

// 所有数字类，实现Comparable接口，重写compareTo方法
public final class Byte extends Number implements Comparable<Byte> {

	// byte 最小值
	public static final byte MIN_VALUE = -128;

	// byte 最大值
	public static final byte MAX_VALUE = 127;

	// SuppressWarnings注解：取消以下静态成员变量(作用域) 的 unchecked 警告
	// 从JVM中取出byte类型的反射类，作为Byte的TYPE
	@SuppressWarnings("unchecked")
	public static final Class<Byte> TYPE = (Class<Byte>) Class.getPrimitiveClass("byte");

	// 获取当前Byte对象内部封装的value的字符串
	public String toString() {
		return Integer.toString((int) value);
	}

	// 底层调用Integer.toString()
	public static String toString(byte b) {
		return Integer.toString((int) b, 10);
	}

	// 内部类 ByteCache
	private static class ByteCache {
		private ByteCache() {
		}

		static final Byte cache[] = new Byte[-(-128) + 127 + 1];

		static {
			for (int i = 0; i < cache.length; i++)
				cache[i] = new Byte((byte) (i - 128));
		}
	}

	/*------------------------------------------- valueOf ----------------------------------------------------*/

	// 1、输入：String
	public static Byte valueOf(String s) throws NumberFormatException {
		return valueOf(s, 10);
	}

	// 2、底层调用parseByte
	public static Byte valueOf(String s, int radix) throws NumberFormatException {
		return valueOf(parseByte(s, radix));
	}

	// 3、输入：byte
	//    底层调取 ByteCache 内部封装的数组，从数组中取对象，而不是直接new Byte(b)
	public static Byte valueOf(byte b) {
		final int offset = 128;
		return ByteCache.cache[(int) b + offset];
	}

	/*------------------------------------------- parseByte ----------------------------------------------------*/

	// 输入的字符串默认是十进制，输出结果都是十进制的byte值
	public static byte parseByte(String s) throws NumberFormatException {
		return parseByte(s, 10);
	}

	// 底层调用 Integer.parseInt()，最后对int i进行强转，可以指定当前字符串的进制，输出结果都是十进制
	public static byte parseByte(String s, int radix)
			throws NumberFormatException {
		int i = Integer.parseInt(s, radix);
		if (i < MIN_VALUE || i > MAX_VALUE)
			throw new NumberFormatException("Value out of range. Value:\"" + s + "\" Radix:" + radix);
		return (byte) i;
	}

	/* --------------------------------- Byte基础参数与构造函数 -----------------------------*/

	private final byte value;

	// Byte 构造函数 - 参数byte
	public Byte(byte value) {
		this.value = value;
	}

	// Byte 构造函数 - 参数String
	public Byte(String s) throws NumberFormatException {
		this.value = parseByte(s, 10);
	}

	/* ------------------------- 以下重写Number的方法，获取对应类型的值 ---------------------*/

	public byte byteValue() {
		return value;
	}

	public short shortValue() {
		return (short) value;
	}

	public int intValue() {
		return (int) value;
	}

	public long longValue() {
		return (long) value;
	}

	public float floatValue() {
		return (float) value;
	}

	public double doubleValue() {
		return (double) value;
	}

	/* --------------------------- 哈希值、equals方法、compareTo --------------------------*/

	@Override
	public int hashCode() {
		return Byte.hashCode(value);
	}

	public static int hashCode(byte value) {
		return (int) value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Byte) {
			return value == ((Byte) obj).byteValue();
		}
		return false;
	}

	public static int compare(byte x, byte y) {
		return x - y;
	}

	//底层调用compare方法
	public int compareTo(Byte anotherByte) {
		return compare(this.value, anotherByte.value);
	}


	/**
	 * Decodes a {@code String} into a {@code Byte}.
	 * Accepts decimal, hexadecimal, and octal numbers given by
	 * the following grammar:
	 *
	 * <blockquote>
	 * <dl>
	 * <dt><i>DecodableString:</i>
	 * <dd><i>Sign<sub>opt</sub> DecimalNumeral</i>
	 * <dd><i>Sign<sub>opt</sub></i> {@code 0x} <i>HexDigits</i>
	 * <dd><i>Sign<sub>opt</sub></i> {@code 0X} <i>HexDigits</i>
	 * <dd><i>Sign<sub>opt</sub></i> {@code #} <i>HexDigits</i>
	 * <dd><i>Sign<sub>opt</sub></i> {@code 0} <i>OctalDigits</i>
	 *
	 * <dt><i>Sign:</i>
	 * <dd>{@code -}
	 * <dd>{@code +}
	 * </dl>
	 * </blockquote>
	 *
	 * <i>DecimalNumeral</i>, <i>HexDigits</i>, and <i>OctalDigits</i>
	 * are as defined in section 3.10.1 of
	 * <cite>The Java&trade; Language Specification</cite>,
	 * except that underscores are not accepted between digits.
	 *
	 * <p>The sequence of characters following an optional
	 * sign and/or radix specifier ("{@code 0x}", "{@code 0X}",
	 * "{@code #}", or leading zero) is parsed as by the {@code
	 * Byte.parseByte} method with the indicated radix (10, 16, or 8).
	 * This sequence of characters must represent a positive value or
	 * a {@link NumberFormatException} will be thrown.  The result is
	 * negated if first character of the specified {@code String} is
	 * the minus sign.  No whitespace characters are permitted in the
	 * {@code String}.
	 *
	 * @param nm the {@code String} to decode.
	 * @return a {@code Byte} object holding the {@code byte}
	 * value represented by {@code nm}
	 * @throws NumberFormatException if the {@code String} does not
	 *                               contain a parsable {@code byte}.
	 * @see java.lang.Byte#parseByte(java.lang.String, int)
	 */
	public static Byte decode(String nm) throws NumberFormatException {
		int i = Integer.decode(nm);
		if (i < MIN_VALUE || i > MAX_VALUE)
			throw new NumberFormatException(
					"Value " + i + " out of range from input " + nm);
		return valueOf((byte) i);
	}

	/**
	 * Converts the argument to an {@code int} by an unsigned
	 * conversion.  In an unsigned conversion to an {@code int}, the
	 * high-order 24 bits of the {@code int} are zero and the
	 * low-order 8 bits are equal to the bits of the {@code byte} argument.
	 * <p>
	 * Consequently, zero and positive {@code byte} values are mapped
	 * to a numerically equal {@code int} value and negative {@code
	 * byte} values are mapped to an {@code int} value equal to the
	 * input plus 2<sup>8</sup>.
	 *
	 * @param x the value to convert to an unsigned {@code int}
	 * @return the argument converted to {@code int} by an unsigned
	 * conversion
	 * @since 1.8
	 */
	public static int toUnsignedInt(byte x) {
		return ((int) x) & 0xff;
	}

	/**
	 * Converts the argument to a {@code long} by an unsigned
	 * conversion.  In an unsigned conversion to a {@code long}, the
	 * high-order 56 bits of the {@code long} are zero and the
	 * low-order 8 bits are equal to the bits of the {@code byte} argument.
	 * <p>
	 * Consequently, zero and positive {@code byte} values are mapped
	 * to a numerically equal {@code long} value and negative {@code
	 * byte} values are mapped to a {@code long} value equal to the
	 * input plus 2<sup>8</sup>.
	 *
	 * @param x the value to convert to an unsigned {@code long}
	 * @return the argument converted to {@code long} by an unsigned
	 * conversion
	 * @since 1.8
	 */
	public static long toUnsignedLong(byte x) {
		return ((long) x) & 0xffL;
	}


	/**
	 * The number of bits used to represent a {@code byte} value in two's
	 * complement binary form.
	 *
	 * @since 1.5
	 */
	public static final int SIZE = 8;

	/**
	 * The number of bytes used to represent a {@code byte} value in two's
	 * complement binary form.
	 *
	 * @since 1.8
	 */
	public static final int BYTES = SIZE / Byte.SIZE;

	// 序列化UID
	private static final long serialVersionUID = -7183698231559129828L;
}
