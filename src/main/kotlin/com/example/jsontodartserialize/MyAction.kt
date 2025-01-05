
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
import java.io.InputStreamReader
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
                        val variablesInitializeRegex = "class ${fileName}Model \\{".toRegex()
                        val variableDeclarations = jsonToDartVariables(jsonData)
                        resultString = resultString.replace(variablesInitializeRegex) {
                            it.value + "\n\n" + variableDeclarations
                        }

                        // add required in constructor
                        val constructionVariablesRegex = "${fileName}Model\\(\\{".toRegex()
                        val variableConstuntonDeclarations = variablesWithRequired(jsonData)
                        resultString = resultString.replace(constructionVariablesRegex) {
                            it.value + "\n" + variableConstuntonDeclarations
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

    private fun replaceModelName(template: String, modelName: String): String {
        val regex = "\\{model_name}".toRegex() // Matches ${model_name}
        return template.replace(regex, modelName) // Replaces ${model_name} with modelName
    }

    private fun jsonToDartVariables(jsonString: String): String {
        // Parse JSON string into a map-like structure
//        val jsonObject = JSONObject(jsonString)
        val jsonObject = manualJsonParser(jsonString)
        val allKeys = jsonObject.keys

        val allSequence = allKeys.asSequence()


        // Transform each key-value pair into Dart variable declarations
        return allSequence.joinToString(separator = "\n") { key ->

            val type = jsonObject[key]?.let { handleJavaLangType(it) }
            println(type)
//            println(type::class.java.typeName)
            val dartVariableName = key // Convert key to camelCase
            "  final $type $dartVariableName;"
        }
    }

    private fun variablesWithRequired(jsonString: String):String{
        // Parse JSON string into a map-like structure
//        val jsonObject = JSONObject(jsonString)
        val jsonObject = manualJsonParser(jsonString)
        val allKeys = jsonObject.keys

        val allSequence = allKeys.asSequence()


        // Transform each key-value pair into Dart variable declarations
        return allSequence.joinToString(separator = "\n") { key ->

            val type = jsonObject[key]?.let { handleJavaLangType(it) }
            println(type)
//            println(type::class.java.typeName)
            val dartVariableName = key // Convert key to camelCase
            "    required this.$dartVariableName,"
        }
    }

    private fun handleJavaLangType(type: Any): String {
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

    private fun manualJsonParser(jsonString: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        // Remove curly braces and trim
        val content = jsonString.trim().removeSurrounding("{", "}").trim()

        // Split by comma to separate key-value pairs
        val keyValuePairs = content.split(",").map { it.trim() }

        for (pair in keyValuePairs) {
            // Split key and value by colon
            val keyValue = pair.split(":").map { it.trim() }

            if (keyValue.size == 2) {
                // Remove quotes from key and value
                val key = keyValue[0].removeSurrounding("\"")
                val value = parseValue(keyValue[1])
                map[key] = value
            }
        }
        return map
    }

    // Parse value based on type
    private fun parseValue(value: String): Any {
        return when {
            value.startsWith("\"") && value.endsWith("\"") -> value.removeSurrounding("\"") // String
            value.equals("true", ignoreCase = true) -> true // Boolean true
            value.equals("false", ignoreCase = true) -> false // Boolean false
            value.contains(".") -> value.toDoubleOrNull() ?: value // Double or fallback
            else -> value.toIntOrNull() ?: value // Integer or fallback
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
