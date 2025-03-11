
# AI Agent Imitation

This project is a command-line interface (CLI) application written in Kotlin that simulates an AI agent interacting with a file system. Unlike a true AI agent that uses a large language model (LLM), this solution mimics AI-like behavior by processing text commands and choosing appropriate tools to perform file operations.

## Overview

The goal of this project is to implement an imitation of an AI agent for an internship task. The agent accepts text-based input and outputs text responses. It handles a specific set of file system commands, demonstrating modular design using the Command pattern (implemented with sealed classes in Kotlin). The agent can either accept manual user input or select commands randomly, simulating an autonomous decision-making process.

## Features

- **Interactive CLI:**  
  Continuously accepts user input and executes file system operations based on commands.

- **Command Pattern Implementation:**  
  Each command (e.g., `ls`, `cd`, `delete`, etc.) is encapsulated in its own class, making the codebase modular, easy to extend, and maintain.

- **File System Operations:**  
  The agent supports the following commands:
  - `ls` – List files in the current directory.
  - `size` – Display the total size of files in the current directory.
  - `cd <dir>` – Change to a specified directory.
  - `delete <file>` – Delete a specified file.
  - `print <file> <property...>` – Print file properties (allowed properties: `length`, `path`, `lastModified`).
  - `delete_old_files` – Delete files older than 30 days.
  - `delete_largest_file` – Delete the largest file in the current directory.
  - `random_cd` – Change to a random subdirectory.
  - `cd_up` – Move up one directory level.
  - `exit` – Terminate the application.

- **Manual and Random Command Selection:**  
  Users can choose the next command manually or let the agent pick one from a predefined list (`delete_old_files`, `delete_largest_file`, `random_cd`, `cd_up`), simulating an autonomous decision.

## Getting Started

### Prerequisites

- **JDK:** Ensure that you have the Java Development Kit (JDK) version 11 or higher installed.
- **Gradle:** The project uses Gradle for building and running. The Gradle wrapper is included, so you don’t need to install Gradle manually.
- **Kotlin:** The project is written in Kotlin, but you don’t need to install Kotlin separately as it’s bundled with Gradle.

### Running the Project

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   cd ai-agent-imitation
   ```

2. **Build and Run using Gradle:**

   ```bash
   ./gradlew run
   ```

   Alternatively, open the project in IntelliJ IDEA and run the `main` function in `Agent.kt`.

   **Note:** If you encounter issues with interactive input when running via `gradlew run`, add the `--console=plain` flag to disable Gradle’s progress bar:

   ```bash
   ./gradlew run --console=plain
   ```

### Test Directory (`testDir`)

The project includes a sample directory named `testDir`, located at `src/main/testDir`, which contains test files and folders for experimenting with the agent’s commands. This directory is designed to allow safe testing of file system operations without modifying your actual filesystem. Here’s the structure of `testDir`:

```plaintext
testDir/
├── file1.txt (100 bytes, current date)
├── file2.bin (500 bytes, date older than 30 days)
├── dir1/
│   ├── small_file.txt (10 bytes, current date)
│   └── old_file.dat (200 bytes, date older than 30 days)
├── dir2/
│   └── large_file.mp4 (1000 bytes, current date)
└── dir3/
    └── empty_dir/ (empty subdirectory)
```

**How to Use `testDir`:**
- **Setup:** The `testDir` folder is pre-configured in the project. You don’t need to create it manually unless you want to modify its contents.
- **Testing Commands:** Use `testDir` to test commands like:
  - `ls` to list files and directories.
  - `size` to check the total size of files.
  - `cd dir1` to navigate into subdirectories.
  - `delete small_file.txt` to test file deletion.
  - `delete_old_files` to remove files older than 30 days (e.g., `file2.bin`, `old_file.dat`).
  - `delete_largest_file` to delete the largest file (e.g., `large_file.mp4` in `dir2`).
- **Recreating `testDir`:** If you modify or delete `testDir`, you can recreate it by following the structure above. Use commands like `dd` to create files of specific sizes (e.g., `dd if=/dev/zero of=src/main/testDir/file1.txt bs=1 count=100`) and `touch -t` to set old dates (e.g., `touch -t 202309010000 src/main/testDir/file2.bin`).

### Usage

When the application starts, you will see a welcome message:

```plaintext
Welcome to the Agent file system interaction!
```

You will then be prompted to choose whether to enter a command manually or let the agent pick one randomly:

```plaintext
Do you want to choose the next command yourself? (y/n)
```

- If you enter `y`, you can type a command (e.g., `ls`, `cd dir1`, `exit`).
- If you enter `n`, the agent will randomly select a command from the list: `delete_old_files`, `delete_largest_file`, `random_cd`, `cd_up`.
- To see the list of available commands, enter an invalid command, and the agent will display them.

**Example Interaction:**

```plaintext
Do you want to choose the next command yourself? (y/n)
y
Please enter your command:
ls
Files in directory:
file1.txt
file2.bin
dir1
dir2
dir3
```

## Code Structure

- **Agent.kt:**  
  Contains the main `Agent` class, which implements file system interactions and defines an interface hierarchy for commands. The `Agent` class manages the current directory, processes user input, and executes commands using a `CommandFactory`.

- **Command Factory:**  
  The `CommandFactory` object parses user input and instantiates the appropriate command class (e.g., `ListFilesCommand`, `ChangeDirectoryCommand`) to handle the request.

- **Command Classes:**  
  Each command (e.g., `ls`, `delete`, `cd`) is implemented as a separate class that implements the `Command` interface, ensuring modularity and extensibility.

## Development Notes

- **File System Safety:**  
  The agent ensures that it cannot navigate above the root directory (`testDir`), preventing unintended access to the user’s filesystem.
- **Error Handling:**  
  The application includes basic error handling for invalid commands, non-existent files, and failed operations (e.g., attempting to delete a file that doesn’t exist).
- **Extensibility:**  
  New commands can be added by creating a new class that implements the `Command` interface and updating the `CommandFactory` to recognize the new command.

