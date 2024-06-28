package bencode

import com.lanli.bencode.Bencode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.StringReader
import java.io.StringWriter

class BencodeTest {
    data class Person(
        val name: String = "",
        val age: Int = 0,
        val address: Address = Address(),
    )

    data class Address(val code: Int = 0, val area: String = "")

    @Test
    fun testEncodeString() {
        val input = "spam"
        val expected = "4:spam"
        val actual = Bencode.encode(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeInt() {
        val input = 123
        val expected = "i123e"
        val actual = Bencode.encode(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeEmptyList() {
        val input = emptyList<String>()
        val expected = "le"
        val actual = Bencode.encode(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeList() {
        val input = listOf("spam", "eggs")
        val expected = "l4:spam4:eggse"
        val actual = Bencode.encode(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodePerson() {
        val person = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val expected = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val actual = Bencode.encode(person)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeWithWriter() {
        val person = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val expected = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val writer = StringWriter()
         writer.buffered().use { Bencode.encode(it,person) }
        val actual = writer.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodePerson() {
        val input = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val reader = BufferedReader(StringReader(input))
        val expected = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val actual = Bencode.decode<Person>(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecode() {
        val input = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val expected = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val actual = Bencode.decode<Person>(input)
        assertEquals(expected, actual)
        println("expected: $expected")
        println("actual: $actual")
    }

    @Test
    fun testEncodeAddress() {
        val address = Address(code = 12345, area = "Main Street")
        val expected = "d4:codei12345e4:area11:Main Streete"
        val actual = Bencode.encode(address)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeAddress() {
        val input = "d4:codei12345e4:area11:Main Streete"
        val reader = BufferedReader(StringReader(input))
        val expected = Address(code = 12345, area = "Main Street")
        val actual = Bencode.decode<Address>(reader)
        assertEquals(expected, actual)
    }

    data class Person2(val name: String = "", val age: Int = 0)
    @Test
    fun testDecodeAndEncode() {
        val person = Bencode.decode<Person2>("d4:name4:John3:agei30ee")
        println(person)

        val result = Bencode.encode(Person2(name="John", age=30))
        println(result)
    }
}