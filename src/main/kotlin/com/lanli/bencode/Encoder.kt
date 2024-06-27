package com.lanli.bencode

import java.io.BufferedWriter

/**
 * 将 BObject 编码为 Bencode 格式并写入指定的 BufferedWriter 中。
 * 返回写入的总字节长度。
 */
internal fun bencode(writer: BufferedWriter, bObject: BObject): Int {
    var writtenLength = 0

    val len = when (bObject) {
        is BObject.BStr -> encodeString(writer, bObject.value)
        is BObject.BInt -> encodeInt(writer, bObject.value)
        is BObject.BList -> encodeList(writer, bObject)
        is BObject.BDict -> encodeDict(writer, bObject)
    }

    writer.flush()
    writtenLength += len

    return writtenLength
}

/**
 * 将 BObject.BDict 编码为 Bencode 字典格式并写入指定的 BufferedWriter 中。
 * 返回写入的总字节长度。
 */
internal fun encodeDict(writer: BufferedWriter, bObject: BObject.BDict): Int {
    writer.write("d")
    val length = bObject.value.entries.fold(2) { acc, (key, value) ->
        encodeString(writer, key) + bencode(writer, value) + acc
    }
    writer.write("e")
    return length
}

internal fun encodeList(writer: BufferedWriter, bObject: BObject.BList): Int {
    writer.write("l")
    val length = bObject.value.fold(2) { acc, e -> acc + bencode(writer, e) }
    writer.write("e")
    return length
}

internal fun encodeString(writer: BufferedWriter, value: String): Int {
    val bytes = value.toByteArray()
    val byteLength = bytes.size
    val lenStr = byteLength.toString()
    writer.write(lenStr)
    writer.write(':'.code)
    writer.write(value)
    return lenStr.length + 1 + byteLength
}

internal fun encodeInt(writer: BufferedWriter, value: Int): Int {
    val intStr = "i${value}e"
    writer.write(intStr)
    return intStr.length
}

/**
 * 返回字符串的字节长度。
 */
internal fun String.size() = this.toByteArray().size