# simple-bencode

A Bencode Encoder/Decoder for Kotlin(基于Bencode协议的解码/编码器)

[![](https://jitpack.io/v/lanlinju/simple-bencode.svg)](https://jitpack.io/#lanlinju/simple-bencode)

### Getting started

#### Decoding

```kotlin
val person = Bencode.decode<Person>("d4:name4:John3:agei30ee")
println(person) // output: Person(name=John, age=30)
```

#### Encoding

```kotlin
val result = Bencode.encode(Person(name = "Laurie", age = 20))
println(result) // output: d4:name6:Laurie3:agei20ee
```

#### Person class

```kotlin
data class Person(val name: String = "", val age: Int = 0)
```

> [!NOTE]
> 1. The class constructor must have default values.
> 2. The Int type can be replaced with the Long type to handle large file lengths.

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
    implementation("com.github.lanlinju:simple-bencode:1.0.2")
}
```