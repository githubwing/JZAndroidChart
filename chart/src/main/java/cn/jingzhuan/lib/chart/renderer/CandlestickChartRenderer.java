package cn.jingzhuan.lib.chart.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import cn.jingzhuan.lib.chart.Chart;
import cn.jingzhuan.lib.chart.Viewport;
import cn.jingzhuan.lib.chart.component.AxisY;
import cn.jingzhuan.lib.chart.component.Highlight;
import cn.jingzhuan.lib.chart.data.CandlestickDataSet;
import cn.jingzhuan.lib.chart.data.CandlestickValue;
import cn.jingzhuan.lib.chart.data.ChartData;
import cn.jingzhuan.lib.chart.event.OnViewportChangeListener;
import java.util.List;

/**
 *
 * Created by donglua on 8/29/17.
 */

public class CandlestickChartRenderer extends AbstractDataRenderer<CandlestickDataSet> {

  private float[] mUpperShadowBuffers = new float[4];
  private float[] mLowerShadowBuffers = new float[4];
  private float[] mBodyBuffers = new float[4];
  private ChartData<CandlestickDataSet> chartData;

  public CandlestickChartRenderer(final Chart chart) {
    super(chart);

    chartData = new ChartData<>();

    chart.setOnViewportChangeListener(new OnViewportChangeListener() {
      @Override public void onViewportChange(Viewport viewport) {
        calcMaxMin();
      }
    });

    chart.addOnTouchPointChangeListener(new Chart.OnTouchPointChangeListener() {
      @Override
      public void touch(float x, float y) {
        for (CandlestickDataSet dataSet : getDataSet()) {
          if (dataSet.isHighlightedEnable()) {

            float candleWidth = dataSet.getCandleWidth();
            float valueCount = dataSet.getEntryCount();
            int index = 0;
            float xPosition = x;
            float yPosition = -1;
            if (x > mContentRect.left) {
              index = (int) (((x - mContentRect.left + candleWidth * 0.5f) * mViewport.width()
                  / (mContentRect.width() - candleWidth) + mViewport.left) * (valueCount - 1f));

              if (index >= dataSet.getValues().size()) index = dataSet.getValues().size() - 1;

              final CandlestickValue candlestickValue = dataSet.getEntryForIndex(index);
              xPosition = candlestickValue.getX();
              yPosition = candlestickValue.getY();
            }
            chart.highlightValue(new Highlight(xPosition, yPosition, index));
          }
        }
      }
    });
  }

  public void calcMaxMin() {
    for (CandlestickDataSet dataSet : getDataSet()) {
      dataSet.onViewportChange(mContentRect);
      setMax(dataSet.getViewportYMax());
      setMin(dataSet.getViewportYMin());
    }
  }

  @Override protected void renderDataSet(Canvas canvas) {
    for (CandlestickDataSet candlestickDataSet : getDataSet()) {
      if (candlestickDataSet.isVisible()) {
        drawDataSet(canvas, candlestickDataSet);
      }
    }
  }

  private void drawDataSet(Canvas canvas, CandlestickDataSet candlestickDataSet) {

    //float min = candlestickDataSet.getViewportYMin();
    //float max = candlestickDataSet.getViewportYMax();

    float min, max;
    switch (candlestickDataSet.getAxisDependency()) {
      case AxisY.DEPENDENCY_RIGHT:
        min = candlestickDataSet.getAxisRight().getYMin();
        max = candlestickDataSet.getAxisRight().getYMax();
        break;
      case AxisY.DEPENDENCY_BOTH:
      case AxisY.DEPENDENCY_LEFT:
      default:
        min = candlestickDataSet.getAxisLeft().getYMin();
        max = candlestickDataSet.getAxisLeft().getYMax();
        break;
    }

    mRenderPaint.setStrokeWidth(5);
    mRenderPaint.setColor(candlestickDataSet.getColor());

    int valueCount = candlestickDataSet.getEntryCount();

    Path path = new Path();
    path.reset();

    for (int i = 0; i < valueCount; i++) {
      final CandlestickValue candlestick = candlestickDataSet.getEntryForIndex(i);

      mRenderPaint.setStyle(Paint.Style.FILL);

      float candleWidth = candlestickDataSet.getCandleWidth();

      if (candleWidth <= 0) {
        candleWidth = mContentRect.width() / valueCount;
      }

      float xPosition = mContentRect.left + candleWidth * 0.5f
          + (mContentRect.width() - candleWidth) * (i / (valueCount - 1f)  - mViewport.left) / mViewport.width();

      float highY  = (max - candlestick.getHigh())  / (max - min) * mContentRect.height();
      float lowY   = (max - candlestick.getLow())   / (max - min) * mContentRect.height();
      float openY  = (max - candlestick.getOpen())  / (max - min) * mContentRect.height();
      float closeY = (max - candlestick.getClose()) / (max - min) * mContentRect.height();

      mBodyBuffers[0] = xPosition - candleWidth * 0.4f;
      mBodyBuffers[1] = closeY;
      mBodyBuffers[2] = xPosition + candleWidth * 0.4f;
      mBodyBuffers[3] = openY;

      mUpperShadowBuffers[0] = xPosition;
      mUpperShadowBuffers[2] = xPosition;
      mLowerShadowBuffers[0] = xPosition;
      mLowerShadowBuffers[2] = xPosition;

      candlestick.setX(xPosition);
      candlestick.setY(closeY);

      if (Float.compare(candlestick.getOpen(), candlestick.getClose()) > 0) { // 阴线

        mUpperShadowBuffers[1] = highY;
        mUpperShadowBuffers[3] = openY;
        mLowerShadowBuffers[1] = lowY;
        mLowerShadowBuffers[3] = closeY;

        mRenderPaint.setColor(candlestickDataSet.getDecreasingColor());

      } else if (Float.compare(candlestick.getOpen(), candlestick.getClose()) < 0) { // 阳线

        mUpperShadowBuffers[1] = highY;
        mUpperShadowBuffers[3] = closeY;
        mLowerShadowBuffers[1] = lowY;
        mLowerShadowBuffers[3] = openY;

        mRenderPaint.setColor(candlestickDataSet.getIncreasingColor());

      } else {

        mUpperShadowBuffers[1] = highY;
        mUpperShadowBuffers[3] = openY;
        mLowerShadowBuffers[1] = lowY;
        mLowerShadowBuffers[3] = mUpperShadowBuffers[3];

        mRenderPaint.setColor(candlestickDataSet.getNeutralColor());
      }

      canvas.drawRect(mBodyBuffers[0],
                      mBodyBuffers[1],
                      mBodyBuffers[2],
                      mBodyBuffers[3], mRenderPaint);

      canvas.drawLines(mUpperShadowBuffers, mRenderPaint);

      canvas.drawLines(mLowerShadowBuffers, mRenderPaint);

    }

  }

  @Override public void renderHighlighted(Canvas canvas, @NonNull Highlight[] highlights) {

    for (Highlight highlight : highlights) {

      mRenderPaint.setColor(getHighlightColor());
      mRenderPaint.setStrokeWidth(2);

      canvas.drawLine(highlight.getX(),
                      mContentRect.top,
                      highlight.getX(),
                      mContentRect.bottom, mRenderPaint);

      canvas.drawLine(mContentRect.left,
                      highlight.getY(),
                      mContentRect.right,
                      highlight.getY(), mRenderPaint);
    }

  }

  @Override public void addDataSet(CandlestickDataSet dataSet) {
    chartData.add(dataSet);
  }

  @Override public List<CandlestickDataSet> getDataSet() {
    return chartData.getDataSets();
  }
}
