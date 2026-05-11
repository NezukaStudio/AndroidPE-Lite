package com.androidpe.lite;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.widget.AppCompatEditText;

public class AndroidCodeView extends AppCompatEditText {
	
	public static final String LANGUAGE_JAVA = "java";
	public static final String LANGUAGE_XML = "xml";
	
	public static final String TOKYO = "tokyo";
	
	private static final int COLOR_BG = 0xFF1A1B26;
	private static final int COLOR_LINE_NUMBER = 0xFF3B4261;
	private static final int COLOR_LINE_NUMBER_BG = 0xFF1F2233;
	
	private String currentLanguage = LANGUAGE_JAVA;
	private String currentTheme = TOKYO;
	private boolean pinchToZoomEnabled = true;
	private float currentTextSize;
	private float minTextSize = 8f;
	private float maxTextSize = 72f;
	private ScaleGestureDetector scaleGestureDetector;
	private boolean isHighlighting = false;
	private Paint lineNumberPaint;
	private Rect lineNumberRect;
	private int lineNumberPadding = 8;
	private int lineNumberWidth;
	private boolean showLineNumbers = true;
	private boolean autoCloseBrackets = true;
	
	private AndroidHighlightCode highlighter;
	
	public AndroidCodeView(Context context) {
		super(context);
		init(null);
	}
	
	public AndroidCodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	
	public AndroidCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}
	
	private void init(AttributeSet attrs) {
		currentTextSize = getTextSize();
		
		lineNumberPaint = new Paint();
		lineNumberPaint.setAntiAlias(true);
		lineNumberPaint.setColor(COLOR_LINE_NUMBER);
		lineNumberPaint.setTypeface(Typeface.MONOSPACE);
		lineNumberRect = new Rect();
		
		highlighter = new AndroidHighlightCode();
		
		setBackgroundColor(COLOR_BG);
		setTextColor(Color.WHITE);
		setGravity(Gravity.TOP | Gravity.START);
		setHorizontallyScrolling(true);
		setInputType(android.text.InputType.TYPE_CLASS_TEXT |
		android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
		android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		setTypeface(Typeface.MONOSPACE);
		
		updateLineNumberWidth();
		
		if (isInEditMode()) {
			return;
		}
		
		scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
		
		lineNumberPaint.setTextSize(getTextSize());
		setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		
		addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (!isHighlighting) {
					highlightSyntax();
				}
			}
		});
	}
	
	private void updateLineNumberWidth() {
		if (lineNumberPaint == null) return;
		int maxLines = Math.max(getLineCount(), 9999);
		String sampleText = String.valueOf(maxLines);
		lineNumberPaint.getTextBounds(sampleText, 0, sampleText.length(), lineNumberRect);
		lineNumberWidth = lineNumberRect.width() + lineNumberPadding * 2;
		setPadding(lineNumberWidth + 8, getPaddingTop(), getPaddingRight(), getPaddingBottom());
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (showLineNumbers && lineNumberPaint != null && lineNumberWidth > 0) {
			canvas.drawColor(COLOR_LINE_NUMBER_BG);
			canvas.save();
			canvas.clipRect(0, 0, lineNumberWidth, getHeight());
			
			int lineCount = getLineCount();
			int scrollY = getScrollY();
			int lineHeight = getLineHeight();
			int baseline = getBaseline();
			
			for (int i = 0; i < lineCount; i++) {
				int lineTop = (i * lineHeight) - scrollY;
				int lineBottom = lineTop + lineHeight;
				
				if (lineBottom < 0 || lineTop > getHeight()) {
					continue;
				}
				
				String lineNumber = String.valueOf(i + 1);
				float textWidth = lineNumberPaint.measureText(lineNumber);
				float x = lineNumberWidth - textWidth - lineNumberPadding;
				float y = lineTop + baseline;
				
				canvas.drawText(lineNumber, x, y, lineNumberPaint);
			}
			
			canvas.restore();
			
			canvas.drawLine(
			lineNumberWidth, 0,
			lineNumberWidth, getHeight(),
			lineNumberPaint
			);
		}
		
		super.onDraw(canvas);
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		updateLineNumberWidth();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateLineNumberWidth();
	}
	
	@Override
	public void setTextSize(float size) {
		super.setTextSize(size);
		if (lineNumberPaint != null) {
			lineNumberPaint.setTextSize(getTextSize());
			updateLineNumberWidth();
		}
	}
	
	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		if (lineNumberPaint != null) {
			lineNumberPaint.setTextSize(getTextSize());
			updateLineNumberWidth();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (pinchToZoomEnabled && scaleGestureDetector != null) {
			scaleGestureDetector.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		if (autoCloseBrackets) {
			return new AutoCloseBracketInputConnection(super.onCreateInputConnection(outAttrs), this);
		}
		return super.onCreateInputConnection(outAttrs);
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scaleFactor = detector.getScaleFactor();
			float newSize = currentTextSize * scaleFactor;
			newSize = Math.max(minTextSize, Math.min(maxTextSize, newSize));
			setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
			currentTextSize = newSize;
			return true;
		}
	}
	
	public void setStyleColorCode(String theme) {
		this.currentTheme = theme;
		highlightSyntax();
	}
	
	public void setLanguage(String language) {
		this.currentLanguage = language;
		highlightSyntax();
	}
	
	public void setPinchToZoom(boolean enabled) {
		this.pinchToZoomEnabled = enabled;
	}
	
	public void setAutoCloseBrackets(boolean enabled) {
		this.autoCloseBrackets = enabled;
	}
	
	public void setShowLineNumbers(boolean show) {
		this.showLineNumbers = show;
		if (show) {
			updateLineNumberWidth();
		} else {
			setPadding(8, getPaddingTop(), getPaddingRight(), getPaddingBottom());
		}
		invalidate();
	}
	
	@Override
	public void setTypeface(Typeface tf, int style) {
		super.setTypeface(tf, style);
		if (lineNumberPaint != null) {
			lineNumberPaint.setTypeface(tf);
		}
	}
	
	@Override
	public void setTypeface(Typeface tf) {
		super.setTypeface(tf);
		if (lineNumberPaint != null) {
			lineNumberPaint.setTypeface(tf);
		}
	}
	
	public void setTypeface(int resId) {
		try {
			Typeface typeface = getResources().getFont(resId);
			super.setTypeface(typeface);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setTypeface(String assetPath) {
		try {
			Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), assetPath);
			super.setTypeface(typeface);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void highlightSyntax() {
		if (isHighlighting) return;
		isHighlighting = true;
		
		Editable editable = getText();
		if (editable == null || editable.length() == 0) {
			isHighlighting = false;
			return;
		}
		
		String code = editable.toString();
		SpannableStringBuilder spannable = new SpannableStringBuilder(code);
		
		highlighter.highlight(spannable, code, currentLanguage, currentTheme);
		
		int selectionStart = getSelectionStart();
		int selectionEnd = getSelectionEnd();
		
		editable.replace(0, editable.length(), spannable);
		
		if (selectionStart <= editable.length() && selectionEnd <= editable.length()) {
			setSelection(selectionStart, selectionEnd);
		}
		
		isHighlighting = false;
	}
}