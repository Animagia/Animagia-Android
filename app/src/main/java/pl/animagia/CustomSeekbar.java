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

public class CustomSeekbar extends DefaultTimeBar {

    private final int markerWidth;
    private final Rect progressBar;
    private final int touchTargetHeight;
    private final Rect seekBounds;
    private final int barHeight;
    
    private ArrayList<Long> chapterMarkerTimeStamps;
    private long duration;
    private long previewMillis;


    public CustomSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        seekBounds = new Rect();
        progressBar = new Rect();
        chapterMarkerTimeStamps = new ArrayList<>();

        Resources res = context.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        int defaultAdMarkerWidth = dpToPx(displayMetrics, DEFAULT_AD_MARKER_WIDTH_DP);
        int defaultTouchTargetHeight = dpToPx(displayMetrics, DEFAULT_TOUCH_TARGET_HEIGHT_DP);
        int defaultBarHeight = dpToPx(displayMetrics, DEFAULT_BAR_HEIGHT_DP);

        if (attrs != null) {
            TypedArray a = context.getTheme().
                    obtainStyledAttributes( attrs, R.styleable .DefaultTimeBar, 0, 0);
            try {
                markerWidth = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_ad_marker_width,
                        defaultAdMarkerWidth);
                touchTargetHeight =
                        a.getDimensionPixelSize(R.styleable.DefaultTimeBar_touch_target_height,
                                defaultTouchTargetHeight);
                barHeight = a.getDimensionPixelSize(R.styleable.DefaultTimeBar_bar_height,
                        defaultBarHeight);
            } finally {
                a.recycle();
            }
        } else {
            markerWidth = defaultAdMarkerWidth;
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
        //drawTimeBarMarker(canvas);
        drawLockedSegment(canvas);
        //drawTestMarker(canvas);
    }


//    private void drawTestMarker(Canvas canvas) {
//        Paint paint = new Paint();
//        paint.setColor(getResources().getColor(R.color.colorPrimaryLight));
//
//        progressBar.left
//    }


    private void drawTimeBarMarker(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.seekbar_chapter_marker));

        int progressBarHeight = progressBar.height();
        int barTop = progressBar.centerY() - progressBarHeight / 2;
        int barBottom = barTop + progressBarHeight;

        int adMarkerOffset = markerWidth / 2;

        for (Long chapterMarkerTimeStamp : chapterMarkerTimeStamps) {
            long adGroupTimeMs = Util.constrainValue(chapterMarkerTimeStamp, 0, duration);

            if (duration != 0) {
                int markerPositionOffset =
                        (int) (progressBar.width() * adGroupTimeMs / duration) - adMarkerOffset;
                int markerLeft = progressBar.left + Math.min(progressBar.width() - markerWidth,
                        Math.max(0, markerPositionOffset));
                canvas.drawRect(markerLeft, barTop, markerLeft + markerWidth / 2, barBottom, paint);
            }
        }
    }


    private void drawLockedSegment(Canvas canvas) {
        if(duration == 0) {
            return;
        }

        Paint lockedSegmentPaint = new Paint();
        lockedSegmentPaint.setColor(getResources().getColor(R.color.seekbar_locked_segment));

        int progressBarHeight = progressBar.height();
        int barTop = progressBar.centerY() - progressBarHeight / 2;
        int barBottom = barTop + progressBarHeight;

        long previewTimeMs = Util.constrainValue(previewMillis, 0, duration);
        int cosmeticMargin = progressBar.width() / 33; //FIXME too large in landscape
        int markerPosition =
                (int) (progressBar.width() * previewTimeMs / duration) + cosmeticMargin;

        while(markerPosition < progressBar.width() - markerWidth) {
            int markerLeft = progressBar.left + Math.min(progressBar.width() - markerWidth,
                    Math.max(0, markerPosition));
            canvas.drawRect(
                    markerLeft, barTop, markerLeft + markerWidth / 2, barBottom,
                    lockedSegmentPaint);
            markerPosition += 1.5 * markerWidth;
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

    public void addChapterMarker(@Nullable long timestamp) {
        chapterMarkerTimeStamps.add(timestamp);
    }

    public void setPreviewMillis(long length) {
        previewMillis = length;
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
        this.duration = duration;
    }

}
