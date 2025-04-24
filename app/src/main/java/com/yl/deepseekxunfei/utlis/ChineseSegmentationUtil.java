package com.yl.deepseekxunfei.utlis;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.yl.deepseekxunfei.model.WordNLPModel;

import java.util.List;

public class ChineseSegmentationUtil {

    /**
     * 分词
     *
     * @param wordsContent 要进行分词的内容
     */
    public static WordNLPModel SegmentWords(String wordsContent) {
        String text = wordsContent;
        WordNLPModel wordNLPModel = new WordNLPModel();
        // 进行分词
        List<Term> terms = StandardTokenizer.segment(text);

        // 遍历分词结果，判断词性并打印
        for (Term term : terms) {
            String word = term.word;
            String pos = term.nature.toString();

            String posInfo = getPosInfo(pos, word, wordNLPModel); // 判断词性属性

            System.out.println("Word: " + word + ", POS: " + pos + ", Attribute: " + posInfo);
        }
        return wordNLPModel;
    }

    /**
     * 判断词性属性
     *
     * @param pos
     * @return 属性
     */
    static String getPosInfo(String pos, String word, WordNLPModel wordNLPModel) {
        // 这里你可以根据需要添加更多的判断逻辑来确定词性属性
        if (pos.equals("n")) {
            wordNLPModel.addN(word);
            return "名词";
        } else if (pos.equals("nr")) {
            wordNLPModel.addNr(word);
            return "人名";
        } else if (pos.equals("ns")) {
            wordNLPModel.addNs(word);
            return "地名";
        } else if (pos.equals("nt")) {
            wordNLPModel.addNt(word);
            return "机构名";
        } else if (pos.equals("nz")) {
            wordNLPModel.addNz(word);
            return "其他专名";
        } else if (pos.equals("nl")) {
            wordNLPModel.addNl(word);
            return "名词性惯用语";
        } else if (pos.equals("ng")) {
            wordNLPModel.addNg(word);
            return "名词性语素";
        } else if (pos.equals("v")) {
            wordNLPModel.addV(word);
            return "动词";
        } else if (pos.equals("vd")) {
            wordNLPModel.addVd(word);
            return "副动词";
        } else if (pos.equals("vn")) {
            wordNLPModel.addVn(word);
            return "名动词";
        } else if (pos.equals("vshi")) {
            wordNLPModel.addVshi(word);
            return "动词'是'";
        } else if (pos.equals("vyou")) {
            wordNLPModel.addV(word);
            return "动词'有'";
        } else if (pos.equals("a")) {
            wordNLPModel.addA(word);
            return "形容词";
        } else if (pos.equals("ad")) {
            wordNLPModel.addAd(word);
            return "副动词";
        } else if (pos.equals("d")) {
            wordNLPModel.addD(word);
            return "副词";
        } else if (pos.equals("r")) {
            wordNLPModel.addR(word);
            return "代词";
        } else if (pos.equals("rr")) {
            wordNLPModel.addRr(word);
            return "人称代词";
        } else if (pos.equals("rz")) {
            wordNLPModel.addRz(word);
            return "指示代词";
        } else if (pos.equals("rzt")) {
            wordNLPModel.addRzt(word);
            return "时间指示代词";
        } else if (pos.equals("c")) {
            wordNLPModel.addC(word);
            return "连词";
        } else if (pos.equals("u")) {
            wordNLPModel.addU(word);
            return "助词";
        } else if (pos.equals("m")) {
            wordNLPModel.addM(word);
            return "数词";
        } else if (pos.equals("q")) {
            wordNLPModel.addQ(word);
            return "量词";
        } else if (pos.equals("y")) {
            wordNLPModel.addY(word);
            return "语气词";
        } else if (pos.equals("e")) {
            wordNLPModel.addE(word);
            return "叹词";
        } else if (pos.equals("o")) {
            wordNLPModel.addO(word);
            return "拟声词";
        } else if (pos.equals("f")) {
            wordNLPModel.addF(word);
            return "方位词";
        } else if (pos.equals("z")) {
            wordNLPModel.addZ(word);
            return "状态词";
        } else if (pos.equals("p")) {
            wordNLPModel.addP(word);
            return "介词";
        } else if (pos.equals("h")) {
            wordNLPModel.addH(word);
            return "前缀";
        } else if (pos.equals("k")) {
            wordNLPModel.addK(word);
            return "后缀";
        } else if (pos.equals("w")) {
            wordNLPModel.addW(word);
            return "标点符号";
        } else if (pos.equals("t")) {
            wordNLPModel.addT(word);
            return "时间";
        } else {
            wordNLPModel.addOther(word);
            return "其他";
        }
    }

}
