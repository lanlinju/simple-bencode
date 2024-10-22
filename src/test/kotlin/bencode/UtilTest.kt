package bencode

import com.lanlinju.bencode.extractNestedType
import com.lanlinju.bencode.isListType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

class UtilTest {

    data class TestClass(val value: String = "test")

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
        assertTrue(isListType(MutableList::class.java))
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

    // Helper class to create TypeReference for generic types
    abstract class TypeReference<T> {
        val type: Type
            get() = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * 例如List<List<String>> 返回 [List,List,String]
     */
    internal fun parseTypeHierarchy(type: Type): List<Class<*>> {
        val result = mutableListOf<Class<*>>()
        parseTypeRecursive(type, result)
        return result
    }

    private fun parseTypeRecursive(currentType: Type, result: MutableList<Class<*>>) {
        when (currentType) {
            is ParameterizedType -> {
                val rawType = currentType.rawType as Class<*>
                result.add(rawType)
                val actualType = currentType.actualTypeArguments[0]
                parseTypeRecursive(actualType, result)
            }

            is WildcardType -> {
                val upperBound = currentType.upperBounds[0]
                parseTypeRecursive(upperBound, result)
            }

            is Class<*> -> {
                result.add(currentType)
            }

            else -> throw IllegalArgumentException("Unknown type: $currentType")
        }
    }
}


