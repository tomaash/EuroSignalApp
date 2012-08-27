package cz.thc.eurosignal.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SVGDrawView extends View {
    public static final float STROKE_WIDTH = 5f;
    public static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
    private Paint paint = new Paint();
    public SVGPath path = new SVGPath();
    //Optimizes painting by invalidating the smallest possible area.
    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    // Reserve four co-ordinates
    public int minX = 9999, minY = 9999;
    public int maxX = 0, maxY = 0;

    public SVGDrawView(Context context, AttributeSet attrs) {
	super(context, attrs);
	paint.setAntiAlias(true);
	paint.setColor(Color.BLACK);
	paint.setStyle(Paint.Style.STROKE);
	paint.setStrokeJoin(Paint.Join.ROUND);
	paint.setStrokeWidth(STROKE_WIDTH);
	paint.getStrokeWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
	canvas.drawARGB(Color.alpha(Color.TRANSPARENT),
		Color.red(Color.TRANSPARENT), Color.green(Color.TRANSPARENT),
		Color.blue(Color.TRANSPARENT));
	canvas.drawPath(path, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
	float eventX = event.getX();
	float eventY = event.getY();
	if (eventX < minX)
	    minX = (int) eventX;
	if (eventX > maxX)
	    maxX = (int) eventX;
	if (eventY < minY)
	    minY = (int) eventY;
	if (eventY > maxY)
	    maxY = (int) eventY;
	switch (event.getAction()) {
	case MotionEvent.ACTION_DOWN:
	    path.moveTo(eventX, eventY);
	    lastTouchX = eventX;
	    lastTouchY = eventY;
	    return true;
	case MotionEvent.ACTION_MOVE:
	case MotionEvent.ACTION_UP:
	    // Start tracking the dirty region.
	    resetDirtyRect(eventX, eventY);
	    /**
	     * When the hardware tracks events faster than they are delivered,
	     * the event will contain a history of those skipped points.
	     */
	    int historySize = event.getHistorySize();
	    for (int i = 0; i < historySize; i++) {
		float historicalX = event.getHistoricalX(i);
		float historicalY = event.getHistoricalY(i);
		expandDirtyRect(historicalX, historicalY);
		path.lineTo(historicalX, historicalY);
	    }
	    path.lineTo(eventX, eventY);
	    break;
	default:
	    return false;
	}
	invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
		(int) (dirtyRect.top - HALF_STROKE_WIDTH),
		(int) (dirtyRect.right + HALF_STROKE_WIDTH),
		(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));
	lastTouchX = eventX;
	lastTouchY = eventY;
	return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
	if (historicalX < dirtyRect.left)
	    dirtyRect.left = historicalX;
	else if (historicalX > dirtyRect.right)
	    dirtyRect.right = historicalX;
	if (historicalY < dirtyRect.top)
	    dirtyRect.top = historicalY;
	else if (historicalY > dirtyRect.bottom)
	    dirtyRect.bottom = historicalY;
    }

    // Resets the dirty region when the motion event occurs.
    private void resetDirtyRect(float eventX, float eventY) {
	dirtyRect.left = Math.min(lastTouchX, eventX);
	dirtyRect.right = Math.max(lastTouchX, eventX);
	dirtyRect.top = Math.min(lastTouchY, eventY);
	dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }


    public static class SVGPath extends Path {

	private static class Polyline {

	    private final List<Point> points = new ArrayList<Point>();

	    public void addPoint(float x, float y) {
		points.add(new Point((int) x, (int) y));
	    }


	    public void addToSVGStringBuffer(StringBuffer sb) {

		sb.append("\n<polyline points=\"");

		for (int i = 0; i < points.size(); i++) {
		    sb.append(points.get(i).x + "," + points.get(i).y);
		    if (i < points.size() - 1)
			sb.append(" ");
		}

		sb.append("\" style=\"fill:none;stroke:black;stroke-width:3\" />");

	    }

	}

	private final List<Polyline> polylines = new ArrayList<SVGDrawView.SVGPath.Polyline>();


	@Override
	public void moveTo(float x, float y) {

	    polylines.add(new Polyline());

	    polylines.get(polylines.size() - 1).addPoint(x, y);

	    super.moveTo(x, y);
	}

	@Override
	public void lineTo(float x, float y) {

	    polylines.get(polylines.size() - 1).addPoint(x, y);

	    super.lineTo(x, y);
	}


	public String toSVGString() {

	    StringBuffer sb = new StringBuffer();
	    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");

	    for (Polyline pl : polylines)
		pl.addToSVGStringBuffer(sb);

	    sb.append("\n</svg> ");

	    return sb.toString();

	}


    }



}
