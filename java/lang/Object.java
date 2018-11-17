/*
 * Copyright (c) 1994, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package java.lang;

public class Object {

	// 1、native关键字是干嘛用的？
	// 一个native Method就是一个java调用非java代码(C代码等)的接口，即该方法的实现由非java语言实现，这个关键字为不同语言之间的交互提供了通道

	// FIXME JNI是什么？JNI涉及JVM的相关概念
	// 以下为私有静态方法（JNI函数），作用：为你自己的类注册本地函数
    private static native void registerNatives();
    static {
        registerNatives();
    }

    // 获取当前类的反射类
    public final native Class<?> getClass();

    // 获取当前对象的 哈希值
    public native int hashCode();

    // 判断两个对象是否相等，所有类从Object继承的equals方法删掉实现，是采用 == 号实现的，也就是比较的是地址值
	// 地址值涉及到哈希值
	// FIXME 地址值和哈希值的关系是什么？
    public boolean equals(Object obj) {
        return (this == obj);
    }

    // 克隆方法居然是native method，也就是这个方法并非由Java代码来具体实现的
    protected native Object clone() throws CloneNotSupportedException;

    // 获取对象字符串，所不同的是：所有的类从Object继承的toString()方法，获取的格式是 "类全名@十六进制的哈希值"
	// 使用该格式的原因：能清楚地区分该对象是 哪个类，且不同的对象的哈希值是不同的，因此该字符串能唯一标记该对象
	// 若想获得每个类特定的字符串，必须重写该方法
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    // 多线程相关：唤醒，有趣的是，它也是一个native method
    public final native void notify();
    // 多线程相关：唤醒所有，一个native method
    public final native void notifyAll();


	// 多线程相关：线程等待，与notify()相关
	public final void wait() throws InterruptedException {
		wait(0);
	}
    // 多线程相关：线程等待，一个native method
    public final native void wait(long timeout) throws InterruptedException;
	// 多线程相关：线程等待
    public final void wait(long timeout, int nanos) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("nanosecond timeout value out of range");
        }

        if (nanos >= 500000 || (nanos != 0 && timeout == 0)) {
            timeout++;
        }

        wait(timeout);
    }

    // TODO 深入理解该方法，该方法涉及JVM相关概念
	// Java的GC只负责内存相关的清理，非内存资源的清理工作由程序员手工完成，GC回收对象之前会调用该方法
	// 建议用于：1、清理本地对象(通过JNI创建的对象)。2、作为确保某些非内存资源(Socket、文件等)释放的一个补充
	// 该方法有很大的不确定性：不保证方法中的任务执行，所以用来回收资源也不会有什么好的表现，所以没什么使用场景
	// 如何释放非内存资源？子类重写该方法，在重写的方法中显示调用其他资源释放方法
    protected void finalize() throws Throwable { }
}
