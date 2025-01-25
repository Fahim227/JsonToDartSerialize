package com.example.jsontodartserialize.json_parser

import com.example.jsontodartserialize.foramter.toLowerCamelCase
import com.example.jsontodartserialize.foramter.toPascalCase

object JsonToDartModel {

    fun manualJsonParser(jsonString: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        // Trim and remove outermost curly braces
        val content = jsonString.trim().removeSurrounding("{", "}").trim()

        var depth = 0
        val keyValuePairs = mutableListOf<String>()
        var currentPair = StringBuilder()

        for (char in content) {
            when (char) {
                '{' -> {
                    depth++
                    currentPair.append(char)
                }

                '}' -> {
                    depth--
                    currentPair.append(char)
                }

                ',' -> {
                    if (depth == 0) {
                        keyValuePairs.add(currentPair.toString().trim())
                        currentPair = StringBuilder()
                        continue
                    } else {
                        currentPair.append(char)
                    }
                }

                else -> currentPair.append(char)
            }
        }

        if (currentPair.isNotEmpty()) {
            keyValuePairs.add(currentPair.toString().trim())
        }

        for (pair in keyValuePairs) {
            val keyValue = pair.split(":", limit = 2).map { it.trim() }
            if (keyValue.size == 2) {
                val key = keyValue[0].removeSurrounding("\"")
                val valuee = keyValue[1]

                // Check if value is a nested JSON object
                if (valuee.startsWith("{") && valuee.endsWith("}")) {
                    map[key] = manualJsonParser(valuee)
                } else {
                    map[key] = parseValue(valuee)
                }
            }
        }

        return map
    }


    fun parseValue(value: String): Any {
        return when {
            value.startsWith("\"") && value.endsWith("\"") -> value.removeSurrounding("\"")
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false
            value.contains(".") -> value.toDoubleOrNull() ?: value
            else -> value.toIntOrNull() ?: value
        }
    }

    fun jsonToDartVariables(jsonString: String): String {
        val jsonObject = manualJsonParser(jsonString)
        return jsonObject.keys.joinToString(separator = "\n") { key ->
            val type = handleJavaLangType(jsonObject[key]!!, key)
            "  final $type ${key.toLowerCamelCase()};"
        }
    }

    fun jsonToDartConstructorVariables(jsonString: String): String {
        val jsonObject = manualJsonParser(jsonString)
        return jsonObject.keys.joinToString(separator = "\n") { key ->
            "    required this.${key.toLowerCamelCase()},"
        }
    }

    private fun handleJavaLangType(type: Any, key: Any): String {
        println("type ==== ${type}")
        println("type ==== ${type::class.java.typeName}")
        return when (type::class.java.typeName) {
            "java.lang.String" -> "String"
            "java.lang.Integer" -> "int"
            "java.lang.Boolean" -> "bool"
            "java.lang.Double", "double",
            "java.lang.Long" -> "double"

            "java.util.LinkedHashMap" -> toPascalCase(str = key.toString())
            else -> "String"
        }
    }
}


