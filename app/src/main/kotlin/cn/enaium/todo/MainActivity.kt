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

package cn.enaium.todo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import cn.enaium.todo.interceptor.BaseEntityDraftInterceptor
import cn.enaium.todo.interceptor.PassiveEntityDraftInterceptor
import cn.enaium.todo.ui.AppNav
import cn.enaium.todo.ui.theme.ToDoTheme
import cn.enaium.todo.ui.vm.HomeViewModel
import cn.enaium.todo.ui.vm.LoginViewModel
import cn.enaium.todo.ui.vm.RegisterViewModel
import cn.enaium.todo.utility.AccountIdStorage
import cn.enaium.todo.utility.sql
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appModule = module {
            single<Context> { this@MainActivity }
            single { AccountIdStorage(get()) }
            single { BaseEntityDraftInterceptor() }
            single { PassiveEntityDraftInterceptor(get()) }
            single<KSqlClient> { sql(get(), get(), get()) }
            viewModel { LoginViewModel(get(), get()) }
            viewModel { RegisterViewModel(get()) }
            viewModel { HomeViewModel(get(), get()) }
        }

        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }

        enableEdgeToEdge()
        setContent {
            ToDoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNav(Modifier.padding(innerPadding))
                }
            }
        }
    }
}