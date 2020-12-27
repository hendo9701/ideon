package common.tree

import common.tree.TreeEventType.CREATE
import common.tree.TreeEventType.DELETE
import common.util.logger
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.control.TreeCell
import javafx.scene.input.KeyCode
import tornadofx.*
import java.io.File
import java.util.concurrent.BlockingQueue

class ContextMenuTreeCellFactory(private val eventQueue: BlockingQueue<TreeEvent>) : TreeCell<File>() {

  private var name: TextField? = null
  private var fileMenu = ContextMenu()
  private var folderMenu = ContextMenu()

  init {
    with(fileMenu) {
      items += MenuItem(OPEN).apply {
        action { eventQueue += TreeEvent(TreeEventType.OPEN, treeItem.value) }
      }
      items += MenuItem(REMOVE).apply {
        action {
          val child = treeItem
          val parent = child.parent
          if (parent != null) {
            child.value.delete()
            eventQueue += TreeEvent(DELETE, child.value)
          }
        }
      }
    }
    with(folderMenu) {
      items += MenuItem(NEW_FILE).apply {
        action {
          val parent = treeItem
          val parentPath = parent.value.absolutePath
          var counter = 1
          var childFile = File("$parentPath${File.separator}$DEFAULT_NAME.txt")

          val result = kotlin.runCatching {
            while (!childFile.createNewFile()) {
              childFile = File("$parentPath${File.separator}$DEFAULT_NAME$counter.txt")
              counter++
            }
          }
          when {
            result.isSuccess -> {
              eventQueue += TreeEvent(CREATE, childFile)
              LOG.debug("File: $childFile was created successfully.")
            }
            result.isFailure -> LOG.debug("Failed at creating $childFile. Exception: ${result.exceptionOrNull()}")
          }
        }
      }

      items += MenuItem(NEW_FOLDER).apply {
        action {
          val parentPath = treeItem.value.absolutePath
          var counter = 1
          var childFile = File("$parentPath${File.separator}$DEFAULT_NAME")
          while (!childFile.mkdir()) {
            childFile = File("$parentPath${File.separator}$DEFAULT_NAME$counter")
            counter++
          }
        }
      }
      items += MenuItem(REMOVE).apply {
        action {
          val child = treeItem
          val parent = child.parent
          if (parent != null) {
            child?.value?.deleteRecursively()
          }
        }
      }
    }
  }

  override fun startEdit() {
    super.startEdit()
    name ?: createTextField()
    text = null
    graphic = name
    name?.selectAll()
  }

  override fun cancelEdit() {
    Platform.runLater { super.cancelEdit() }
    if (item != null) {
      text = item.name
      graphic = treeItem.graphic
    }
  }

  override fun updateItem(item: File?, empty: Boolean) {
    super.updateItem(item, empty)
    if (empty) {
      text = null
      graphic = null
    } else when {
      isEditing -> {
        name?.text = getString()
        text = null
        graphic = name
      }
      else -> {
        text = getString()
        graphic = treeItem.graphic
        contextMenu = if (treeItem.value.isDirectory) folderMenu else fileMenu
      }
    }
  }

  private fun getString() = item?.name ?: ""

  private fun createTextField() {
    name = TextField(getString())
    name?.onKeyReleased = EventHandler {
      when (it?.code) {
        KeyCode.ENTER -> {
          val oldFile = item
          oldFile.absolutePath.lastIndexOf(File.separator)
          val newFile = File("${oldFile.parent}${File.separator}${name?.text}")
          oldFile.renameTo(newFile)
          val result = kotlin.runCatching {
            eventQueue += TreeEvent(DELETE, oldFile)
            eventQueue += TreeEvent(CREATE, newFile)
          }
          when {
            result.isSuccess -> LOG.debug("File '$oldFile' renamed to '$newFile'")
            result.isFailure -> LOG.debug("Failed at rename file '$oldFile' due to ${result.exceptionOrNull()}")
          }
          commitEdit(newFile)
        }
        KeyCode.ESCAPE -> cancelEdit()
        else -> it.consume()
      }
    }
  }

  companion object {
    const val DEFAULT_NAME = "untitled"
    const val NEW_FILE = "New file"
    const val NEW_FOLDER = "New folder"
    const val OPEN = "Open"
    const val REMOVE = "Remove"
    private val LOG by logger { }
  }

}