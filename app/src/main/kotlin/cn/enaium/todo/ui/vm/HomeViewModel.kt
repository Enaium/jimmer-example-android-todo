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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.enaium.todo.model.entity.Category
import cn.enaium.todo.model.entity.Task
import cn.enaium.todo.model.entity.accountId
import cn.enaium.todo.model.entity.completed
import cn.enaium.todo.model.entity.createdTime
import cn.enaium.todo.model.entity.dto.CategoryInput
import cn.enaium.todo.model.entity.dto.CategoryView
import cn.enaium.todo.model.entity.dto.TaskInput
import cn.enaium.todo.model.entity.dto.TaskView
import cn.enaium.todo.model.entity.important
import cn.enaium.todo.utility.AccountIdStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.UUID

/**
 * @author Enaium
 */
class HomeViewModel(
    val sql: KSqlClient,
    val accountIdStorage: AccountIdStorage
) : ViewModel() {
    private val _categories = MutableStateFlow<List<CategoryView>>(emptyList())
    val categories: StateFlow<List<CategoryView>> = _categories.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskView>>(emptyList())
    val tasks: StateFlow<List<TaskView>> = _tasks.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryView?>(null)
    val selectedCategory: StateFlow<CategoryView?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableStateFlow<String?>(null)
    val event: StateFlow<String?> = _event.asStateFlow()

    init {
        loadCategoriesAndTasks()
    }

    fun loadCategoriesAndTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val cats = getCategories()
                _categories.value = cats
                if (cats.isNotEmpty()) {
                    _selectedCategory.value = cats.first()
                    val tasks = getTasks().filter { it.category.id == cats.first().id }
                    _tasks.value = tasks
                }
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to load data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: CategoryView) {
        _selectedCategory.value = category
        loadTasksForCategory(category)
    }

    fun loadTasksForCategory(category: CategoryView) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val tasks = getTasks().filter { it.category.id == category.id }
                _tasks.value = tasks
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to load tasks"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                saveCategory(CategoryInput(name = name))
                val cats = getCategories()
                _categories.value = cats
                if (cats.isNotEmpty()) {
                    _selectedCategory.value = cats.last()
                    val tasks = getTasks().filter { it.category.id == cats.last().id }
                    _tasks.value = tasks
                }
                _event.value = "Category added"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to add category"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(content: String) {
        val category = _selectedCategory.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                saveTask(
                    TaskInput(
                        content = content,
                        category = TaskInput.TargetOf_category(category.id),
                        completed = false,
                        important = false
                    )
                )
                val tasks = getTasks().filter { it.category.id == category.id }
                _tasks.value = tasks
                _event.value = "Task added"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to add task"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTask(task: TaskView, completed: Boolean? = null, important: Boolean? = null) {
        val category = task.category
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                saveTask(
                    TaskInput(
                        id = task.id,
                        content = task.content,
                        category = TaskInput.TargetOf_category(category.id),
                        completed = completed ?: task.completed,
                        important = important ?: task.important
                    )
                )
                val tasks = getTasks().filter { it.category.id == category.id }
                _tasks.value = tasks
                _event.value = "Task updated"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to update task"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(category: CategoryView) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                deleteCategory(category.id)
                val cats = getCategories()
                _categories.value = cats
                if (cats.isNotEmpty()) {
                    _selectedCategory.value = cats.first()
                    val tasks = getTasks().filter { it.category.id == cats.first().id }
                    _tasks.value = tasks
                } else {
                    _selectedCategory.value = null
                    _tasks.value = emptyList()
                }
                _event.value = "Category deleted"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to delete category"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renameCategory(category: CategoryView, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                saveCategory(CategoryInput(id = category.id, name = newName))
                val cats = getCategories()
                _categories.value = cats
                val updatedCategory = cats.find { it.id == category.id }
                if (updatedCategory != null && _selectedCategory.value?.id == category.id) {
                    _selectedCategory.value = updatedCategory
                }
                _event.value = "Category renamed"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to rename category"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(task: TaskView) {
        val category = task.category
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                deleteTask(task.id)
                val tasks = getTasks().filter { it.category.id == category.id }
                _tasks.value = tasks
                _event.value = "Task deleted"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to delete task"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renameTask(task: TaskView, newContent: String) {
        val category = task.category
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                saveTask(
                    TaskInput(
                        id = task.id,
                        content = newContent,
                        category = TaskInput.TargetOf_category(category.id),
                        completed = task.completed,
                        important = task.important
                    )
                )
                val tasks = getTasks().filter { it.category.id == category.id }
                _tasks.value = tasks
                _event.value = "Task renamed"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to rename task"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun saveCategory(input: CategoryInput) = withContext(Dispatchers.IO) {
        sql.save(input)
    }

    suspend fun getCategories(): List<CategoryView> = withContext(Dispatchers.IO) {
        sql.createQuery(Category::class) {
            select(table.fetch(CategoryView::class))
        }.execute()
    }

    suspend fun deleteCategory(id: UUID) = withContext(Dispatchers.IO) {
        sql.deleteById(CategoryView::class, id)
    }

    suspend fun saveTask(input: TaskInput) = withContext(Dispatchers.IO) {
        sql.save(input) {
            setMode(SaveMode.NON_IDEMPOTENT_UPSERT)
        }
    }

    suspend fun getTasks(): List<TaskView> = withContext(Dispatchers.IO) {
        val accountId = accountIdStorage.getId()
        sql.createQuery(Task::class) {
            where(table.accountId eq accountId)
            orderBy(table.important.desc(), table.completed.asc(), table.createdTime.asc())
            select(table.fetch(TaskView::class))
        }.execute()
    }

    suspend fun deleteTask(id: UUID) = withContext(Dispatchers.IO) {
        sql.deleteById(TaskView::class, id)
    }

    fun clearEvent() {
        _event.value = null
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                accountIdStorage.clearId()
                _event.value = "logout"
            } catch (e: Exception) {
                _event.value = e.message ?: "Failed to logout"
            }
        }
    }
}