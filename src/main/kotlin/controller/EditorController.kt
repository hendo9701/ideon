package controller

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import common.antlr.AntlrOption
import common.excepts.MissingMainGrammarException
import common.task.DirectoryWatcherTask
import common.tree.TreeEvent
import common.tree.TreeEventType.*
import common.util.*
import common.util.Asset.LINE_INDICATOR
import common.util.Asset.LINE_NUMBER
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TreeView
import javafx.scene.layout.StackPane
import model.Project
import org.apache.commons.io.IOUtils
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import tornadofx.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.Future

class EditorController : Controller() {

  val isCurrentProjectAbsent = SimpleBooleanProperty(true)
  private val executor = Executors.newFixedThreadPool(NUM_THREADS)
  private val eventQueue: BlockingQueue<TreeEvent> = ArrayBlockingQueue(NUM_EVENTS)
  private val editing: BiMap<File, Tab> = HashBiMap.create()
  lateinit var currentProject: Project
  lateinit var editorTabs: TabPane
  var directoryWatcherTask: DirectoryWatcherTask? = null
  var eventQueueListenerTask: Future<*>? = null

  @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  fun createProject(name: String, location: String, treeView: TreeView<File>) {
    currentProject = Project.newProject(name, File(location))
    directoryWatcherTask?.cancel(true)
    directoryWatcherTask = DirectoryWatcherTask(
        currentProject.root.absolutePath,
        treeView,
        eventQueue
    )
    executor.submit(directoryWatcherTask)

    isCurrentProjectAbsent.value = false
    eventQueueListenerTask?.cancel(true)
    eventQueueListenerTask = executor.submit {
      while (true) {
        val event = eventQueue.take()
        when (event.type) {
          OPEN, CREATE -> {
            openTab(event.source)
          }
          DELETE -> closeTab(event.source)
        }
      }
    }
  }

  fun openProject(location: String, treeView: TreeView<File>) {
    currentProject = Project.loadProject(location)
    isCurrentProjectAbsent.value = false
    directoryWatcherTask?.cancel(true)
    eventQueueListenerTask?.cancel(true)
    directoryWatcherTask = DirectoryWatcherTask(
        currentProject.root.absolutePath,
        treeView,
        eventQueue
    )
    executor.submit(directoryWatcherTask)
    eventQueueListenerTask = executor.submit {
      while (true) {
        val event = eventQueue.take()
        when (event.type) {
          OPEN, CREATE -> {
            openTab(event.source)
          }
          DELETE -> closeTab(event.source)
        }
      }
    }
  }

  private fun closeTab(file: File) {
    if (editing.containsKey(file)) {
      Platform.runLater { editorTabs.tabs.removeAll(editing[file]) }
    }
  }

  private fun openTab(source: File) {
    if (editing.containsKey(source)) {
      editorTabs.selectionModel.select(editing[source])
    } else {
      Platform.runLater {
        editorTabs.tabs += Tab(
            source.name,
            StackPane(VirtualizedScrollPane(inflate(source)))
        ).apply {
          editing += source to this
          setOnCloseRequest {
            val codeArea =
                ((((this.content as StackPane).children.first()) as VirtualizedScrollPane<*>).getContent()) as CodeArea
            val textInTab = codeArea.text
            val textInFile = getContent(source).orElseGet { "" }
            if (textInTab.length != textInFile.length || textInFile != textInTab) {
              val commitQ = showConfirmation(
                  stage = primaryStage,
                  headerText = "Warning",
                  bodyText = "You have some unsaved changes in this tab. Commit changes?"
              )
              if (commitQ) commit(source)
            }
            editorTabs.tabs.remove(this)
            editing -= source
          }
        }
      }
    }
  }

  private fun commit(source: File) {
    val content =
        (((editing[source]!!.content as StackPane).children.first() as VirtualizedScrollPane<*>).getContent() as CodeArea).text
    if (!setContent(content, source)) {
      showError(primaryStage, "Operation failed", "Something went wrong with: $source")
    }

  }

  private fun inflate(source: File) = CodeArea().apply {
    addAssetsToLine(this, LINE_NUMBER, LINE_INDICATOR)
    ResourceProvider.getLanguageConcept(source.extension).map { concept ->
      ResourceProvider.getLanguageDefinition(source.extension).map { definition ->
        this.stylesheets += File(concept).toURI().toURL().toExternalForm()
        addTextHighlighting(this, executor, definition)
        addAutoIndentation(this)
      }
    }
    getContent(source).map { replaceText(0, 0, it) }
  }

  fun mkTemp(): File = File(currentProject.root, "tmp").also { it.mkdir() }

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  fun isAnyGrammarDefined(): Boolean {
    val src = currentProject.src
    return src.listFiles().any { it.absolutePath.endsWith(".g4") }

  }

  fun createGrammar(name: String) {
    val location = currentProject.src
    val grammarFile = File(location, "$name.g4")
    grammarFile.createNewFile()
    currentProject.mainGrammar = grammarFile
  }

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  fun compileRecognizer(
      tracker: CodeArea,
      src: File = currentProject.src,
      gen: File = currentProject.gen,
      out: File = currentProject.out
  ): Int {
    val report = StringBuilder()
    notifyStart(report)
    val filesInSrc =
        src.listFiles().filter { it.extension == "java" }.map { "\"${it.absolutePath}\"" }.toTypedArray()
    val filesInGen =
        gen.listFiles().filter { it.extension == "java" }.map { "\"${it.absolutePath}\"" }.toTypedArray()
    LOG.debug(filesInSrc.joinToString(separator = " "))
    LOG.debug(filesInGen.joinToString(separator = " "))
    val command = listOf(
        JAVAC,
        "-cp",
        """"${ANTLR_PATH};${currentProject.gen.absolutePath};${currentProject.src.absolutePath}"""",
        *filesInGen,
        *filesInSrc,
        "-d",
        "$out"
    )

    val compiler = ProcessBuilder(command).start()
    notifyEnd(compiler, report)
    Platform.runLater { tracker.replaceText(report.toString()) }
    return compiler.exitValue()
  }

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  @Throws(MissingMainGrammarException::class)
  fun compileGrammar(tracker: CodeArea, out: File = currentProject.gen): Int {
    val grammarFile = currentProject.mainGrammar
    grammarFile ?: throw MissingMainGrammarException(currentProject.name)
    currentProject.gen.listFiles().forEach { it.deleteRecursively() }
    val report = StringBuilder()
    notifyStart(report)
    val options = filterArgumentsFromMeta()
    LOG.debug("Command options are: " + options.get().joinToString(separator = " "))
    val command = listOf(
        JAVA,
        "-cp",
        CLASS_PATH,
        "-jar",
        ANTLR_PATH,
        grammarFile.absolutePath,
        "-o",
        "$out",
        *options.get()
    )

    val compiler = ProcessBuilder(command).start()
    notifyEnd(compiler, report)
    Platform.runLater { tracker.replaceText(report.toString()) }
    return compiler.exitValue()
  }

  @Throws(IllegalArgumentException::class)
  private fun filterArgumentsFromMeta(): Optional<Array<String>> =
      readFromJson(currentProject.cfg.absolutePath).map { filterArguments(it) }


  fun close() {
    executor.shutdownNow()
  }

  companion object {
    private const val NUM_THREADS = 3
    private const val NUM_EVENTS = 10
    private const val PROMPT = ">>>"
    private val GLOBAL_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    private val JAVA = "${System.getProperty("java.home")}${File.separator}bin${File.separator}java"
    private val JAVAC = "${System.getProperty("java.home")}${File.separator}bin${File.separator}javac"
    private val JAR = "${System.getProperty("java.home")}${File.separator}bin${File.separator}jar"
    private val ANTLR_PATH = System.getenv("ANTLR_HOME")
    private val CLASS_PATH: String = System.getProperty("java.class.path")
    private val LOG by logger {}

    @Throws(IllegalArgumentException::class)
    fun filterArguments(data: Map<String, Any>?): Array<String> {
      return data?.flatMap { entry ->
        AntlrOption.values().filter { it.rep == entry.key }.map { AntlrOption.encode(it, entry.value) }
      }?.toTypedArray()
          ?: emptyArray()
    }

    private fun notifyEnd(compiler: Process, report: StringBuilder) {
      val stderr = IOUtils.toString(compiler.errorStream, StandardCharsets.UTF_8)
      compiler.waitFor()
      Platform.runLater {
        if (compiler.exitValue() != 0) {
          stderr.lines().forEach { report.append("\t$it\n") }
        }
        report.append(
            """
          $PROMPT Compilation finished with exit code: ${compiler.exitValue()} at ${GLOBAL_FORMAT.format(Date())}
          
        """.trimIndent()
        )
      }
    }

    private fun notifyStart(report: StringBuilder) {
      report.append(
          """
          $PROMPT Compilation started at: ${GLOBAL_FORMAT.format(Date())}
          
        """.trimIndent()
      )
    }
  }

}