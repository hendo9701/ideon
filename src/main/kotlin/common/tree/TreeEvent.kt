package common.tree

import java.io.File

data class TreeEvent(val type: TreeEventType, val source: File)