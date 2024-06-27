package bencode

import com.lanli.bencode.BObject
import com.lanli.bencode.parse
import com.lanli.bencode.readNChars
import kotlin.test.Test
import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * from chatgpt
 */
class ParserTest {

    @Test
    fun testDecodeString() {
        val input = "4:spam"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BStr("spam")
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeEmptyStringAndInt() {
        val input = ":"
        val input2 = "ie"
        val reader = BufferedReader(StringReader(input))
        assertFailsWith<IllegalArgumentException> {
            parse(reader)
        }
        val reader2 = BufferedReader(StringReader(input2))
        assertFailsWith<NumberFormatException> {
            parse(reader2)
        }
    }

    @Test
    fun testDecodeInt() {
        val input = "i32e"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BInt(32)
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeList() {
        val input = "l4:spam4:eggsi42ee"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BList(listOf(
            BObject.BStr("spam"),
            BObject.BStr("eggs"),
            BObject.BInt(42)
        ))
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeEmptyList() {
        val input = "le"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BList(emptyList())
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeDict() {
        val input = "d4:name4:John3:agei30ee"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BDict(mapOf(
            "name" to BObject.BStr("John"),
            "age" to BObject.BInt(30)
        ))
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeEmptyDict() {
        val input = "de"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BDict(emptyMap())
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeNested() {
        val input = "d4:info4:test4:listli1ei2ei3eee"
        val reader = BufferedReader(StringReader(input))
        val expected = BObject.BDict(mapOf(
            "info" to BObject.BStr("test"),
            "list" to BObject.BList(listOf(
                BObject.BInt(1),
                BObject.BInt(2),
                BObject.BInt(3)
            ))
        ))
        val actual = parse(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testReadNChars() {
        val input = "Hello, World!"
        val reader = BufferedReader(StringReader(input))

        val actual = reader.readNChars(input.length)
        assertEquals(input, actual)
    }
}