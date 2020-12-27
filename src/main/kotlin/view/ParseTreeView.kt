package view

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXProgressBar
import com.jfoenix.controls.JFXSlider
import common.util.Asset
import common.util.addAssetsToLine
import controller.ParseTreeController
import javafx.application.Platform
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.control.ComboBox
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import tornadofx.*
import java.io.File
import javax.swing.Timer

class ParseTreeView : Fragment("Parse Tree") {

    private val controller: ParseTreeController by inject()
    val grammarName: String by param()
    val classesLocation: File by param()

    init {
        controller.setGrammarName(grammarName)
        controller.setClassesLocation(classesLocation.absolutePath)
        controller.reflect()
    }

    private val inputArea = CodeArea().also { addAssetsToLine(it, Asset.LINE_NUMBER) }

    private val errorArea = CodeArea().apply { isEditable = false }

    private val scale = JFXSlider(0.0, 5.0, 1.0).apply {
        isShowTickMarks = true
        blockIncrement = 0.1
        majorTickUnit = 0.5
        minorTickCount = 1
    }

    private val treeViewParent = stackpane { }

    private val rules = ComboBox<String>().apply {
        promptText = "Select rule"
        items.addAll(controller.getRuleNames())
    }

    private var timer: Timer? = null
    private var timerClosesWhenStageExists = false
    private val status: TaskStatus by inject()
    private val statusLabel = label {
        addClass(ParseTreeStyle.STATUS_LABEL)
        visibleWhen { status.running }
    }

    private val progressBar by lazy {
        JFXProgressBar().apply {
            prefWidth = 80.0
            progressProperty().bind(status.progress)
            visibleWhen { status.running }
        }
    }

    override val root = splitpane(orientation = HORIZONTAL) {
        prefWidth = 800.0
        prefHeight = 450.0
        addStylesheet(ParseTreeStyle::class)
        vbox {
            toolbar {
                items += JFXButton("Open").apply {
                    action {
                        controller.loadContentInto(destination = inputArea, anchor = primaryStage, source = FILE_CHOOSER)
                    }
                    disableProperty().bind(status.running)
                }
                items += JFXButton("Parse").apply {
                    action {
                        runAsync(status) {
                            Platform.runLater { statusLabel.text = "Parsing..." }
                            if (rules.selectionModel.selectedItem != null) {
                                timer?.stop()
                                timer = controller.parse(
                                        input = inputArea.text,
                                        selectedRule = rules.selectionModel.selectedItem,
                                        parent = treeViewParent,
                                        error = errorArea,
                                        scale = scale
                                )
                                if (!timerClosesWhenStageExists) {
                                    this@splitpane.scene.window.setOnCloseRequest { timer?.stop() }
                                    timerClosesWhenStageExists = true
                                }
                            }
                        }
                    }
                    disableProperty().bind(status.running)
                }
                items += rules
                items += statusLabel
                items += progressBar
            }
            splitpane(orientation = VERTICAL) {
                vboxConstraints { vGrow = Priority.ALWAYS }
                items += VirtualizedScrollPane(inputArea)
                items += VirtualizedScrollPane(errorArea)
            }
        }
        vbox {
            children += treeViewParent.also { it.vgrow = Priority.ALWAYS }
            children += scale
        }
    }

    companion object {
        private val FILE_CHOOSER = FileChooser()
    }
}