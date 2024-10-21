package com.lanli.bencode

import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Target(AnnotationTarget.FIELD)
@Retention
annotation class BencodeName(val name: String)

/**
 * 根据 BObject 类型 [bObject] 执行反序列化操作。
 *
 * 返回反序列化后的对象。
 */
internal fun unmarshal(clazz: Class<*>, bObject: BObject): Any {
    return when (bObject) {
        is BObject.BStr -> unmarshalString(clazz, bObject.value)
        is BObject.BInt -> unmarshalInt(clazz, bObject.value)
        is BObject.BList -> unmarshalList(clazz, bObject.value)
        is BObject.BDict -> unmarshalDict(clazz, bObject.value)
    }
}

internal fun unmarshalString(clazz: Class<*>, bytes: ByteArray): Any {
    return when (clazz) {
        ByteArray::class.java -> bytes
        else -> String(bytes)
    }
}

/**
 * 执行整数类型的反序列化
 *
 * 执行字符串类型的反序列化
 * [Nothing]只是用于表明直接解析[Int] e.g. i32e
 */
internal fun unmarshalInt(clazz: Class<*>, value:Long): Any {
    return when (clazz) {
        Int::class.java -> value.toInt()
        else -> value
    }
}

/**
 * 根据指定的 [genericClazz] 和 BObject.BList 对象 [list] 执行列表类型的反序列化。
 * 返回反序列化后的列表对象。
 */
internal fun unmarshalList(clazz: Class<*>, list: List<BObject>): Any {
    if (list.isEmpty()) return emptyList<Any>()

    return list.map { bObject -> unmarshal(clazz, bObject) }
}

/**
 * 根据指定的 [Class] 和 BObject.BDict 对象 [dict] 执行字典类型的反序列化。
 * 返回反序列化后的实例对象。
 */
internal fun unmarshalDict(clazz: Class<*>, dict: Map<String, BObject>): Any {
    val fields = getAllFields(clazz)
    val instance = createInstance(clazz, fields, dict)

    for ((key, value) in dict) {
        val field = findField(fields, key) ?: continue
        field.isAccessible = true
        field.set(instance, unmarshal(field.type, value))
    }
    return instance
}

private fun createInstance(
    clazz: Class<*>,
    fields: List<Field>,
    dict: Map<String, BObject>
): Any {
    val constructor =
        clazz.kotlin.primaryConstructor ?:  error("Class must have a primary constructor")
    val args = constructor.parameters.associateWith { param ->
        val bencodeName = fields.find { field -> field.name == param.name }.let { it?.getAnnotation(BencodeName::class.java)?.name }
        val fieldName = bencodeName ?: param.name ?: error("Constructor parameter must have a name")
        val value = dict[fieldName] ?: error("Missing value for parameter $fieldName")
        unmarshal((param.type.classifier as KClass<*>).java, value)
    }
    val instance = constructor.callBy(args)
    return instance
}

/**
 * 查找类 [clazz] 中具有给定键 [key] 的字段。
 * 如果字段上有 @BencodeName 注解且注解的名称与键匹配，或者字段名称与键匹配，则返回该字段。
 */
private fun findField(fields: List<Field>, key: String): Field? {
    return fields.find {
        val annotation = it.getAnnotation(BencodeName::class.java)
        it.name == key || annotation?.name == key
    }
}