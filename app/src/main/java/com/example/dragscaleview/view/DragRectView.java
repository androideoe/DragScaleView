package com.example.dragscaleview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.example.dragscaleview.R;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.ViewCompat;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;
import static android.widget.RelativeLayout.CENTER_HORIZONTAL;
import static android.widget.RelativeLayout.CENTER_IN_PARENT;
import static android.widget.RelativeLayout.CENTER_VERTICAL;

/**
 * Created by zero_android on 2020/6/29.
 * <p>
 * 一个可以自由拖拽、拉伸的view
 */
public class DragRectView extends View implements View.OnTouchListener {
    private static final String TAG = "DragRectView";
    // view移动方向
    private static final int LEFT_TOP = 0x17;
    private static final int RIGHT_TOP = 0x18;
    private static final int LEFT_BOTTOM = 0x19;
    private static final int RIGHT_BOTTOM = 0x20;
    private static final int TOP = 0x21;
    private static final int LEFT = 0x22;
    private static final int BOTTOM = 0x23;
    private static final int RIGHT = 0x24;
    private static final int CENTER = 0x25;

    private int offset = 40;


    private int dbottom;
    private int dleft;
    private int downx;
    private int downy;
    private int dragDirection;
    private int dright;
    private int dtop;
    private int starh;
    private int starw;
    private int endh;
    private int endw;
    private int lastX;
    private int lastY;

    // view的宽、高初始化
    private int width = 0;
    private int height = 0;
    // 线条的宽度
    private int mLineSize = 3;
    // 图片大小
    private int mRectSize = 50;
    private Paint mPaint;
    private Rect mRect;
    private Paint.Style mStyle;
    private RelativeLayout.LayoutParams mlp;
    // 拉伸的view
    private Bitmap mRightBmp;
    private Rect mRightRect;
    private Bitmap mBottomBmp;
    private Rect mBottomRect;
//    private boolean isMove = false;

    private OnMyTouchListener myTouchListener;

    public DragRectView(Context context) {
        this(context, null);
    }

    public DragRectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    /**
     * 初始化view
     */
    private void init() {
        setBackground(getResources().getDrawable(R.drawable.bg_view_select));
        setOnTouchListener(this);

        mPaint = new Paint();
        mStyle = Paint.Style.STROKE;
        mPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        mPaint.setAntiAlias(true);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                width = 200;
                height = 200;
                mlp = (RelativeLayout.LayoutParams) getLayoutParams();
                invalidate();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mRightBmp = BitmapFactory.decodeResource(getResources(), R.drawable.stretch_right);
        mBottomBmp = BitmapFactory.decodeResource(getResources(), R.drawable.stretch_bottom);
        mRightRect = new Rect();
        mBottomRect = new Rect();
        mRect = new Rect();

    }

    public interface OnMyTouchListener {
        void onClick();
    }

    public void setMyTouchListener(OnMyTouchListener myTouchListener) {
        this.myTouchListener = myTouchListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.getParent().requestDisallowInterceptTouchEvent(true);
        if (myTouchListener != null) {
            myTouchListener.onClick();
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            dleft = v.getLeft();
            dright = v.getRight();
            dtop = v.getTop();
            dbottom = v.getBottom();
            downx = (int) event.getRawX();
            downy = (int) event.getRawY();
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            starw = width;
            starh = height;
            dragDirection = getDirection(v, (int) event.getX(), (int) event.getY());
        }
        handleDrag(v, event, action);
        invalidate();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        mPaint.setStyle(mStyle);
        mPaint.setStrokeWidth((float) mLineSize);
        mRect.set(15, 15, width, height);
        canvas.drawRect(mRect, mPaint);
        mlp.width = (int) (width + 20.0f);
        mlp.height = (int) (height + 20.0f);
        setLayoutParams(mlp);
        canvas.restore();
        if (isSelected()) {
            mRightRect.set(getWidth() - mRectSize, (getHeight() / 2) - (mRectSize / 2), getWidth(), (getHeight() / 2) + (mRectSize / 2));
            canvas.drawBitmap(mRightBmp, null, mRightRect, this.mPaint);
            mBottomRect.set((getWidth() / 2) - (mRectSize / 2), getHeight() - mRectSize, (getWidth() / 2) + (mRectSize / 2), getHeight());
            canvas.drawBitmap(mBottomBmp, null, mBottomRect, this.mPaint);
        }
    }


    private void handleDrag(View v, MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_UP:
                endw = width;
                endh = height;
                if (dragDirection == CENTER) {
                    if (Math.abs(lastX - downx) > 10 || Math.abs(lastY - downy) > 10) {
                        move(v);
                    }
                } else {
                    if (Math.abs(endw - starw) > 5 || Math.abs(endh - starh) > 5) {
                        handleSizeChanged(v);
                    }
                }
                dragDirection = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = ((int) event.getRawX()) - lastX;
                int dy = ((int) event.getRawY()) - lastY;
                switch (dragDirection) {
                    case BOTTOM:
                        bottom(v, dy);
                        break;
                    case RIGHT:
                        right(v, dx);
                        break;
                    case CENTER:
                        center(v, dx, dy);
                        break;
//                    default:
//                        break;
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            default:
                break;
        }
    }

    /**
     * 处理view 大小变化
     *
     * @param v
     */
    private void handleSizeChanged(View v) {
        Log.d(TAG, "...handleSizeChanged ...");
        mlp = (RelativeLayout.LayoutParams) v.getLayoutParams();
        mlp.width = endw + mRectSize / 2;
        mlp.height = endh + mRectSize / 2;
        setHeights((float) endh);
        setWidths((float) endw);
        v.setLayoutParams(mlp);
    }

    /**
     * 处理view move
     *
     * @param v
     */
    private void move(View v) {
        Log.d(TAG, "...move ...");
        mlp = (RelativeLayout.LayoutParams) v.getLayoutParams();
        cleanParams(mlp);
        mlp.setMargins(v.getLeft(), v.getTop(), NotificationManagerCompat.IMPORTANCE_UNSPECIFIED,
                NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        setLayoutParams(mlp);
    }

    /**
     * 通过坐标和移动距离，确定移动方向
     *
     * @param v
     * @param x
     * @param y
     * @return
     */
    private int getDirection(View v, int x, int y) {
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int top = v.getTop();
        if (x < offset && y < offset) {
            return LEFT_TOP;
        }
        if (y < offset && (right - left) - x < offset) {
            return RIGHT_TOP;
        }
        if (x < offset && (bottom - top) - y < offset) {
            return LEFT_BOTTOM;
        }
        if ((right - left) - x < offset && (bottom - top) - y < offset) {
            return RIGHT_BOTTOM;
        }
        if (x < offset) {
            return LEFT;
        }
        if (y < offset) {
            return TOP;
        }
        if ((right - left) - x < offset) {
            return RIGHT;
        }
        if ((bottom - top) - y < offset) {
            return BOTTOM;
        }
        return CENTER;
    }

    public void cleanParams(RelativeLayout.LayoutParams lp) {
        lp.addRule(CENTER_IN_PARENT, 0);
        lp.addRule(ALIGN_PARENT_RIGHT, 0);
        lp.addRule(ALIGN_PARENT_BOTTOM, 0);
        lp.addRule(ALIGN_PARENT_TOP, 0);
        lp.addRule(CENTER_VERTICAL, 0);
        lp.addRule(CENTER_HORIZONTAL, 0);
    }

    public void setHeights(float height) {
        this.height = (int) (0.5f + height);
    }

    public void setWidths(float width) {
        this.width = (int) (0.5f + width);
    }

    private void bottom(View v, int dy) {
        Log.d(TAG, "...bottom ..." + dy);
        height += dy;
        if (height < 100) {
            height = 100;
        }
    }

    private void right(View v, int dx) {
        Log.d(TAG, "...right ..." + dx);
        width += dx;
        if (width < 100) {
            width = 100;
        }
    }

    private void center(View v, int dx, int dy) {
        Log.d(TAG, "...center ..." + dx + "/" + dy);
        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;
        cleanParams(mlp);
        mlp.setMargins(left, top, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
        setLayoutParams(mlp);
    }


}
