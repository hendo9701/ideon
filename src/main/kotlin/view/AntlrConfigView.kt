package view

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXComboBox
import common.antlr.AntlrOption
import common.util.JAVA_VALID_PACKAGE_NAME_REGEX
import common.util.WINDOWS_VALID_FOLDER_NAME_REGEX
import common.util.readFromJson
import common.util.writeToJson
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.NodeOrientation
import javafx.scene.paint.Color
import model.Project
import model.ProjectDefaults
import tornadofx.*
import view.DefaultFormStyle.Companion.BANNER
import java.io.File

class AntlrConfigView : Fragment("ANTLR Settings") {

    val project: Project by param()

    private val _configurationModel = ViewModel()
    private val _location: StringProperty
    private val _package: StringProperty
    private var _listener: BooleanProperty
    private var _visitor: BooleanProperty
    private var _selectedGrammar: StringProperty

    init {
        _location = _configurationModel.bind { SimpleStringProperty() }
        _package = _configurationModel.bind { SimpleStringProperty() }
        _listener = _configurationModel.bind { SimpleBooleanProperty() }
        _visitor = _configurationModel.bind { SimpleBooleanProperty() }
        _selectedGrammar = _configurationModel.bind { SimpleStringProperty() }
    }

    override val root = borderpane {
        addStylesheet(DefaultFormStyle::class)
        top {
            anchorpane {
                prefHeight = 80.0
                useMaxWidth = true
                addClass(BANNER)
                this += FontAwesomeIcon().apply {
                    anchorpaneConstraints {
                        topAnchor = 20
                        leftAnchor = 20
                    }
                    setIcon(FontAwesomeIconName.SLIDERS)
                    style {
                        fill = Color.WHITE
                        fontSize = 2.5.em
                        fontFamily = "FontAwesome"
                    }
                }
            }
        }
        center {
            form {
                prefWidth = WIDTH; minWidth = WIDTH
                prefHeight = HEIGHT; minHeight = HEIGHT
                fieldset("Location of imported grammars:") {
                    field {
                        textfield(_location) {
                            promptText = "(Optional)"
                            validator {
                                if (it.isNullOrEmpty() || it.matches(WINDOWS_VALID_FOLDER_NAME_REGEX)) null
                                else error("Invalid folder name")
                            }
                        }
                        this += JFXButton("Open")
                    }
                }
                fieldset("Package for generated code:") {
                    field {
                        textfield(_package) {
                            promptText = "(Optional)"
                            validator {
                                if (it.isNullOrEmpty() || it.matches(JAVA_VALID_PACKAGE_NAME_REGEX)) null
                                else error("Invalid java package name")
                            }
                        }
                    }
                }
                fieldset("Main grammar:") {
                    field {
                        val jsonData = readFromJson(project.cfg.absolutePath).get()
                        val srcFile = File(jsonData[ProjectDefaults.SRC.toString()] as String)

                        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        val grammars = srcFile.listFiles().filter { it.absolutePath.endsWith(".g4") }.map { it.name }.toObservable()
                        this += JFXComboBox(grammars).apply {
                            this.selectionModel.select(project.mainGrammar!!.name)
                            _selectedGrammar.set(this.selectedItem)
                            this.selectionModelProperty().addListener { _, _, newValue ->
                                _selectedGrammar.set(newValue.selectedItem)
                            }
                        }
                    }
                }
                fieldset("Others:") {
                    field {
                        this += JFXCheckBox("Generate parse tree listener").apply {
                            this.selectedProperty().addListener { _, _, newValue ->
                                _listener.set(newValue)
                            }
                        }
                        this += JFXCheckBox("Generate parse tree visitor").apply {
                            this.selectedProperty().addListener { _, _, newValue ->
                                _visitor.set(newValue)
                            }
                        }
                    }
                }
                buttonbar {
                    nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
                    buttons += JFXButton("Cancel").apply {
                        isCancelButton = true
                        action { close() }
                    }
                    buttons += JFXButton("Accept").apply {
                        isDefaultButton = true
                        enableWhen { _configurationModel.valid }
                        action {
                            val configs = mutableMapOf<String, Any>()
                            if (!_location.value.isNullOrEmpty()) {
                                configs += AntlrOption.IMPORTED_GRAMMARS_LOCATION.rep to _location.value
                            }
                            if (!_package.value.isNullOrEmpty()) {
                                configs += AntlrOption.PACKAGE_NAME.rep to _package.value
                            }
                            configs += AntlrOption.VISITOR.rep to _visitor.value
                            configs += AntlrOption.LISTENER.rep to _listener.value
                            project.mainGrammar = File(project.src, _selectedGrammar.value)
                            writeToJson(project.cfg.absolutePath, configs)
                            close()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val HEIGHT = 360.0
        const val WIDTH = 425.0
    }
}