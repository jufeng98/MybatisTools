package org.javamaster.mybatis.tools.utils

object GenerateUtils {

    fun generateJavaField(name: String, typeName: String): String {
        val fieldName = StringUtil.toCamelCase(name)

        return if (typeName.contains("bigint")) {
            "    private Long $fieldName;"
        } else if (typeName.contains("int")) {
            "    private Integer $fieldName;"
        } else if (typeName.contains("date") || typeName.contains("time")) {
            "    private Date $fieldName;"
        } else if (typeName.contains("char") || typeName.contains("text")) {
            "    private String $fieldName;"
        } else if (typeName.contains("float") || typeName.contains("double")
            || typeName.contains("real") || typeName.contains("decimal") || typeName.contains("numeric")
        ) {
            "    private Double $fieldName;"
        } else {
            "    private String $fieldName;"
        }
    }

}
