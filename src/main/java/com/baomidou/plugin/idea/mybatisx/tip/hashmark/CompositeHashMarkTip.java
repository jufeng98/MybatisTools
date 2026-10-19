package com.baomidou.plugin.idea.mybatisx.tip.hashmark;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * #{}提示的内容, 不仅仅只是字段， 还包括mybatis支持的7大属性配置
 *
 * @author ls9527
 */
@Slf4j
public class CompositeHashMarkTip {
    public static final List<HashMarkTip> hashMarkTips = new ArrayList<>() {
        {
            add(new JdbcTypeHashMarkTip());
            add(new TypeHandlerHashMarkTip());
            add(new JavaTypeHashMark());
            add(new ResultMapHashMarkTip());
            add(new ModeHashMarkTip());
            add(new NumericScaleHashMarkTip());
            add(new JdbcTypeNameHashMarkTip());
        }
    };

    private CompositeHashMarkTip() {
    }
}
