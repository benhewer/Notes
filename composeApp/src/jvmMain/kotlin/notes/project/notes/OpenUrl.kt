package notes.project.notes

import java.awt.Desktop
import java.net.URI

actual fun openUrl(urlString: String) {
    val url = URI(urlString)
    Desktop.getDesktop().browse(url)
}