import java.io.File

/**
 * Agent for interacting with the file system.
 * It works within a root directory provided at initialization.
 */
class Agent(private var rootDirectory: File) {
    var currentDirectory: File = rootDirectory
        private set

    // Retrieves files in the current directory based on a filter
    private fun getFilesInCurDirectory(mask: (File) -> Boolean): List<File> {
        return currentDirectory.listFiles()?.filter(mask) ?: emptyList()
    }

    // Retrieves the total size of files in the current directory based on a filter
    private fun getSizeFilesInCurDirectory(mask: (File) -> Boolean): Long {
        return getFilesInCurDirectory(mask).sumOf { it.length() }
    }

    // Deletes a file in the current directory
    fun deleteFileInCurDirectory(file: File): Boolean {
        return file.delete()
    }

    // Changes the current directory with a check to prevent moving outside the root directory
    fun changeDirectory(newDirectory: File): Boolean {
        try {
            if (newDirectory.isDirectory && newDirectory.canonicalPath.startsWith(rootDirectory.canonicalPath)) {
                currentDirectory = newDirectory
                println("Changed to directory: ${currentDirectory.path}")
                return true
            } else {
                println("Error: ${newDirectory.path} is not a valid directory or it is above the root directory.")
                return false
            }
        } catch (e: Exception) {
            println("Error changing directory: ${e.message}")
            return false
        }
    }

    // Prints properties of a file. Allowed properties: length, path, lastModified.
    fun printFile(file: File, arguments: List<String>) {
        if (!file.exists()) {
            println("File not found: ${file.name}")
            return
        }
        arguments.forEach { argument ->
            when (argument.lowercase()) {
                "length" -> println("length: ${file.length()} bytes")
                "path" -> println("path: ${file.path}")
                "lastmodified", "last_modified" -> println("lastModified: ${file.lastModified()}")
                else -> println("Unknown property: $argument. Allowed properties: length, path, lastModified")
            }
        }
    }

    /**
     * Sealed class for commands.
     */
    sealed class Command {
        abstract fun execute(agent: Agent)

        // Command: list files (ls)
        object ListFilesCommand : Command() {
            override fun execute(agent: Agent) {
                val files = agent.getFilesInCurDirectory { true }
                if (files.isNotEmpty()) {
                    println("Files in directory:")
                    files.forEach { println(it.name) }
                } else {
                    println("Directory is empty.")
                }
            }
        }

        // Command: print file properties (print)
        data class PrintFileCommand(val fileName: String, val arguments: List<String>) : Command() {
            override fun execute(agent: Agent) {
                val file = File(agent.currentDirectory, fileName)
                if (file.exists()) {
                    agent.printFile(file, arguments)
                } else {
                    println("File not found: $fileName")
                }
            }
        }

        // Command: display total size of files (size)
        object TotalSizeCommand : Command() {
            override fun execute(agent: Agent) {
                val size = agent.getSizeFilesInCurDirectory { true }
                println("Total size of files: $size bytes")
            }
        }

        // Command: change directory (cd)
        data class ChangeDirectoryCommand(val path: String) : Command() {
            override fun execute(agent: Agent) {
                val newDirectory = File(agent.currentDirectory, path)
                agent.changeDirectory(newDirectory)
            }
        }

        // Command: delete file (delete)
        data class DeleteFileCommand(val fileName: String) : Command() {
            override fun execute(agent: Agent) {
                val file = File(agent.currentDirectory, fileName)
                if (file.exists() && agent.deleteFileInCurDirectory(file)) {
                    println("Deleted: $fileName")
                } else {
                    println("Failed to delete: $fileName")
                }
            }
        }

        // Command: delete files older than 30 days (delete_old_files)
        object DeleteOldFilesCommand : Command() {
            override fun execute(agent: Agent) {
                val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 // 30 days
                val oldFiles = agent.getFilesInCurDirectory { it.lastModified() < threshold }
                if (oldFiles.isNotEmpty()) {
                    oldFiles.forEach {
                        if (agent.deleteFileInCurDirectory(it)) {
                            println("Deleted old file: ${it.name}")
                        } else {
                            println("Failed to delete: ${it.name}")
                        }
                    }
                } else {
                    println("No old files found.")
                }
            }
        }

        // Command: change to a random subdirectory (random_cd)
        object ChangeDirectoryRandomCommand : Command() {
            override fun execute(agent: Agent) {
                val directories = agent.getFilesInCurDirectory { it.isDirectory }
                if (directories.isNotEmpty()) {
                    val randomDir = directories.random()
                    agent.changeDirectory(randomDir)
                } else {
                    println("No subdirectories found.")
                }
            }
        }

        // Command: move up one directory level (cd_up)
        object ChangeDirectoryUpCommand : Command() {
            override fun execute(agent: Agent) {
                val parent = agent.currentDirectory.parentFile
                if (parent != null && parent.canonicalPath.startsWith(agent.rootDirectory.canonicalPath)) {
                    agent.changeDirectory(parent)
                } else {
                    println("Cannot move up from root directory.")
                }
            }
        }

        // Command: delete the largest file (delete_largest_file)
        object DeleteLargestFileCommand : Command() {
            override fun execute(agent: Agent) {
                val files = agent.getFilesInCurDirectory { it.isFile }
                val largestFile = files.maxByOrNull { it.length() }
                if (largestFile != null && agent.deleteFileInCurDirectory(largestFile)) {
                    println("Deleted largest file: ${largestFile.name}")
                } else {
                    println("No files found or failed to delete.")
                }
            }
        }

        // Command: exit the application
        object ExitCommand : Command() {
            override fun execute(agent: Agent) {
                println("Exiting application...")
                System.exit(0)
            }
        }
    }

    /**
     * Command factory for creating a command based on user input.
     */
    object CommandFactory {
        fun getCommand(commandList: List<String>): Command? {
            if (commandList.isEmpty()) return null

            return when (commandList[0].lowercase()) {
                "ls" -> Command.ListFilesCommand
                "size" -> Command.TotalSizeCommand
                "cd" -> if (commandList.size > 1) Command.ChangeDirectoryCommand(commandList[1]) else null
                "delete" -> if (commandList.size > 1) Command.DeleteFileCommand(commandList[1]) else null
                "print" -> if (commandList.size > 2) Command.PrintFileCommand(commandList[1], commandList.drop(2)) else null
                "delete_old_files" -> Command.DeleteOldFilesCommand
                "delete_largest_file" -> Command.DeleteLargestFileCommand
                "random_cd" -> Command.ChangeDirectoryRandomCommand
                "cd_up" -> Command.ChangeDirectoryUpCommand
                "exit" -> Command.ExitCommand
                else -> null
            }
        }
    }

    /**
     * Prompts the user to choose a command: manual input or random selection.
     */
    private fun chooseCommand(): List<String> {
        println("\nDo you want to choose the next command yourself? (y/n)")
        val userInput = readlnOrNull()?.trim()?.lowercase() ?: "n"

        return if (userInput == "y") {
            println("Please enter your command (e.g., ls, cd <dir>, print <file> <property>):")
            val input = readlnOrNull()?.trim() ?: ""
            if (input.isEmpty()) {
                println("Empty command. Try again.")
                emptyList()
            } else {
                input.split(" ")
            }
        } else {
            // Randomly choose from a set of commands
            val randomCommands = listOf(
                "delete_old_files",
                "delete_largest_file",
                "random_cd",
                "cd_up"
            )
            val randomCommand = randomCommands.random()
            println("Agent has chosen the command: $randomCommand")
            randomCommand.split(" ")
        }
    }

    /**
     * Main loop of the agent.
     */
    fun mainLoop() {
        println("Welcome to the Agent file system interaction!")
        println("Available commands: ls, size, cd <dir>, delete <file>, print <file> <property...>, delete_old_files, delete_largest_file, random_cd, cd_up, exit")
        while (true) {
            try {
                val commandParts = chooseCommand()
                if (commandParts.isEmpty()) continue
                val command = CommandFactory.getCommand(commandParts)
                if (command != null) {
                    command.execute(this)
                } else {
                    println("Unknown command. Please try again.")
                }
            } catch (e: Exception) {
                println("An error occurred: ${e.message}")
            }
        }
    }
}

fun main() {
    val rootDir = File("testDir")
    if (!rootDir.exists()) {
        rootDir.mkdir()
    }
    val agent = Agent(rootDir)
    agent.mainLoop()
}
