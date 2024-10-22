package com.lanlinju.bencode

import java.io.*

class Bencode {
    companion object {
        /**
         * Deserializes the string [data] into the specified type [T].
         */
        inline fun <reified T : Any> decodeFromString(data: String): T {
            return decodeFromByteArray<T>(data.toByteArray())
        }

        /**
         * Deserializes the byte array [bytes] into the specified type [T].
         */
        inline fun <reified T : Any> decodeFromByteArray(bytes: ByteArray): T {
            val result = bytes.inputStream().use { decode<T>(it) }
            return result
        }

        /**
         * Deserializes data from the [inputStream] into the specified type [T].
         */
        inline fun <reified T : Any> decode(inputStream: InputStream): T {
            return inputStream.buffered().use { decode<T>(it) }
        }

        /**
         * Deserializes data from the [BufferedInputStream] into the specified type [T].
         */
        inline fun <reified T : Any> decode(reader: BufferedInputStream): T {
            return decode(T::class.java, reader)
        }

        /**
         * Performs deserialization based on the given [Class] type and [BufferedInputStream].
         * Returns the deserialized object.
         */
        fun <T> decode(clazz: Class<*>, reader: BufferedInputStream): T {
            val bObject = parse(reader)
            return unmarshal(clazz, bObject) as T
        }

        /**
         * Serializes the given object [any] into a string.
         * Returns the serialized string.
         */
        fun encodeToString(any: Any): String {
            val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
            writer.buffered().use { encode(it, any) }
            return writer.toString(Charsets.UTF_8)
        }

        /**
         * Serializes the given object [any] into a byte array.
         * Returns the serialized byte array.
         */
        fun encodeToByteArray(any: Any): ByteArray {
            val stream = ByteArrayOutputStream()
            stream.buffered().use { encode(it, any) }
            return stream.toByteArray()
        }

        /**
         * Serializes the given object [any] and writes it into the specified [OutputStream].
         * Returns the number of bytes written.
         */
        fun encode(outputStream: OutputStream, any: Any): Int {
            return outputStream.buffered().use { encode(it, any) }
        }

        /**
         * Serializes the given object [any] and writes it into the specified [BufferedOutputStream].
         * Returns the number of bytes written.
         */
        fun encode(writer: BufferedOutputStream, any: Any): Int {
            val bObject = marshal(any)
            return bencode(writer, bObject)
        }
    }
}
