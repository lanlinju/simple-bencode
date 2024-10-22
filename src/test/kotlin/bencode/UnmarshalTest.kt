package bencode

import bencode.BencodeTest.Person
import com.lanlinju.bencode.Bencode
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class UnmarshalTest {

    @Test
    fun testEncodePerson() {
        val address = List(3) { Address(code = 12345, area = "Main Street$it") }
        val person = Person(name = "John", age = 30, address = address)
        val expected =
            "d4:name4:John3:agei30e7:addressld4:codei12345e4:area12:Main Street0ed4:codei12345e4:area12:Main Street1ed4:codei12345e4:area12:Main Street2eee"
        val actual = Bencode.encodeToString(person)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodePerson() {
        val input =
            "d4:name4:John3:agei30e7:addressld4:codei12345e4:area12:Main Street0ed4:codei12345e4:area12:Main Street1ed4:codei12345e4:area12:Main Street2eee"
        val address = List(3) { Address(code = 12345, area = "Main Street$it") }
        val expected = Person(name = "John", age = 30, address = address)
        val actual = Bencode.decodeFromString<Person>(input)
        assertEquals(expected, actual)
    }

    data class Person(val name: String, val age: Int, val address: List<Address>)
    data class Address(val code: Int, val area: String)
}