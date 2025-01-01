
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
import org.apache.commons.lang.text.StrSubstitutor
import org.json.JSONObject
import java.io.InputStreamReader
import javax.swing.*
import kotlin.reflect.typeOf


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

                if (fileName.isNotEmpty() && jsonData.isNotEmpty()) {
                    try {

                        val templateUrl = "/META-INF/templates/model.dart.template"
                        val resourceAsStream = MyAction::class.java.getResourceAsStream(templateUrl)
                        var processedContent = CharStreams.toString(InputStreamReader(resourceAsStream, Charsets.UTF_8))

                        val replacements = mapOf(
                            "model_name" to fileName,
                        )



                        val substitutor = StrSubstitutor(replacements)

                        // Perform the substitution
                        var resultString = substitutor.replace(processedContent)
                        resultString = replaceModelName(resultString,fileName)

                        // Step 2: Dynamically build variable declarations
                        val classRegex = "class ${fileName}Model \\{".toRegex()
                        val variableDeclarations = jsonToDartVariables(jsonData)
                        resultString = resultString.replace(classRegex) {
                            it.value + "\n\n" + variableDeclarations
                        }

                        fileName = "$fileName.dart"
                        // Use runWriteAction to perform the write operation
                        ApplicationManager.getApplication().runWriteAction {
                            // Create a new file in the project
                            val newFile = folder.createChildData(this, fileName)
                            // Write content to the file
                            VfsUtil.saveText(newFile, resultString)
                        }


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

    fun replaceModelName(template: String, modelName: String): String {
        val regex = "\\{model_name}".toRegex() // Matches ${model_name}
        return template.replace(regex, modelName) // Replaces ${model_name} with modelName
    }

    fun jsonToDartVariables(jsonString: String): String {
        // Parse JSON string into a map-like structure
        val jsonObject = JSONObject(jsonString)
        val allKeys = jsonObject.keys()

        val allSequence = allKeys.asSequence()


        // Transform each key-value pair into Dart variable declarations
        return allSequence.joinToString(separator = "\n") { key ->

            val type = handleJavaLangType(jsonObject.get(key))
            println(type)
            println(type::class.java.typeName)
            val dartVariableName = key// Convert key to camelCase
            "  final $type $dartVariableName;"
        }
    }

    fun handleJavaLangType(type: Any): String {
        return when (type::class.java.typeName) {
            "java.lang.String" -> "String"
            "java.lang.Integer" -> "int"
            "java.lang.Boolean" -> "bool"
            "java.lang.Double" -> "double"
            "java.lang.Float" -> "double"
            "java.lang.Long" -> "double"
            else -> "String"
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
