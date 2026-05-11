package com.androidpe.lite;

import android.text.Editable;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public class AutoCloseBracketInputConnection extends InputConnectionWrapper {

    private AndroidCodeView codeView;

    public AutoCloseBracketInputConnection(InputConnection target, AndroidCodeView codeView) {
        super(target, true);
        this.codeView = codeView;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        if (text.length() == 1) {
            char c = text.charAt(0);
            String autoClose = null;

            if (c == '(') autoClose = ")";
            else if (c == '[') autoClose = "]";
            else if (c == '{') autoClose = "}";
            else if (c == '"') autoClose = "\"";
            else if (c == '\'') autoClose = "'";

            if (autoClose != null) {
                int selStart = codeView.getSelectionStart();
                int selEnd = codeView.getSelectionEnd();

                if (selStart != selEnd) {
                    String selectedText = codeView.getText().subSequence(
                            Math.min(selStart, selEnd),
                            Math.max(selStart, selEnd)
                    ).toString();
                    String wrapped = text.toString() + selectedText + autoClose;
                    codeView.getText().replace(
                            Math.min(selStart, selEnd),
                            Math.max(selStart, selEnd),
                            wrapped
                    );
                    int newPos = Math.min(selStart, selEnd) + wrapped.length() - autoClose.length();
                    codeView.setSelection(newPos);
                    return true;
                } else {
                    super.commitText(text, newCursorPosition);
                    super.commitText(autoClose, 0);
                    codeView.setSelection(selStart + 1);
                    return true;
                }
            }
        }
        return super.commitText(text, newCursorPosition);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (beforeLength == 1 && afterLength == 0) {
            int selStart = codeView.getSelectionStart();
            if (selStart > 0 && selStart < codeView.getText().length()) {
                Editable editable = codeView.getText();
                char before = editable.charAt(selStart - 1);
                char after = editable.charAt(selStart);

                if ((before == '(' && after == ')') ||
                    (before == '[' && after == ']') ||
                    (before == '{' && after == '}') ||
                    (before == '"' && after == '"') ||
                    (before == '\'' && after == '\'')) {
                    return super.deleteSurroundingText(1, 1);
                }
            }
        }
        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}