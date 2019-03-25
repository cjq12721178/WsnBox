package com.weisi.tool.wsnbox.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object NullHelper {

    @JvmStatic
    fun <T> doNullOrNot(obj: T?, isNull: () -> Unit, notNull: (obj: T) -> Unit) {
        obj?.let(notNull) ?: isNull()
    }

    @JvmStatic
    fun <T, R> ifNullOrNot(obj: T?, isNull: () -> R, notNull: (obj: T) -> R): R {
        return if (obj === null) {
            isNull()
        } else {
            notNull(obj)
        }
    }

    @JvmStatic
    fun <T> doNull(obj: T?, isNull: () -> Unit) {
        obj ?: isNull()
    }

    @JvmStatic
    fun <T> ifNull(obj: T?, isNull: () -> Boolean): Boolean {
        return if (obj === null) {
            isNull()
            true
        } else {
            false
        }
    }

    @JvmStatic
    fun <T : Any> readonlyNotNull() = ReadonlyNotNullVar<T>()
}

class ReadonlyNotNullVar<T : Any>() : ReadWriteProperty<Any?, T> {

    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value === null) {
            value
        } else {
            throw IllegalStateException("不能设置为null，或已经有了")
        }
    }
}