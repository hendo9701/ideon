package view


import common.*
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class DefaultFormStyle : Stylesheet() {

  init {
    s(label, textField, button) {
      fontFamily = DEFAULT_FONT_FAMILY
    }
    s(label) {
      fontSize = DEFAULT_FONT_SIZE * 0.90
    }
    s(button, textField) {
      fontSize = DEFAULT_FONT_SIZE * 0.80
    }
    button {
      backgroundColor += DEFAULT_VIVID_COLOR
      fontWeight = FontWeight.BOLD
      textFill = DEFAULT_SECONDARY_COLOR
      borderColor += box(DEFAULT_VIVID_COLOR)
      and(hover) {
        backgroundColor += DEFAULT_SECONDARY_COLOR
        textFill = DEFAULT_VIVID_COLOR
      }
    }
    BANNER {
      backgroundColor += DEFAULT_VIVID_COLOR
    }

    checkBox {
      fontSize = DEFAULT_FONT_SIZE * 0.70
      checkedColor.value = DEFAULT_VIVID_COLOR
    }

    comboBox {
      select(cell) {
        backgroundColor += DEFAULT_SECONDARY_COLOR
        fontWeight = FontWeight.BOLD
        fontFamily = DEFAULT_FONT_FAMILY
        fontSize = DEFAULT_FONT_SIZE * 0.70
        and(selected) {
          backgroundColor += DEFAULT_VIVID_COLOR
        }
        and(hover) {
          textFill = DEFAULT_OPAQUE_COLOR
        }
      }
    }
  }

  companion object {
    val BANNER by cssclass()
    val checkedColor by cssproperty<Color>("-jfx-checked-color")
  }
}