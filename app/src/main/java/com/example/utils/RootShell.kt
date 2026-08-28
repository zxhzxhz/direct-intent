package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootShell {

    data class CommandResult(
        val success: Boolean,
        val exitCode: Int,
        val output: String,
        val error: String
    )

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        val result = executeCommand("id")
        result.success && (result.output.contains("uid=0") || result.output.contains("root"))
    }

    suspend fun getSuVersion(): String = withContext(Dispatchers.IO) {
        val result = executeCommand("su -v")
        if (result.success && result.output.isNotBlank()) {
            result.output.trim()
        } else {
            val idResult = executeCommand("id")
            if (idResult.output.contains("uid=0")) "Granted (su)" else "Not Available"
        }
    }

    suspend fun executeCommand(command: String, timeoutMs: Long = 5000): CommandResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs) {
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                val inputStream = process.inputStream
                val errorStream = process.errorStream

                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()

                coroutineScope {
                    val outputDeferred = async(Dispatchers.IO) {
                        val sb = StringBuilder()
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line).append("\n")
                            }
                        }
                        sb.toString().trim()
                    }

                    val errorDeferred = async(Dispatchers.IO) {
                        val sb = StringBuilder()
                        BufferedReader(InputStreamReader(errorStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line).append("\n")
                            }
                        }
                        sb.toString().trim()
                    }

                    val exitCode = process.waitFor()
                    val output = outputDeferred.await()
                    val error = errorDeferred.await()

                    CommandResult(
                        success = exitCode == 0,
                        exitCode = exitCode,
                        output = output,
                        error = error
                    )
                }
            } catch (e: Exception) {
                CommandResult(
                    success = false,
                    exitCode = -1,
                    output = "",
                    error = e.localizedMessage ?: e.message ?: "Execution failed"
                )
            }
        }

        result ?: CommandResult(
            success = false,
            exitCode = -2,
            output = "",
            error = "Root 命令执行超时 (${timeoutMs}ms)"
        )
    }
}
