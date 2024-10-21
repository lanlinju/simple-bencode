package com.lanli

import com.lanlinju.bencode.Bencode
import com.lanlinju.bencode.BencodeName
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
