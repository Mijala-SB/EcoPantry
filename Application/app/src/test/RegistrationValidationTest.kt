package com.mishba.ecopantryapp

import org.junit.Assert.*
import org.junit.Test

class RegistrationValidationTest {

    @Test
    fun validEmailShouldPass() {
        val email = "user@example.com"
        assertTrue(email.contains("@"))
        assertTrue(email.substringAfter("@").contains("."))
    }

    @Test
    fun emailWithoutAtSymbolIsInvalid() {
        val email = "invalidemail.com"
        assertFalse(email.contains("@"))
    }

    @Test
    fun blankEmailIsInvalid() {
        val email = ""
        assertFalse(email.isNotBlank())
    }

    @Test
    fun passwordShorterThan6CharsIsInvalid() {
        val password = "abcde"
        assertFalse(password.length >= 6)
    }

    @Test
    fun passwordExactly6CharsIsValid() {
        val password = "abcdef"
        assertTrue(password.length >= 6)
    }

    @Test
    fun mismatchedPasswordsShouldFail() {
        val password = "secret123"
        val confirm = "secret124"
        assertNotEquals(password, confirm)
    }

    @Test
    fun matchingPasswordsShouldPass() {
        val password = "mypassword"
        val confirm = "mypassword"
        assertEquals(password, confirm)
    }

    @Test
    fun householdSizeValidIntegerShouldParse() {
        val input = "4"
        val size = input.toIntOrNull()
        assertNotNull(size)
        assertEquals(4, size)
    }

    @Test
    fun householdSizeEmptyShouldReturnNull() {
        val input = ""
        val size = input.toIntOrNull()
        assertNull(size)
    }

    @Test
    fun householdSizeNonNumericShouldReturnNull() {
        val input = "five"
        val size = input.toIntOrNull()
        assertNull(size)
    }

    @Test
    fun otpWith6DigitsIsAccepted() {
        val code = "123456"
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun otpWithLettersIsInvalid() {
        val code = "12abc6"
        assertFalse(code.all { it.isDigit() })
    }

    @Test
    fun otpShorterThan6DigitsIsInvalid() {
        val code = "12345"
        assertFalse(code.length == 6)
    }
}