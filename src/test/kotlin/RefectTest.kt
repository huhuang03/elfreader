import org.junit.Test

fun ByteArray.union(byteArrayList: List<ByteArray>): ByteArray {
    byteArrayList.map {
        it.asIterable()
    }.forEach {
        this.union(it)
    }
    return this
}

fun ByteArray.hexStr(): String {
    return this.joinToString(separator = " ") {
        String.format("%02X", it)
    }
}

class Test {
    @Test
    fun test_union() {
        val b1 = ByteArray(1, {pos -> pos.toByte()})
        val b2 = ByteArray(1, { 2.toByte()})
//        val rst = ByteArray(0).union(listOf(b1, b2))
        val rst = b1 + b2

        println(rst.hexStr())
    }

    @Test
    fun test_when() {
        println(whenRst(1))
    }

}

fun whenRst(i: Int): String {
    when(i) {
        1 -> return "1"
        2-> return "2"
    }
    return "haha"
}
