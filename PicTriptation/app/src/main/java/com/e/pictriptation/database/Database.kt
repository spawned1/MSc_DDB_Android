package com.e.pictriptation.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.e.pictriptation.R
import com.e.pictriptation.database.annotations.Column
import com.e.pictriptation.database.annotations.ForeignKeyColumn
import com.e.pictriptation.database.annotations.Table
import com.e.pictriptation.model.Picture
import com.e.pictriptation.model.Route
import com.e.pictriptation.model.Trip
import com.e.pictriptation.model.User
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

class Database(context: Context): SQLiteOpenHelper(context, "PicTriptation", null, DATABASE_VERSION) {


    companion object {

        private const val DATABASE_VERSION = 1
    }


    //region Database Methods

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    override fun onCreate(db: SQLiteDatabase?) {

        createTable<User>(db)
        createTable<Trip>(db)
        createTable<Route>(db)
        createTable<Picture>(db)
    }

    inline fun <reified T: Any> createTable(db: SQLiteDatabase?) {

        var columns = ArrayList<String>()
        for (property in T::class.declaredMemberProperties) {

            val column = property.annotations.find { it is Column } as? Column
            if (column == null)
                continue;

            var columnText = property.name + " " + column.columnType;
            if (column.isPrimaryKey)
                columnText += " PRIMARY KEY AUTOINCREMENT"

            columns.add(columnText)
        }

        val tableName = getTableName<T>()
        var sql = "CREATE TABLE $tableName(" + columns.joinToString(", ") + ")"

        db!!.execSQL(sql)
    }


    inline fun <reified T: Any> getTableName() :String {

        val table = T::class.annotations.find { it is Table } as? Table
        return table!!.name
    }

    inline fun <reified T: Any> getColumns(includePrimaryKey: Boolean): Map<KMutableProperty<*>, Column> {

        var columns = mutableMapOf<KMutableProperty<*>, Column>()
        for (property in T::class.declaredMemberProperties) {

            val column = property.annotations.find { it is Column } as? Column
            if (column == null || (!includePrimaryKey && column.isPrimaryKey))
                continue;

            columns.put(property as KMutableProperty<*>, column)
        }

        return columns
    }

    inline fun <reified T: Any> getForeignKeyColumn(): KMutableProperty<*>? {

        for (property in T::class.declaredMemberProperties) {

            val column = property.annotations.find { it is ForeignKeyColumn } as? ForeignKeyColumn
            if (column == null)
                continue;

            return property as KMutableProperty<*>
        }

        return null
    }

    inline fun <reified T: Any> getColumnValues(instance: T, columns: Map<KMutableProperty<*>, Column>): ArrayList<Any> {

        var columnValues = ArrayList<Any>()
        for (column in columns) {

            if (column.key.returnType == Date::class.createType()) {

                var timestamp = column.key.getter.call(instance) as Date
                columnValues.add(timestamp.time)
            }
            else if (column.key.returnType == Bitmap::class.createType()) {

                var image = column.key.getter.call(instance) as Bitmap
                if (image == null)
                    continue

                var stream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);

                columnValues.add(stream.toByteArray())
            }
            else {
                columnValues.add(column.key.getter.call(instance)!!)
            }
        }

        return columnValues
    }

    inline fun <reified T: Any> getPrimaryKey(): Map.Entry<KMutableProperty<*>, Column>? {

        for (column in getColumns<T>(true)) {

            if (!column.value.isPrimaryKey)
                continue;

            return column
        }

        return null
    }

    //endregion



    //region Sql Methods

    inline fun <reified T: Any> select(): ArrayList<T> {

        val instances = ArrayList<T>()

        val tableName = getTableName<T>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $tableName", null)

        cursor.moveToFirst().run {

            do {
                cursorToInstance<T>(cursor)?.let { instances.add(it) }
            } while (cursor.moveToNext())
        }
        readableDatabase.close()

        return instances
    }

    inline fun <reified T: Any> selectById(id: Long): T? {

        val primaryKeyColumn = getPrimaryKey<T>()
        if (primaryKeyColumn == null)
            return null

        val tableName = getTableName<T>()
        val primaryKeyColumnName = primaryKeyColumn.key.name
        val cursor = readableDatabase.rawQuery("SELECT * FROM $tableName WHERE $primaryKeyColumnName = ?", arrayOf(id.toString()))

        val instances = ArrayList<T>()
        cursor.moveToFirst().run {

            do {
                cursorToInstance<T>(cursor)?.let { instances.add(it) }
            } while (cursor.moveToNext())
        }
        readableDatabase.close()

        return instances.firstOrNull()
    }

    inline fun <reified T: Any, reified U: Any> selectByForeignKey(foreignKey :Any): ArrayList<T> {

        val instances = ArrayList<T>()

        val primaryKeyColumn = getPrimaryKey<U>()
        if (primaryKeyColumn == null)
            return instances

        val foreignKeyColumn = getForeignKeyColumn<T>()
        if (foreignKeyColumn == null)
            return instances

        val tableName = getTableName<T>()
        val foreignKeyColumnName = foreignKeyColumn.name

        val cursor = readableDatabase.rawQuery("SELECT * FROM $tableName WHERE $foreignKeyColumnName = ?", arrayOf(primaryKeyColumn.key.getter.call(foreignKey).toString()))

        cursor.moveToFirst().run {

            do {
                cursorToInstance<T>(cursor)?.let { instances.add(it) }
            } while (cursor.moveToNext())
        }
        readableDatabase.close()

        return instances
    }

    inline fun <reified T: Any> selectLastInserted(): T {

        val tableName = getTableName<T>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $tableName WHERE rowid = (SELECT rowid from $tableName order by ROWID DESC limit 1)", null)

        lateinit var instance : T
        cursor.moveToFirst().run {
            cursorToInstance<T>(cursor)?.let { instance = it }
        }
        readableDatabase.close()

        return instance
    }


    inline fun <reified T: Any> save(instance: T): T {

        val primaryKey = getPrimaryKey<T>()!!
        val primaryKeyValue = primaryKey.key.getter.call(instance) as Long
        if (primaryKeyValue == 0L) {
            return insert(instance)
        }
        else {
            return update(instance)
        }
    }

    inline fun <reified T: Any> insert(instance: T): T {

        val tableName = getTableName<T>()
        val columns = getColumns<T>(false)
        val values = getColumnValues<T>(instance, columns)

        writableDatabase.execSQL(
            "INSERT INTO $tableName (" + columns.keys.joinToString(separator = ", ", transform = { it.name }) + ") VALUES (" + columns.values.joinToString(separator = ", ", transform = { "?" }) + ")",
            values.toArray()
        )

        return selectLastInserted()
    }

    inline fun <reified T: Any> update(instance: T): T {

        val tableName = getTableName<T>()
        val primaryKey = getPrimaryKey<T>()
        val primaryKeyName = primaryKey!!.key.name
        val columns = getColumns<T>(false)

        val values = getColumnValues(instance, columns)
        values.add(primaryKey.key.getter.call(instance)!!)

        writableDatabase.execSQL(
            "UPDATE $tableName SET " + columns.keys.joinToString(separator = ", ", transform = { it.name + " = ?" }) + " WHERE $primaryKeyName = ?",
            values.toArray()
        )

        return instance
    }

    //endregion



    //region Methods

    inline fun <reified T: Any> cursorToInstance(cursor: Cursor): T? {

        if (cursor.count == 0)
            return null

        val columns = getColumns<T>(true)

        cursor.run {

            var instance = T::class.createInstance()
            for (column in columns) {

                var value :Any? = null

                if (column.key.returnType == String::class.createType() || column.key.returnType == String::class.createType(nullable = true)) {
                    value = getString(getColumnIndex(column.key.name))
                }

                if (column.key.returnType == Date::class.createType() || column.key.returnType == Date::class.createType(nullable = true)) {
                    value = Date(getLong(getColumnIndex(column.key.name)))
                }

                if (column.key.returnType == Long::class.createType() || column.key.returnType == Long::class.createType(nullable = true)) {
                    value = getLong(getColumnIndex(column.key.name))
                }

                if (column.key.returnType == Double::class.createType() || column.key.returnType == Double::class.createType(nullable = true)) {
                    value = getDouble(getColumnIndex(column.key.name))
                }

                if (column.key.returnType == Bitmap::class.createType()) {

                    value = getBlob(getColumnIndex(column.key.name))
                    value = BitmapFactory.decodeByteArray(value, 0, value.size)
                }

                column.key.setter.call(instance, value)
            }

            return instance
        }
    }

    //endregion


}