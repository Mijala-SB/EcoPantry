package com.mishba.ecopantryapp

import org.junit.Assert.*
import org.junit.Test

class LoginValidationTest {

    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && email.contains("@") && email.substringAfter("@").contains(".")

    private fun isValidPassword(password: String): Boolean = password.length >= 6

    @Test
    fun validEmailWithAtAndDomainShouldPass() {
        val email = "user@domain.com"
        assertTrue(isValidEmail(email))
    }

    @Test
    fun emailMissingAtSymbolShouldFail() {
        val email = "userdomain.com"
        assertFalse(isValidEmail(email))
    }



    @Test
    fun blankEmailShouldFail() {
        val email = "   "
        assertFalse(isValidEmail(email))
    }

    @Test
    fun passwordLessThan6CharsShouldFail() {
        assertFalse(isValidPassword("12345"))
    }

    @Test
    fun password6CharsOrMoreShouldPass() {
        assertTrue(isValidPassword("123456"))
        assertTrue(isValidPassword("longpassword"))
    }

    @Test
    fun resetEmailShouldOnlyBeSentForValidEmail() {
        val email = "test@example.com"
        val isEmailValid = isValidEmail(email)
        assertTrue(isEmailValid)
    }

    @Test
    fun resetEmailShouldNotBeSentForInvalidEmail() {
        val email = "invalid"
        assertFalse(isValidEmail(email))
    }
}