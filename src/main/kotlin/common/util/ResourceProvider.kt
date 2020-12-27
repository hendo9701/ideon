package common.util

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

object ResourceProvider {

    private val APP_PATHS = javaClass.getResource("/config.json").file

    private const val SYNTAX_DEFINITIONS = "syntax-definitions"

    @JvmStatic
    private fun getSyntaxLocation(): Optional<String> = getPathTo(SYNTAX_DEFINITIONS)

    @JvmStatic
    private fun getPathTo(resource: String): Optional<String> = readFromJson(APP_PATHS)
            .flatMap {
                if (it.containsKey(resource)) Optional.of(it[resource]!! as String) else Optional.empty()
            }

    @JvmStatic
    fun getLanguageDefinition(language: String): Optional<KeywordSetPattern> =
            getSyntaxLocation().flatMap { location ->
                readFromJson("$location${File.separator}$language${File.separator}$language-definition.json")
                        .map {
                            KeywordSetPattern(
                                    keywordSet = it.keys,
                                    pattern = it.entries.joinToString(separator = "|") { r -> "(?<${r.key}>${r.value})" })
                        }
            }

    @JvmStatic
    fun getLanguageConcept(language: String): Optional<String> =
            getSyntaxLocation().map { location ->
                "$location${File.separator}$language${File.separator}$language-concept.css"
            }

    @JvmStatic
    fun loadClass(path: String, clazz: String) =
            URLClassLoader(arrayOf<URL>(File(path).toURI().toURL()), this.javaClass.classLoader).let {
                Class.forName(clazz, true, it)
            }
}