package view

import com.jfoenix.controls.JFXButton
import common.util.WINDOWS_VALID_FILE_PATH_REGEX
import common.util.WINDOWS_VALID_FOLDER_NAME_REGEX
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName
import javafx.geometry.NodeOrientation
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import model.SimplifiedProjectScope
import tornadofx.*

/**
 * @author Hayder
 */

class ProjectAssistantView : Fragment("Project assistant") {
  override val scope = super.scope as SimplifiedProjectScope
  private val projectModel = scope.model

  override val root = borderpane {
    addStylesheet(DefaultFormStyle::class)
    prefWidth = 500.0
    top {
      anchorpane {
        prefHeight = 80.0
        useMaxWidth = true
        addClass(DefaultFormStyle.BANNER)
        this += FontAwesomeIcon().apply {
          anchorpaneConstraints {
            topAnchor = 20
            leftAnchor = 20
          }
          setIcon(FontAwesomeIconName.ALIGN_JUSTIFY)
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
        fieldset("Project name:") {
          field {
            textfield(projectModel.name) {
              required()
              validator {
                if (it.isNullOrEmpty() || !it.matches(WINDOWS_VALID_FOLDER_NAME_REGEX)) error(nameTooltipText)
                else null
              }
              tooltip(nameTooltipText)
              promptText = "Sample"
            }
          }
        }
        fieldset("Project location:") {
          field {
            textfield(projectModel.location) {
              promptText = """C:\Users\User\Projects"""
              tooltip(locationTooltipText)
              validator {
                if (it.isNullOrEmpty() || !it.matches(WINDOWS_VALID_FILE_PATH_REGEX)) error(locationTooltipText)
                else null
              }
            }
            this += JFXButton("Open").apply {
              action {
                val dir = (DirectoryChooser()).showDialog(primaryStage)
                if (dir != null) {
                  projectModel.location.value = dir.absolutePath
                }
              }
            }
          }
        }
        fieldset {
          buttonbar {
            nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
            buttons += JFXButton("Cancel").apply {
              isCancelButton = true
              action {
                projectModel.cancelled.value = true
                close()
              }
            }
            buttons += JFXButton("Accept").apply {
              isDefaultButton = true
              enableWhen { projectModel.valid }
              action { close() }
            }
          }
        }
      }
    }
  }

  companion object {
    val nameTooltipText = """
      This field must contain a valid folder name under your
      operating system
    """.trimIndent()
    val locationTooltipText = """
      This field must contain a valid file path to a folder
      under your operating system.
    """.trimIndent()
  }
}
