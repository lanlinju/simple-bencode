package bencode

import com.lanli.bencode.Bencode
import com.lanli.bencode.BencodeName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

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
        val actual = Bencode.encodeToString(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeInt() {
        val input = 123
        val expected = "i123e"
        val actual = Bencode.encodeToString(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeEmptyList() {
        val input = emptyList<String>()
        val expected = "le"
        val actual = Bencode.encodeToString(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeList() {
        val input = listOf("spam", "eggs")
        val expected = "l4:spam4:eggse"
        val actual = Bencode.encodeToString(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodePerson() {
        val person = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val expected = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val actual = Bencode.encodeToString(person)
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeWithWriter() {
        val person = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val expected = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val writer = ByteArrayOutputStream()
        writer.buffered().use { Bencode.encode(it, person) }
        val actual = writer.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodePerson() {
        val input = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val reader = ByteArrayInputStream(input.toByteArray())
        val expected = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val actual = Bencode.decode<Person>(reader)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecode() {
        val input = "d4:name4:John3:agei30e7:addressd4:codei12345e4:area11:Main Streetee"
        val expected = Person(name = "John", age = 30, address = Address(code = 12345, area = "Main Street"))
        val actual = Bencode.decodeFromString<Person>(input)
        assertEquals(expected, actual)
        println("expected: $expected")
        println("actual: $actual")
    }

    @Test
    fun testEncodeAddress() {
        val address = Address(code = 12345, area = "Main Street")
        val expected = "d4:codei12345e4:area11:Main Streete"
        val actual = Bencode.encodeToString(address)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeAddress() {
        val input = "d4:codei12345e4:area11:Main Streete"
        val reader = ByteArrayInputStream(input.toByteArray())
        val expected = Address(code = 12345, area = "Main Street")
        val actual = Bencode.decode<Address>(reader)
        assertEquals(expected, actual)
    }

    data class Person2(val name: String = "", val age: Int = 0)

    @Test
    fun testDecodeAndEncode() {
        val person = Bencode.decodeFromString<Person2>("d4:name4:John3:agei30ee")
        println(person)

        val result = Bencode.encodeToString(Person2(name = "John", age = 30))
        println(result)
    }

    data class Person3(val name: String = "", val age: Long = 0)

    @Test
    fun testDecodePersonWithLong() {
        val input = "d4:name4:John3:agei30ee"
        val expected = Person3(name = "John", age = 30L)
        val actual = Bencode.decodeFromString<Person3>(input)
        assertEquals(expected, actual)
        println(expected)
    }

    @Test
    fun testEncodePersonWithLong() {
        val expected = "d4:name4:John3:agei30ee"
        val actual = Bencode.encodeToString(Person3(name = "John", age = 30L))
        assertEquals(expected, actual)
        println(expected)
    }

    data class RawFile(
        val announce: String = "",
        val `created by`: String = "",
        val info: RawInfo = RawInfo(),
    ) {
        fun toTorrentFile(): TorrentFile {
            return TorrentFile(
                announce = announce,
                length = info.length,
                name = info.name,
                piecesLength = info.`piece length`,
                pieces = info.pieces
            )
        }
    }

    data class RawInfo(
        val length: Long = 0L,
        val name: String = "",
        val `piece length`: Long = 0L,
        val pieces: ByteArray = ByteArray(0)
    )

    data class TorrentFile(
        val announce: String = "",
        val length: Long = 0L,
        val name: String = "",
        val piecesLength: Long = 0L,
        val pieces: ByteArray = ByteArray(0)
    )


    @Test
    fun testTorrentFile() {
        val file = File("sample.torrent")
        val readBytes = file.readBytes()
        val rowFile = Bencode.decodeFromByteArray<RawFile>(readBytes)

        val result = ByteArrayOutputStream().use { Bencode.encodeToByteArray(rowFile) }
        val encodedInfo = Bencode.encodeToByteArray(rowFile.info)
        val infoHash = sha1Hash(encodedInfo)

        assertEquals("d69f91e6b2ae4c542468d1073a71d4ea13879a7f", infoHash)
        assertEquals(readBytes.size, result.size)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sha1Hash(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(data)
        return digest.toHexString()
    }


    data class RawInfo2(
        val length: Long = 0L,
        val name: String = "",
        @BencodeName("piece length")
        val pieceLength: Long = 0L,
    )

    @Test
    fun testDecodeBencodeName() {
        val input = "d6:lengthi92063e4:name10:sample.txt12:piece lengthi32768ee"
        val expected = 32768L
        val rowInfo = Bencode.decodeFromString<RawInfo2>(input)
        val actual = rowInfo.pieceLength
        assertEquals(expected, actual)
    }

    @Test
    fun testEncodeBencodeName() {
        val input = RawInfo2(length = 92063L, name = "sample.txt", pieceLength = 32768L)
        val expected = "d6:lengthi92063e4:name10:sample.txt12:piece lengthi32768ee"
        val actual = Bencode.encodeToString(input)
        assertEquals(expected, actual)
    }
}

