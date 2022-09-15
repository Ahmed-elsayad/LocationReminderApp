package com.udacity.project4.base

abstract class BaseDataClass {
    abstract val id: String

    abstract override fun equals(other: Any?): Boolean
    override fun hashCode(): Int {
        return id.hashCode()
    }
}