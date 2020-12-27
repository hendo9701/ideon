package common.util

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

/**
 *@author Hayder
 */

private val LOG by logger { }

/**
 * Persists data into a file (possibly non-existing)
 * @param content The data to be persisted
 * @param destination Location of data to be persisted
 * @return true iff operation succeeded, false otherwise
 */
fun setContent(content: String, destination: File): Boolean =
    runCatching {
      BufferedWriter(FileWriter(destination)).use {
        it.write(content.trimEnd())
        LOG.debug("Content set to file $destination.")
      }
      true
    }.getOrElse { LOG.debug("$it"); false }

/**
 * Fetches content from a file
 * @param source Location of data to be fetched
 * @return An optional string containing the result of this operation, empty otherwise
 */
fun getContent(source: File): Optional<String> =
    runCatching {
      Optional.of(source.useLines {
        StringBuilder().apply { it.forEach { line -> this.append(line).append("\n") } }.toString().trimEnd()
      })
    }.getOrElse { LOG.debug("$it"); Optional.empty() }
