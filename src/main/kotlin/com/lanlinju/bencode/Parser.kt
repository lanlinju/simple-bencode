package com.lanlinju.bencode

import java.io.BufferedInputStream
import java.io.InputStream

/**
 * Sealed class representing the different types of Bencode-encoded objects.
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
            is BStr -> "BStr('${value.decodeToString()}')"  // Decodes byte array to a string for printing
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
 * Reads the byte stream from the given [reader] and converts it into a [BObject].
 *
 * Determines the data type based on the first character and calls the corresponding decoding function.
 */
internal fun parse(reader: BufferedInputStream): BObject {
    val peek = reader.peek()
    return when {
        peek in '0'..'9' -> decodeString(reader)
        peek == 'i' -> decodeInt(reader)
        peek == 'l' -> decodeList(reader)
        peek == 'd' -> decodeDict(reader)
        else -> throw IllegalArgumentException("Invalid Bencode data")
    }
}

/**
 * Peeks at the next character in the stream without consuming it.
 */
internal fun BufferedInputStream.peek(): Char {
    mark(1)
    val char: Int = read()
    reset()
    return char.toChar()
}

/**
 * Decodes a byte array in the format of length:value, e.g., 4:spam
 */
internal fun decodeString(reader: BufferedInputStream): BObject.BStr {
    val length = buildString {
        while (true) {
            val char = reader.read().toChar()
            if (char == ':') break
            append(char)
        }
    }.toInt()
    return BObject.BStr(reader.readNBytes(length))
}

/**
 * Decodes an integer in the format of i<integer>e, e.g., i32e
 */
internal fun decodeInt(reader: BufferedInputStream): BObject.BInt {
    reader.read() // consume 'i'
    val number = buildString {
        while (true) {
            val char = reader.read().toChar()
            if (char == 'e') break
            append(char)
        }
    }.toLong()
    return BObject.BInt(number)
}

/**
 * Decodes a list in the format of l<item1><item2>...e, e.g., l4:spam4:eggse
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
        // Recursively parse each item in the list
        list.add(parse(reader))
    }
    return BObject.BList(list)
}

/**
 * Decodes a dictionary in the format of d<key><value>...e, e.g., d3:cow3:moo4:spam4:eggse
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
        val key = decodeString(reader).value.decodeToString()
        val value = parse(reader)
        dict[key] = value
    }
    return BObject.BDict(dict)
}
