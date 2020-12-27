import tornadofx.*
import view.EditorStyle
import view.EditorView

class Application : App(EditorView::class, EditorStyle::class) {
  init {
    reloadStylesheetsOnFocus()
  }
}

fun main(args: Array<String>) = launch<Application>(args)