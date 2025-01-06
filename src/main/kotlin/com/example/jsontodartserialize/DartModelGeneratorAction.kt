import com.example.jsontodartserialize.Generators.EntityGenerator
import com.example.jsontodartserialize.Generators.ModelGenerator
import com.example.jsontodartserialize.view.CustomFileDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile


class DartModelGeneratorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project
        val folder: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)



        if (project != null && folder != null && folder.isDirectory) {
            val dialog = CustomFileDialog("Create Dart Model Class")
            if (dialog.showAndGet()) {
                val fileName = dialog.getClassName
                val jsonData = dialog.getJsonData
                val isEntitySelected = dialog.isEntitySelected

                if (isEntitySelected) {
                    // make entity class
                    val generator = EntityGenerator(
                        className = fileName,
                        jsonData = jsonData,
                        e = e
                    )
                    generator.generateClass()
                } else {
                    // Making Model
                    val generator = ModelGenerator(
                        className = fileName,
                        jsonData = jsonData,
                        e = e
                    )
                    generator.generateClass()

                }

            }
        }
    }
}

