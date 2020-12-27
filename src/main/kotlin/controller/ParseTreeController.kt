package controller

import com.jfoenix.controls.JFXSlider
import common.antlr.UnderlineErrorListener
import common.util.ResourceProvider
import common.util.getContent
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingNode
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.fxmisc.richtext.CodeArea
import tornadofx.*
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
import javax.swing.SwingUtilities
import javax.swing.Timer

class ParseTreeController : Controller() {

  private lateinit var grammarName: String
  private lateinit var classesLocation: String
  private lateinit var lexerClass: Class<*>
  private lateinit var parserClass: Class<*>
  private lateinit var lexerConstructor: Constructor<*>
  private lateinit var parserConstructor: Constructor<*>
  private lateinit var getRuleNamesMethod: Method

  fun setGrammarName(grammarName: String) {
    this.grammarName = grammarName
  }

  fun setClassesLocation(classesLocation: String) {
    this.classesLocation = classesLocation
  }

  fun reflect() {
    lexerClass = ResourceProvider.loadClass(classesLocation, "${grammarName}Lexer")
    parserClass = ResourceProvider.loadClass(classesLocation, "${grammarName}Parser")
    lexerConstructor = lexerClass.getConstructor(CharStream::class.java)
    parserConstructor = parserClass.getConstructor(TokenStream::class.java)
    getRuleNamesMethod = parserClass.getMethod("getRuleNames")
  }

  private fun getTreeViewer(stream: TokenSource, ruleName: String): TreeViewer {
    val parser = parserConstructor.newInstance(CommonTokenStream(stream))
    val ruleMethod = parserClass.getMethod(ruleName)
    val parseTree = ruleMethod.invoke(parser) as ParseTree
    return TreeViewer((getRuleNamesMethod.invoke(parser) as Array<String>).toList(), parseTree).also { it.scale = 1.5 }
  }

  private fun getLexerForSource(source: String?) = lexerConstructor.newInstance(CharStreams.fromString(source)) as Lexer

  @Suppress("UNCHECKED_CAST")
  fun getRuleNames(): Array<String> {
    val parser = parserConstructor.newInstance(CommonTokenStream(getLexerForSource("")))
    return getRuleNamesMethod.invoke(parser) as Array<String>
  }

  fun loadContentInto(destination: CodeArea, anchor: Stage, source: FileChooser) {
    Platform.runLater {
      val file = source.showOpenDialog(anchor)
      if (file != null) {
        getContent(file).map { destination.replaceText(it) }
      }
    }
  }

  fun parse(input: String?, selectedRule: String, parent: StackPane, error: CodeArea, scale: JFXSlider): Timer {
    val lexer = getLexerForSource(input).also { it.removeErrorListeners() }
    val parser = parserConstructor.newInstance(CommonTokenStream(lexer))
    val ruleMethod = parserClass.getMethod(selectedRule)
    val listener = redirectError(parser as Parser)
    val parseTree = ruleMethod.invoke(parser) as ParseTree
    val viewer = TreeViewer((getRuleNamesMethod.invoke(parser) as Array<String>).toList(), parseTree).also { it.scale = 1.5 }
    val panel = JScrollPane(viewer, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED)
    val swingNode = SwingNode()
    SwingUtilities.invokeLater { swingNode.content = panel }
    val timer = Timer(250) { swingNode.content.repaint() }.also { it.start() }
    Platform.runLater {
      parent.children += swingNode
      parent.widthProperty().addListener(repaintAction(parent, panel))
      parent.heightProperty().addListener(repaintAction(parent, panel))
      scale.valueProperty().unbind()
      scale.valueProperty().addListener { _, _, newValue -> viewer.scale = newValue.toDouble() }
      error.clear()
      error.replaceText(listener.stack.toString())
    }
    return timer
  }

  companion object {

    private fun repaintAction(parent: StackPane, child: JScrollPane): (observable: ObservableValue<out Number>, oldValue: Number, newValue: Number) -> Unit {
      return { _, _, _ ->
        val newWidth = parent.width.toInt()
        val newHeight = parent.height.toInt()
        SwingUtilities.invokeLater { child.setSize(newWidth, newHeight) }
      }
    }

    private fun redirectError(parser: Parser): UnderlineErrorListener {
      parser.removeErrorListeners()
      val listener = UnderlineErrorListener()
      parser.addErrorListener(listener)
      return listener
    }
  }
}