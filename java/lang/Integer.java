/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package java.lang;

import java.lang.annotation.Native;

// integer 类继承 Number类型，并实现Comparable
public final class Integer extends Number implements Comparable<Integer> {

	// @Native 表示被注解的内容是原生(本机)相关的。不影响java本身代码逻辑，通常用于生成JNI相关的头文件
	// 以下两个参数定义了int的最大值和最小值，且采用static的方式，可以通过类名访问
	// 且最大最小值采用十六进制表示
	@Native
	public static final int MIN_VALUE = 0x80000000;
	@Native
	public static final int MAX_VALUE = 0x7fffffff;

	// Concept：进制表示
	//			八进制：以0开头，比如：03452612
	//			十六进制：以0x或0X开头，比如0X12AFD、0x12afd
	//

	// SuppressWarnings注解：取消以下静态成员变量(作用域) 的 unchecked 警告
	// 从JVM中取出int类型的反射类，作为Integer的TYPE
	@SuppressWarnings("unchecked")
	public static final Class<Integer> TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

	// 获取int的字符，比如 1 的字符为 digit[1] = '1'
	final static char[] digits = {
			'0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't',
			'u', 'v', 'w', 'x', 'y', 'z'
	};

	// 获取当前对象的value的字符串，用的比较多
	public String toString() {
		return toString(value);
	}

	// 输出 数字 的字符串，默认输出的进制为10进制数（字符串）
	public static String toString(int i) {
		if (i == Integer.MIN_VALUE)
			return "-2147483648";
		int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
		char[] buf = new char[size];
		getChars(i, size, buf);
		return new String(buf, true);
	}

	// 输出 数字的 n 进制字符串
	// radix表示 输出的进制数，比如 Integer.toString(6,2) --> 6的二进制表示是 110，返回值为"110"
	// 任何一个Java环境中定义的数字，默认都是10进制，所以没有必要指定输入的进制数
	public static String toString(int i, int radix) {
		//最小的进制为2进制，最大的进制为36进制
		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
			radix = 10;//如果输入的进制数太大或太小，默认给10进制

		if (radix == 10) {//如果要输出10进制数，去找toString(int)方法
			return toString(i);
		}

		char buf[] = new char[33];// TODO
		boolean negative = (i < 0);
		int charPos = 32;

		if (!negative) {
			i = -i;
		}

		while (i <= -radix) {
			buf[charPos--] = digits[-(i % radix)];
			i = i / radix;
		}
		buf[charPos] = digits[-i];

		if (negative) {
			buf[--charPos] = '-';
		}

		return new String(buf, charPos, (33 - charPos));
	}

	// 转换数值为 二进制的字符串 2^1 = 2
	public static String toBinaryString(int i) {
		return toUnsignedString0(i, 1);
	}

	// 转换数值为 八进制的字符串 2^3 = 8
	public static String toOctalString(int i) {
		return toUnsignedString0(i, 3);
	}

	// 转换数值为 十六进制的字符串 2^4 = 16
	public static String toHexString(int i) {
		return toUnsignedString0(i, 4);
	}

	// 输出 一个无符号（正负号）的 字符串，默认输出10进制
	public static String toUnsignedString(int i) {
		return Long.toString(toUnsignedLong(i));
	}

	// 输出 一个无符号（正负号）的 字符串，可指定输出进制
	// Compare:
	//		  toString("-6",2)--> "-110" 一个带符号的二进制数字符串
	//		  toUnsignedString("-6",2)--> "11111111111111111111111111111010" 一个不带符号的二进制数字符串
	public static String toUnsignedString(int i, int radix) {
		return Long.toUnsignedString(toUnsignedLong(i), radix);
	}

	// 辅助方法（private方法）
	// 功能：转换成对应进制的字符串
	// shift 是进制数而不是进制，比如转换成十六进制的字符串时，进制数是4，2^4 = 16
	// shift：1-二进制，3-八进制，4-十六进制
		// 进制数shift 与 进制radix 的关系：int radix = 1 << shift = 2^shift;
	private static String toUnsignedString0(int val, int shift) {
		// assert shift > 0 && shift <=5 : "Illegal shift value";
		// Integer.SIZE表示integer的总比特位数32
		// 32 - numberOfLeadingZeros 表示这个数用二进制数来表示时，实际所用的比特位数
		// 比如10的二进制数补码为0000 0000 0000 0000 0000 0000 0000 1010，但实际只需要1010就可以表示10，此时实际使用比特位数为4，所以10的 mag = 4
		int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
		// 确定val转换成字符数组所需要的长度
		int chars = Math.max(((mag + (shift - 1)) / shift), 1);
		char[] buf = new char[chars];
		// 将数字转换为字符数据存放在buf中
		formatUnsignedInt(val, shift, buf, 0, chars);
		// take over接收：String接收buf数组
		return new String(buf, true);
	}

	/**
	 * 将数字转换为字符数据存放在buf中 - toString()方法的关键
	 *
	 * @param val 输入的数，比如10
	 * @param shift 进制数1（二进制）| 3（八进制）| 4（十六进制），如果调用的方法是要转成八进制，则shift = 3
	 * @param buf 数字转化成字符串前，用数组存储数字的字符，数组位数由数字以及转化的进制决定
	 * @param offset TODO 尚未理解
	 * @param len 数组buf的长度
	 */
	static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
		// 字符位置 char-position
		int charPos = len;
		// 将进制数shift 转换成 进制radix
		// GOOD：进制计算常常会用到位运算，注意：这里是1左移 shift位，而非 shift 左移1位
		int radix = 1 << shift;
		// 掩码mask：掩码的值比进制radix小1
		// 掩码的特点：它的二进制数最后全都是1，比如8的掩码7，7的二进制数0000 0111，16的掩码15，15的二进制数0000 1111
		// Concept-TODO 掩码是什么概念？
		int mask = radix - 1;
		// 循环体：每次计算出数字对应进制的最后一位数据，存放到数组中
		// 例如：如果想输出10的八进制数12，则以下循环体，第一次计算出'2'，存入buf字符数组的最后一个空位，第二次，存入'1'。
		do {
			// GOOD：val & mask 相当于 val % (mask+1)，前提是mask是掩码，即最后一部分都是由1组成，如0111
			// 若val = 10，转8进制时，mask = 7（十进制）= 0111（二进制）
			// 1010
			// 0111
			// -----
			// 0010 （此处用val&掩码mask，可以获取1010的最低三位010），这样就能获取余数
			// 因此 val & mask 相当于 val % (mask+1)，上面的案例是 10 & 7 = 10 %（7+1） = 10 % 8
			buf[offset + --charPos] = Integer.digits[val & mask];
			val >>>= shift;//val = val >>> shift
		} while (val != 0 && charPos > 0);
		return charPos;
		// Concept：与运算
		// a、与运算的本质是什么?
		// 	  与运算的运算规则：两位同时为'1'，结果才为'1'，否则皆为'0'
		// b、与运算的应用场景?
		// 	  1、与运算可以用来将一个比特单元清零
		// 	  2、与运算可以用于取出二进制数中的指定位（例如取X=1010 1101的低四位 则将X&00001111得到0000 1101）
		//    3、判断奇偶性：任何数与1，结果为1，则该数为奇数。
		// c、位运算还可以用于加密解密

		// Concept:模
		// a、什么是模？
		// b、模的应用有哪些？
	}

	// 个位数上的数字：100以内的数字对10取模的结果，digit是数字的意思
	final static char[] DigitOnes = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	};

	// 十位数上的数字：100以内的数字除以10的结果（取整）
	// 例如 86 = DigitTens[86] + DigitOnes[86] = '8' + '6' = '86'
	final static char[] DigitTens = {
			'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
			'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
			'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
			'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
			'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
			'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
			'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
			'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
			'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
			'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	};

	// 为什么不设置一个千位数的数组？
	// 1、效率可能不高 2、算法麻烦 3、会定义一个巨大的数组

	// Good Method 此方法作用将int数字转换放进一个字符数组
	// GOOD 字符串 与 字符数组是好朋友，一旦涉及到输出字符串的，中间过程往往使用字符数组作为中间存储
	// i = min_integer时，方法失败
	static void getChars(int i, int index, char[] buf) {
		int q, r;
		int charPos = index;
		char sign = 0;

		// 1、判断i的正负，对负数取反，方便处理
		// int的范围是-2147483648 ~ 2147483647，对min_integer取反时，超过了int的范围，此时方法失败
		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// 2、i>=65535时，每次取数字i的的最后两位转为字符，存放到字符数组中（移位 + 除法）
		// 该代码段使用除法和移位的算法
		while (i >= 65536) {
			q = i / 100;
			// really: r = i - (q * 100); 改等式表示：r的值等于i的最后两位
			// 以下采用了以下位运算的算法，提高计算效率
			r = i - ((q << 6) + (q << 5) + (q << 2));// (q << 6) + (q << 5) + (q << 2) = q * 100
			i = q;

			// 下方巧妙地采用数组查找的方式来获取字符，避免除法等计算
			// 取数字r%10的结果 赋值给左边
			buf[--charPos] = DigitOnes[r];
			// 取数字r/10的结果 赋值给左边
			buf[--charPos] = DigitTens[r];
		}

		// 3、当i<65535时，每次取一位转为字符（移位 + 乘法）
		// 该代码段采用乘法与移位的算法，提高计算效率
		for (; ; ) {
			q = (i * 52429) >>> (16 + 3);//约等于 q = i/10;用位运算，效率更高，这里巧妙运用了乘法和移位避免使用除法来提高效率
			r = i - ((q << 3) + (q << 1));// r = i-(q*10) ...
			buf[--charPos] = digits[r];//将数字i的最后一位存入字符数组
			i = q;
			if (i == 0) break;
		}
		if (sign != 0) {
			buf[--charPos] = sign;
		}
		// Concept-什么是移位运算？
		// a、移位运算针对的是二进制数，即实际进行移位运算的对象是二进制数，在移位运算前，必须将十进制数转化成二进制数
		// 	  这些进行计算的二进制数，都是补码
		// b、移位运算有三种：左移，右移，无符号右移
		// c、什么是左移？
		// 	  左移就是二进制数的头向左移动，比如0000 1010的头是1(千位)左移3之后，空缺的三个位置补0，变成0 1010 000，相当于这个数乘以2的3次方
		// d、什么是右移？
		//	  右移就是二进制数的头向右移动，比如0000 1010的头是1(千位)右移3之后，空缺的三个位置补1，变成111 0 1010，相当于这个数除以2的3次方
		// 	 （若原数为正数，则补0，若原数为负数，则补1）
		// e、什么是无符号右移？
		//	  首先，计算机表示数字正负不是用+ -加减号来表示，而是用最高位数字来表示，0表示正，1表示负
		// 	  无符号右移就是不论正负，左边都是补0
		// f、为什么没有无符号左移呢？因为左移都是补0

		// g、计算机如何进行乘法运算？
		// 		以下案例的前提：乘法结果不溢出
		// 		以下案例以12比特进行计算，下方用8位写的，前面的4位0省略不写
		// 		以下计算用的是补码计算
		//
		// 	   算法一：53 x 5 = 0011 0101 x 0000 0101 = 0011 0101 << 2 + 0011 0101 << 0 = （265）
		//	   		  上面的2指5的二进制数0000 0101中的百位带1的数的右边有2位，则就让53左移2位，结果为a
		//			  上面的0指5的二进制数0000 0101中的个位带1的数的右边有0位，则就让53左移0位，结果为b，最终乘法结果为：a + b
		//	   算法二：53 x 5 = 0011 0101 x 0000 0101 = 0011 0101
		//				0011 0101
		//				0000 0101
		// 			---------------
		// 				0011 0101	(实际上就是 0011 0101 << 0)
		// 			  0011 0101		(实际上就是 0011 0101 << 2)
		// 			---------------
		// 				0011 0101
		//		 	 00 1101 0100
		// 			---------------
		//			 01 0000 1001	（265）
		// TODO 两个很大的int相乘，乘积结果溢出怎么办？

		// 思考1：为什么要在大于等于65536、小于65536这两种情况采用不同的处理方式？
		// 背景前提：移位运算效率高于乘除法，乘法效率高于除法
		// 那以上两种情况，不是采用 （移位 + 乘法）更为高效，为什么要分成两种情况？
		// 原因：q是int类型，q的值不能超出int的范围
		// q = (i * 52429) >>> (16 + 3);----- q = i * 0.100000381  效率高，有前提：i < 65536，否则i * 52429超出int的范围
		// q = i / 10;----------------------- q = i / 10		   效率低，但通用
		// 选取几组移位算术式进行叠加，可以获取你想要的值，比如 (q << 6) + (q << 5) + (q << 2) = q * 100，即如果你想让一个数乘以100，那么用移位运算就可以办到
		// 选取几组移位算术式进行叠加，在精度允许的情况下，可以获得相同的值，却有不同的效率
	}

	// 一个设计巧妙的int数组，结合下方一个设计巧妙的方法-stringSize方法
	final static int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

	// 输入一个数，返回该数是几位数，比如：输入10，返回2，输入100，返回3
	static int stringSize(int x) {
		for (int i = 0; ; i++)
			if (x <= sizeTable[i])
				return i + 1;
	}

	// 将字符串转化成数字，默认转化为十进制
	public static int parseInt(String s) throws NumberFormatException {
		return parseInt(s, 10);
	}

	/**
	 * <Examples>
	 * parseInt("0", 10) returns 0
	 * parseInt("473", 10) returns 473
	 * parseInt("+42", 10) returns 42
	 * parseInt("-0", 10) returns 0
	 * parseInt("-FF", 16) returns -255
	 * parseInt("1100110", 2) returns 102
	 * parseInt("2147483647", 10) returns 2147483647
	 * parseInt("-2147483648", 10) returns -2147483648
	 * parseInt("2147483648", 10) throws a NumberFormatException
	 * parseInt("99", 8) throws a NumberFormatException
	 * parseInt("Kona", 10) throws a NumberFormatException
	 * parseInt("Kona", 27) returns 411787
	 */
	// 该方法用于将 字符串 转化 为int类型,字符串中的字符不能包含数字以外的字符，否则抛出数字转化异常
	// radix参数 表示字符串的当前的进制数(输入的进制数)，输出的结果都是10进制数
	// 该方法可能引起JVM的初始化问题
	public static int parseInt(String s, int radix) throws NumberFormatException {
		if (s == null) {
			throw new NumberFormatException("null");
		}

		// Character.MIN_RADIX = 2（最小进制）
		if (radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix +
					" less than Character.MIN_RADIX");
		}

		// Character.MAX_RADIX = 36（最大进制）
		if (radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix +
					" greater than Character.MAX_RADIX");
		}

		int result = 0;
		boolean negative = false;//是否为负数
		int i = 0, len = s.length();//设置初始位置和字符串的长度
		int limit = -Integer.MAX_VALUE;//最值限制
		int multmin;
		int digit;

		if (len > 0) {
			char firstChar = s.charAt(0);
			// 1、该字符串是带符号的：如果第一个字符的值 比 字符'0' 的值小，做进一步的检查
			if (firstChar < '0') { // Possible leading "+" or "-"
					if (firstChar == '-') {
					negative = true;
					limit = Integer.MIN_VALUE;
				} else if (firstChar != '+')
					throw NumberFormatException.forInputString(s);

				if (len == 1) // Cannot have lone "+" or "-"
					throw NumberFormatException.forInputString(s);
				i++;
			}
			multmin = limit / radix;
			while (i < len) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				// 获取数字的逻辑：字符-->字符的codePoint(每个字符都有自己的编码值：查询UTF-8或其他编码表)-->对应进制的数
				// 不同类型的字符存在于底层CharacterData(接口)的不同实现类中
				digit = Character.digit(s.charAt(i++), radix);
				if (digit < 0) {
					throw NumberFormatException.forInputString(s);
				}
				if (result < multmin) {
					throw NumberFormatException.forInputString(s);
				}
				result *= radix;
				if (result < limit + digit) {
					throw NumberFormatException.forInputString(s);
				}
				result -= digit;
			}
		} else {
			throw NumberFormatException.forInputString(s);
		}
		return negative ? result : -result;
	}

	/**
	 * Parses the string argument as an unsigned integer in the radix
	 * specified by the second argument.  An unsigned integer maps the
	 * values usually associated with negative numbers to positive
	 * numbers larger than {@code MAX_VALUE}.
	 * <p>
	 * The characters in the string must all be digits of the
	 * specified radix (as determined by whether {@link
	 * java.lang.Character#digit(char, int)} returns a nonnegative
	 * value), except that the first character may be an ASCII plus
	 * sign {@code '+'} ({@code '\u005Cu002B'}). The resulting
	 * integer value is returned.
	 *
	 * <p>An exception of type {@code NumberFormatException} is
	 * thrown if any of the following situations occurs:
	 * <ul>
	 * <li>The first argument is {@code null} or is a string of
	 * length zero.
	 *
	 * <li>The radix is either smaller than
	 * {@link java.lang.Character#MIN_RADIX} or
	 * larger than {@link java.lang.Character#MAX_RADIX}.
	 *
	 * <li>Any character of the string is not a digit of the specified
	 * radix, except that the first character may be a plus sign
	 * {@code '+'} ({@code '\u005Cu002B'}) provided that the
	 * string is longer than length 1.
	 *
	 * <li>The value represented by the string is larger than the
	 * largest unsigned {@code int}, 2<sup>32</sup>-1.
	 *
	 * </ul>
	 *
	 * @param s the {@code String} containing the unsigned integer
	 * representation to be parsed
	 * @param radix the radix to be used while parsing {@code s}.
	 * @return the integer represented by the string argument in the
	 * specified radix.
	 * @throws NumberFormatException if the {@code String}
	 *                               does not contain a parsable {@code int}.
	 * @since 1.8
	 */
	public static int parseUnsignedInt(String s, int radix)
			throws NumberFormatException {
		if (s == null) {
			throw new NumberFormatException("null");
		}

		int len = s.length();
		if (len > 0) {
			char firstChar = s.charAt(0);
			if (firstChar == '-') {
				throw new
						NumberFormatException(String.format("Illegal leading minus sign " +
						"on unsigned string %s.", s));
			} else {
				if (len <= 5 || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
						(radix == 10 && len <= 9)) { // Integer.MAX_VALUE in base 10 is 10 digits
					return parseInt(s, radix);
				} else {
					long ell = Long.parseLong(s, radix);
					if ((ell & 0xffff_ffff_0000_0000L) == 0) {
						return (int) ell;
					} else {
						throw new
								NumberFormatException(String.format("String value %s exceeds " +
								"range of unsigned int.", s));
					}
				}
			}
		} else {
			throw NumberFormatException.forInputString(s);
		}
	}

	/**
	 * Parses the string argument as an unsigned decimal integer. The
	 * characters in the string must all be decimal digits, except
	 * that the first character may be an an ASCII plus sign {@code
	 * '+'} ({@code '\u005Cu002B'}). The resulting integer value
	 * is returned, exactly as if the argument and the radix 10 were
	 * given as arguments to the {@link
	 * #parseUnsignedInt(java.lang.String, int)} method.
	 *
	 * @param s a {@code String} containing the unsigned {@code int}
	 * representation to be parsed
	 * @return the unsigned integer value represented by the argument in decimal.
	 * @throws NumberFormatException if the string does not contain a
	 *                               parsable unsigned integer.
	 * @since 1.8
	 */
	public static int parseUnsignedInt(String s) throws NumberFormatException {
		return parseUnsignedInt(s, 10);
	}


	// 返回字符串的数值，默认输入的字符串是十进制，输出结果也是10进制
	// Compare: Integer.valueOf()的底层实现是Integer.parseInt()
	// 			Integer.valueOf()  返回值类型为 Integer
	//			Integer.parseInt() 返回值类型为 int
	public static Integer valueOf(String s) throws NumberFormatException {
		return Integer.valueOf(parseInt(s, 10));
	}

	// radix参数 表示字符串的当前的进制数(输入的进制数)，输出的结果都是十进制
	// 如果想获得其他进制的输出结果，则有Integer.toBinaryString()等
	public static Integer valueOf(String s, int radix) throws NumberFormatException {
		return Integer.valueOf(parseInt(s, radix));
	}

	// 对int类的数据，进行包装--> TODO 装箱的实现是这个方法吗 ?
	// 先从IntegerCache中取对象，如果取不到，才new一个返回
	public static Integer valueOf(int i) {
		if (i >= IntegerCache.low && i <= IntegerCache.high)
			return IntegerCache.cache[i + (-IntegerCache.low)];
		return new Integer(i);
	}

	// 内部类IntegerCache，支持的数据范围 -128 ~ 127
	// 该内部类存在的意义，减少不必要的内存浪费，提高效率
	// a、内部类IntegerCache有什么好处 ? 第一次被调用的会一次性加载256个对象，即向数组cache添加256个Integer数据，有什么好处？
	// 	  好处：减少内存占用
	// 		Integer a = Integer.parseInt("100");
	// 		Integer b = Integer.parseInt("100");
	//		a 与 b访问的是同一个对象，也就是都访问IntegerCache中的cache[100]这个Integer对象
	//		即 a == b ? 'true':'false'  -----> 结果为true
	// b、场景：淘宝很多产品的价格，都小于128，所以该内部类很重要
	private static class IntegerCache {
		static final int low = -128;
		static final int high;
		static final Integer cache[];

		static {
			// high value may be configured by property
			int h = 127;
			String integerCacheHighPropValue =
					sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
			if (integerCacheHighPropValue != null) {
				try {
					int i = parseInt(integerCacheHighPropValue);
					i = Math.max(i, 127);
					// Maximum array size is Integer.MAX_VALUE
					h = Math.min(i, Integer.MAX_VALUE - (-low) - 1);
				} catch (NumberFormatException nfe) {
					// If the property cannot be parsed into an int, ignore it.
				}
			}
			high = h;

			cache = new Integer[(high - low) + 1];
			int j = low;
			for (int k = 0; k < cache.length; k++)
				cache[k] = new Integer(j++);

			// range [-128, 127] must be interned (JLS7 5.1.7)
			assert IntegerCache.high >= 127;
		}

		private IntegerCache() {
		}
	}


	private final int value;
	// Integer的构造函数- 参数int
	public Integer(int value) {
		this.value = value;
	}

	// Integer的构造函数 - 参数String
	public Integer(String s) throws NumberFormatException {
		this.value = parseInt(s, 10);
	}

	/* ------------------------- 以下重写Number的方法，获取对应类型的值 ---------------------*/
	// 获取byte类型的值
	public byte byteValue() {
		return (byte) value;
	}

	// 获取short类型的值
	public short shortValue() {
		return (short) value;
	}

	// 获取int类型的值
	public int intValue() {
		return value;
	}

	// 获取long类型的值
	public long longValue() {
		return (long) value;
	}

	// 获取float类型的值
	public float floatValue() {
		return (float) value;
	}
	// 获取double类型的值
	public double doubleValue() {
		return (double) value;
	}

	// 获取当前对象内部value的哈希值
	@Override
	public int hashCode() {
		return Integer.hashCode(value);
	}

	// Integer类型的哈希值，就是当前对象内部的value本身
	public static int hashCode(int value) {
		return value;
	}

	// Integer类型的equals方法，比较的是当前对象内部value的值是否相等
	// 应用：对比两个Integer类型的数据是否相等，不用 == ，而是用equals
	// 如果传入的obj是int类型，会被自动装箱为Integer类型
	public boolean equals(Object obj) {
		if (obj instanceof Integer) {
			return value == ((Integer) obj).intValue();
		}
		return false;
	}

	/**
	 * Determines the integer value of the system property with the
	 * specified name.
	 *
	 * <p>The first argument is treated as the name of a system
	 * property.  System properties are accessible through the {@link
	 * java.lang.System#getProperty(java.lang.String)} method. The
	 * string value of this property is then interpreted as an integer
	 * value using the grammar supported by {@link Integer#decode decode} and
	 * an {@code Integer} object representing this value is returned.
	 *
	 * <p>If there is no property with the specified name, if the
	 * specified name is empty or {@code null}, or if the property
	 * does not have the correct numeric format, then {@code null} is
	 * returned.
	 *
	 * <p>In other words, this method returns an {@code Integer}
	 * object equal to the value of:
	 *
	 * <blockquote>
	 * {@code getInteger(nm, null)}
	 * </blockquote>
	 *
	 * @param nm property name.
	 * @return the {@code Integer} value of the property.
	 * @throws SecurityException for the same reasons as
	 *                           {@link System#getProperty(String) System.getProperty}
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 */
	public static Integer getInteger(String nm) {
		return getInteger(nm, null);
	}

	/**
	 * Determines the integer value of the system property with the
	 * specified name.
	 *
	 * <p>The first argument is treated as the name of a system
	 * property.  System properties are accessible through the {@link
	 * java.lang.System#getProperty(java.lang.String)} method. The
	 * string value of this property is then interpreted as an integer
	 * value using the grammar supported by {@link Integer#decode decode} and
	 * an {@code Integer} object representing this value is returned.
	 *
	 * <p>The second argument is the default value. An {@code Integer} object
	 * that represents the value of the second argument is returned if there
	 * is no property of the specified name, if the property does not have
	 * the correct numeric format, or if the specified name is empty or
	 * {@code null}.
	 *
	 * <p>In other words, this method returns an {@code Integer} object
	 * equal to the value of:
	 *
	 * <blockquote>
	 * {@code getInteger(nm, new Integer(val))}
	 * </blockquote>
	 * <p>
	 * but in practice it may be implemented in a manner such as:
	 *
	 * <blockquote><pre>
	 * Integer result = getInteger(nm, null);
	 * return (result == null) ? new Integer(val) : result;
	 * </pre></blockquote>
	 * <p>
	 * to avoid the unnecessary allocation of an {@code Integer}
	 * object when the default value is not needed.
	 *
	 * @param nm property name.
	 * @param val default value.
	 * @return the {@code Integer} value of the property.
	 * @throws SecurityException for the same reasons as
	 *                           {@link System#getProperty(String) System.getProperty}
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 */
	public static Integer getInteger(String nm, int val) {
		Integer result = getInteger(nm, null);
		return (result == null) ? Integer.valueOf(val) : result;
	}

	/**
	 * Returns the integer value of the system property with the
	 * specified name.  The first argument is treated as the name of a
	 * system property.  System properties are accessible through the
	 * {@link java.lang.System#getProperty(java.lang.String)} method.
	 * The string value of this property is then interpreted as an
	 * integer value, as per the {@link Integer#decode decode} method,
	 * and an {@code Integer} object representing this value is
	 * returned; in summary:
	 *
	 * <ul><li>If the property value begins with the two ASCII characters
	 * {@code 0x} or the ASCII character {@code #}, not
	 * followed by a minus sign, then the rest of it is parsed as a
	 * hexadecimal integer exactly as by the method
	 * {@link #valueOf(java.lang.String, int)} with radix 16.
	 * <li>If the property value begins with the ASCII character
	 * {@code 0} followed by another character, it is parsed as an
	 * octal integer exactly as by the method
	 * {@link #valueOf(java.lang.String, int)} with radix 8.
	 * <li>Otherwise, the property value is parsed as a decimal integer
	 * exactly as by the method {@link #valueOf(java.lang.String, int)}
	 * with radix 10.
	 * </ul>
	 *
	 * <p>The second argument is the default value. The default value is
	 * returned if there is no property of the specified name, if the
	 * property does not have the correct numeric format, or if the
	 * specified name is empty or {@code null}.
	 *
	 * @param nm property name.
	 * @param val default value.
	 * @return the {@code Integer} value of the property.
	 * @throws SecurityException for the same reasons as
	 *                           {@link System#getProperty(String) System.getProperty}
	 * @see System#getProperty(java.lang.String)
	 * @see System#getProperty(java.lang.String, java.lang.String)
	 */
	public static Integer getInteger(String nm, Integer val) {
		String v = null;
		try {
			v = System.getProperty(nm);
		} catch (IllegalArgumentException | NullPointerException e) {
		}
		if (v != null) {
			try {
				return Integer.decode(v);
			} catch (NumberFormatException e) {
			}
		}
		return val;
	}

	/**
	 * Decodes a {@code String} into an {@code Integer}.
	 * Accepts decimal, hexadecimal, and octal numbers given
	 * by the following grammar:
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
	 * Integer.parseInt} method with the indicated radix (10, 16, or
	 * 8).  This sequence of characters must represent a positive
	 * value or a {@link NumberFormatException} will be thrown.  The
	 * result is negated if first character of the specified {@code
	 * String} is the minus sign.  No whitespace characters are
	 * permitted in the {@code String}.
	 *
	 * @param nm the {@code String} to decode.
	 * @return an {@code Integer} object holding the {@code int}
	 * value represented by {@code nm}
	 * @throws NumberFormatException if the {@code String} does not
	 *                               contain a parsable integer.
	 * @see java.lang.Integer#parseInt(java.lang.String, int)
	 */
	public static Integer decode(String nm) throws NumberFormatException {
		int radix = 10;
		int index = 0;
		boolean negative = false;
		Integer result;

		if (nm.length() == 0)
			throw new NumberFormatException("Zero length string");
		char firstChar = nm.charAt(0);
		// Handle sign, if present
		if (firstChar == '-') {
			negative = true;
			index++;
		} else if (firstChar == '+')
			index++;

		// Handle radix specifier, if present
		if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
			index += 2;
			radix = 16;
		} else if (nm.startsWith("#", index)) {
			index++;
			radix = 16;
		} else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
			index++;
			radix = 8;
		}

		if (nm.startsWith("-", index) || nm.startsWith("+", index))
			throw new NumberFormatException("Sign character in wrong position");

		try {
			result = Integer.valueOf(nm.substring(index), radix);
			result = negative ? Integer.valueOf(-result.intValue()) : result;
		} catch (NumberFormatException e) {
			// If number is Integer.MIN_VALUE, we'll end up here. The next line
			// handles this case, and causes any genuine format error to be
			// rethrown.
			String constant = negative ? ("-" + nm.substring(index))
					: nm.substring(index);
			result = Integer.valueOf(constant, radix);
		}
		return result;
	}

	// 实现Comparable接口，重写compareTo方法
	// 比较两个数的大小，返回值为-1，0，1，底层调用compare方法
	public int compareTo(Integer anotherInteger) {
		return compare(this.value, anotherInteger.value);
	}

	// x > y, 返回值 1   我比你大，返回1
	// x = y, 返回值 0   我等于你，返回0
	// x < y, 返回值 -1  我比你小，返回-1
	public static int compare(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);//多层 三目运算符，减少代码量
	}

	/**
	 * Compares two {@code int} values numerically treating the values
	 * as unsigned.
	 *
	 * @param x the first {@code int} to compare
	 * @param y the second {@code int} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less
	 * than {@code 0} if {@code x < y} as unsigned values; and
	 * a value greater than {@code 0} if {@code x > y} as
	 * unsigned values
	 * @since 1.8
	 */
	public static int compareUnsigned(int x, int y) {
		return compare(x + MIN_VALUE, y + MIN_VALUE);
	}

	/**
	 * Converts the argument to a {@code long} by an unsigned
	 * conversion.  In an unsigned conversion to a {@code long}, the
	 * high-order 32 bits of the {@code long} are zero and the
	 * low-order 32 bits are equal to the bits of the integer
	 * argument.
	 * <p>
	 * Consequently, zero and positive {@code int} values are mapped
	 * to a numerically equal {@code long} value and negative {@code
	 * int} values are mapped to a {@code long} value equal to the
	 * input plus 2<sup>32</sup>.
	 *
	 * @param x the value to convert to an unsigned {@code long}
	 * @return the argument converted to {@code long} by an unsigned
	 * conversion
	 * @since 1.8
	 */
	public static long toUnsignedLong(int x) {
		return ((long) x) & 0xffffffffL;
	}

	/**
	 * Returns the unsigned quotient of dividing the first argument by
	 * the second where each argument and the result is interpreted as
	 * an unsigned value.
	 *
	 * <p>Note that in two's complement arithmetic, the three other
	 * basic arithmetic operations of add, subtract, and multiply are
	 * bit-wise identical if the two operands are regarded as both
	 * being signed or both being unsigned.  Therefore separate {@code
	 * addUnsigned}, etc. methods are not provided.
	 *
	 * @param dividend the value to be divided
	 * @param divisor the value doing the dividing
	 * @return the unsigned quotient of the first argument divided by
	 * the second argument
	 * @see #remainderUnsigned
	 * @since 1.8
	 */
	public static int divideUnsigned(int dividend, int divisor) {
		// In lieu of tricky code, for now just use long arithmetic.
		return (int) (toUnsignedLong(dividend) / toUnsignedLong(divisor));
	}

	/**
	 * Returns the unsigned remainder from dividing the first argument
	 * by the second where each argument and the result is interpreted
	 * as an unsigned value.
	 *
	 * @param dividend the value to be divided
	 * @param divisor the value doing the dividing
	 * @return the unsigned remainder of the first argument divided by
	 * the second argument
	 * @see #divideUnsigned
	 * @since 1.8
	 */
	public static int remainderUnsigned(int dividend, int divisor) {
		// In lieu of tricky code, for now just use long arithmetic.
		return (int) (toUnsignedLong(dividend) % toUnsignedLong(divisor));
	}

	// 以二进制补码形式表示int的总比特位数，int 的最大值 2^(32-1)-1
	@Native
	public static final int SIZE = 32;


	public static final int BYTES = SIZE / Byte.SIZE;

	/**
	 * Returns an {@code int} value with at most a single one-bit, in the
	 * position of the highest-order ("leftmost") one-bit in the specified
	 * {@code int} value.  Returns zero if the specified value has no
	 * one-bits in its two's complement binary representation, that is, if it
	 * is equal to zero.
	 *
	 * @param i the value whose highest one bit is to be computed
	 * @return an {@code int} value with a single one-bit, in the position
	 * of the highest-order one-bit in the specified value, or zero if
	 * the specified value is itself equal to zero.
	 * @since 1.5
	 */
	public static int highestOneBit(int i) {
		// HD, Figure 3-1
		i |= (i >> 1);
		i |= (i >> 2);
		i |= (i >> 4);
		i |= (i >> 8);
		i |= (i >> 16);
		return i - (i >>> 1);
	}

	/**
	 * Returns an {@code int} value with at most a single one-bit, in the
	 * position of the lowest-order ("rightmost") one-bit in the specified
	 * {@code int} value.  Returns zero if the specified value has no
	 * one-bits in its two's complement binary representation, that is, if it
	 * is equal to zero.
	 *
	 * @param i the value whose lowest one bit is to be computed
	 * @return an {@code int} value with a single one-bit, in the position
	 * of the lowest-order one-bit in the specified value, or zero if
	 * the specified value is itself equal to zero.
	 * @since 1.5
	 */
	public static int lowestOneBit(int i) {
		// HD, Section 2-1
		return i & -i;
	}

	/**
	 * Returns the number of zero bits preceding the highest-order
	 * ("leftmost") one-bit in the two's complement binary representation
	 * of the specified {@code int} value.  Returns 32 if the
	 * specified value has no one-bits in its two's complement representation,
	 * in other words if it is equal to zero.
	 *
	 * <p>Note that this method is closely related to the logarithm base 2.
	 * For all positive {@code int} values x:
	 * <ul>
	 * <li>floor(log<sub>2</sub>(x)) = {@code 31 - numberOfLeadingZeros(x)}
	 * <li>ceil(log<sub>2</sub>(x)) = {@code 32 - numberOfLeadingZeros(x - 1)}
	 * </ul>
	 *
	 * @param i the value whose number of leading zeros is to be computed
	 * @return the number of zero bits preceding the highest-order
	 * ("leftmost") one-bit in the two's complement binary representation
	 * of the specified {@code int} value, or 32 if the value
	 * is equal to zero.
	 * @since 1.5
	 */
	public static int numberOfLeadingZeros(int i) {
		// HD, Figure 5-6
		if (i == 0)
			return 32;
		int n = 1;
		if (i >>> 16 == 0) {
			n += 16;
			i <<= 16;
		}
		if (i >>> 24 == 0) {
			n += 8;
			i <<= 8;
		}
		if (i >>> 28 == 0) {
			n += 4;
			i <<= 4;
		}
		if (i >>> 30 == 0) {
			n += 2;
			i <<= 2;
		}
		n -= i >>> 31;
		return n;
	}

	/**
	 * Returns the number of zero bits following the lowest-order ("rightmost")
	 * one-bit in the two's complement binary representation of the specified
	 * {@code int} value.  Returns 32 if the specified value has no
	 * one-bits in its two's complement representation, in other words if it is
	 * equal to zero.
	 *
	 * @param i the value whose number of trailing zeros is to be computed
	 * @return the number of zero bits following the lowest-order ("rightmost")
	 * one-bit in the two's complement binary representation of the
	 * specified {@code int} value, or 32 if the value is equal
	 * to zero.
	 * @since 1.5
	 */
	public static int numberOfTrailingZeros(int i) {
		// HD, Figure 5-14
		int y;
		if (i == 0) return 32;
		int n = 31;
		y = i << 16;
		if (y != 0) {
			n = n - 16;
			i = y;
		}
		y = i << 8;
		if (y != 0) {
			n = n - 8;
			i = y;
		}
		y = i << 4;
		if (y != 0) {
			n = n - 4;
			i = y;
		}
		y = i << 2;
		if (y != 0) {
			n = n - 2;
			i = y;
		}
		return n - ((i << 1) >>> 31);
	}

	/**
	 * Returns the number of one-bits in the two's complement binary
	 * representation of the specified {@code int} value.  This function is
	 * sometimes referred to as the <i>population count</i>.
	 *
	 * @param i the value whose bits are to be counted
	 * @return the number of one-bits in the two's complement binary
	 * representation of the specified {@code int} value.
	 * @since 1.5
	 */
	public static int bitCount(int i) {
		// HD, Figure 5-2
		i = i - ((i >>> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
		i = (i + (i >>> 4)) & 0x0f0f0f0f;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		return i & 0x3f;
	}

	/**
	 * Returns the value obtained by rotating the two's complement binary
	 * representation of the specified {@code int} value left by the
	 * specified number of bits.  (Bits shifted out of the left hand, or
	 * high-order, side reenter on the right, or low-order.)
	 *
	 * <p>Note that left rotation with a negative distance is equivalent to
	 * right rotation: {@code rotateLeft(val, -distance) == rotateRight(val,
	 * distance)}.  Note also that rotation by any multiple of 32 is a
	 * no-op, so all but the last five bits of the rotation distance can be
	 * ignored, even if the distance is negative: {@code rotateLeft(val,
	 * distance) == rotateLeft(val, distance & 0x1F)}.
	 *
	 * @param i the value whose bits are to be rotated left
	 * @param distance the number of bit positions to rotate left
	 * @return the value obtained by rotating the two's complement binary
	 * representation of the specified {@code int} value left by the
	 * specified number of bits.
	 * @since 1.5
	 */
	public static int rotateLeft(int i, int distance) {
		return (i << distance) | (i >>> -distance);
	}

	/**
	 * Returns the value obtained by rotating the two's complement binary
	 * representation of the specified {@code int} value right by the
	 * specified number of bits.  (Bits shifted out of the right hand, or
	 * low-order, side reenter on the left, or high-order.)
	 *
	 * <p>Note that right rotation with a negative distance is equivalent to
	 * left rotation: {@code rotateRight(val, -distance) == rotateLeft(val,
	 * distance)}.  Note also that rotation by any multiple of 32 is a
	 * no-op, so all but the last five bits of the rotation distance can be
	 * ignored, even if the distance is negative: {@code rotateRight(val,
	 * distance) == rotateRight(val, distance & 0x1F)}.
	 *
	 * @param i the value whose bits are to be rotated right
	 * @param distance the number of bit positions to rotate right
	 * @return the value obtained by rotating the two's complement binary
	 * representation of the specified {@code int} value right by the
	 * specified number of bits.
	 * @since 1.5
	 */
	public static int rotateRight(int i, int distance) {
		return (i >>> distance) | (i << -distance);
	}

	/**
	 * Returns the value obtained by reversing the order of the bits in the
	 * two's complement binary representation of the specified {@code int}
	 * value.
	 *
	 * @param i the value to be reversed
	 * @return the value obtained by reversing order of the bits in the
	 * specified {@code int} value.
	 * @since 1.5
	 */
	public static int reverse(int i) {
		// HD, Figure 7-1
		i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
		i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
		i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
		i = (i << 24) | ((i & 0xff00) << 8) |
				((i >>> 8) & 0xff00) | (i >>> 24);
		return i;
	}

	/**
	 * Returns the signum function of the specified {@code int} value.  (The
	 * return value is -1 if the specified value is negative; 0 if the
	 * specified value is zero; and 1 if the specified value is positive.)
	 *
	 * @param i the value whose signum is to be computed
	 * @return the signum function of the specified {@code int} value.
	 * @since 1.5
	 */
	public static int signum(int i) {
		// HD, Section 2-7
		return (i >> 31) | (-i >>> 31);
	}

	/**
	 * Returns the value obtained by reversing the order of the bytes in the
	 * two's complement representation of the specified {@code int} value.
	 *
	 * @param i the value whose bytes are to be reversed
	 * @return the value obtained by reversing the bytes in the specified
	 * {@code int} value.
	 * @since 1.5
	 */
	public static int reverseBytes(int i) {
		return ((i >>> 24)) |
				((i >> 8) & 0xFF00) |
				((i << 8) & 0xFF0000) |
				((i << 24));
	}

	// 获取两个值的和，不过一般人不这么用，
	public static int sum(int a, int b) {
		return a + b;
	}

	// 获取两个数的最大值，底层调用Math.max()
	public static int max(int a, int b) {
		return Math.max(a, b);
	}

	// 获取两个数的最小值，底层调用Math.min()
	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	// 序列化ID
	// TODO 设计JDK的时候，是怎么定义该值的？
	@Native
	private static final long serialVersionUID = 1360826667806852920L;
}
