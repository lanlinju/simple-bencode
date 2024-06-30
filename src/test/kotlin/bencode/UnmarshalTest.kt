package bencode

import com.lanli.bencode.BObject
import com.lanli.bencode.unmarshal
import com.lanli.bencode.unmarshalDict
import kotlin.test.Test
import kotlin.test.assertEquals

class UnmarshalTest {

    @Test
    fun testUnmarshalString() {
        val clazz = String::class.java
        val input = BObject.BStr("test".toByteArray())
        val expected = "test"
        val actual = unmarshal(clazz, input) as String
        assertEquals(expected, actual)
    }

    @Test
    fun testUnmarshalInt() {
        val input = BObject.BInt(42L)
        val input2 = BObject.BInt(32L)
        val expected = 42L
        val expected2 = 32
        val actual = unmarshal(Long::class.java, input) as Long
        val actual2 = unmarshal(Int::class.java, input2) as Int
        assertEquals(expected, actual)
        assertEquals(expected2, actual2)
    }

    @Test
    fun testUnmarshalList() {
        val input = BObject.BList(listOf(BObject.BStr("item0".toByteArray()), BObject.BStr("item1".toByteArray())))
        val expected = listOf("item0", "item1")
        val actual = unmarshal(List::class.java, input) as List<*>
        assertEquals(expected, actual)
    }

    @Test
    fun testUnmarshalDict() {
        val input = BObject.BDict(mapOf("name" to BObject.BStr("John".toByteArray()), "age" to BObject.BInt(20)))
        val expected = Person(name = "John", age = 20)
        val actual = unmarshalDict(Person::class.java, input.value) as Person
        assertEquals(expected, actual)
    }

    internal data class Person(
        val name: String = "",
        val age: Int = 0,
        val address: Address = Address(),
        val listStr: List<String> = emptyList(),
        val listWithList: List<List<String>> = emptyList(),
        val listWithAddrInList: List<List<Address>> = emptyList(),
        val addressWithList: List<Address> = emptyList()
    ) {
        fun toMap(): Map<String, BObject> {
            val list2D = listWithList.map { l -> BObject.BList(l.map { BObject.BStr(it.toByteArray()) }) }
            val list2DWithAddress = listWithAddrInList.map { l -> BObject.BList(l.map { BObject.BDict(it.toMap()) }) }
            val listAddress = addressWithList.map { BObject.BDict(it.toMap()) }
            return mapOf(
                "name" to BObject.BStr("John".toByteArray()),
                "age" to BObject.BInt(20),
                "address" to BObject.BDict(address.toMap()),
                "listStr" to BObject.BList(listStr.map { BObject.BStr(it.toByteArray()) }),
                "listWithList" to BObject.BList(list2D),
                "listWithAddrInList" to BObject.BList(list2DWithAddress),
                "addressWithList" to BObject.BList(listAddress)
            )
        }
    }

    internal data class Address(val code: Int = 0, val area: String = "", val list: List<String> = emptyList()) {
        fun toMap(): Map<String, BObject> {
            return mapOf(
                "code" to BObject.BInt(code.toLong()),
                "area" to BObject.BStr(area.toByteArray()),
                "list" to BObject.BList(list.map { BObject.BStr(it.toByteArray()) })
            )
        }
    }

    @Test
    fun testUnmarshal() {
        val input = BObject.BDict(mapOf("name" to BObject.BStr("John".toByteArray()), "age" to BObject.BInt(20)))
        val expected = Person(name = "John", age = 20)
        val actual = unmarshal(Person::class.java, input) as Person
        assertEquals(expected, actual)
    }

    @Test
    fun testUnmarshalNestedDict() {
        val addr = Address(code = 111, area = "henan")
        val input = BObject.BDict(
            mapOf(
                "name" to BObject.BStr("John".toByteArray()),
                "age" to BObject.BInt(20),
                "address" to BObject.BDict(addr.toMap())
            )
        )
        val expected = Person(name = "John", age = 20, address = addr)
        val actual = unmarshal(Person::class.java, input) as Person
        assertEquals(expected, actual)
    }

    @Test
    fun testUnmarshalWithList() {
        val person = Person(
            name = "John",
            age = 20,
            listStr = listOf("str1", "str2", "str3")
        )
        val input = BObject.BDict(person.toMap())
        val expected = person
        val actual = unmarshal(Person::class.java, input) as Person
        assertEquals(expected, actual)
    }

    @Test
    fun testUnmarshalNestedListInList() {
        val innerList = List(3) { "str$it" }
        val person = Person(
            name = "John",
            age = 20,
            listWithList = listOf(innerList, innerList, innerList)
        )
        val input = BObject.BDict(person.toMap())
        val expected = person
        val actual = unmarshal(Person::class.java, input) as Person
        assertEquals(expected, actual)
        println("expected:$expected")
        println("actual:$actual")
    }

    @Test
    fun testUnmarshalNestedListWithAddrInList() {
        val innerList = List(3) { Address(122, "earth") }
        val person = Person(
            name = "John",
            age = 20,
            listWithAddrInList = listOf(innerList, innerList, innerList)
        )
        val input = BObject.BDict(person.toMap())
        val expected = person
        val actual = unmarshal(Person::class.java, input) as Person
        assertEquals(expected, actual)
        println("expected:$expected")
        println("actual:$actual")
    }

    @Test
    fun testUnmarshalNestedListIncludedAddrWitchList() {
        val innerList = List(3) { Address(122, "earth", listOf("1", "2", "3")) }
        val person = Person(
            name = "John",
            age = 20,
            addressWithList = innerList
        )
        val input = BObject.BDict(person.toMap())
        val expected = person
        val actual = unmarshal(Person::class.java, input) as Person
        assertEquals(expected, actual)
        println("expected:$expected")
        println("actual:$actual")
    }
}