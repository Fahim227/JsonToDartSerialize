package com.example.jsontodartserialize.json_parser

import com.example.jsontodartserialize.foramter.toLowerCamelCase

object JsonToDartModel {

    fun manualJsonParser(jsonString: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        val content = jsonString.trim().removeSurrounding("{", "}").trim()
        val keyValuePairs = content.split(",").map { it.trim() }

        for (pair in keyValuePairs) {
            val keyValue = pair.split(":").map { it.trim() }
            if (keyValue.size == 2) {
                val key = keyValue[0].removeSurrounding("\"")
                val value = parseValue(keyValue[1])
                map[key] = value
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
            val type = handleJavaLangType(jsonObject[key]!!)
            "  final $type ${key.toLowerCamelCase()};"
        }
    }

    fun jsonToDartConstructorVariables(jsonString: String): String {
        val jsonObject = manualJsonParser(jsonString)
        return jsonObject.keys.joinToString(separator = "\n") { key ->
            "    required this.${key.toLowerCamelCase()},"
        }
    }

    private fun handleJavaLangType(type: Any): String {
        return when (type::class.java.typeName) {
            "java.lang.String" -> "String"
            "java.lang.Integer" -> "int"
            "java.lang.Boolean" -> "bool"
            "java.lang.Double", "java.lang.Float", "java.lang.Long" -> "double"
            else -> "String"
        }
    }
}
