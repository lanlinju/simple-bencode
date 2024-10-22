package com.lanlinju.bencode

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Retrieves the actual generic parameter type of a List.
 * For example, List<List<String>> returns String.
 * For example, List<List<Int>> returns Int.
 */
internal fun extractNestedType(type: Type): Class<*> {
    return when (type) {
        is ParameterizedType -> extractNestedType(type.actualTypeArguments[0])
        is WildcardType -> extractNestedType(type.upperBounds[0])
        else -> type as Class<*>
    }
}

/**
 * Checks whether a Class<*> is a List type.
 */
internal fun isListType(clazz: Class<*>): Boolean {
    return List::class.java.isAssignableFrom(clazz)
}

/**
 * Recursively retrieves all fields of a class and its superclasses.
 */
internal fun getAllFields(clazz: Class<*>): List<Field> {
    val fields = clazz.declaredFields.toMutableList()
    clazz.superclass?.let { superClass ->
        fields.addAll(getAllFields(superClass)) // Recursively retrieve fields from superclass
    }
    return fields
}

/**
 * Gets the field name. If the field has a BencodeName annotation, it uses the name from the annotation.
 * Otherwise, it uses the default field name.
 */
internal fun getFieldName(field: Field): String {
    return field.getAnnotation(BencodeName::class.java)?.name ?: field.name
}
