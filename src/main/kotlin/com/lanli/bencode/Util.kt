package com.lanli.bencode

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

internal fun <T> Class<T>.createInstance(): T {
    return getDeclaredConstructor().newInstance()
}

/**
 * 获取实际的最List内部泛型参数类型
 * e.g. List<List<String>> 返回 String
 * e.g. List<List<Int>> 返回 Int
 */
internal fun extractNestedType(type: Type): Class<*> {
    return when (type) {
        is ParameterizedType -> extractNestedType(type.actualTypeArguments[0])
        is WildcardType -> extractNestedType(type.upperBounds[0])
        else -> type as Class<*>
    }
}

/**
 * 判断一个Class<*>是否为List集合类型
 */
internal fun isListType(clazz: Class<*>): Boolean {
    return List::class.java.isAssignableFrom(clazz)
}

/**
 * 例如List<List<String>> 返回 [List,List,String]
 */
internal fun parseTypeHierarchy(type: Type): List<Class<*>> {
    val result = mutableListOf<Class<*>>()
    parseTypeRecursive(type, result)
    return result
}

private fun parseTypeRecursive(currentType: Type, result: MutableList<Class<*>>) {
    when (currentType) {
        is ParameterizedType -> {
            val rawType = currentType.rawType as Class<*>
            result.add(rawType)
            val actualType = currentType.actualTypeArguments[0]
            parseTypeRecursive(actualType, result)
        }

        is WildcardType -> {
            val upperBound = currentType.upperBounds[0]
            parseTypeRecursive(upperBound, result)
        }

        is Class<*> -> {
            result.add(currentType)
        }

        else -> throw IllegalArgumentException("Unknown type: $currentType")
    }
}