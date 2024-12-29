
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import io.ktor.utils.io.errors.*
import java.awt.GridLayout
import javax.swing.*


public class MyAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val folder: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)

        if (project != null && folder != null && folder.isDirectory) {
            // Show custom dialog
            val dialog = CustomFileDialog(folder.url)
            if (dialog.showAndGet()) { // Show dialog and check if OK was clicked
                val fileName = dialog.getClassName
                val jsonData = dialog.getJsonData

                if (fileName.isNotEmpty() && jsonData.isNotEmpty()) {
                    try {
                        val fullFileName = "$fileName.dart"
                        folder.createChildData(this, fullFileName)
                        Messages.showMessageDialog(
                            project,
                            "File '$fullFileName' created successfully!",
                            "Success",
                            Messages.getInformationIcon()
                        )
                    } catch (ioException: IOException) {
                        Messages.showErrorDialog(
                            project,
                            "Error creating file: ${ioException.message}",
                            "Error"
                        )
                    }
                } else {
                    Messages.showMessageDialog(
                        project,
                        "File creation canceled or invalid input.",
                        "Information",
                        Messages.getInformationIcon()
                    )
                }
            }
        }

    }

      fun showNotification(message:String,e:AnActionEvent){
        Messages.showMessageDialog(
            e.project,
            "Great! You just created your first action!",
            "My First Action",
            Messages.getInformationIcon());
    }

}

class CustomFileDialog(private val dialogTitle: String) : DialogWrapper(true) {
    private val className = JTextField()
    private val jsonData = JTextArea(10, 40)
    private val scrollPane = JScrollPane(jsonData)  // To make the JTextArea scrollable

    init {
        init()
        title = dialogTitle
    }
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS) // Vertical column layout


        panel.add(JLabel("Class Name:"))
        panel.add(className)

        panel.add(JLabel("Json Body:"))
        panel.add(scrollPane)  // Add JScrollPane to enable scrolling in case of long content


        return panel
    }

    val getClassName: String
        get() = className.text

    val getJsonData: String
        get() = jsonData.text
}