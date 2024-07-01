# simple-bencode

A Bencode Encoder/Decoder for Kotlin(基于Bencode协议的解码/编码器)

[![](https://jitpack.io/v/lanlinju/simple-bencode.svg)](https://jitpack.io/#lanlinju/simple-bencode)

### Getting started

#### Decoding

```kotlin
val person = Bencode.decodeFromString<Person>("d4:name4:John3:agei30ee")
println(person) // output: Person(name=John, age=30)
```

#### Encoding

```kotlin
val result = Bencode.encodeToString(Person(name = "Laurie", age = 20))
println(result) // output: d4:name6:Laurie3:agei20ee
```

#### Person class

```kotlin
data class Person(val name: String = "", val age: Int = 0)
```

> [!NOTE]
> 1. The class constructor must have default values.
> 2. The Int type can be replaced with the Long type to handle large file lengths.
> 3. Using the annotation `@BencodeName` allows customizing variable names.

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
    implementation("com.github.lanlinju:simple-bencode:1.0.3")
}
```

### Parse TorrentFile
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