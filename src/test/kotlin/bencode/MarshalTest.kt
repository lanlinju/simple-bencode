package bencode

import com.lanli.bencode.BObject
import com.lanli.bencode.marshal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.BufferedWriter
import java.io.StringWriter

class MarshalTest {

    @Test
    fun testMarshalString() {
        val input = "test"
        val expected = BObject.BStr("test")
        val actual = marshal(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testMarshalInt() {
        val input = 123
        val expected = BObject.BInt(123)
        val actual = marshal(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testMarshalList() {
        val input = listOf("test1", 123, "test2")
        val expected = BObject.BList(listOf(BObject.BStr("test1"), BObject.BInt(123), BObject.BStr("test2")))
        val actual = marshal(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testMarshalDict() {
        data class TestClass(val name: String, val age: Int)
        val input = TestClass("John", 30)
        val expected = BObject.BDict(mapOf("name" to BObject.BStr("John"), "age" to BObject.BInt(30)))
        val actual = marshal(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testMarshalToOutputStream() {
        val input = "test"
        val outputStream = StringWriter()
        val expected = BObject.BStr("test")
        val actualLength = marshal(outputStream.buffered(), input)
        val actualOutput = outputStream.toString()
        assertTrue(actualOutput.contains("4:test"))
        assertEquals(expected, marshal(input))
        assertEquals(6, actualLength)
    }

    @Test
    fun testMarshalToBufferedWriter() {
        val input = 123
        val stringWriter = StringWriter()
        val bufferedWriter = BufferedWriter(stringWriter)
        val expected = BObject.BInt(123)
        val actualLength = marshal(bufferedWriter, input)
        bufferedWriter.flush()
        val actualOutput = stringWriter.toString()
        assertTrue(actualOutput.contains("i123e"))
        assertEquals(expected, marshal(input))
        assertEquals(5, actualLength)
    }
}
