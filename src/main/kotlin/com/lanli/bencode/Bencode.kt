package com.lanli.bencode

import java.io.*

class Bencode {
    companion object {
        /**
         * 将字符串 [data] 反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decodeFromString(data: String): T {
            return decodeFromByteArray<T>(data.toByteArray())
        }

        /**
         * 将字节数组 [bytes] 反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decodeFromByteArray(bytes: ByteArray): T {
            val result = bytes.inputStream().use { decode<T>(it) }
            return result
        }

        /**
         * 从输入流 [inputStream] 中反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decode(inputStream: InputStream): T {
            return inputStream.buffered().use { decode<T>(it) }
        }

        /**
         * 从缓冲读入器 [reader] 中反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decode(reader: BufferedInputStream): T {
            return decode(T::class.java, reader)
        }

        /**
         * 根据给定的 [Class] 和 [BufferedInputStream] 执行反序列化操作。
         * 返回反序列化后的对象。
         */
        fun <T> decode(clazz: Class<*>, reader: BufferedInputStream): T {
            val bObject = parse(reader)
            return unmarshal(clazz, bObject) as T
        }

        /**
         * 将给定对象 [any] 序列化为字符串。
         * 返回序列化后的字符串。
         */
        fun encodeToString(any: Any): String {
            val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
            writer.buffered().use { encode(it, any) }
            return writer.toString(Charsets.UTF_8)
        }

        /**
         * 将给定对象 [any] 序列化为字节数组。
         * 返回序列化后的字符串。
         */
        fun encodeToByteArray(any: Any): ByteArray {
            val stream = ByteArrayOutputStream()
            stream.buffered().use { encode(it, any) }
            return stream.toByteArray()
        }

        /**
         * 将给定对象 [any] 序列化并写入到指定的输出流 [outputStream] 中。
         * 返回写入的字节数。
         */
        fun encode(outputStream: OutputStream, any: Any): Int {
            return outputStream.buffered().use { encode(it, any) }
        }

        /**
         * 将给定对象 [any] 序列化并写入到指定的缓冲写入器 [writer] 中。
         * 返回写入的字节数。
         */
        fun encode(writer: BufferedOutputStream, any: Any): Int {
            val bObject = marshal(any)
            return bencode(writer, bObject)
        }
    }
}