package pl.animagia;


import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.util.Util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import java.util.ArrayList;

public class OwnTimeBar extends DefaultTimeBar {

    private final Paint adMarkerPaint;
    private final int adMarkerWidth;
    private final Rect progressBar;
    private final int touchTargetHeight;
    private final Rect seekBounds;
    private final int barHeight;
    private ArrayList<Long> chapterMarkerTimeStamps;
    private long duration;
    public Context eContext;

    public OwnTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        eContext = context;
        seekBounds = new Rect();
        progressBar = new Rect();
        adMarkerPaint = new Paint();
        adMarkerPaint.setColor(getResources().getColor(R.color.seekbar_marker));
        chapterMarkerTimeStamps = new ArrayList<Long>();

        Resources res = context.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        int defaultAdMarkerWidth = dpToPx(displayMetrics, DEFAULT_AD_MARKER_WIDTH_DP);
        int defaultTouchTargetHeight = dpToPx(displayMetrics, DEFAULT_TOUCH_TARGET_HEIGHT_DP);
        int defaultBarHeight = dpToPx(displayMetrics, DEFAULT_BAR_HEIGHT_DP);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DefaultTimeBar, 0,
                    0);
            try {


                adMarkerWidth = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_ad_marker_width,
                        defaultAdMarkerWidth);
                touchTargetHeight = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_touch_target_height,
                        defaultTouchTargetHeight);
                barHeight = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_bar_height,
                        defaultBarHeight);
            } finally {
                a.recycle();
            }
        } else {
            adMarkerWidth = defaultAdMarkerWidth;
            touchTargetHeight = defaultTouchTargetHeight;
            barHeight = defaultBarHeight;
        }
    }


    private static int dpToPx(DisplayMetrics displayMetrics, int dps) {
        return (int) (dps * displayMetrics.density + 0.5f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTimeBarMarker(canvas);
    }

    private void drawTimeBarMarker(Canvas canvas) {

        int progressBarHeight = progressBar.height();
        int barTop = progressBar.centerY() - progressBarHeight / 2;
        int barBottom = barTop + progressBarHeight;

        int adMarkerOffset = adMarkerWidth / 2;

        for (int i = 0; i < chapterMarkerTimeStamps.size(); i++) {
            long adGroupTimeMs = Util.constrainValue(chapterMarkerTimeStamps.get(i), 0, duration);

            if (duration != 0) {
                int markerPositionOffset =
                        (int) (progressBar.width() * adGroupTimeMs / duration) - adMarkerOffset;
                int markerLeft = progressBar.left + Math.min(progressBar.width() - adMarkerWidth,
                        Math.max(0, markerPositionOffset));
                Paint paint = adMarkerPaint;
                canvas.drawRect(markerLeft, barTop, markerLeft + adMarkerWidth/2, barBottom, paint);
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = right - left;
        int height = bottom - top;
        int barY = (height - touchTargetHeight) / 2;
        int seekLeft = getPaddingLeft();
        int seekRight = width - getPaddingRight();
        int progressY = barY + (touchTargetHeight - barHeight) / 2;
        seekBounds.set(seekLeft, barY, seekRight, barY + touchTargetHeight);
        seekBounds.set(seekLeft, barY, seekRight, barY + touchTargetHeight);
        progressBar.set(seekBounds.left, progressY,
                seekBounds.right, progressY + barHeight);

    }


    public void setChapterMarkers(@Nullable ArrayList<Long> timestamp) {
        for(Long e : timestamp)
            chapterMarkerTimeStamps.add(e);
    }

    public void addChapterMarker(@Nullable long timestamp) {
        chapterMarkerTimeStamps.add(timestamp);
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
        this.duration = duration;
    }

}
