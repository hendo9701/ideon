package common.util

import common.task.TextHighlightingTask
import common.util.Asset.LINE_INDICATOR
import common.util.Asset.LINE_NUMBER
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent.KEY_PRESSED
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.reactfx.value.Val
import tornadofx.*
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.function.IntFunction
import java.util.regex.Pattern

enum class Asset {
  LINE_NUMBER,
  LINE_INDICATOR
}

private val LOG by logger { }
private val WHITE_SPACE = Pattern.compile("""^\s+""")

fun addAssetsToLine(codeArea: CodeArea, vararg assets: Asset) {
  codeArea.paragraphGraphicFactory = IntFunction { line: Int ->
    val nodeAssets = assets.map { asset: Asset ->
      when (asset) {
        LINE_NUMBER -> LineNumberFactory.get(codeArea).apply(line)
        LINE_INDICATOR -> ArrowFactory(codeArea.currentParagraphProperty()).apply(line)
      }
    }.toTypedArray()
    return@IntFunction HBox(*nodeAssets).apply { alignment = Pos.CENTER_LEFT }
  }
}

fun addTextHighlighting(codeArea: CodeArea, service: ExecutorService, keywordsAndPattern: KeywordSetPattern) {
  codeArea.multiPlainChanges()
      .successionEnds(Duration.ofMillis(100))
      .supplyTask {
        TextHighlightingTask(codeArea, keywordsAndPattern).let {
          service.submit(it)
          it
        }
      }
      .awaitLatest(codeArea.multiPlainChanges())
      .filterMap {
        if (it.isSuccess)
          Optional.of(it.get())
        else {
          LOG.debug(it.failure.stackTrace)
          Optional.empty()
        }
      }
      .subscribe { codeArea.setStyleSpans(0, it) }

}

fun addAutoIndentation(codeArea: CodeArea) = with(codeArea) {
  addEventHandler(KEY_PRESSED) { event ->
    if (event.code == KeyCode.ENTER) {
      WHITE_SPACE.matcher(this.getParagraph(currentParagraph - 1).segments.first()).let { matcher ->
        if (matcher.find()) Platform.runLater { codeArea.insertText(caretPosition, matcher.group()) }
      }
    }
  }
}

private class ArrowFactory(private val shownLine: ObservableValue<Int>) : IntFunction<Node> {

  override fun apply(lineNumber: Int): Node {
    return Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0)
        .apply {
          fill = Color.GREEN
          val visible = Val.map(shownLine) { it == lineNumber }
          visibleWhen(
              Val.flatMap(
                  sceneProperty()
              ) { if (it != null) visible else Val.constant(false) })
        }
  }

}