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

package cn.enaium.todo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.enaium.todo.utility.AccountIdStorage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * @author Enaium
 */
@Composable
fun AppNav(modifier: Modifier, accountIdStorage: AccountIdStorage = koinInject()) {
    val nav = rememberNavController()
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutine.launch {
            accountIdStorage.getId()?.also {
                nav.navigate("home")
            }
        }
    }
    Column(modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "auth/login") {
            composable("auth/login") {
                Login(nav)
            }

            composable("auth/register") {
                Register(nav)
            }
            composable("home") {
                Home(nav)
            }
        }
    }
}