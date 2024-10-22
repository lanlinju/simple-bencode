package com.lanlinju.bencode

import java.io.BufferedOutputStream

/**
 * Encodes the given [BObject] into Bencode format and writes it to the specified [BufferedOutputStream].
 * Returns the total number of bytes written.
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
 * Encodes [BObject.BDict] as a Bencode dictionary and writes it to the specified [BufferedOutputStream].
 * Returns the total number of bytes written.
 *
 * Format example: dkey1:value1key2:value2e (where 'd' and 'e' mark the dictionary start and end)
 */
internal fun encodeDict(writer: BufferedOutputStream, bObject: BObject.BDict): Int {
    writer.write('d'.code)
    val length = bObject.value.entries.fold(2) { acc, (key, value) ->
        encodeString(writer, key.toByteArray()) + bencode(writer, value) + acc
    }
    writer.write('e'.code)
    return length
}

/**
 * Encodes [BObject.BList] as a Bencode list and writes it to the specified [BufferedOutputStream].
 * Returns the total number of bytes written.
 *
 * Format example: lvalue1value2e (where 'l' and 'e' mark the list start and end)
 */
internal fun encodeList(writer: BufferedOutputStream, bObject: BObject.BList): Int {
    writer.write('l'.code)
    val length = bObject.value.fold(2) { acc, e -> acc + bencode(writer, e) }
    writer.write('e'.code)
    return length
}

/**
 * Encodes the [ByteArray] value of [BObject.BStr] as a Bencode string and writes it to the specified [BufferedOutputStream].
 * Returns the total number of bytes written.
 *
 * Format example: 4:test (where '4' is the length of the string and 'test' is the value)
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
 * Encodes the [Long] value of [BObject.BInt] as a Bencode integer and writes it to the specified [BufferedOutputStream].
 * Returns the total number of bytes written.
 *
 * Format example: i42e (where 'i' and 'e' mark the integer start and end)
 */
internal fun encodeInt(writer: BufferedOutputStream, value: Long): Int {
    val intStr = "i${value}e".toByteArray()
    writer.write(intStr)
    return intStr.size
}
