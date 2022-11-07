/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.writer

import androidx.room.compiler.codegen.toJavaPoet
import androidx.room.ext.CommonTypeNames
import androidx.room.ext.L
import androidx.room.ext.N
import androidx.room.ext.RoomTypeNames
import androidx.room.ext.S
import androidx.room.ext.T
import androidx.room.ext.capitalize
import androidx.room.ext.stripNonJava
import androidx.room.ext.typeName
import androidx.room.parser.SQLTypeAffinity
import androidx.room.vo.Entity
import androidx.room.vo.columnNames
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import java.util.Arrays
import java.util.Locale

class TableInfoValidationWriter(val entity: Entity) : ValidationWriter() {

    companion object {
        const val CREATED_FROM_ENTITY = "CREATED_FROM_ENTITY"
    }

    override fun write(dbParam: ParameterSpec, scope: CountingCodeGenScope) {
        val suffix = entity.tableName.stripNonJava().capitalize(Locale.US)
        val expectedInfoVar = scope.getTmpVar("_info$suffix")
        scope.builder().apply {
            val columnListVar = scope.getTmpVar("_columns$suffix")
            val columnListType = ParameterizedTypeName.get(
                HashMap::class.typeName,
                CommonTypeNames.STRING.toJavaPoet(), RoomTypeNames.TABLE_INFO_COLUMN
            )

            addStatement(
                "final $T $L = new $T($L)", columnListType, columnListVar,
                columnListType, entity.fields.size
            )
            entity.fields.forEach { field ->
                addStatement(
                    "$L.put($S, new $T($S, $S, $L, $L, $S, $T.$L))",
                    columnListVar, field.columnName, RoomTypeNames.TABLE_INFO_COLUMN,
                    /*name*/ field.columnName,
                    /*type*/ field.affinity?.name ?: SQLTypeAffinity.TEXT.name,
                    /*nonNull*/ field.nonNull,
                    /*pkeyPos*/ entity.primaryKey.fields.indexOf(field) + 1,
                    /*defaultValue*/ field.defaultValue,
                    /*createdFrom*/ RoomTypeNames.TABLE_INFO, CREATED_FROM_ENTITY
                )
            }

            val foreignKeySetVar = scope.getTmpVar("_foreignKeys$suffix")
            val foreignKeySetType = ParameterizedTypeName.get(
                HashSet::class.typeName,
                RoomTypeNames.TABLE_INFO_FOREIGN_KEY
            )
            addStatement(
                "final $T $L = new $T($L)", foreignKeySetType, foreignKeySetVar,
                foreignKeySetType, entity.foreignKeys.size
            )
            entity.foreignKeys.forEach {
                val myColumnNames = it.childFields
                    .joinToString(",") { "\"${it.columnName}\"" }
                val refColumnNames = it.parentColumns
                    .joinToString(",") { "\"$it\"" }
                addStatement(
                    "$L.add(new $T($S, $S, $S," +
                        "$T.asList($L), $T.asList($L)))",
                    foreignKeySetVar,
                    RoomTypeNames.TABLE_INFO_FOREIGN_KEY,
                    /*parent table*/ it.parentTable,
                    /*on delete*/ it.onDelete.sqlName,
                    /*on update*/ it.onUpdate.sqlName,
                    Arrays::class.typeName,
                    /*parent names*/ myColumnNames,
                    Arrays::class.typeName,
                    /*parent column names*/ refColumnNames
                )
            }

            val indicesSetVar = scope.getTmpVar("_indices$suffix")
            val indicesType = ParameterizedTypeName.get(
                HashSet::class.typeName,
                RoomTypeNames.TABLE_INFO_INDEX
            )
            addStatement(
                "final $T $L = new $T($L)", indicesType, indicesSetVar,
                indicesType, entity.indices.size
            )
            entity.indices.forEach { index ->
                val columnNames = index.columnNames.joinToString(",") { "\"$it\"" }
                val orders = if (index.orders.isEmpty()) {
                    index.columnNames.map { "ASC" }.joinToString(",") { "\"$it\"" }
                } else {
                    index.orders.joinToString(",") { "\"$it\"" }
                }
                addStatement(
                    "$L.add(new $T($S, $L, $T.asList($L), $T.asList($L)))",
                    indicesSetVar,
                    RoomTypeNames.TABLE_INFO_INDEX,
                    index.name,
                    index.unique,
                    Arrays::class.typeName,
                    columnNames,
                    Arrays::class.typeName,
                    orders,
                )
            }

            addStatement(
                "final $T $L = new $T($S, $L, $L, $L)",
                RoomTypeNames.TABLE_INFO, expectedInfoVar, RoomTypeNames.TABLE_INFO,
                entity.tableName, columnListVar, foreignKeySetVar, indicesSetVar
            )

            val existingVar = scope.getTmpVar("_existing$suffix")
            addStatement(
                "final $T $L = $T.read($N, $S)",
                RoomTypeNames.TABLE_INFO, existingVar, RoomTypeNames.TABLE_INFO,
                dbParam, entity.tableName
            )

            beginControlFlow("if (! $L.equals($L))", expectedInfoVar, existingVar).apply {
                addStatement(
                    "return new $T(false, $S + $L + $S + $L)",
                    RoomTypeNames.OPEN_HELPER_VALIDATION_RESULT,
                    "${entity.tableName}(${entity.element.qualifiedName}).\n Expected:\n",
                    expectedInfoVar, "\n Found:\n", existingVar
                )
            }
            endControlFlow()
        }
    }
}
