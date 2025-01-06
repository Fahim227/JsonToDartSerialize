package com.example.jsontodartserialize.view
import java.awt.Component
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class CustomFileDialog(private val dialogTitle: String) : DialogWrapper(true) {
    private val entityRadioButton = JRadioButton("Entity").apply { isSelected = true } // Default selection
    private val modelRadioButton = JRadioButton("Model")
    private val className = JTextField()
    private val jsonData = JTextArea(10, 40)
    private val scrollPane = JScrollPane(jsonData)

    init {
        init()
        title = dialogTitle
    }

    override fun createCenterPanel(): JComponent {


        // Horizontal panel for "Model type" and checkboxes
        val horizontalPanel = JPanel().apply {
            layout = GridBagLayout()
            val constraints = GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST // Align components to the left
                fill = GridBagConstraints.NONE
                gridy = 0
                insets = JBUI.insets(5) // Add padding around components
            }

            // Add the "Model Type" label
            constraints.gridx = 0
            add(JLabel("Model Type:"), constraints)


            // Group the radio buttons to ensure mutual exclusivity
            val buttonGroup = ButtonGroup().apply {
                add(entityRadioButton)
                add(modelRadioButton)
            }

            // Add the Entity radio button
            constraints.gridx = 1
            add(entityRadioButton, constraints)

            // Add the Model radio button
            constraints.gridx = 2
            add(modelRadioButton, constraints)
        }



        // Vertical panel for other components
        val verticalPanel = JPanel().apply {
            layout = GridBagLayout()
            val constraints = GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST // Align components to the left
                fill = GridBagConstraints.HORIZONTAL
                gridx = 0
                insets = JBUI.insets(5) // Add padding around components
            }

            // Add "Class Name" label
            add(JLabel("Class Name:"), constraints)

            // Add className field
            constraints.gridy = 1
            add(className, constraints)

            // Add "Json Body" label
            constraints.gridy = 2
            add(JLabel("Json Body:"), constraints)

            // Add scrollPane
            constraints.gridy = 3
            constraints.weightx = 1.0
            constraints.weighty = 1.0
            add(scrollPane, constraints)
        }

        // Main panel to combine both horizontal and vertical panels
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT // Align to the left

            add(horizontalPanel) // Add horizontal panel at the top
            add(verticalPanel)   // Add vertical panel below it
        }

        return mainPanel
    }



    val getClassName: String
        get() = className.text

    val getJsonData: String
        get() = jsonData.text

    val isEntitySelected: Boolean
        get() = entityRadioButton.isSelected


}

