package com.xm.text_to_cypher.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.xm.text_to_cypher.dto.TextTerm;
import com.xm.text_to_cypher.service.TextToSegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextToSegmentServiceImpl implements TextToSegmentService {
    @Override
    public List<TextTerm> segmentText(String text) {
        if (StrUtil.isEmpty(text)){
            return new ArrayList<>();
        }
        Segment segment = HanLP.newSegment()
                .enableOrganizationRecognize(true)
                .enablePlaceRecognize(true)
                .enableNumberQuantifierRecognize(true);

        List<Term> seg = segment.seg(text);
        if (CollUtil.isEmpty(seg)){
            return new ArrayList<>();
        }
        List<TextTerm> terms = new ArrayList<>();
        for (Term term : seg) {
            TextTerm textTerm = new TextTerm();
            textTerm.setWord(term.word);
            textTerm.setLabel(term.nature.toString());
            terms.add(textTerm);
        }
        return terms;
    }
    @Override
    public void addDict(String word, String label,int frequency) {
        CustomDictionary.insert(word, label + " " + frequency);
    }
}
