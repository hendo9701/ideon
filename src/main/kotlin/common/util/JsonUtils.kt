package common.util

import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.FileReader
import java.util.*


/**
 * @author Hayder
 */

private val LOG by logger {}

/**
 * Retrieves json object from the json file represented by 'path' parameter
 * @param path Location of json object to be retrieved
 * @return An optional containing the json object requested, an empty optional otherwise
 */
private fun openJson(path: String): Optional<JSONObject> =
        runCatching {
            Optional.of(JSONObject(JSONTokener(FileReader(path))))
        }.getOrDefault(Optional.empty())

/**
 * Converts a JSON object contained in a file into a map
 * @param path Location of json object
 * @return An optional containing the requested data, an empty optional otherwise
 */
fun readFromJson(path: String): Optional<MutableMap<String, Any>> =
        runCatching {
            openJson(path).map { json ->
                mutableMapOf<String, Any>().apply {
                    json.keys().forEach { this += (it as String) to json.get(it) }
                }
            }
        }.getOrElse { LOG.debug("$it"); Optional.empty() }

/**
 * Persists data to a JSON file
 * @param path Location of the JSON file to be persisted
 * @param content Data to be persisted
 * @return true iff operation succeeded, false otherwise
 */
fun writeToJson(path: String, content: Map<String, Any>): Boolean =
        runCatching {
            File(path).let {
                val old = if (!it.exists()) {
                    it.createNewFile(); JSONObject()
                } else openJson(path).orElseGet { JSONObject() }
                content.entries.forEach { entry -> old.put(entry.key, entry.value) }
                val result = setContent(content = old.toString(2), destination = it)
                result
            }
        }.getOrElse { LOG.debug("Something went wrong.\n$it"); false }

