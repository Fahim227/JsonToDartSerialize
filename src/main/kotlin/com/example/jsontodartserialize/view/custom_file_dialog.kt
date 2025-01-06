package com.example.jsontodartserialize.view

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*

class CustomFileDialog(private val dialogTitle: String) : DialogWrapper(true) {
    private val className = JTextField()
    private val jsonData = JTextArea(10, 40)
    private val scrollPane = JScrollPane(jsonData)

    init {
        init()
        title = dialogTitle
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("Class Name:"))
            add(className)
            add(JLabel("Json Body:"))
            add(scrollPane)
        }
        return panel
    }

    val getClassName: String
        get() = className.text

    val getJsonData: String
        get() = jsonData.text
}

