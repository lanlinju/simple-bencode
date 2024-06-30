package bencode

import com.lanli.bencode.*
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class EncoderTest {
    @Test
    fun testEncodeString() {
        val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BStr("spam".toByteArray())
        val expected = "4:spam"
        val actualLength = encodeString(bWriter, bObject.value)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeInt() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
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
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val expected = "i-32e"
        val actual = encodeInt(bWriter, input)
        bWriter.flush()
        val actual2 = writer.toString()
        assertEquals(5, actual)
        assertEquals(expected, actual2)
    }

    @Test
    fun testEncodeList() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BList(listOf(BObject.BStr("spam".toByteArray()), BObject.BInt(42)))
        val expected = "l4:spami42ee"
        val actualLength = encodeList(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeDict() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BDict(mapOf("cow" to BObject.BStr("moo".toByteArray()), "spam" to BObject.BStr("eggs".toByteArray())))
        val expected = "d3:cow3:moo4:spam4:eggse"
        val actualLength = encodeDict(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testEncodeDictNested() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val input = BObject.BDict(
            mapOf(
                "info" to BObject.BStr("test".toByteArray()),
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
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BStr("test".toByteArray())
        val expected = "4:test"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testBencodeInt() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BInt(123)
        val expected = "i123e"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testBencodeList() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BList(listOf(BObject.BStr("a".toByteArray()), BObject.BInt(1)))
        val expected = "l1:ai1ee"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test
    fun testBencodeDict() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BDict(mapOf("a" to BObject.BStr("b".toByteArray()), "c" to BObject.BInt(2)))
        val expected = "d1:a1:b1:ci2ee"
        val actualLength = bencode(bWriter, bObject)
        bWriter.flush()
        assertEquals(expected, writer.toString())
        assertEquals(expected.length, actualLength)
    }

    @Test()
    fun testBencodeEmptyBList() {
         val writer = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
        val bWriter = writer.buffered()
        val bObject = BObject.BList(emptyList())
        assertEquals(2, bencode(bWriter, bObject))// le
    }

}