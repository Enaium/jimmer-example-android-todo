# Jimmer Example Android Todo App

## Overview
This is a simple Android Todo application that demonstrates how to use Jimmer with Android. The app
allows users to create, read, update, and delete todo items. It uses Jimmer for data persistence and provides a clean architecture with MVVM pattern.

## Features

- **User Authentication**
  - User registration with username and password validation
  - Secure login with MD5 password hashing
  - Session management with persistent login state
  - Logout functionality

- **Category Management**
  - Create, rename, and delete task categories
  - Organize tasks by categories for better productivity
  - Category selection to filter tasks

- **Task Management**
  - Create, edit, and delete tasks
  - Mark tasks as completed or incomplete
  - Mark tasks as important for priority management
  - Task content editing with rename functionality
  - Long-press context menu for task actions

- **User Interface**
  - Modern Material Design 3 UI with Jetpack Compose
  - Dark/light theme support with dynamic color adaptation
  - Responsive design with smooth animations
  - Intuitive navigation between screens
  - Loading indicators and error handling
  - Toast notifications for user feedback

- **Data Persistence**
  - Local SQLite database using Jimmer ORM
  - Automatic database schema creation
  - Efficient data querying and caching
  - Data integrity with foreign key relationships

- **Architecture**
  - MVVM (Model-View-ViewModel) architecture pattern
  - Dependency injection with Koin
  - Reactive UI updates with StateFlow
  - Coroutines for asynchronous operations
  - Clean separation of concerns

## Technologies Used
- Android SDK
- Kotlin
- Jimmer
- MVVM architecture
- Room for local database
- LiveData and ViewModel for reactive UI updates

## Getting Started

### Prerequisites
- Android Studio installed
- Basic knowledge of Android development

### Installation
1. Clone the repository:
    ```bash
    git clone https://github.com/Enaium/jimmer-example-android-todo.git
    ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an Android device or emulator.

### Usage
- Open the app on your Android device or emulator.
- You can add a new todo item by clicking the "Add Todo" button.
- Enter the title and description of the todo item.
- Click "Save" to add the item to the list.
- You can edit or delete existing todo items by clicking on them in the list.
- The app will automatically update the UI to reflect any changes made to the todo items.

## Contributing
If you would like to contribute to this project, please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them with a descriptive message.
4. Push your changes to your forked repository.
5. Create a pull request to the main repository.

## Screenshots

![20250702131526](https://s2.loli.net/2025/07/02/PU7MToOjBpAJKeC.png)
![20250702131541](https://s2.loli.net/2025/07/02/6daBFg1QGquCL7D.png)
![20250702131557](https://s2.loli.net/2025/07/02/5APLqtfgbZTnaBl.png)

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
