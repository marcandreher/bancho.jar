package com.osuserverlist.bjar.modules.achievements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonMevlRewriter {

    private static final Pattern CHAINED_COMPARISON = Pattern.compile(
            "(\\S+)\\s*(<=|<|>=|>)\\s*(\\S+)\\s*(<=|<|>=|>)\\s*(\\S+)");

    private static final Pattern BITWISE_COMPARE = Pattern.compile(
            "(\\S+)\\s*&\\s*(\\d+)\\s*(==|!=)\\s*(\\d+)");

    public static String rewrite(String expr) {

        Matcher m = CHAINED_COMPARISON.matcher(expr);

        while (m.find()) {
            String left = m.group(1);
            String op1 = m.group(2);
            String middle = m.group(3);
            String op2 = m.group(4);
            String right = m.group(5);

            String replacement =
                    "(" + left + " " + op1 + " " + middle + ")" +
                    " && " +
                    "(" + middle + " " + op2 + " " + right + ")";

            expr = expr.replace(m.group(0), replacement);
            m = CHAINED_COMPARISON.matcher(expr);
        }

        m = BITWISE_COMPARE.matcher(expr);

        while (m.find()) {
            String value = m.group(1);
            String mask = m.group(2);
            String op = m.group(3);
            String compare = m.group(4);

            String replacement =
                    "((" + value + " & " + mask + ") " +
                    op + " " + compare + ")";

            expr = expr.replace(m.group(0), replacement);
            m = BITWISE_COMPARE.matcher(expr);
        }

        expr = expr.replaceAll("\\band\\b", "&&");
        expr = expr.replaceAll("\\bor\\b", "||");
        expr = expr.replaceAll("\\bnot\\b", "!");

        return expr;
    }
}