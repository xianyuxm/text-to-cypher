package com.xm.text_to_cypher.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import java.util.List;

@Data
public class TextTerm {

    /**
     * 词
     */
    public String word;

    /**
     * 标签
     */
    public String label;

    private float[] embedding;

    public String getLabelValue(List<String> keyWords) {
        if (CollUtil.isNotEmpty(keyWords) && keyWords.contains(word)) {
            return word;
        }
        if (StrUtil.equalsAny(label,"n","nl","nr","ns","nsf","nz")){
            return word;
        }
        // 可以自定义标签值
        if (StrUtil.equals(label,"nt")){
            return "机构";
        }
        if (StrUtil.equalsAny(label,"ntc","公司")){
            return "公司";
        }
        if (StrUtil.equals(label,"ntcf")){
            return "工厂";
        }
        if (StrUtil.equals(label,"nto")){
            return "政府机构";
        }
        if (StrUtil.equals(label,"企业")){
            return "企业";
        }
        return null;

    }
}
