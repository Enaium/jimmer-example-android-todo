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

package cn.enaium.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.enaium.todo.model.entity.dto.TaskView

@Composable
fun TaskList(
    tasks: List<TaskView>,
    onToggleComplete: (TaskView) -> Unit,
    onToggleImportant: (TaskView) -> Unit,
    onDeleteTask: (TaskView) -> Unit,
    onRenameTask: (TaskView, String) -> Unit
) {
    var showContextMenu by remember { mutableStateOf<TaskView?>(null) }
    var showRenameDialog by remember { mutableStateOf<TaskView?>(null) }
    var newContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        tasks.forEach { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showContextMenu = task
                            }
                        )
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            task.content,
                            fontWeight = if (task.important) FontWeight.Bold else FontWeight.Normal
                        )
                        if (task.completed) {
                            Text(
                                "Completed",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    IconButton(onClick = { onToggleComplete(task) }) {
                        Icon(
                            if (task.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete"
                        )
                    }
                    IconButton(onClick = { onToggleImportant(task) }) {
                        Icon(
                            if (task.important) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = "Toggle Important"
                        )
                    }
                }
            }
        }
    }

    // Context Menu Dialog
    showContextMenu?.let { task ->
        Dialog(
            onDismissRequest = { showContextMenu = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    "Task Options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    "Rename",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showContextMenu = null
                            showRenameDialog = task
                            newContent = task.content
                        }
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    "Delete",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showContextMenu = null
                            onDeleteTask(task)
                        }
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Rename Dialog
    showRenameDialog?.let { task ->
        Dialog(
            onDismissRequest = { showRenameDialog = null }
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    "Rename Task",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.material3.OutlinedTextField(
                    value = newContent,
                    onValueChange = { newContent = it },
                    label = { Text("Task Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = { showRenameDialog = null }
                    ) {
                        Text("Cancel")
                    }
                    
                    androidx.compose.material3.TextButton(
                        onClick = {
                            if (newContent.isNotBlank()) {
                                onRenameTask(task, newContent)
                                showRenameDialog = null
                            }
                        }
                    ) {
                        Text("Rename")
                    }
                }
            }
        }
    }
} 