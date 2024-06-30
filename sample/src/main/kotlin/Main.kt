package com.lanli

import com.lanli.bencode.Bencode

data class Person(val name: String = "", val age: Int = 0)

fun main() {
    val person = Bencode.decodeFromString<Person>("d4:name4:John3:agei30ee")
    println(person) // output: Person(name=John, age=30)

    val result = Bencode.encodeToString(Person(name = "Laurie", age = 20))
    println(result) // output: d4:name6:Laurie3:agei20ee
}