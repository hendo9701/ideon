package common.task

import common.util.KeywordSetPattern
import javafx.concurrent.Task
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import java.util.*
import java.util.regex.Pattern
import org.fxmisc.richtext.model.StyleSpansBuilder as builder

class TextHighlightingTask(
    private val codeArea: CodeArea,
    keywordSetPattern: KeywordSetPattern
) : Task<StyleSpans<Collection<String>>>() {

  private val keywords = keywordSetPattern.keywordSet
  private val pattern = Pattern.compile(keywordSetPattern.pattern)


  override fun call() =
      computeHighlighting(codeArea.text)


  @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private fun computeHighlighting(text: String?): StyleSpans<Collection<String>> {
    val matcher = pattern.matcher(text)
    var lastKwEnd = 0
    val spansBuilder: builder<Collection<String>> = builder()
    while (matcher.find()) {
      var clazz = ""
      for (keyword in keywords) {
        if (matcher.group(keyword.toUpperCase()) != null) {
          clazz = keyword.toLowerCase()
          break
        }
      }
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd)
      spansBuilder.add(Collections.singleton(clazz), matcher.end() - matcher.start())
      lastKwEnd = matcher.end()
    }
    spansBuilder.add(Collections.emptyList(), (text?.length ?: 0) - lastKwEnd)
    return spansBuilder.create()
  }
}