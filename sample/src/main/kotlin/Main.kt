package com.lanli

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
    @BencodeName("q") val messageType: String,     // Query type
    @BencodeName("y") val y: String = "q",         // q,r,e
    @BencodeName("t") val transactionId: String    // Transaction ID
)

// Json example: {"t":"bb", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
class PingRequest(
    transactionId: String,
    @BencodeName("a") val argument: PingRequestArgument
) : KRPCRequest(messageType = "ping", transactionId = transactionId)

class PingRequestArgument(
    @BencodeName("id") val id: String
)

data class KRPCError(
    @BencodeName("t") val transactionId: String,
    @BencodeName("y") val messageType: String = "e",
    @BencodeName("e") val errors: List<Any>           // error code and error message
)

