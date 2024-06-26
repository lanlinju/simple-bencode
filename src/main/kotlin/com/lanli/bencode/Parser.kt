package com.lanli.com.lanli.bencode

import java.io.BufferedReader
import java.io.InputStream

internal sealed class BObject {
    data class BStr(val value: String) : BObject()
    data class BInt(val value: Int) : BObject()
    data class BList(val value: List<BObject>) : BObject()
    data class BDict(val value: Map<String, BObject>) : BObject()

    final override fun toString(): String {
        return when (this) {
            is BStr -> "BStr('$value')"
            is BInt -> "BInt($value)"
            is BList -> "BList(${value.joinToString(", ", "[", "]") { it.toString() }})"
            is BDict -> "BDict(${value.entries.joinToString(", ", "{", "}") { (k, v) -> "'$k': ${v.toString()}" }})"
        }
    }
}

internal fun parse(reader: BufferedReader): BObject {
    val peek = reader.peek()
    return when {
        peek in '0'..'9' -> BObject.BStr(decodeString(reader))
        peek == 'i' -> BObject.BInt(decodeInt(reader))
        peek == 'l' -> decodeList(reader)
        peek == 'd' -> decodeDict(reader)
        else -> throw IllegalArgumentException("Invalid Bencode data")
    }
}

internal fun BufferedReader.peek(): Char {
    mark(1)
    val char: Int = read()
    reset()
    return char.toChar()
}

/**
 * 4:spam
 */
internal fun decodeString(reader: BufferedReader): String {
    val length = buildString {
        while (true) {
            val char = reader.read().toChar()
            if (char == ':') break
            append(char)
        }
    }.toInt()
    return reader.readNChars(length)
}

/**
 * i32e
 */
internal fun decodeInt(reader: BufferedReader): Int {
    reader.read() // consume 'i'
    val number = buildString {
        while (true) {
            val char = reader.read().toChar()
            if (char == 'e') break
            append(char)
        }
    }.toInt()
    return number
}

internal fun decodeList(reader: BufferedReader): BObject.BList {
    reader.read() // consume 'l'
    val list = mutableListOf<BObject>()
    while (true) {
        val peek = reader.peek()
        if (peek == 'e') {
            reader.read() // consume 'e'
            break
        }
        list.add(parse(reader)!!)
    }
    return BObject.BList(list)
}

/**
 *  d3:cow3:moo4:spam4:eggse
 */
internal fun decodeDict(reader: BufferedReader): BObject.BDict {
    reader.read() // consume 'd'
    val dict = mutableMapOf<String, BObject>()
    while (true) {
        val peek = reader.peek()
        if (peek == 'e') {
            reader.read() // consume 'e'
            break
        }
        val key = decodeString(reader)
        val value = parse(reader)!!
        dict[key] = value
    }
    return BObject.BDict(dict)
}

internal fun BufferedReader.readNChars(n: Int): String {
    val charArray = CharArray(n)
    read(charArray, 0, n)
    return String(charArray)
}