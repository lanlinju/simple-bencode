package bencode

import com.lanli.bencode.createInstance
import com.lanli.bencode.extractNestedType
import com.lanli.bencode.isListType
import com.lanli.bencode.parseTypeHierarchy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class UtilTest {

    data class TestClass(val value: String = "test")

    // 测试 createInstance 函数
    @Test
    fun testCreateInstance() {
        val instance = TestClass::class.java.createInstance()
        assertEquals("test", instance.value)
    }

    // 测试 extractNestedType 函数
    @Test
    fun testExtractNestedType() {
        val type = object : TypeReference<List<List<String>>>() {}.type
        val nestedType = extractNestedType(type)
        assertEquals(String::class.java, nestedType)
    }

    // 测试 isListType 函数
    @Test
    fun testIsListType() {
        assertTrue(isListType(List::class.java))
        assertFalse(isListType(String::class.java))
    }

    // 测试 parseTypeHierarchy 函数
    @Test
    fun testParseTypeHierarchy() {
        val type = object : TypeReference<List<List<String>>>() {}.type
        val hierarchy = parseTypeHierarchy(type)
        assertEquals(3, hierarchy.size)
        assertEquals(List::class.java, hierarchy[0])
        assertEquals(List::class.java, hierarchy[1])
        assertEquals(String::class.java, hierarchy[2])
    }
}

// Helper class to create TypeReference for generic types
abstract class TypeReference<T> {
    val type: Type
        get() = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
}
