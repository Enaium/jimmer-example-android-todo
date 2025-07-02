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
import cn.enaium.todo.error.AccountException
import cn.enaium.todo.model.entity.Account
import cn.enaium.todo.model.entity.username
import cn.enaium.todo.utility.AccountIdStorage
import cn.enaium.todo.utility.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq

/**
 * @author Enaium
 */
class LoginViewModel(
    val sql: KSqlClient,
    val accountIdStorage: AccountIdStorage
) : ViewModel() {

    // Form state
    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    // Validation state
    var usernameError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
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
    }

    fun validateForm(): Boolean {
        val usernameValidation = validateUsername(username)
        val passwordValidation = validatePassword(password)

        usernameError = usernameValidation
        passwordError = passwordValidation

        return usernameValidation == null && passwordValidation == null
    }

    suspend fun login(onSuccess: suspend () -> Unit) = withContext(Dispatchers.IO) {
        if (!validateForm()) {
            throw IllegalArgumentException("Please fix the validation errors")
        }

        isLoading = true
        generalError = null

        try {
            val account = sql.createQuery(Account::class) {
                where(table.username eq username)
                select(table)
            }.fetchOneOrNull() ?: throw AccountException.usernameNotExist("Username not exists")

            if (account.password == password.md5()) {
                accountIdStorage.setId(account.id)
                onSuccess()
            } else {
                throw AccountException.passwordIncorrect("Password incorrect")
            }
        } catch (e: Exception) {
            generalError = e.message ?: "Login failed"
            throw e
        } finally {
            isLoading = false
        }
    }

    fun clearErrors() {
        usernameError = null
        passwordError = null
        generalError = null
    }

    fun resetForm() {
        username = ""
        password = ""
        clearErrors()
    }
}