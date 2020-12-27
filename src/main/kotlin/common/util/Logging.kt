package common.util

import org.apache.log4j.LogManager
import org.apache.log4j.Logger

fun logger(lambda: () -> Unit): Lazy<Logger> = lazy { LogManager.getLogger(getClassName(lambda.javaClass)) }

private fun <T : Any> getClassName(clazz: Class<T>): String = clazz.name.replace(Regex("""\$.*$"""), "")
