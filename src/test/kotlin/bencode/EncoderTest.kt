package bencode

import com.lanli.bencode.*
import java.io.BufferedWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class EncoderTest {
    @Test
    fun testEncodeString() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BStr("spam")
        val expected = "4:spam"
        val actualLength = encodeString(bWriter, bObject.value)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeInt() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BInt(42)
        val expected = "i42e"
        val actualLength = encodeInt(bWriter, bObject.value)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeIntWithMinus() {
        val input = -32L
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val expected = "i-32e"
        val actual = encodeInt(bWriter, input)
        bWriter.flush()
        val actual2 = writer.toString()
        assertEquals(5, actual)
        assertEquals(expected, actual2)
    }

    @Test
    fun testEncodeList() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BList(listOf(BObject.BStr("spam"), BObject.BInt(42)))
        val expected = "l4:spami42ee"
        val actualLength = encodeList(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeDict() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BDict(mapOf("cow" to BObject.BStr("moo"), "spam" to BObject.BStr("eggs")))
        val expected = "d3:cow3:moo4:spam4:eggse"
        val actualLength = encodeDict(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeDictNested() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val input = BObject.BDict(
            mapOf(
                "info" to BObject.BStr("test"),
                "list" to BObject.BList(
                    listOf(
                        BObject.BInt(1),
                        BObject.BInt(2),
                        BObject.BInt(3)
                    )
                )
            )
        )
        val expected = "d4:info4:test4:listli1ei2ei3eee"

        encodeDict(bWriter, input)
        bWriter.flush()
        val actual = writer.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testBencodeString() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BStr("test")
        val expected = "4:test"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testBencodeInt() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BInt(123)
        val expected = "i123e"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testBencodeList() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BList(listOf(BObject.BStr("a"), BObject.BInt(1)))
        val expected = "l1:ai1ee"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testBencodeDict() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BDict(mapOf("a" to BObject.BStr("b"), "c" to BObject.BInt(2)))
        val expected = "d1:a1:b1:ci2ee"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test()
    fun testBencodeEmptyBList() {
        val writer = StringWriter()
        val bWriter = BufferedWriter(writer)
        val bObject = BObject.BList(emptyList())
        assertEquals(2, bencode(bWriter, bObject))// le
    }

}