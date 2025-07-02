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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cn.enaium.todo.ui.component.CategoryList
import cn.enaium.todo.ui.component.TaskList
import cn.enaium.todo.ui.vm.HomeViewModel
import cn.enaium.todo.utility.toast
import org.koin.androidx.compose.koinViewModel

/**
 * @author Enaium
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(nav: NavController) {
    val viewModel = koinViewModel<HomeViewModel>()
    val context = LocalContext.current

    val categories by viewModel.categories.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newTaskContent by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // Show toast for events and handle logout
    LaunchedEffect(event) {
        event?.let {
            if (it == "logout") {
                nav.navigate("auth/login") {
                    popUpTo("home") { inclusive = true }
                }
            } else {
                context.toast(it)
            }
            viewModel.clearEvent()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Todo App") },
            actions = {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Exit Login") },
                        onClick = {
                            showMenu = false
                            viewModel.logout()
                        }
                    )
                }
            }
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Categories Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }

            CategoryList(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                onDeleteCategory = { viewModel.deleteCategory(it) },
                onRenameCategory = { category, newName -> viewModel.renameCategory(category, newName) }
            )

            Spacer(Modifier.height(16.dp))

            // Tasks Section Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { showAddTaskDialog = true },
                    enabled = selectedCategory != null
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }

            // Tasks Section Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                TaskList(
                    tasks = tasks,
                    onToggleComplete = { viewModel.updateTask(it, completed = !it.completed) },
                    onToggleImportant = { viewModel.updateTask(it, important = !it.important) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onRenameTask = { task, newContent -> viewModel.renameTask(task, newContent) }
                )
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCategory(newCategoryName)
                    showAddCategoryDialog = false
                    newCategoryName = ""
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddCategoryDialog = false
                    newCategoryName = ""
                }) { Text("Cancel") }
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Task") },
            text = {
                OutlinedTextField(
                    value = newTaskContent,
                    onValueChange = { newTaskContent = it },
                    label = { Text("Task Content") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addTask(newTaskContent)
                    showAddTaskDialog = false
                    newTaskContent = ""
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddTaskDialog = false
                    newTaskContent = ""
                }) { Text("Cancel") }
            }
        )
    }
}