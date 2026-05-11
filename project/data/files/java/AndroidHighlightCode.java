package com.androidpe.lite;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidHighlightCode {

    private static final int COLOR_KEYWORD = 0xFFBB9AF7;
    private static final int COLOR_STRING = 0xFF9ECE6A;
    private static final int COLOR_COMMENT = 0xFF565F89;
    private static final int COLOR_NUMBER = 0xFFFF9E64;
    private static final int COLOR_ANNOTATION = 0xFFF7768E;
    private static final int COLOR_CLASS = 0xFF7DCFFF;
    private static final int COLOR_METHOD = 0xFF7AA2F7;
    private static final int COLOR_TAG = 0xFFBB9AF7;
    private static final int COLOR_ATTRIBUTE = 0xFF7DCFFF;
    private static final int COLOR_XML_STRING = 0xFF9ECE6A;
    private static final int COLOR_XML_COMMENT = 0xFF565F89;

    private static final String LANGUAGE_JAVA = "java";
    private static final String LANGUAGE_XML = "xml";

    private String[] javaKeywords = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
        "float", "for", "if", "implements", "import", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null", "instanceof"
    };

    public void highlight(SpannableStringBuilder spannable, String code, String language, String theme) {
        clearHighlights(spannable);

        if (LANGUAGE_JAVA.equals(language)) {
            highlightJava(spannable, code);
        } else if (LANGUAGE_XML.equals(language)) {
            highlightXml(spannable, code);
        }
    }

    private void clearHighlights(SpannableStringBuilder spannable) {
        ForegroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            spannable.removeSpan(span);
        }
    }

    private void highlightJava(SpannableStringBuilder spannable, String code) {
        highlightJavaComments(spannable, code);
        highlightJavaStrings(spannable, code);
        highlightJavaAnnotations(spannable, code);
        highlightJavaNumbers(spannable, code);
        highlightJavaKeywords(spannable, code);
    }

    private void highlightJavaComments(SpannableStringBuilder spannable, String code) {
        Pattern singleLineComment = Pattern.compile("//[^\n]*");
        Matcher m = singleLineComment.matcher(code);
        while (m.find()) {
            spannable.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Pattern multiLineComment = Pattern.compile("/\\*[\\s\\S]*?\\*/");
        m = multiLineComment.matcher(code);
        while (m.find()) {
            spannable.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void highlightJavaStrings(SpannableStringBuilder spannable, String code) {
        Pattern stringPattern = Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"");
        Matcher m = stringPattern.matcher(code);
        while (m.find()) {
            spannable.setSpan(new ForegroundColorSpan(COLOR_STRING), m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void highlightJavaAnnotations(SpannableStringBuilder spannable, String code) {
        Pattern annotationPattern = Pattern.compile("@\\w+");
        Matcher m = annotationPattern.matcher(code);
        while (m.find()) {
            spannable.setSpan(new ForegroundColorSpan(COLOR_ANNOTATION), m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void highlightJavaNumbers(SpannableStringBuilder spannable, String code) {
        Pattern numberPattern = Pattern.compile("\\b\\d+\\.?\\d*[fFlL]?\\b");
        Matcher m = numberPattern.matcher(code);
        while (m.find()) {
            if (!isInsideString(code, m.start()) && !isInsideComment(code, m.start())) {
                spannable.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void highlightJavaKeywords(SpannableStringBuilder spannable, String code) {
        for (String keyword : javaKeywords) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            Matcher m = pattern.matcher(code);
            while (m.find()) {
                if (!isInsideString(code, m.start()) && !isInsideComment(code, m.start())) {
                    int color = COLOR_KEYWORD;
                    if (keyword.equals("class") || keyword.equals("interface") || keyword.equals("extends")
                            || keyword.equals("implements")) {
                        color = COLOR_CLASS;
                    }
                    spannable.setSpan(new ForegroundColorSpan(color), m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        Pattern methodPattern = Pattern.compile("\\b([a-z_][a-zA-Z0-9_]*)\\s*\\(");
        Matcher m = methodPattern.matcher(code);
        while (m.find()) {
            int pos = m.start(1);
            if (!isInsideString(code, pos) && !isInsideComment(code, pos) &&
                    !isKeyword(code, m.group(1))) {
                spannable.setSpan(new ForegroundColorSpan(COLOR_METHOD), m.start(1), m.end(1),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void highlightXml(SpannableStringBuilder spannable, String code) {
        Pattern commentPattern = Pattern.compile("<!--[\\s\\S]*?-->");
        Matcher m = commentPattern.matcher(code);
        while (m.find()) {
            spannable.setSpan(new ForegroundColorSpan(COLOR_XML_COMMENT), m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Pattern stringPattern = Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"");
        m = stringPattern.matcher(code);
        while (m.find()) {
            if (!isInsideXmlComment(code, m.start())) {
                spannable.setSpan(new ForegroundColorSpan(COLOR_XML_STRING), m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        Pattern tagPattern = Pattern.compile("</?[a-zA-Z][a-zA-Z0-9:_.-]*");
        m = tagPattern.matcher(code);
        while (m.find()) {
            if (!isInsideXmlComment(code, m.start()) && !isInsideXmlString(code, m.start())) {
                spannable.setSpan(new ForegroundColorSpan(COLOR_TAG), m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        Pattern attrPattern = Pattern.compile("\\b[a-zA-Z][a-zA-Z0-9:_.-]*\\s*=");
        m = attrPattern.matcher(code);
        while (m.find()) {
            if (!isInsideXmlComment(code, m.start()) && !isInsideXmlString(code, m.start())) {
                spannable.setSpan(new ForegroundColorSpan(COLOR_ATTRIBUTE), m.start(), m.end() - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private boolean isInsideString(String code, int position) {
        boolean inString = false;
        for (int i = 0; i < position && i < code.length(); i++) {
            if (code.charAt(i) == '"' && (i == 0 || code.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
        }
        return inString;
    }

    private boolean isInsideComment(String code, int position) {
        int singleCommentStart = code.lastIndexOf("//", position);
        if (singleCommentStart >= 0) {
            int newlinePos = code.indexOf('\n', singleCommentStart);
            if (newlinePos == -1 || newlinePos > position) {
                return true;
            }
        }

        int multiCommentStart = code.lastIndexOf("/*", position);
        if (multiCommentStart >= 0) {
            int multiCommentEnd = code.indexOf("*/", multiCommentStart);
            if (multiCommentEnd == -1 || multiCommentEnd > position) {
                return true;
            }
        }

        return false;
    }

    private boolean isKeyword(String code, String word) {
        for (String kw : javaKeywords) {
            if (kw.equals(word)) return true;
        }
        return false;
    }

    private boolean isInsideXmlComment(String code, int position) {
        int commentStart = code.lastIndexOf("<!--", position);
        if (commentStart >= 0) {
            int commentEnd = code.indexOf("-->", commentStart);
            return commentEnd == -1 || commentEnd > position;
        }
        return false;
    }

    private boolean isInsideXmlString(String code, int position) {
        boolean inString = false;
        boolean inTag = false;
        for (int i = 0; i < position && i < code.length(); i++) {
            if (code.charAt(i) == '<') inTag = true;
            if (code.charAt(i) == '>') inTag = false;
            if (inTag && code.charAt(i) == '"') {
                inString = !inString;
            }
        }
        return inString;
    }
}