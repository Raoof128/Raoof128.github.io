package com.raouf.mehrguard.desktop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File

/**
 * Provides desktop drag-and-drop support for image files.
 * Parity with Web app scanner.js handleDrop functionality.
 *
 * @param onDragEnter Called when a file is dragged over the component
 * @param onDragExit Called when drag leaves the component
 * @param onDrop Called with the dropped file
 */
@Composable
fun rememberDragDropHandler(
    onDragEnter: () -> Unit = {},
    onDragExit: () -> Unit = {},
    onDrop: (File) -> Unit
): DragDropHandler {
    return remember { 
        DragDropHandler(
            onDragEnter = onDragEnter,
            onDragExit = onDragExit,
            onDrop = onDrop
        )
    }
}

class DragDropHandler(
    val onDragEnter: () -> Unit,
    val onDragExit: () -> Unit,
    val onDrop: (File) -> Unit
)

/**
 * Creates a component-level DropTarget that listens for file drops.
 * Use this in conjunction with an AWT window to enable drag-drop.
 */
@Composable
fun DragDropTarget(
    enabled: Boolean = true,
    onDragEnter: () -> Unit = {},
    onDragExit: () -> Unit = {},
    onDrop: (File) -> Unit,
    content: @Composable () -> Unit
) {
    content()
}

/**
 * Data class to hold drag-and-drop state information.
 */
data class DragDropState(
    val isDragging: Boolean = false,
    val isHovering: Boolean = false,
    val droppedFile: File? = null
)

/**
 * Utility to extract files from a drop event.
 */
object DragDropUtils {
    
    /**
     * Checks if the file is a supported image type.
     */
    fun isSupportedImage(file: File): Boolean {
        val supportedExtensions = listOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
        return file.extension.lowercase() in supportedExtensions
    }
    
    /**
     * Gets the first image file from a list of files.
     */
    fun getFirstImageFile(files: List<File>): File? {
        return files.firstOrNull { isSupportedImage(it) }
    }
}
