package common.task

import common.tree.ContextMenuTreeCellFactory
import common.tree.TreeEvent
import common.util.IconName
import common.util.logger
import common.util.makeIcon
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.BlockingQueue

/**
 * @author Hayder
 */

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DirectoryWatcherTask(
    _root: String,
    private val fileTreeView: TreeView<File>,
    private val eventQueue: BlockingQueue<TreeEvent>
) : Task<Any?>() {
  private val root: TreeItem<File>
  private val map = mutableMapOf<File, TreeItem<File>>()
  private val watcher = FileSystems.getDefault().newWatchService()
  private val keys = mutableMapOf<WatchKey, Path>()

  init {
    File(_root).let {
      root = TreeItem(it, makeIcon(IconName.FOLDER))
      map += it to root
      buildSubtree(root, map)
      Platform.runLater {
        fileTreeView.root = root
        fileTreeView.setCellFactory { ContextMenuTreeCellFactory(eventQueue) }
      }
      registerAll(Paths.get(_root))
    }
  }

  override fun call(): Any? {
    processEvents()
    return null
  }

  @Throws(IOException::class)
  private fun register(dir: Path) {
    keys += dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE) to dir
  }

  private fun registerAll(start: Path?) {
    Files.walkFileTree(start, object : SimpleFileVisitor<Path>() {
      override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
        register(dir)
        return FileVisitResult.CONTINUE
      }
    })
  }

  private fun processEvents() {
    while (true) {
      kotlin.runCatching { watcher.take() }
          .onFailure { LOG.debug("Process interrupted."); return }
          .onSuccess {
            if (it != null && keys[it] != null) {
              for (event in it.pollEvents()) {
                val kind = event.kind()
                if (kind == OVERFLOW)
                  continue
                @Suppress("UNCHECKED_CAST")
                val pathEvent: WatchEvent<Path> = event as WatchEvent<Path>
                val name = pathEvent.context()
                val source = keys[it]?.resolve(name)
                if (kind == ENTRY_CREATE) kotlin.runCatching {
                  if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) registerAll(source)
                }.onFailure { LOG.debug("Failed at registering new directory.") }
                update(kind, source)
              }
              val valid = it.reset()
              if (!valid) {
                keys.remove(it)
                LOG.debug("Removing invalid key.")
                if (keys.isEmpty()) {
                  LOG.debug("All directories are inaccessible.")
                  return
                }
              }
            } else {
              LOG.debug("WatchKey not recognized. Ignoring...")
            }
          }
    }
  }

  @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  @Throws(IOException::class)
  private fun update(kind: WatchEvent.Kind<out Any>?, source: Path?) {
    when (kind) {
      ENTRY_CREATE -> {
        val parentPath = parentPath(source)
        val parentKey = File(parentPath)
        val childKey = File(source?.toFile()?.absolutePath)
        val parent = map[parentKey]
        if (source != null && source.toFile().isDirectory) {
          val child = TreeItem(childKey, makeIcon(IconName.FOLDER))
          buildSubtree(child, map)
        } else {
          val name = (source ?: return).toFile().absolutePath
          val child = TreeItem(
              childKey, with(name) {
            when {
              endsWith(".java") -> makeIcon(IconName.JAVA_FILE)
              endsWith(".g4") -> makeIcon(IconName.ANTLR_FILE)
              endsWith(".class") -> makeIcon(IconName.CLASS_FILE)
              endsWith(".json") -> makeIcon(IconName.JSON_FILE)
              else -> makeIcon(IconName.REGULAR_FILE)
            }
          }
          )
          Platform.runLater { if (parent != null) parent.children += child }
          map[childKey] = child
        }
      }
      ENTRY_DELETE -> {
        val childKey = File(source?.toFile()?.absolutePath)
        val child = map[childKey]
        val parent = child?.parent
        Platform.runLater { parent?.children?.remove(child) }
        map.remove(childKey)
      }
    }
  }

  private fun parentPath(child: Path?) = child?.toFile()?.parent

  @Throws(IOException::class)
  private fun buildSubtree(child: TreeItem<File>, map: MutableMap<File, TreeItem<File>>) {
    Files.walkFileTree(
        Paths.get(child.value.absolutePath),
        object : SimpleFileVisitor<Path>() {

          override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            val absolutePath = parentPath(dir)
            val parentKey = File(absolutePath)
            val parent = map[parentKey]
            val childKey = File(dir?.toFile()?.absolutePath)
            val name = if (childKey.isDirectory) IconName.FOLDER else {
              with(childKey.absolutePath) {
                when {
                  endsWith(".java") -> IconName.JAVA_FILE
                  endsWith(".g4") -> IconName.ANTLR_FILE
                  endsWith(".class") -> IconName.CLASS_FILE
                  endsWith(".json") -> IconName.JSON_FILE
                  else -> IconName.REGULAR_FILE
                }
              }
            }
            val item = TreeItem(childKey, makeIcon(name))
            if (parent == null)
              return FileVisitResult.CONTINUE
            Platform.runLater { parent.children += item }
            map[childKey] = item
            return FileVisitResult.CONTINUE
          }

          override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            return preVisitDirectory(file, attrs)
          }
        }
    )
  }

  companion object {
    private val LOG by logger { }
  }

}