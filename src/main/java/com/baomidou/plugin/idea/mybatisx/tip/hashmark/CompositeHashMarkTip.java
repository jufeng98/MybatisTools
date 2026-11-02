package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * #{}提示的内容, 不仅仅只是字段， 还包括mybatis支持的7大属性配置
 *
 * @author ls9527
 */
@Slf4j
public class CompositeHashMarkTip {
    public static final Map<String, HashMarkTip> hashMarkTips = new HashMap<>() {
        {
            JdbcTypeHashMarkTip jdbcTypeHashMarkTip = new JdbcTypeHashMarkTip();
            put(jdbcTypeHashMarkTip.getName(), jdbcTypeHashMarkTip);

            TypeHandlerHashMarkTip typeHandlerHashMarkTip = new TypeHandlerHashMarkTip();
            put(typeHandlerHashMarkTip.getName(), typeHandlerHashMarkTip);

            JavaTypeHashMark javaTypeHashMark = new JavaTypeHashMark();
            put(javaTypeHashMark.getName(), javaTypeHashMark);

            ResultMapHashMarkTip resultMapHashMarkTip = new ResultMapHashMarkTip();
            put(resultMapHashMarkTip.getName(), resultMapHashMarkTip);

            ModeHashMarkTip modeHashMarkTip = new ModeHashMarkTip();
            put(modeHashMarkTip.getName(), modeHashMarkTip);

            NumericScaleHashMarkTip numericScaleHashMarkTip = new NumericScaleHashMarkTip();
            put(numericScaleHashMarkTip.getName(), numericScaleHashMarkTip);

            JdbcTypeNameHashMarkTip jdbcTypeNameHashMarkTip = new JdbcTypeNameHashMarkTip();
            put(jdbcTypeNameHashMarkTip.getName(), jdbcTypeNameHashMarkTip);
        }
    };

    private CompositeHashMarkTip() {
    }
}
