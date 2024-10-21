# Simple-Bencode

A Bencode Encoder/Decoder for Kotlin(基于Bencode协议的解码/编码器)

[![](https://jitpack.io/v/lanlinju/simple-bencode.svg)](https://jitpack.io/#lanlinju/simple-bencode)

### Getting started
[Usage Example](sample/src/main/kotlin/Main.kt)
```kotlin
import com.lanlinju.bencode.Bencode
import com.lanlinju.bencode.BencodeName

fun main() {
    // Decoding
    val person = Bencode.decodeFromString<Person>("d4:name4:John3:agei30ee")
    println(person) // output: Person(name=John, age=30)

    // Encoding
    val result = Bencode.encodeToString(Person(name = "Laurie", age = 20))
    println(result) // output: d4:name6:Laurie3:agei20ee

    // Inheritance example
    val request = PingRequest(transactionId = "aa", PingRequestArgument(id = "node123"))
    val encodeRequest = Bencode.encodeToString(request)
    println(encodeRequest) // d1:ad2:id7:node123e1:q4:ping1:y1:q1:t2:aae

    // Example of list elements with different types.
    val error = KRPCError(transactionId = "aa", errors = listOf<Any>(201, "A Generic Error Ocurred"))
    val encodeError = Bencode.encodeToString(error)
    println(encodeError) // d1:t2:aa1:y1:e1:eli201e23:A Generic Error Ocurredee
    // Decoding
    val decodeError = Bencode.decodeFromString<KRPCError>("d1:t2:aa1:y1:e1:eli201e23:A Generic Error Ocurredee")
    println(decodeError.errors) // [201, A Generic Error Ocurred], different types: [Long, String]
}

data class Person(val name: String, val age: Int)

abstract class KRPCRequest(
    @BencodeName("q") val messageType: String,     // q,r,e
    @BencodeName("y") val y: String = "q",         // query
    @BencodeName("t") val transactionId: String    // Transaction ID
)

//json example: {"t":"bb", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
class PingRequest(
    transactionId: String,
    @BencodeName("a") val argument: PingRequestArgument
) : KRPCRequest("ping", transactionId = transactionId)

class PingRequestArgument(
    @BencodeName("id") val id: String
)

class KRPCError(
    @BencodeName("t") val transactionId: String,
    @BencodeName("y") val messageType: String = "e",
    @BencodeName("e") val errors: List<Any>           // error code and error message
)
```

> [!NOTE]
> 1. The Int type can be replaced with the Long type to handle large file lengths.
> 2. Using the annotation `@BencodeName` allows customizing variable names.

## Add Dependencies

### Gradle

Step 1. Add the JitPack repository to your build file
> Add it in your root build.gradle.kts at the end of repositories:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

Step 2. Add the dependency

```kotlin
dependencies {
    implementation("com.github.lanlinju:simple-bencode:1.0.4")
}
```

### Parse TorrentFile example
```kotlin
import com.lanli.bencode.Bencode
import com.lanli.bencode.BencodeName
import java.io.File
import java.security.MessageDigest

fun main() {
    val file = File("sample.torrent")
    val rowFile = Bencode.decodeFromByteArray<RawFile>(file.readBytes())
    // Encode info.
    val encodedInfo = Bencode.encodeToByteArray(rowFile.info)
    // Calculate the SHA1 value of info.
    val infoHash = sha1Hash(encodedInfo)
    println(infoHash) // d69f91e6b2ae4c542468d1073a71d4ea13879a7f
}

data class RawFile(
    val announce: String = "",
    @BencodeName("created by")
    val createdBy: String = "",
    val info: RawInfo = RawInfo(),
)

data class RawInfo(
    val length: Long = 0L,
    val name: String = "",
    @BencodeName("piece length")
    val pieceLength: Long = 0L,
    val pieces: ByteArray = ByteArray(0)
)

@OptIn(ExperimentalStdlibApi::class)
fun sha1Hash(data: ByteArray): String {
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md.digest(data)
    return digest.toHexString()
}
```