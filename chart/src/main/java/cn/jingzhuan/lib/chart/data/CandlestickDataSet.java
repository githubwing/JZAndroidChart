package cn.jingzhuan.lib.chart.data;

import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import cn.jingzhuan.lib.chart.Chart;
import cn.jingzhuan.lib.chart.Viewport;
import cn.jingzhuan.lib.chart.component.AxisY;
import cn.jingzhuan.lib.chart.widget.CandlestickChart;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 8/29/17.
 */

public class CandlestickDataSet extends AbstractDataSet<CandlestickValue> {

  private List<CandlestickValue> candlestickValues;
  private boolean mAutoWidth = true;
  private float mCandleWidth = -1;

  private int mIncreasingColor = 0xFFf84b4b;   // 阳线
  private int mDecreasingColor = 0xFF1deb5b; // 阴线
  private int mNeutralColor = Color.WHITE;    // 十字线

  private float mViewportWidth = 1f;

  public CandlestickDataSet(List<CandlestickValue> candlestickValues) {
    this(candlestickValues, AxisY.DEPENDENCY_BOTH);
  }

  public CandlestickDataSet(List<CandlestickValue> candlestickValues, @AxisY.AxisDependency int axisDependency) {
    this.candlestickValues = candlestickValues;

    this.mViewport = new Viewport();

    calcMinMax();

    mDepsAxis = axisDependency;
  }

  @Override public void calcMinMax() {

    if (candlestickValues == null || candlestickValues.isEmpty())
      return;

    mYMax = -Float.MAX_VALUE;
    mYMin = Float.MAX_VALUE;
    mXMax = -Float.MAX_VALUE;
    mXMin = Float.MAX_VALUE;

    for (CandlestickValue e : candlestickValues) {
      calcMinMaxY(e);
    }

    calcViewportY();
  }

  private void calcViewportY() {
    mViewportYMax = -Float.MAX_VALUE;
    mViewportYMin = Float.MAX_VALUE;

    for (CandlestickValue e : getVisiblePoints(mViewport)) {
      calcViewportMinMaxX(e);
    }

    switch (mDepsAxis) {
      case AxisY.DEPENDENCY_BOTH:
      case AxisY.DEPENDENCY_LEFT:
        setAxisViewportY(mAxisLeft, mViewportYMin, mViewportYMax);
        break;
      case AxisY.DEPENDENCY_RIGHT:
        setAxisViewportY(mAxisRight, mViewportYMin, mViewportYMax);
        break;
    }
  }

  private void calcViewportMinMaxX(CandlestickValue e) {
    if (e.getLow() < mViewportYMin)
      mViewportYMin = e.getLow();

    if (e.getHigh() > mViewportYMax)
      mViewportYMax = e.getHigh();
  }

  public int getVisibleCount(Viewport viewport) {
    return getVisiblePoints(viewport).size();
  }

  protected List<CandlestickValue> getVisiblePoints(Viewport viewport) {
    int from = (int) (viewport.left * candlestickValues.size());
    int to  = (int) (viewport.right * candlestickValues.size());

    return candlestickValues.subList(from, to);
  }

  public void calcMinMaxY(CandlestickValue e) {
    if (e.getLow() < mYMin)
      mYMin = e.getLow();

    if (e.getHigh() > mYMax)
      mYMax = e.getHigh();
  }

  @Override public int getEntryCount() {
    return candlestickValues.size();
  }

  @Override public void setValues(List<CandlestickValue> values) {
    this.candlestickValues = values;
    calcMinMax();
  }

  @Override public List<CandlestickValue> getValues() {
    return candlestickValues;
  }

  @Override public boolean addEntry(CandlestickValue e) {
    if (e == null)
      return false;

    if (candlestickValues == null) {
      candlestickValues = new ArrayList<>();
    }

    calcMinMaxY(e);

    // add the entry
    return candlestickValues.add(e);
  }

  @Override public boolean removeEntry(CandlestickValue e) {
    if (e == null) return false;

    calcMinMaxY(e);

    return candlestickValues.remove(e);
  }

  @Override public int getEntryIndex(CandlestickValue e) {
    return candlestickValues.indexOf(e);
  }

  @Override public CandlestickValue getEntryForIndex(int index) {
    return candlestickValues.get(index);
  }

  public void onViewportChange(Rect content) {

    boolean needCalcCandleWidth = Float.compare(mViewport.width(), mViewportWidth) != 0;

    calcViewportY();

    if (needCalcCandleWidth) {
      mCandleWidth = content.width() / (getVisibleCount(mViewport) + 1);
    }
    mViewportWidth = mViewport.width();
  }

  public void setAutoWidth(boolean mAutoWidth) {
    this.mAutoWidth = mAutoWidth;
  }

  public boolean isAutoWidth() {
    return mAutoWidth;
  }

  public int getDecreasingColor() {
    return mDecreasingColor;
  }

  public void setDecreasingColor(int decreasingColor) {
    this.mDecreasingColor = decreasingColor;
  }

  public int getIncreasingColor() {
    return mIncreasingColor;
  }

  public void setIncreasingColor(int increasingColor) {
    this.mIncreasingColor = increasingColor;
  }

  public float getCandleWidth() {
    return mCandleWidth;
  }

  public int getNeutralColor() {
    return mNeutralColor;
  }
}
