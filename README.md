# MybatisTools

## IDEA MyBatis 插件(plugin)

### Main feature:

- Mapper and xml can jump back and forth
- Mybatis.xml prompt
- Prompt and reference jumps for if test, when test, and other areas in the mapper.xml file
- Prompt and reference jumps for #{} / ${} areas in the mapper.xml file
- Extract console mybatis SQL logs as complete executable SQL
- Integrate all functions of the MybatisX plugin(copy from MybatisX)
- Integrate mybatis generator Gui (copy from free mybatis plugin)
- Integrate OGNL language support(copy from intellij struct 2 plugin)
- Generate Java fields based on SQL column name or alias name
- Generate XML result tags based on the type attribute of tags such as XML resultMap

### 主要功能如下：

- 在 Mapper 接口类和 xml 文件之间跳转
- mybatis.xml 智能提示
- mapper.xml 文件的 if test、when test 等区域的智能输入提示及引用跳转
- mapper.xml 文件的 #{} / ${} 区域的智能输入提示及引用跳转
- 提取控制台 mybatis SQL 日志为完整的可执行 SQL
- 集成 MybatisX 插件所有功能(源码来自 MybatisX)
- 集成 mybatis generator Gui (源码来自 free mybatis plugin)
- 集成 OGNL 语言支持(源码来自 intellij struct 2 插件)

#### MyBatis Param 输入智能提示

![param-prompt.png](./images/param-prompt.gif)
![param-static-method-prompt.gif](images/param-static-method-prompt.gif)

#### MyBatis test 等属性的输入智能提示

![ognl-field-prompt.gif](images/ognl-field-prompt.gif)
![ognl-static-method-prompt.gif](images/ognl-static-method-prompt.gif)

#### MyBatis parameterType resultType 属性输入智能提示

![type-prompt.gif](images/type-prompt.gif)

#### MyBatis resultMap 属性输入智能提示

![resultMap-prompt.png](images/resultMap-prompt.png)

#### MyBatis Param 引用跳转

![param-reference.gif](images/param-reference.gif)
![static-method-reference.gif](images/static-method-reference.gif)

#### MyBatis test 等属性的引用跳转

![ognl-field-reference.gif](images/ognl-field-reference.gif)

### MyBatis #{} if test等区域变量引用支持重命名功能

![rename-param.gif](images/rename-param.gif)
![rename-param1.gif](images/rename-param1.gif)
![rename-param2.gif](images/rename-param2.gif)
![rename-param3.gif](images/rename-param3.gif)

#### 根据 sql 的列名或列别名生成 Java 类字段(旗舰版可用)

![sql-to-field.gif](images/sql-to-field.gif)

#### 根据 SQL 列名 / 别名快速新增不存在的类字段或 result 标签(旗舰版可用)

![create-field.gif](images/create-field.gif)
![create-xml-result.gif](images/create-xml-result.gif)

#### 根据 resultMap collection association 等的 type 属性快速生成所有的 result 标签

![generate-all-result.gif](images/generate-all-result.gif)

#### 将 resultType 转换为 resultMap

![resultType-to-resultMap.gif](images/resultType-to-resultMap.gif)

#### MyBatis 未解析的字段或方法错误提示

![field-error-tip.png](images/field-error-tip.png)
![method-error-tip.png](images/method-error-tip.png)

#### 提取控制台的 Mybatis SQL 语句

![sql-log.gif](images/sql-log.gif)

#### 为 Mybatis param 参数添加 if 标签

![addIfTag.gif](images/addIfTag.gif)

#### 其他

- 解决 Spring Service 注入 mapper 时 IDEA 出现红色波浪线提示Could not autowire的报错问题(包括TkMapper)

## 作者信息

- 作者博客：[知乎](https://www.zhihu.com/people/liang-yu-dong-44)
- 作者邮箱：375709770@qq.com
- github 地址：https://github.com/jufeng98