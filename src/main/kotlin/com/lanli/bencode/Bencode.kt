package com.lanli.bencode

import java.io.*

class Bencode {
    companion object {
        /**
         * 将字符串 [data] 反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decode(data: String): T {
            return decode<T>(data.toByteArray())
        }

        /**
         * 将字节数组 [data] 反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decode(data: ByteArray): T {
            val result = data.inputStream().use { decode<T>(it) }
            return result
        }

        /**
         * 从输入流 [inputStream] 中反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decode(inputStream: InputStream): T {
            return inputStream.bufferedReader().use { decode<T>(it) }
        }

        /**
         * 从缓冲读入器 [reader] 中反序列化为指定类型 [T]。
         */
        inline fun <reified T : Any> decode(reader: BufferedReader): T {
            return decode(T::class.java, reader) as T
        }

        /**
         * 根据给定的 [Class] 和 [BufferedReader] 执行反序列化操作。
         * 返回反序列化后的对象。
         */
        fun decode(clazz: Class<*>, reader: BufferedReader): Any {
            val bObject = parse(reader)
            return unmarshal(clazz, bObject)
        }

        /**
         * 将给定对象 [any] 序列化为字符串。
         * 返回序列化后的字符串。
         */
        fun encode(any: Any): String {
            val writer = StringWriter()
            writer.buffered().use { encode(it, any) }
            return writer.toString()
        }

        /**
         * 将给定对象 [any] 序列化并写入到指定的输出流 [outputStream] 中。
         * 返回写入的字节数。
         */
        fun encode(outputStream: OutputStream, any: Any): Int {
            return outputStream.bufferedWriter().use { encode(it, any) }
        }

        /**
         * 将给定对象 [any] 序列化并写入到指定的缓冲写入器 [writer] 中。
         * 返回写入的字节数。
         */
        fun encode(writer: BufferedWriter, any: Any): Int {
            val bObject = marshal(any)
            return bencode(writer, bObject)
        }
    }
}