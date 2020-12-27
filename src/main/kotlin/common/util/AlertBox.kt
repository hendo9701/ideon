package common.util

import com.jfoenix.controls.JFXAlert
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialogLayout
import com.jfoenix.controls.JFXTextField
import common.util.Level.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import tornadofx.*
import java.util.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName as FAName

/**
 *@author Hayder
 */

/**
 * Shows a notification according to Level.SEVERE color
 *
 * @param stage      The anchor for this notification
 * @param headerText The header text for this notification
 * @param bodyText   The body text for this notification
 */
fun showError(stage: Stage, headerText: String, bodyText: String) {
    showSingle(stage, SEVERE, headerText, bodyText)
}

/**
 * Shows a notification according to Level.INFO color
 *
 * @param stage      The anchor for this notification
 * @param headerText The header text for this notification
 * @param bodyText   The body text for this notification
 */
fun notify(stage: Stage, headerText: String, bodyText: String) {
    showSingle(stage, INFO, headerText, bodyText)
}

/**
 * Shows a single content notification (Used by showError and notify methods)
 *
 * @param stage      The anchor for this notification
 * @param level      The color for this notification according to Level.`level`
 * @param headerText The header text for this notification
 * @param bodyText   The body text for this notification
 */
private fun showSingle(stage: Stage, level: Level, headerText: String, bodyText: String) {
    val dismissButton = buildButton(FAName.MINUS, false)
    val alert = getTemplate(
            level,
            stage,
            headerText,
            listOf(Label(bodyText).apply { isWrapText = true }),
            listOf(dismissButton)
    )
    dismissButton.setOnAction { alert.hideWithAnimation() }
    alert.showAndWait()
}

/**
 * Show a single field form
 *
 * @param stage           The anchor for this notification
 * @param header          The header text for this notification
 * @param validationRegex The regex for the field validation
 * @param onFailure       Error text shown when written text does not match validationRegex
 * @param suggestion      Tip shown when written text does not match validationRegex
 * @return The String representation for the requested field
 */
@Suppress("UNCHECKED_CAST")
fun request(
        stage: Stage,
        header: String,
        validationRegex: Regex,
        onFailure: String,
        suggestion: String
): Optional<String> {
    val field = JFXTextField().apply {
        styleClass += Tag.FIELD.toString()
        textProperty().addListener { _, _, _ ->
            styleClass.removeAll(Tag.WRONG.toString(), Tag.RIGHT.toString())
            styleClass += if (!text.matches(validationRegex))
                Tag.WRONG.toString()
            else
                Tag.RIGHT.toString()
        }
    }

    val accept = buildButton(FAName.CHECK, isDefault = true)
    val cancel = buildButton(FAName.CLOSE, isDefault = false)

    val alert = getTemplate(DEFAULT, stage, header, listOf(field), listOf(accept, cancel))

    accept.setOnAction {
        if (!field.text.matches(validationRegex)) {
            showError(stage, onFailure, suggestion)
            field.requestFocus()
        } else {
            alert.result = field.text
            alert.hideWithAnimation()
        }
    }
    cancel.setOnAction {
        alert.result = ""
        alert.hideWithAnimation()
    }

    return alert.showAndWait() as Optional<String>
}

/**
 * Shows a confirmation notification
 *
 * @param stage      The anchor for this notification
 * @param headerText The header text for this notification
 * @param bodyText   The body text for this notification
 * @return Returns true or false depending on selected choice
 */
fun showConfirmation(
        stage: Stage,
        headerText: String,
        bodyText: String
): Boolean {
    val yes = buildButton(FAName.PLUS, isDefault = true)
    val no = buildButton(FAName.MINUS, isDefault = false)

    val alert = getTemplate(
            WARN, stage, headerText,
            listOf(Label(bodyText).apply { isWrapText = true }),
            listOf(yes, no)
    )

    yes.setOnAction { with(alert) { result = true; hideWithAnimation() } }
    no.setOnAction { with(alert) { result = false; hideWithAnimation() } }

    val result = alert.showAndWait()
    return result.isPresent && result.get() as Boolean
}

/**
 * Builds a template used by all the showXXX methods in this class
 *
 * @param level       The level representing the color for this notification
 * @param stage       The anchor for this notification
 * @param headerText  The header text for this notification
 * @param bodyContent The nodes inside the body of this template
 * @param buttons     The button located in the bottom region of this template
 * @return A pre-built alert according to specified parameters
 */
fun getTemplate(
        level: Level,
        stage: Stage,
        headerText: String,
        bodyContent: List<Node>,
        buttons: List<Button>
): JFXAlert<*> = JFXAlert<Any>(stage).apply {
    initModality(Modality.APPLICATION_MODAL)
    isOverlayClose = false
    setContent(JFXDialogLayout().apply {
        addStylesheet(AlertBoxStyle::class)
        styleClass += level.toString()
        setHeading(Label(headerText).apply { this.styleClass += Tag.HEADER.toString() })
        setBody(VBox(*bodyContent.toTypedArray()))
        setActions(*buttons.toTypedArray())
    })
}

/**
 * Builds a button with an icon on demand
 *
 * @param name      The name of the icon for this button
 * @param isDefault Sets the type of the button (yes for default button, no for cancel button)
 * @return The pre-built button1
 */
fun buildButton(name: FAName, isDefault: Boolean) = JFXButton().apply {
    if (isDefault) isDefaultButton = true
    else isCancelButton = true
    graphic = FontAwesomeIcon().apply {
        setIcon(name)
        styleClass += Tag.ACTION.toString()
    }
}


/**
 * Represents the priority of a notification interpreted as a color
 */
enum class Level(private val level: String) {
    /**
     * Red
     */
    SEVERE("severe"),

    /**
     * Green
     */
    INFO("info"),

    /**
     * Grey
     */
    DEFAULT("default"),

    /**
     * Yellow
     */
    WARN("warn");

    override fun toString(): String {
        return level
    }

}

/**
 * Represents a css class inside AlertBox.css file
 */
private enum class Tag(private val kind: String) {
    HEADER("header"),
    ACTION("action"),
    FIELD("field"),
    RIGHT("right"),
    WRONG("wrong");

    override fun toString(): String {
        return kind
    }

}