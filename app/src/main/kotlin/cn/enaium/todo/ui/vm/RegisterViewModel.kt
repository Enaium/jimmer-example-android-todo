/*
 * Copyright (c) 2025 Enaium
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.enaium.todo.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cn.enaium.todo.model.entity.dto.AccountInput
import cn.enaium.todo.utility.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.kt.KSqlClient

/**
 * @author Enaium
 */
class RegisterViewModel(
    val sql: KSqlClient
) : ViewModel() {

    // Form state
    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    // Validation state
    var usernameError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var generalError by mutableStateOf<String?>(null)
        private set

    // Validation rules
    private fun validateUsername(value: String): String? {
        return when {
            value.isEmpty() -> "Username is required"
            value.length < 3 -> "Username must be at least 3 characters"
            value.length > 20 -> "Username must be less than 20 characters"
            !value.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username can only contain letters, numbers, and underscores"
            else -> null
        }
    }

    private fun validatePassword(value: String): String? {
        return when {
            value.isEmpty() -> "Password is required"
            value.length < 6 -> "Password must be at least 6 characters"
            value.length > 50 -> "Password must be less than 50 characters"
            !value.matches(Regex(".*[A-Z].*")) -> "Password must contain at least one uppercase letter"
            !value.matches(Regex(".*[a-z].*")) -> "Password must contain at least one lowercase letter"
            !value.matches(Regex(".*\\d.*")) -> "Password must contain at least one number"
            else -> null
        }
    }

    private fun validateConfirmPassword(value: String): String? {
        return when {
            value.isEmpty() -> "Please confirm your password"
            value != password -> "Passwords do not match"
            else -> null
        }
    }

    fun updateUsername(value: String) {
        username = value
        usernameError = null // Clear error when user types
    }

    fun updatePassword(value: String) {
        password = value
        passwordError = null // Clear error when user types
        // Re-validate confirm password when password changes
        if (confirmPassword.isNotEmpty()) {
            confirmPasswordError = validateConfirmPassword(confirmPassword)
        }
    }

    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        confirmPasswordError = null // Clear error when user types
    }

    fun validateForm(): Boolean {
        val usernameValidation = validateUsername(username)
        val passwordValidation = validatePassword(password)
        val confirmPasswordValidation = validateConfirmPassword(confirmPassword)

        usernameError = usernameValidation
        passwordError = passwordValidation
        confirmPasswordError = confirmPasswordValidation

        return usernameValidation == null && passwordValidation == null && confirmPasswordValidation == null
    }

    suspend fun register(onSuccess: suspend () -> Unit) = withContext(Dispatchers.IO) {
        if (!validateForm()) {
            throw IllegalArgumentException("Please fix the validation errors")
        }

        isLoading = true
        generalError = null

        try {
            if (sql.save(
                    AccountInput(
                        username,
                        password
                    ).copy(password = password.md5())
                ).isModified
            ) {
                onSuccess()
            } else {
                throw Exception("Registration failed")
            }
        } catch (e: Exception) {
            generalError = e.message ?: "Registration failed"
            throw e
        } finally {
            isLoading = false
        }
    }

    fun clearErrors() {
        usernameError = null
        passwordError = null
        confirmPasswordError = null
        generalError = null
    }

    fun resetForm() {
        username = ""
        password = ""
        confirmPassword = ""
        clearErrors()
    }
}