
import com.google.common.io.CharStreams
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import io.ktor.utils.io.errors.*
import java.io.File
import java.io.InputStreamReader
import org.apache.commons.lang.text.StrSubstitutor
import javax.swing.*


public class MyAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val folder: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)

        if (project != null && folder != null && folder.isDirectory) {
            // Show custom dialog
            val dialog = CustomFileDialog(folder.url)
            if (dialog.showAndGet()) { // Show dialog and check if OK was clicked
                var fileName = dialog.getClassName
                val jsonData = dialog.getJsonData

                if (fileName.isEmpty() && jsonData.isEmpty()) {
                    try {

                        val templateUrl = "/META-INF/templates/model.dart.template"
//                        val bufferedReader: BufferedReader = File(templateUrl).bufferedReader()
//                        val inputString = bufferedReader.use { it.readText() }
//                        println(inputString)
                        println("project.basePath ==== ${project.basePath}")
                        println(project.projectFile?.url)


                        val resourceAsStream = MyAction::class.java.getResourceAsStream(templateUrl)
                        val templateString = CharStreams.toString(InputStreamReader(resourceAsStream, Charsets.UTF_8))

                        val file = File(templateUrl)
                        val templateFile = VfsUtil.findFileByIoFile(file, true)
                        println(templateFile?.url)
                        println(templateString)
                        println("TEst 1")

                        val templateContent = templateFile?.let { VfsUtil.loadText(it) }

                        var processedContent = templateString

                        val replacements = mapOf(
                            "model_name" to "fileName",
                        )

//                        for ((placeholder, value) in replacements) {
//                            println(placeholder)
//                            processedContent = processedContent.replace("{{${placeholder}}}", value)
//                            println(processedContent)
//
//                        }

                        // Create a StringSubstitutor from the values map

//                        val escapedTemplate = processedContent.replace("_$\\$\\{(\\w+)}".toRegex(), "ESCAPED_PLACEHOLDER_$1")


                        val substitutor = StrSubstitutor(replacements)

                        // Perform the substitution
                        var resultString = substitutor.replace(processedContent)

                        replacements.forEach { (key, value) ->
                            val regex = "_\\$\\{${key}}".toRegex()
                            println(regex)
                                resultString = resultString.replace(regex, "_$$value")
                            }

                        println("resultString $resultString")



                        fileName = "$fileName.dart"
                        // Use runWriteAction to perform the write operation
                        ApplicationManager.getApplication().runWriteAction {
                            // Create a new file in the project
                            val newFile = folder.createChildData(this, fileName)
                            // Write content to the file
                            VfsUtil.saveText(newFile, resultString)
                        }

                        // Define the file path
//                        val filePath = file.path
//
//                        // Create the parent directories if they don't exist
//
//                        // Write the template content to the file
//                        Files.write(Paths.get(filePath), templateContent.toByteArray())

//                        val templateFolder =  "bloc_without_equatable"
//                        val resourcePath = "/templates/model.dart.template"
//                        val resourceAsStream = BlocGenerator::class.java.getResourceAsStream(resourcePath)
//                        val templateContent = CharStreams.toString(InputStreamReader(resourceAsStream, Charsets.UTF_8))


                        Messages.showMessageDialog(
                            project,
                            "File  created successfully!",
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
