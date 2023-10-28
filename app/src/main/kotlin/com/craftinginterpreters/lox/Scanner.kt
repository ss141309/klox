package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

class Scanner(private val source: String) {
    private val tokens = ArrayList<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)

            '/' -> {
                if (match('/')) while (peek() != '\n' && !isAtEnd()) advance()
                else if (match('*')) {
		    var nestLevel = 1
		    while (nestLevel > 0 && !isAtEnd()) {
			if (peek() == '\n') line++
			else if (peek() == '/' && peekNext() == '*') nestLevel++
			else if (peek() == '*' && peekNext() == '/') nestLevel--
			advance()
		    }

		    if (isAtEnd()) Lox.error(line, "Unterminated comment.")
		    else advance()

		} else addToken(SLASH)
            }

            ' ', '\r', '\t' -> Unit
            '\n' -> line++
            '"' -> string()

            else -> {
                if (isDigit(c)) number()
                else if (isAlpha(c)) identifier()
                else Lox.error(line, "Unexpected character.")
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current)
        val type = Keywords[text] ?: IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()
        }

        while (isDigit(peek())) advance()

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun advance(): Char = source[current++]
    private fun isAtEnd(): Boolean = current >= source.length
    private fun isDigit(c: Char): Boolean = c in ('0'..'9')
    private fun isAlpha(c: Char): Boolean = c in ('a'..'z') || c in ('A'..'Z') || c == '_'
    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    companion object Keywords : HashMap<String, TokenType>() {

        init {
            put("and", AND)
            put("class", CLASS)
            put("else", ELSE)
            put("false", FALSE)
            put("for", FOR)
            put("fun", FUN)
            put("if", IF)
            put("nil", NIL)
            put("or", OR)
            put("return", RETURN)
            put("super", SUPER)
            put("this", THIS)
            put("true", TRUE)
            put("var", VAR)
            put("while", WHILE)
        }
    }

}
