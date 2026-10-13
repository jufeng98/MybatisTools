package org.javamaster.mybatis.tools.console

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project

/**
 * @author yudong
 */
class MyBatisSqlConsoleFilterProvider : ConsoleFilterProvider {

    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(MyBatisSqlConsoleFilter())
    }

}
