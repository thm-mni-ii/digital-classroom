package de.thm.mni.ii.classroom.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T> setOnce(): ReadWriteProperty<Any, T> = SetOnceProperty()

@Suppress("UNCHECKED_CAST")
class SetOnceProperty<T> : ReadWriteProperty<Any, T> {

    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value == EMPTY) {
            throw IllegalStateException("Value isn't initialized")
        } else {
            return value as T
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value != EMPTY) {
            throw IllegalStateException("Value is initialized")
        }
        this.value = value
    }
}
