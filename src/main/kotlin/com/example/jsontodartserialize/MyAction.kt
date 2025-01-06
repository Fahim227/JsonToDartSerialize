

import com.example.jsontodartserialize.json_parser.JsonToDartModel
import com.example.jsontodartserialize.view.CustomFileDialog
import com.google.common.io.CharStreams
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import io.ktor.utils.io.errors.*
import org.apache.commons.lang.text.StrSubstitutor
import java.io.InputStreamReader


class DartModelGeneratorAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val folder: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)

        if (project != null && folder != null && folder.isDirectory) {
            val dialog = CustomFileDialog("Create Dart Model Class")
            if (dialog.showAndGet()) {
                val fileName = dialog.getClassName
                val jsonData = dialog.getJsonData

                if (fileName.isNotEmpty() && jsonData.isNotEmpty()) {
                    try {
                        val templateUrl = "/META-INF/templates/model_class.dart.template"
                        val resourceAsStream = DartModelGeneratorAction::class.java.getResourceAsStream(templateUrl)
                        val templateContent = CharStreams.toString(InputStreamReader(resourceAsStream, Charsets.UTF_8))

                        val replacements = mapOf("model_name" to fileName)
                        var processedContent = StrSubstitutor(replacements).replace(templateContent)

                        processedContent = processedContent.replace(
                            "class ${fileName}Model \\{".toRegex()
                        ) { it.value + "\n\n" + JsonToDartModel.jsonToDartVariables(jsonData) }

                        processedContent = processedContent.replace(
                            "${fileName}Model\\(\\{".toRegex()
                        ) { it.value + "\n" + JsonToDartModel.jsonToDartConstructorVariables(jsonData) }

                        processedContent = replaceModelName(processedContent,fileName)


                        ApplicationManager.getApplication().runWriteAction {
                            val newFile = folder.createChildData(this, "$fileName.dart")
                            VfsUtil.saveText(newFile, processedContent)
                        }

                        Messages.showMessageDialog(
                            project,
                            "File created successfully!",
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
}

