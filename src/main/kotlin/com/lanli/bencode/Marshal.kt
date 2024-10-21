package com.lanli.bencode

import java.lang.reflect.Field

/**
 * 将给定对象 [any] 转换为对应的 BObject。
 */
internal fun marshal(any: Any): BObject {
    return when (any) {
        is String -> marshalString(any.toByteArray())
        is ByteArray -> marshalString(any)
        is Number -> marshalInt(any.toLong())
        is List<*> -> marshalList(any)
        else -> marshalDict(any)
    }
}

/**
 * 将字节数组 [value] 转换为 BObject.BStr。
 */
internal fun marshalString(value: ByteArray): BObject.BStr {
    return BObject.BStr(value)
}

/**
 * 将整数 [value] 转换为 BObject.BInt。
 */
internal fun marshalInt(value: Long): BObject.BInt {
    return BObject.BInt(value)
}

/**
 * 将列表 [value] 转换为 BObject.BList。
 */
internal fun marshalList(value: List<*>): BObject.BList {
    val bList = value.map { marshal(it!!) }
    return BObject.BList(bList)
}

internal fun marshalDict(value: Any): BObject.BDict {
    val allFields = getAllFields(value::class.java)
    val bDict = allFields.associate { field ->
        field.isAccessible = true
        val name = getFieldName(field)
        name to marshal(field.get(value)!!)
    }
    return BObject.BDict(bDict)
}

/**
 * 优先获取注解BencodeName里的名称，否则使用字段默认名
 */
private fun getFieldName(field: Field): String {
    val annotation = field.getAnnotation(BencodeName::class.java)
    return annotation?.name ?: field.name
}