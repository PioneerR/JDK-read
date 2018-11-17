/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package java.lang;

// 该类是一个抽象类，且是BigDecimal、BigInteger、Byte、Double、Float、Integer、Long 和 Short 类的超类
// 基本的基础类型，都实现了序列化类，即可以进行序列化操作
// 必须确保所有实现Serializable的类中的UID的唯一性
public abstract class Number implements java.io.Serializable {

	// 以下均为抽象方法，使得继承Number的类都必须重写，重写后，不同的数据类型直接可以互相转换
	// 比如integer类的值，可以直接通过方法，获取到long类型的值
	// 只不过命名方法，没有动词作为前缀
    public abstract int intValue();
    public abstract long longValue();
    public abstract float floatValue();
    public abstract double doubleValue();

    // 以下两个方法理论上应该是抽象方法
    public byte byteValue() {
        return (byte)intValue();
    }
    public short shortValue() {
        return (short)intValue();
    }

    // 实现序列化类之后，必须定义一个内部的序列化UID，用来验证版本的一致性，反序列化会用到该UID进行判断UID是否一致
	// 如果一致，才可以进行反序列化，且UID是一个long类型的值
	// serialVersionUID用来表明类的不同版本间的兼容性
    private static final long serialVersionUID = -8742448824652078965L;
}
