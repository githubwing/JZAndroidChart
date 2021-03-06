package cn.jingzhuan.lib.chart.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.support.annotation.NonNull;
import java.lang.ref.WeakReference;
import java.util.List;

import cn.jingzhuan.lib.chart.Chart;
import cn.jingzhuan.lib.chart.Viewport;
import cn.jingzhuan.lib.chart.component.Highlight;
import cn.jingzhuan.lib.chart.data.IDataSet;

/**
 * Created by Donglua on 17/7/19.
 */

public abstract class AbstractDataRenderer<D extends IDataSet> implements Renderer {

    protected Viewport mViewport;
    protected Rect mContentRect;
    protected Paint mRenderPaint;

    private float min = Integer.MAX_VALUE;
    private float max = -Integer.MAX_VALUE;

    /**
     * Bitmap object used for drawing the paths (otherwise they are too long if
     * rendered directly on the canvas)
     */
    protected WeakReference<Bitmap> mDrawBitmap;

    /**
     * on this canvas, the paths are rendered, it is initialized with the
     * pathBitmap
     */
    protected Canvas mBitmapCanvas;
    /**
     * the bitmap configuration to be used
     */
    protected Bitmap.Config mBitmapConfig = Bitmap.Config.ARGB_8888;

    private int mHighlightColor = Color.WHITE;

    public AbstractDataRenderer(Chart chart) {
        this.mViewport = chart.getCurrentViewport();
        this.mContentRect = chart.getContentRect();

        mRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRenderPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public final void renderer(Canvas canvas) {
        int width = mContentRect.width();
        int height = mContentRect.height();

        if (mDrawBitmap == null
                || (mDrawBitmap.get().getWidth() != width)
                || (mDrawBitmap.get().getHeight() != height)) {

            if (width > 0 && height > 0) {

                mDrawBitmap = new WeakReference<>(Bitmap.createBitmap(width, height, mBitmapConfig));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else
                return;
        }

        mDrawBitmap.get().eraseColor(Color.TRANSPARENT);

        renderDataSet(mBitmapCanvas);

        canvas.drawBitmap(mDrawBitmap.get(), 0, 0, mRenderPaint);
    }


    protected abstract void renderDataSet(Canvas canvas);

    public abstract void renderHighlighted(Canvas canvas, @NonNull Highlight[] highlights);

    /**
     * Computes the pixel offset for the given X lib value. This may be outside the view bounds.
     */
    protected float getDrawX(float x) {
        return mContentRect.left
                + mContentRect.width() * (x - mViewport.left) / mViewport.width();
    }

    /**
     * Computes the pixel offset for the given Y lib value. This may be outside the view bounds.
     */
    protected float getDrawY(float y) {
        return mContentRect.bottom
                - mContentRect.height() * (y - mViewport.top) / mViewport.height();
    }

    public abstract void addDataSet(D dataSet);

    public abstract List<D> getDataSet();

    public void setHighlightColor(int highlightColor) {
        this.mHighlightColor = highlightColor;
    }

    public int getHighlightColor() {
        return mHighlightColor;
    }

    public void resetMaxMin() {
      min = Integer.MAX_VALUE;
      max = -Integer.MAX_VALUE;
    }

    public float getMax() {
      return max;
    }

    public float getMin() {
      return min;
    }

    public void setMin(float min) {
      this.min = min;
    }

    public void setMax(float max) {
      this.max = max;
    }
}
