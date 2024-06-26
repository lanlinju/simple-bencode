package com.lanli.bencode

import java.io.BufferedInputStream
import java.io.InputStream

/**
 * 用于表示Bencode编码的不同类型的类
 */
internal sealed class BObject {
    data class BStr(val value: ByteArray) : BObject() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BStr

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    data class BInt(val value: Long) : BObject()
    data class BList(val value: List<BObject>) : BObject()
    data class BDict(val value: Map<String, BObject>) : BObject()

    final override fun toString(): String {
        return when (this) {
            is BStr -> "BStr('${value.decodeToString()}')"  // 将字节数组解码为字符串以进行打印
            is BInt -> "BInt($value)"
            is BList -> "BList(${value.joinToString(", ", "[", "]") { it.toString() }})"
            is BDict -> "BDict(${value.entries.joinToString(", ", "{", "}") { (k, v) -> "'$k': ${v.toString()}" }})"
        }
    }
}

internal fun parse(inputStream: InputStream): BObject {
    return parse(inputStream.buffered())
}

/**
 * 从给定的[reader]中读取字节流转化成[BObject]
 *
 * 根据第一个字符判断数据类型并调用相应的解码函数
 */
internal fun parse(reader: BufferedInputStream): BObject {
    val peek = reader.peek()
    return when {
        peek in '0'..'9' -> BObject.BStr(decodeString(reader))
        peek == 'i' -> BObject.BInt(decodeInt(reader))
        peek == 'l' -> decodeList(reader)
        peek == 'd' -> decodeDict(reader)
        else -> throw IllegalArgumentException("Invalid Bencode data")
    }
}

internal fun BufferedInputStream.peek(): Char {
    mark(1)
    val char: Int = read()
    reset()
    return char.toChar()
}

/**
 * 解码字节数组：格式为length:value，例如4:spam
 */
internal fun decodeString(reader: BufferedInputStream): ByteArray {
    val length = buildString {
        while (true) {
            val char = reader.read().toChar()
            if (char == ':') break
            append(char)
        }
    }.toInt()
    return reader.readNBytes(length)
}

/**
 * 解码整数：格式为i<integer>e，例如i32e
 */
internal fun decodeInt(reader: BufferedInputStream): Long {
    reader.read() // consume 'i'
    val number = buildString {
        while (true) {
            val char = reader.read().toChar()
            if (char == 'e') break
            append(char)
        }
    }.toLong()
    return number
}

/**
 * 解码列表：格式为l<item1><item2>...e，例如l4:spam4:eggse
 */
internal fun decodeList(reader: BufferedInputStream): BObject.BList {
    reader.read() // consume 'l'
    val list = mutableListOf<BObject>()
    while (true) {
        val peek = reader.peek()
        if (peek == 'e') {
            reader.read() // consume 'e'
            break
        }
        // 递归解析列表中的每一项
        list.add(parse(reader))
    }
    return BObject.BList(list)
}

/**
 * 解码字典：格式为d<key><value>...e，例如d3:cow3:moo4:spam4:eggse
 */
internal fun decodeDict(reader: BufferedInputStream): BObject.BDict {
    reader.read() // consume 'd'
    val dict = mutableMapOf<String, BObject>()
    while (true) {
        val peek = reader.peek()
        if (peek == 'e') {
            reader.read() // consume 'e'
            break
        }
        val key = decodeString(reader).decodeToString()
        val value = parse(reader)
        dict[key] = value
    }
    return BObject.BDict(dict)
}