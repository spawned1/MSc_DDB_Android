package com.e.pictriptation.database.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
annotation class ForeignKeyColumn(val foreignKeyType: KClass<*>) {

}