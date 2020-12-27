package view

import common.*
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class EditorStyle : Stylesheet() {

  init {
    toolBar {
      backgroundColor += DEFAULT_SECONDARY_COLOR
      effect = DropShadow()
      select(button) {
        borderColor += box(DEFAULT_SECONDARY_COLOR)
        borderInsets += box(0.5.px)
        borderWidth += box(1.5.px)
        and(hover) {
          borderColor += box(DEFAULT_VIVID_COLOR)
          borderRadius += box(5.px)
        }
      }

    }
    HAMBURGER {
      select("StackPane") {
        backgroundColor += DEFAULT_VIVID_COLOR
      }
    }
    RIGHT_PANE {
      backgroundColor += DEFAULT_VIVID_COLOR
      select(button) {
        textFill = DEFAULT_SECONDARY_COLOR
        fontSize = DEFAULT_FONT_SIZE
        fontFamily = DEFAULT_FONT_FAMILY
        fontWeight = FontWeight.BOLD
        and(hover) {
          backgroundColor += DEFAULT_OPAQUE_COLOR
        }
      }
    }

    BOTTOM_PANE {
      tabHeaderBackground {
        backgroundColor += DEFAULT_VIVID_COLOR
      }
      tabLabel {
        fontWeight = FontWeight.BOLD
        fontSize = 20.px
      }
      tabHeaderArea {
        jfxRippler {
          jfxRipplerFill.value = DEFAULT_SECONDARY_COLOR
        }
      }
      tabSelectedLine {
        prefHeight = 3.px
        borderColor += box(DEFAULT_SECONDARY_COLOR)
      }
    }
    EDITOR_TABS {
      tabHeaderBackground {
        backgroundColor += DEFAULT_VIVID_COLOR
      }
      tabHeaderArea {
        jfxRippler {
          jfxRipplerFill.value = DEFAULT_SECONDARY_COLOR
        }
      }
      tabSelectedLine {
        prefHeight = 3.px
        borderColor += box(DEFAULT_SECONDARY_COLOR)
      }
      tab {
        tabLabel {
          fontWeight = FontWeight.BOLD
          fontSize = 14.px
          fontFamily = DEFAULT_FONT_FAMILY
        }
      }
    }

    PROJECT_TREE {
      backgroundColor += DEFAULT_SECONDARY_COLOR
      treeCell {
        backgroundColor += DEFAULT_SECONDARY_COLOR
        fontWeight = FontWeight.BOLD
        fontFamily = DEFAULT_FONT_FAMILY
        fontSize = DEFAULT_FONT_SIZE * (0.80)
        textFill = DEFAULT_VIVID_COLOR
        and(selected) {
          borderInsets += box(0.5.px)
          borderColor += box(DEFAULT_VIVID_COLOR)

        }
        and(hover) {
          backgroundColor += MICROSOFT_GREY_LIGHT
        }
      }

    }

    PROJECT_TREE_LABEL {
      backgroundColor += DEFAULT_VIVID_COLOR
      fontSize = DEFAULT_FONT_SIZE
      textFill = DEFAULT_SECONDARY_COLOR
      fontFamily = DEFAULT_FONT_FAMILY
      fontWeight = FontWeight.BOLD
    }

    PROGRESS_BAR {
      select(track) {
        backgroundInsets += box(0.px)
        backgroundRadius += box(0.px)
      }
      select(bar) {
        backgroundColor += DEFAULT_VIVID_COLOR
      }
    }

    STATUS_LABEL {
      fontFamily = DEFAULT_FONT_FAMILY
      fontSize = DEFAULT_FONT_SIZE * 0.90
      textFill = DEFAULT_VIVID_COLOR
    }
  }

  companion object {

    val MICROSOFT_GREY_LIGHT = c("#C6C6C6")
    val HAMBURGER by cssclass()
    val RIGHT_PANE by cssclass()
    val BOTTOM_PANE by cssclass()
    val EDITOR_TABS by cssclass()
    val PROJECT_TREE by cssclass()
    val PROJECT_TREE_LABEL by cssclass()
    val PROGRESS_BAR by cssclass()
    val STATUS_LABEL by cssclass()
    private val jfxRipplerFill by cssproperty<Color>("-jfx-rippler-fill")
    private val jfxRippler by cssclass()
    private val tabSelectedLine by cssclass()
  }

}
