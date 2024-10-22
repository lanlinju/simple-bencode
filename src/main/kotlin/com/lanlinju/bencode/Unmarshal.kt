package com.lanlinju.bencode

import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Target(AnnotationTarget.FIELD)
@Retention
annotation class BencodeName(val name: String)

/**
 * Deserializes the given BObject [bObject] into an instance of the specified class [clazz].
 * Returns the deserialized object of type [clazz].
 */
internal fun unmarshal(clazz: Class<*>, bObject: BObject): Any {
    return when (bObject) {
        is BObject.BStr -> unmarshalString(clazz, bObject.value)
        is BObject.BInt -> unmarshalInt(clazz, bObject.value)
        is BObject.BList -> unmarshalList(clazz, bObject.value)
        is BObject.BDict -> unmarshalDict(clazz, bObject.value)
    }
}

/**
 * Deserializes a BObject.BStr (byte array) into an object of the specified class type [clazz].
 * If [clazz] is ByteArray, it returns the byte array directly. Otherwise, it decodes the byte array into a String.
 */
internal fun unmarshalString(clazz: Class<*>, bytes: ByteArray): Any {
    return when (clazz) {
        ByteArray::class.java -> bytes
        else -> String(bytes)
    }
}

/**
 * Deserializes a BObject.BInt into a numeric type of the specified class [clazz].
 * If the target class is [Int], it converts the value to Int, otherwise it returns the [Long] value.
 */
internal fun unmarshalInt(clazz: Class<*>, value: Long): Any {
    return when (clazz) {
        Int::class.java -> value.toInt()
        else -> value
    }
}

/**
 * Deserializes a BObject.BList into a list of objects of the specified [clazz].
 * Each element in the list is deserialized recursively.
 */
internal fun unmarshalList(clazz: Class<*>, list: List<BObject>): Any {
    if (list.isEmpty()) return emptyList<Any>()
    return list.map { bObject -> unmarshal(clazz, bObject) }
}

/**
 * Deserializes a BObject.BDict into an instance of the specified class [clazz].
 * The dictionary values are used to set the corresponding fields of the class.
 */
internal fun unmarshalDict(clazz: Class<*>, dict: Map<String, BObject>): Any {
    val fields = getAllFields(clazz)
    val instance = createInstance(clazz, fields, dict)

    fields.forEach { field ->
        val key = getFieldName(field)  // Get the field name (including any annotation name processing)
        val value = dict[key] ?: return@forEach  // Skip if the field is not in the dictionary
        field.isAccessible = true

        // Determine if the field type is a List, and if so, extract the nested type
        val targetType = getFieldTargetType(field)

        // Deserialize and set the field value
        field.set(instance, unmarshal(targetType, value))
    }

    return instance
}

/**
 * Determines if the field type is a List and extracts its nested type,
 * otherwise returns the original type of the field.
 */
private fun getFieldTargetType(field: Field): Class<*> {
    return if (isListType(field.type)) {
        extractNestedType(field.genericType)
    } else {
        field.type
    }
}

/**
 * Creates an instance of the specified class [clazz] using its primary constructor.
 * Constructor arguments are matched with the corresponding values from the provided [dict].
 */
private fun createInstance(
    clazz: Class<*>,
    fields: List<Field>,
    dict: Map<String, BObject>
): Any {
    val constructor = clazz.kotlin.primaryConstructor
        ?: throw IllegalArgumentException("Class must have a primary constructor")

    val args = constructor.parameters.associateWith { param ->
        val fieldAnnotation = fields.find { field -> field.name == param.name }
            ?.getAnnotation(BencodeName::class.java)?.name
        val fieldName = fieldAnnotation ?: param.name
        ?: throw IllegalArgumentException("Constructor parameter must have a name")

        val value = dict[fieldName]
            ?: throw IllegalArgumentException("Missing value for parameter $fieldName")

        val fieldType = (param.type.classifier as KClass<*>).java
        if (isListType(fieldType)) return@associateWith emptyList<Any>()

        unmarshal(fieldType, value)
    }

    val instance = constructor.callBy(args)
    return instance
}