package com.yl.deepseekxunfei.scene;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ComputeChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class ComputeScene extends BaseChildScene {
    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        ComputeChildModel computeChildModel = new ComputeChildModel();
        String resultText = normalizeExpression(sceneModel.getText());
        String result = calculate(resultText);
        computeChildModel.setResultText(resultText);
        computeChildModel.setResult(result);
        computeChildModel.setType(SceneTypeConst.COMPUTE);
        return computeChildModel;
    }

    // 转换中文运算符为数学符号
    private String normalizeExpression(String input) {
        return input.trim()
                .replaceAll("[\\s？?等于几多少得]", "") // 移除无关词和空格
                .replaceAll("＋|加", "+")
                .replaceAll("－|减|−", "-")
                .replaceAll("[×xX]|乘", "*")
                .replaceAll("[÷/]|除", "/")
                .replaceAll("平方", "^2")
                .replaceAll("立方", "^3");
    }

    // 执行计算并返回结果
    private String calculate(String input) {
        try {
            String exprStr = normalizeExpression(input);
            // 表达式校验（防止非法字符）
            if (!exprStr.matches("^[\\d\\+\\-*/^().,]+$")) {
                return "错误：包含非法字符";
            }
            Expression expr = new ExpressionBuilder(exprStr)
                    .build();
            double result = expr.evaluate();
            // 处理除以零
            if (Double.isInfinite(result)) {
                return "错误：除数不能为零";
            }
            return String.format("%.2f", result); // 保留两位小数

        } catch (ArithmeticException e) {
            return "错误：" + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "错误：表达式格式无效";
        } catch (Exception e) {
            return "错误：无法计算该表达式";
        }
    }

}
