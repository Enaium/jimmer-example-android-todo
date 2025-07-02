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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.enaium.todo.model.entity.dto.CategoryView

@Composable
fun CategoryList(
    categories: List<CategoryView>,
    selectedCategory: CategoryView?,
    onCategorySelected: (CategoryView) -> Unit,
    onDeleteCategory: (CategoryView) -> Unit,
    onRenameCategory: (CategoryView, String) -> Unit
) {
    var showContextMenu by remember { mutableStateOf<CategoryView?>(null) }
    var showRenameDialog by remember { mutableStateOf<CategoryView?>(null) }
    var newName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(bottom = 8.dp)
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedCategory?.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onCategorySelected(category) }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showContextMenu = category
                            }
                        )
                    }
                    .padding(8.dp),
            ) {
                Text(
                    category.name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    // Context Menu Dialog
    showContextMenu?.let { category ->
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
                    "Category Options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    "Rename",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showContextMenu = null
                            showRenameDialog = category
                            newName = category.name
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
                            onDeleteCategory(category)
                        }
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Rename Dialog
    showRenameDialog?.let { category ->
        Dialog(
            onDismissRequest = { showRenameDialog = null }
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    "Rename Category",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.material3.OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Category Name") },
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
                            if (newName.isNotBlank()) {
                                onRenameCategory(category, newName)
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