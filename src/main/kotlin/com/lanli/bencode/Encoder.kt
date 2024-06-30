package com.lanli.bencode

import java.io.BufferedOutputStream

/**
 * 将 [BObject] 编码为 Bencode 格式并写入指定的 [BufferedOutputStream] 中。
 * 返回写入的总字节长度。
 */
internal fun bencode(writer: BufferedOutputStream, bObject: BObject): Int {
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
 * 将 BObject.BDict 编码为 Bencode 字典格式并写入指定的 BufferedOutputStream 中。
 * 返回写入的总字节长度。
 */
internal fun encodeDict(writer: BufferedOutputStream, bObject: BObject.BDict): Int {
    writer.write('d'.code)
    val length = bObject.value.entries.fold(2) { acc, (key, value) ->
        encodeString(writer, key.toByteArray()) + bencode(writer, value) + acc
    }
    writer.write('e'.code)
    return length
}

internal fun encodeList(writer: BufferedOutputStream, bObject: BObject.BList): Int {
    writer.write('l'.code)
    val length = bObject.value.fold(2) { acc, e -> acc + bencode(writer, e) }
    writer.write('e'.code)
    return length
}

/**
 * 将 BObject.BStr 的 ByteArray 值编码为 Bencode 字符串格式并写入指定的 BufferedOutputStream 中。
 * 返回写入的总字节长度。
 */
internal fun encodeString(writer: BufferedOutputStream, value: ByteArray): Int {
    val byteLength = value.size
    val lenStr = byteLength.toString().toByteArray()
    writer.write(lenStr)
    writer.write(':'.code)
    writer.write(value)
    return lenStr.size + 1 + byteLength
}


/**
 * 将 BObject.BInt 的值编码为 Bencode 整数格式并写入指定的 BufferedOutputStream 中。
 * 返回写入的总字节长度。
 */
internal fun encodeInt(writer: BufferedOutputStream, value: Long): Int {
    val intStr = "i${value}e".toByteArray()
    writer.write(intStr)
    return intStr.size
}