package org.javamaster.mybatis.tools.console

import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.mybatis.tools.console.MyBatisSqlConsoleFilter.Companion.PARAM_KEY
import org.javamaster.mybatis.tools.console.MyBatisSqlConsoleFilter.Companion.PRE_KEY
import org.javamaster.mybatis.tools.sql.BasicFormatter
import java.math.BigDecimal
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.regex.Pattern

/**
 * @author yudong
 */
class ExtractSqlHyperlinkInfo(private val line: String, private val sqlLine: String) : HyperlinkInfo {

    override fun navigate(project: Project) {
        val params = parseParams(StringUtils.substringAfter(line, PARAM_KEY))
        val sql = StringUtils.substringAfter(sqlLine, PRE_KEY)

        val wholeSql = parseSql(sql, params)

        val formatSql = BasicFormatter.format(wholeSql) + ";"

        val fileEditorManager = FileEditorManager.getInstance(project)
        val allEditors = fileEditorManager.allEditors

        val fileName = "mybatis-log.sql"

        val editor = allEditors.firstOrNull { it.file is LightVirtualFile && it.file.name == fileName }
        if (editor == null) {
            val virtualFile = LightVirtualFile(fileName, formatSql)

            fileEditorManager.openFile(virtualFile, true)
        } else {
            val textEditor = editor as TextEditor
            val myEditor = textEditor.editor
            val caretModel = myEditor.caretModel
            val scrollingModel = myEditor.scrollingModel
            val doc = myEditor.document

            val m = TIME_PATTERN.matcher(line)
            val dateStr = if (m.find()) {
                m.group(1)
            } else {
                DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss,SSS")
            }

            var offset = 0
            WriteCommandAction.runWriteCommandAction(project) {
                doc.insertString(doc.textLength, "\n-- $dateStr --\n")

                offset = doc.textLength

                doc.insertString(doc.textLength, formatSql)
            }

            myEditor.contentComponent.requestFocus()

            caretModel.primaryCaret.moveToOffset(offset)
            scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
    }

    companion object {
        private val MASK_PATTERN = Pattern.compile("\\?")
        val TIME_PATTERN: Pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})")
        private val NEED_BRACKETS: Set<String>

        init {
            val types: MutableSet<String> = HashSet(8)
            types.add(String::class.java.simpleName)
            types.add(Date::class.java.simpleName)
            types.add(Time::class.java.simpleName)
            types.add(LocalDate::class.java.simpleName)
            types.add(LocalTime::class.java.simpleName)
            types.add(LocalDateTime::class.java.simpleName)
            types.add(BigDecimal::class.java.simpleName)
            types.add(Timestamp::class.java.simpleName)
            NEED_BRACKETS = Collections.unmodifiableSet(types)
        }

        fun parseSql(sql: String, params: Queue<Map.Entry<String, String>>): String {
            return MASK_PATTERN.matcher(sql)
                .replaceAll {
                    val entry = params.poll()
                    if (NEED_BRACKETS.contains(entry.value)) {
                        String.format("'%s'", entry.key)
                    } else {
                        entry.key
                    }
                }
        }

        fun parseParams(line: String?): Queue<Map.Entry<String, String>> {
            val lineStr = line?.trimEnd('\n')

            val params = StringUtils.splitByWholeSeparator(lineStr, ", ")
            val queue: Queue<Map.Entry<String, String>> = ArrayDeque(params.size)

            for (param in params) {
                val value = StringUtils.substringBeforeLast(param, "(")
                val type = StringUtils.substringBetween(param, "(", ")")
                if (StringUtils.isEmpty(type)) {
                    queue.offer(AbstractMap.SimpleEntry<String, String>(value, null))
                } else {
                    queue.offer(AbstractMap.SimpleEntry(value, type))
                }
            }

            return queue
        }
    }
}
