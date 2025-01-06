package com.example.jsontodartserialize.Generators


import com.example.jsontodartserialize.json_parser.JsonToDartModel
import com.google.common.io.CharStreams
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import io.ktor.utils.io.errors.*
import org.apache.commons.lang.text.StrSubstitutor
import java.io.InputStreamReader

class EntityGenerator(private val className: String, private val jsonData: String, private val e: AnActionEvent) {
    private val template: String = "/META-INF/templates/entity_class.dart.template"

    private val project = e.project
    private val folder: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)

    fun generateClass() {
        if (className.isNotEmpty() && jsonData.isNotEmpty()) {
            try {
                val resourceAsStream = ModelGenerator::class.java.getResourceAsStream(template)
                val templateContent =
                    CharStreams.toString(InputStreamReader(resourceAsStream, Charsets.UTF_8))

                val replacements = mapOf("model_name" to className)
                var processedContent = StrSubstitutor(replacements).replace(templateContent)

                processedContent = processedContent.replace(
                    "class ${className}Model \\{".toRegex()
                ) { it.value + "\n\n" + JsonToDartModel.jsonToDartVariables(jsonData) }

                processedContent = processedContent.replace(
                    "${className}Model\\(\\{".toRegex()
                ) { it.value + "\n" + JsonToDartModel.jsonToDartConstructorVariables(jsonData) }

                ApplicationManager.getApplication().runWriteAction {
                    val newFile = folder?.createChildData(this, "$className.dart")
                    if (newFile != null) {
                        VfsUtil.saveText(newFile, processedContent)
                    }
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