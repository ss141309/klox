package com.craftinginterpreters.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths

private var hadError = false

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        kotlin.system.exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes))

    if (hadError) kotlin.system.exitProcess(65)
}

fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")
        val line = reader.readLine() ?: break
        run(line)
        hadError = false
    }
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    for (token in tokens) {
        println(token)
    }
}

object Lox {
    fun error(line: Int, message: String, where: String = "") {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }
}
