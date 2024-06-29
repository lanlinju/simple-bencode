package com.lanli.bencode

import java.lang.reflect.Field

/**
 * 根据 BObject 类型 [bObject] 执行反序列化操作。
 * 由于类型擦除(Type Erasure),以l开头的List列表的内部类型，只能是String或者Int类型
 *
 * 返回反序列化后的对象。
 */
internal fun unmarshal(clazz: Class<*>, bObject: BObject): Any {
    return when (bObject) {
        is BObject.BStr -> unmarshalString(clazz, bObject)
        is BObject.BInt -> unmarshalInt(clazz, bObject)
        is BObject.BList -> unmarshalList(clazz, bObject.value, Nothing::class.java)
        is BObject.BDict -> unmarshalDict(clazz, bObject.value)
    }
}

internal fun unmarshalString(clazz: Class<*>, o: BObject.BStr): String {
    if (clazz == String::class.java || clazz == Nothing::class.java) {
        return o.value
    }
    throw IllegalArgumentException("Type mismatch: expected String but got ${clazz.name}")
}

/**
 * 执行整数类型的反序列化
 *
 * 执行字符串类型的反序列化
 * [Nothing]只是用于表明直接解析[Int] e.g. i32e
 */
internal fun unmarshalInt(clazz: Class<*>, o: BObject.BInt): Number {
    return when (clazz) {
        Int::class.java -> o.value.toInt()
        Long::class.java -> o.value
        Nothing::class.java -> o.value
        else -> throw IllegalArgumentException("Type mismatch: expected Int but got ${clazz.name}")
    }
}

/**
 * [clazz]用于判断是否为集合类型，[genericClazz]为集合内部的类型
 */
internal fun unmarshalList(clazz: Class<*>, list: List<BObject>, genericClazz: Class<*>): List<Any> {
    if (!isListType(clazz)) {
        throw IllegalArgumentException("Type mismatch: expected List but got ${clazz.name}")
    }

    return unmarshalList(genericClazz, list)
}

/**
 * 根据指定的 [genericClazz] 和 BObject.BList 对象 [list] 执行列表类型的反序列化。
 * 返回反序列化后的列表对象。
 */
internal fun unmarshalList(genericClazz: Class<*>, list: List<BObject>): List<Any> {
    if (list.isEmpty()) return emptyList()

    return list.map { bObject ->
        when (bObject) {
            is BObject.BStr -> unmarshalString(genericClazz, bObject)
            is BObject.BInt -> unmarshalInt(genericClazz, bObject)
            is BObject.BList -> unmarshalList(genericClazz, bObject.value) // TODO():未作类型检查
            is BObject.BDict -> unmarshalDict(genericClazz, bObject.value) // 未作类型检查
        }
    }
}

/**
 * 根据指定的 [Class] 和 BObject.BDict 对象 [dict] 执行字典类型的反序列化。
 * 返回反序列化后的实例对象。
 */
internal fun unmarshalDict(clazz: Class<*>, dict: Map<String, BObject>): Any {
    val instance = clazz.createInstance()
    for ((key, value) in dict) {
        val field = clazz.declaredFields.find { it.name == key } ?: continue
        field.isAccessible = true
        setFieldValue(instance, field, value)
    }
    return instance
}

private fun setFieldValue(any: Any, field: Field, value: BObject) {
    val fieldType = field.type
    when (value) {
        is BObject.BStr -> setStringField(any, field, value, fieldType)
        is BObject.BInt -> setIntField(any, field, value, fieldType)
        is BObject.BList -> setListField(any, field, value, fieldType)
        is BObject.BDict -> setDictField(any, field, value, fieldType)
    }
}

private fun setStringField(any: Any, field: Field, value: BObject.BStr, fieldType: Class<*>) {
    when (fieldType) {
        String::class.java -> field.set(any, value.value)
        else -> throw IllegalArgumentException("Type mismatch: expected String but got ${fieldType.name}")
    }
}

private fun setIntField(any: Any, field: Field, value: BObject.BInt, fieldType: Class<*>) {
    when (fieldType) {
        // If the field type is Int, convert the value to Int and set the field value.
        Int::class.java -> field.set(any, value.value.toInt())
        // If the field type is Long, set the field value.
        Long::class.java -> field.set(any, value.value)
        else -> throw IllegalArgumentException("Type mismatch: expected Int or Long but got ${fieldType.name}")
    }
}

private fun setListField(any: Any, field: Field, value: BObject.BList, fieldType: Class<*>) {
    val genericType = extractNestedType(field.genericType)
    val list = unmarshalList(fieldType, value.value, genericType)
    field.set(any, list)
}

private fun setDictField(any: Any, field: Field, value: BObject.BDict, fieldType: Class<*>) {
    val nestedInstance = unmarshalDict(fieldType, value.value)
    field.set(any, nestedInstance)
}