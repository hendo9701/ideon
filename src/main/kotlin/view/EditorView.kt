package view

import com.jfoenix.controls.*
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition
import com.jfoenix.transitions.hamburger.HamburgerNextArrowBasicTransition
import common.DEFAULT_FONT_SIZE
import common.DEFAULT_SECONDARY_COLOR
import common.DEFAULT_VIVID_COLOR
import common.excepts.MissingMainGrammarException
import common.util.JAVA_VALID_ID_REGEX
import common.util.notify
import common.util.request
import common.util.showError
import controller.EditorController
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.DirectoryChooser
import javafx.stage.StageStyle
import model.SimplifiedProjectScope
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import tornadofx.*
import java.io.File
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon as FAIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName as FAName

class EditorView : View("Editor") {

  init {
    setStageIcon(Image(javaClass.getResourceAsStream("/icon/DINO.PNG")))
    primaryStage.setOnCloseRequest {
      controller.close()
    }
  }

  private val drawerStack by lazy {
    JFXDrawersStack().apply {
      vboxConstraints { vgrow = Priority.ALWAYS }
      prefWidth = 800.0
      prefHeight = 500.0
    }
  }

  private val editorTabs: JFXTabPane = JFXTabPane()

  init {
    editorTabs.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
    editorTabs.addClass(EditorStyle.EDITOR_TABS)
    drawerStack.content = editorTabs
  }

  private val controller: EditorController by inject()

  init {
    controller.editorTabs = editorTabs
  }

  private val projectTree by lazy {
    JFXTreeView<File>().apply {
      isEditable = true
      isShowRoot = true
      addClass(EditorStyle.PROJECT_TREE)
    }
  }

  private val status: TaskStatus by inject()
  private val statusLabel = label {
    addClass(EditorStyle.STATUS_LABEL)
    visibleWhen { status.running }
  }

  private val progressBar by lazy {
    JFXProgressBar().apply {
      prefWidth = 80.0
      addClass(EditorStyle.PROGRESS_BAR)
      visibleWhen { status.running }
      progressProperty().bind(status.progress)
    }
  }

  private val leftPane by lazy {
    JFXDrawer().apply {
      direction = JFXDrawer.DrawerDirection.LEFT
      isResizeContent = true
      isOverLayVisible = false
      isResizableOnDrag = true
      defaultDrawerSize = DRAWER_SIZE
      sidePane += vbox {
        label("Project") {
          addClass(EditorStyle.PROJECT_TREE_LABEL)
          fitToWidth(this@vbox)
          prefHeight = 30.0
          alignment = Pos.CENTER
        }
        this += projectTree
        projectTree.apply { vboxConstraints { vGrow = Priority.ALWAYS } }
      }
    }
  }

  private val rightPane by lazy {
    JFXDrawer().apply {
      direction = JFXDrawer.DrawerDirection.RIGHT
      isResizeContent = false
      isOverLayVisible = false
      isResizableOnDrag = true
      defaultDrawerSize = DRAWER_SIZE - 50
      sidePane += vbox {
        addClass(EditorStyle.RIGHT_PANE)
        spacing = 0.0
        children += ButtonFactory.create("New Project", callback = {
          val editScope = SimplifiedProjectScope()
          find(ProjectAssistantView::class, editScope).openModal(block = true, stageStyle = StageStyle.UTILITY)
          if (editScope.model.isValid && !editScope.model.cancelled.value) {
            runAsync(status = this@EditorView.status) {
              Platform.runLater { statusLabel.text = "Creating..." }
              with(editScope.model) {
                controller.createProject(name.value, location.value, projectTree)
              }
            }.setOnSucceeded {
              drawerStack.toggle(leftPane, true)
            }
          }
        })
        children += ButtonFactory.create("Open Project", callback = {
          val location = (DirectoryChooser()).showDialog(primaryStage)
          if (location != null) {
            runAsync(status = this@EditorView.status) {
              Platform.runLater { statusLabel.text = "Opening..." }
              controller.openProject(location.absolutePath, projectTree)
            }.setOnSucceeded { drawerStack.toggle(leftPane, true) }
          }
        })
        children += ButtonFactory.create("New Program")
        children += ButtonFactory.create("Save")
        children += ButtonFactory.create("Save All")
        children += ButtonFactory.create("Exit")
      }
    }
  }

  private val taskReport = CodeArea().apply {
    isWrapText = true
    isEditable = false
    style {
      fontSize = DEFAULT_FONT_SIZE * 0.80
      fontFamily = "Consolas"
      fontWeight = FontWeight.BOLD
      backgroundColor += DEFAULT_SECONDARY_COLOR
    }
  }

  private val bottomPane by lazy {
    JFXDrawer().apply {
      sidePane += JFXTabPane().apply {
        addClass(EditorStyle.BOTTOM_PANE)
        side = Side.LEFT
        tabs += makePrettyTab(FAName.TASKS, VirtualizedScrollPane(taskReport), helpMsg = "Compiler output")
        tabs += makePrettyTab(FAName.KEYBOARD_ALT, helpMsg = "Terminal")
//        tabs += makePrettyTab(FAName.EYE, helpMsg = "Watches")
      }
      defaultDrawerSize = DRAWER_SIZE
      direction = JFXDrawer.DrawerDirection.BOTTOM
      isResizeContent = true
      isOverLayVisible = false
      isResizableOnDrag = true
    }
  }

  override val root = vbox {
    toolbar {
      items += JFXHamburger().apply {
        addClass(EditorStyle.HAMBURGER)
        val transition = HamburgerBackArrowBasicTransition(this)
            .apply { rate = -1.0 }
        addEventHandler(MouseEvent.MOUSE_PRESSED) {
          transition.rate *= -1
          transition.play()
          drawerStack.toggle(leftPane)
        }
      }
      pane { prefWidth = 40.0; prefHeight = 33.0 }
      items += ButtonFactory.createWithIcon(FAName.CUBE, tooltip = "Add grammar", callback = {
        val grammarName = request(
            stage = primaryStage,
            header = "Enter grammar name:",
            validationRegex = JAVA_VALID_ID_REGEX,
            onFailure = "Something went wrong",
            suggestion = "Grammar must follow java naming convention."
        )
        if (grammarName.isPresent && grammarName.get() != "") {
          controller.createGrammar(grammarName.get())
        }
      }).apply { this.disableProperty().bind(controller.isCurrentProjectAbsent) }
      items += ButtonFactory.createWithIcon(FAName.CUBES, tooltip = "Generate recognizer", callback = {
        runAsync(status = this@EditorView.status) {
          Platform.runLater { statusLabel.text = "Compiling..." }
          try {
            if (controller.compileGrammar(taskReport) != 0) {
              Platform.runLater { drawerStack.toggle(bottomPane, true) }
            }
          } catch (e: MissingMainGrammarException) {
            showError(
                primaryStage,
                "Something went wrong",
                "It's seems that you have not define a grammar your project"
            )
          }
        }
      }).apply { this.disableProperty().bind(controller.isCurrentProjectAbsent) }
      items += ButtonFactory.createWithIcon(FAName.GEARS, tooltip = "Compile recognizer", callback = {
        runAsync(status = this@EditorView.status) {
          Platform.runLater { statusLabel.text = "Compiling..." }
          if (controller.compileRecognizer(taskReport) != 0) {
            Platform.runLater { drawerStack.toggle(bottomPane, true) }
          }
        }
      })
          .apply { this.disableProperty().bind(controller.isCurrentProjectAbsent) }
      items += ButtonFactory.createWithIcon(FAName.PLUG, tooltip = "Plug-in recognizer")
          .apply { this.disableProperty().bind(controller.isCurrentProjectAbsent) }
      items += ButtonFactory.createWithIcon(FAName.SLIDERS, tooltip = "ANTLR configuration", callback = {
        if (controller.isAnyGrammarDefined())
          find(AntlrConfigView::class, mapOf(AntlrConfigView::project to controller.currentProject))
              .openModal(block = true, stageStyle = StageStyle.UTILITY)
        else
          notify(primaryStage, "Info", "No main grammars were found for this project")
      }).apply { this.disableProperty().bind(controller.isCurrentProjectAbsent) }
      pane { prefWidth = 40.0; prefHeight = 33.0 }
      items += ButtonFactory.createWithIcon(FAName.PLAY, tooltip = "Run")
      items += ButtonFactory.createWithIcon(FAName.STOP, tooltip = "Stop")
      items += ButtonFactory.createWithIcon(FAName.COG, tooltip = "Compile")
//      items += ButtonFactory.createWithIcon(FAName.BUG, tooltip = "Debug")
      items += ButtonFactory.createWithIcon(FAName.TREE, tooltip = "Show parse tree", callback = {
        val tmp = controller.mkTemp()
        val gen = File(tmp, "gen").also { it.mkdir() }
        var result = true
        runAsync(status = status) {
          Platform.runLater { statusLabel.text = "Preparing stage..." }
          if (controller.compileGrammar(tracker = taskReport, out = gen) == 0
              && controller.compileRecognizer(tracker = taskReport, gen = gen, out = tmp) == 0) {
            gen.deleteRecursively()
            result = true
          } else
            result = false
        }.setOnSucceeded {
          if (result) {
            val grammarName = controller.currentProject.mainGrammar?.nameWithoutExtension
            find(ParseTreeView::class, mapOf(ParseTreeView::grammarName to grammarName,
                ParseTreeView::classesLocation to tmp)).openModal(block = true, stageStyle = StageStyle.UTILITY)
            tmp.deleteRecursively()
          } else {
            showError(stage = primaryStage, headerText = "Something went wrong", bodyText = "Grammar pre-compilation failed")
          }
        }
      })
      items += ButtonFactory.createWithIcon(
          FAName.LIST_ALT,
          tooltip = "Show bottom pane",
          callback = { drawerStack.toggle(bottomPane) })
      items += statusLabel
      items += progressBar
      pane { prefWidth = 40.0; prefHeight = 33.0; hboxConstraints { hgrow = Priority.ALWAYS } }
      items += JFXHamburger().apply {
        addClass(EditorStyle.HAMBURGER)
        val transition = HamburgerNextArrowBasicTransition(this)
            .apply { rate = -1.0 }
        addEventHandler(MouseEvent.MOUSE_PRESSED) {
          transition.rate *= -1
          transition.play()
          drawerStack.toggle(rightPane)
        }
      }
    }
    children += drawerStack
  }

  companion object {

    private const val DRAWER_SIZE = 200.0

    private object ButtonFactory {
      fun create(text: String = "", aligned: Boolean = true, callback: () -> Unit = {}) = JFXButton(text).apply {
        if (aligned) {
          alignment = Pos.BASELINE_LEFT
          useMaxWidth = true
        }
        action(callback)
      }

      fun createWithIcon(
          name: FAName,
          text: String = "",
          tooltip: String = "",
          callback: () -> Unit = {}
      ) =
          JFXButton().apply {
            this.text = text
            this.tooltip = Tooltip(tooltip)
            graphic = FAIcon().apply {
              setIcon(name)
              style {
                fill = DEFAULT_VIVID_COLOR
                fontSize = 1.8.em
                fontFamily = "FontAwesome"
              }
            }
            action(callback)
          }
    }


    private fun makePrettyTab(name: FAName, node: Node? = null, helpMsg: String = "") = Tab().apply {
      graphic = FAIcon().apply {
        setIcon(name)
        style {
          fill = Color.WHITE
          rotate = 90.deg
          fontFamily = "FontAwesome"
        }
      }
      tooltip = Tooltip(helpMsg)
      if (node != null) content = node
    }

  }

}