package com.lanlinju.bencode

/**
 * Converts the given object [any] to the corresponding BObject.
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
 * Converts a byte array [bytes] to a BObject.BStr.
 */
internal fun marshalString(bytes: ByteArray): BObject.BStr {
    return BObject.BStr(bytes)
}

/**
 * Converts an integer [value] to a BObject.BInt.
 */
internal fun marshalInt(value: Long): BObject.BInt {
    return BObject.BInt(value)
}

/**
 * Converts a list [list] to a BObject.BList.
 */
internal fun marshalList(list: List<*>): BObject.BList {
    val bList = list.map { marshal(it!!) }
    return BObject.BList(bList)
}

/**
 * Converts an object [any] to a BObject.BDict by reflecting on its fields.
 * Uses reflection to retrieve all fields from the class and maps them to Bencode format.
 */
internal fun marshalDict(any: Any): BObject.BDict {
    val allFields = getAllFields(any::class.java)
    val bDict = allFields.associate { field ->
        field.isAccessible = true
        val name = getFieldName(field)
        name to marshal(field.get(any))
    }
    return BObject.BDict(bDict)
}