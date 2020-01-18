package com.e.pictriptation.database.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
annotation class Column(val columnType: String, val isPrimaryKey: Boolean = false) {

    companion object {

        public const val TYPE_INT = "INTEGER"
        public const val TYPE_BLOB = "BLOB"
        public const val TYPE_TEXT = "TEXT"
        public const val TYPE_DOUBLE = "DOUBLE"
    }
}