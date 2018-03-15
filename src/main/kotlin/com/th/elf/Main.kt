package com.th.elf

import java.io.File

fun main(args: Array<String>) {
    val elf = elf32_hdr()
    val content = File(testApkPath).readBytes()
    elf.parse(content, 0)
    println(elf)
}