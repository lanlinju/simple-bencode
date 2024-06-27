package bencode

import com.lanli.bencode.BObject
import com.lanli.bencode.marshal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
}
