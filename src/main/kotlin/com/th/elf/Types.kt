package com.th.elf

import sun.misc.Version

fun ByteArray.hexStr(): String {
    return this.joinToString(separator = " ") {
        String.format("%02X", it)
    }
}

val INT_MAX_BYTE_LEN = 4

fun ByteArray.toInt(): Int {
    if (this.size > INT_MAX_BYTE_LEN) {
        throw IllegalStateException("parseInt max length > $INT_MAX_BYTE_LEN")
    }
    var rst = 0
    this.forEach {
        rst = rst.shl(1)
        rst += it
    }
    return rst
}

fun doubleSpace(len: Int): String {
    val sb = StringBuilder()
    for (i in 1..len) {
        sb.append(" ")
    }
    return sb.toString()
}

/**
 * 格式化打印
 */
fun formatPrint(toPrint: String): String {
    val divideLen = 2
    var maxLen = 0

    val lines = toPrint.split("\n")

    fun includeTab(line: String): Boolean {
        return line.contains("\t")
    }

    fun key(toPrint: String): String {
        if (!includeTab(toPrint)) return ""
        return toPrint.split("\t")[0]
    }

    fun value(toPrint: String): String {
        return toPrint.split("\t")[1]
    }

    lines.forEach {
        if (key(it).length > maxLen) {
            maxLen = key(it).length
        }
    }

    return lines.map { line ->
        if (includeTab(line)) {
            key(line) + doubleSpace(maxLen + divideLen - key(line).length) + value(line)
        } else {
            line
        }
    }
    .joinToString(separator = "\n")
}

open class Node(var size: Int) {
    var content: ByteArray = ByteArray(0)

    /**
     * 解析content，返回解析之后的新的offsert
     */
    open fun parse(content: ByteArray, off: Int): Int {
        this.content = content.sliceArray(IntRange(off, off + size - 1))
        return off + size
    }
}

class Elf32_OneByte: Node(1)


class Elf32_Addr(): Node(4) {
}

class Elf32_Half(): Node(2) {

}

class Elf32_Off(): Node(4) {

}

class Elf32_Sword(): Node(4) {

}

class Elf32_Word(): Node(4) {
}

open class NodeList: Node(0) {
    var nodes: MutableList<Node> = mutableListOf()

    override fun parse(content: ByteArray, off: Int): Int {
        var curOff = off
        nodes.forEach {
            it.parse(content, curOff)
            curOff += it.size
            this.content += it.content
        }
        return curOff
    }

    override fun toString(): String {
        return ""
    }
}

val EI_MAGIC = 4
val PAD_LEN = 7

class elf32_ident: NodeList() {
    var ident_magic  = Node(EI_MAGIC)
    var ident_class = Elf32_OneByte()
    var ident_data = Elf32_OneByte()
    var ident_version = Elf32_OneByte()
    var ident_osabi = Elf32_OneByte()
    var ident_abiversion = Elf32_OneByte()
    var ident_pad = Node(PAD_LEN)

    init {
        this.nodes.add(ident_magic)
        this.nodes.add(ident_class)
        this.nodes.add(ident_data)
        this.nodes.add(ident_version)
        this.nodes.add(ident_osabi)
        this.nodes.add(ident_abiversion)
        this.nodes.add(ident_pad)
    }


    private fun typeStr(): String {
        return if (ident_class.content.toInt() == 1) {
            "ELF32"
        } else {
            "ELF64"
        }
    }

    private fun endianStr(): String {
        return if (ident_data.content.toInt() == 1) {
            "little endian"
        } else {
            "big endian"
        }
    }

    private fun versionStr(): String {
        return if (ident_version.content.toInt() == 1) {
            "1 (current)"
        } else {
            "${ident_version.content.toInt()} (修改版本)"
        }
    }

    private fun VersionAbi(): String {
        when(ident_osabi.content.toInt()) {
            0 -> return "System V"
            0x01 -> return "HP-UX"
            0x02 -> return "NetBSD"
            0x03 -> return "Linux"
            0x04 -> return "GNU Hurd"
            0x06 -> return "Solaris"
            0x07 -> return "AIX"
            0x08 -> return "RIX"
            0x09 -> return "FreeBSD"
            0x0A -> return "Tru64"
            0x0B -> return "Novell Modesto"
            0x0C -> return "OpenBSD"
            0x0D -> return "OPenVMS"
            0x0E -> return "NonStop Kernel"
            0x0F -> return "AROS"
            0x11 -> return "Fenix OS"
            0x53 -> return "Sortix"
        }
        return "Unknown"
    }

    override fun toString(): String {
        return "  Magic:    " + this.content.hexStr() + "\n" +
            "  Class:\t" + typeStr() + "\n" +
            "  Data:\t" + endianStr() + "\n" +
            "  Version:\t" + versionStr() + "\n" +
            "  OS/ABI:\t" + VersionAbi() + "\n"
    }
}

class elf32_hdr: NodeList() {
    @NodeElemnt(0)
    var e_indent = elf32_ident()

    @NodeElemnt(1)
    var e_type = Elf32_Half()

    @NodeElemnt(2)
    var e_machine = Elf32_Half()

    @NodeElemnt(3)
    var e_version = Elf32_Word()

    @NodeElemnt(4)
    /* Entry point */
    var e_entry = Elf32_Addr()

    @NodeElemnt(5)
    var e_phoff = Elf32_Off()

    @NodeElemnt(6)
    var e_shoff = Elf32_Off()

    @NodeElemnt(7)
    var e_flags = Elf32_Word()

    @NodeElemnt(8)
    var e_ehsize = Elf32_Half()

    @NodeElemnt(9)
    var e_phentsize = Elf32_Half()

    @NodeElemnt(10)
    var e_phnum = Elf32_Half()

    @NodeElemnt(11)
    var e_shentsize = Elf32_Half()

    @NodeElemnt(12)
    var e_shnum = Elf32_Half()

    @NodeElemnt(13)
    var e_shstrndx = Elf32_Half()

    init {
        nodes.add(e_indent)
        nodes.add(e_type)
        nodes.add(e_machine)
        nodes.add(e_version)
        nodes.add(e_entry)
        nodes.add(e_phoff)
        nodes.add(e_shoff)
        nodes.add(e_flags)
        nodes.add(e_ehsize)
        nodes.add(e_phentsize)
        nodes.add(e_phnum)
        nodes.add(e_shentsize)
        nodes.add(e_shnum)
        nodes.add(e_shstrndx)
    }


    override fun toString(): String {
//        return "ELF Header:\n" + magic + rest
        return formatPrint("$e_indent")
    }
}

@Target(AnnotationTarget.FIELD)
annotation class NodeElemnt(val index: Int)
