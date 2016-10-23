package com.kk.wuziqi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Gaojingang on 2016/10/23.
 */

public class WuziqiPannel extends View {

    private String TAG = "WuziqiPannel";

    //棋盘宽度
    private int mPannelWidth;
    //每一行的高度
    private float mLineHeight;

    //最大支持多少行 10x10
    private int MAX_LINE = 10;
    //最大计数为5时 ，计算赢得比赛
    private int MAX_COUNT_IN_LINE = 5;

    //画笔
    private Paint mPaint;
    //白棋 图片
    private Bitmap mWhitePiece;
    //黑棋 图片
    private Bitmap mBlackPiece;
    //旗子图片与棋盘每一个格的缩放比例 3/4
    private float ratioPieceOfLineHeight = 3 * 1.0f / 4;

    //白棋先手 ，或者当前是谁该出了
    private boolean mIsWhite = true;

    //记录所有白棋的点
    private List<Point> mWhiteArray;
    //记录所有黑棋的点
    private List<Point> mBlackArray;

    //游戏结束标志
    private boolean mIsGameOver;
    //记录游戏胜利者
    private boolean mIsWhiteWinner;


    public WuziqiPannel(Context context, AttributeSet attrs) {

        super(context, attrs);

        setBackgroundColor(0x44ff0000);
        init();

    }

    //初始化函数 初始化画笔，以及图片资源，棋子点坐标
    private void init() {
        mPaint = new Paint();
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);

        mWhiteArray = new ArrayList<Point>();
        mBlackArray = new ArrayList<Point>();

    }

    public WuziqiPannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //重新测量函数 适配不同的宽度大小，重新设置屏幕大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = width;
        }

        setMeasuredDimension(width, width);

    }

    //当size发生改变时，重新计算 棋盘 大小，重新计算 棋盘高度 以及图片大小
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i(TAG, "onSizeChanged: ");

        super.onSizeChanged(w, h, oldw, oldh);
        mPannelWidth = w;
        mLineHeight = mPannelWidth * 1.0f / MAX_LINE;

        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);

        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);


    }

    //响应屏幕事件，当有Up事件，以及Down事件的时候，进行处理其余事件不处理，当游戏结束之后，不在处理事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver) {
            return false;
        }

        int action = event.getAction();
        if (MotionEvent.ACTION_UP == action) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getValidPoint(x, y);

            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {

                return false;
            }

            if (mIsWhite) {
                mWhiteArray.add(p);
            } else {
                mBlackArray.add(p);
            }
            invalidate();
            mIsWhite = !mIsWhite;
            return true;
        }
        return true;
    }

    //根据x,y构造一个点
    private Point getValidPoint(int x, int y) {

        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    /**
     * 绘制函数
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        drawBoard(canvas);

        drawPiece(canvas);

        checkGame();
    }


    /**
     * 绘制棋盘线框
     * @param canvas
     */
    private void drawBoard(Canvas canvas) {

        int w = mPannelWidth;
        float lineHeight = mLineHeight;
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (lineHeight / 2);
            int endX = (int) (w - (lineHeight / 2));

            int y = (int) ((0.5 + i) * lineHeight);

            canvas.drawLine(startX, y, endX, y, mPaint);
            canvas.drawLine(y, startX, y, endX, mPaint);
        }
    }


    /**
     * 绘制棋子
     * @param canvas
     */
    private void drawPiece(Canvas canvas) {

        //绘制白色棋子
        for (int i = 0, n = mWhiteArray.size(); i < n; i++) {

            Point whitePoint = mWhiteArray.get(i);
            //棋子的 x y 坐标计算，是基于 棋盘最小单位缩小ratioPieceOfLineHeight 3/4 的比例，所以在计算的时候，需要减去 ratioPieceOfLineHeight ，并且除2 获取到对应的x坐标，
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (whitePoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight, null);
        }

        for (int i = 0, n = mBlackArray.size(); i < n; i++) {

            Point whitePoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (whitePoint.x + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight,
                    (whitePoint.y + (1 - ratioPieceOfLineHeight) / 2) * mLineHeight, null);
        }

    }


    /**
     * 检测游戏胜利者
     *
     */
    private void checkGame() {

        boolean whiteWin = checkeFiveInLine(mWhiteArray);
        boolean blackWin = checkeFiveInLine(mBlackArray);

        if (whiteWin || blackWin) {
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;

            String winStr = mIsWhiteWinner ? "白棋胜利" : "黑棋胜利";
            Toast.makeText(getContext(), winStr, Toast.LENGTH_LONG).show();
        }


    }

    /**
     * 检测棋子是否 五子连珠
     * @param points
     * @return
     */
    private boolean checkeFiveInLine(List<Point> points) {

        for (Point p : points) {

            int x = p.x;
            int y = p.y;
            boolean win = checkHorizontal(x, y, points);
            if(win) return true;
            win = checkVertical(x,y,points);
            if(win) return true;
            win = checkLeftDiagonal(x,y,points);
            if(win) return true;
            win = checkRightDiagonal(x,y,points);
            if(win) return true;
        }

        return false;
    }


    /**
     *
     * 检测水平方向是否 五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {

        Log.i(TAG, "checkHorizontal: points = " + points.toString());

        int count = 1;
        //计算中心棋子  ← 左边是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
                Log.i(TAG, "checkHorizontal: left count =" + count);
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            Log.i(TAG, "checkHorizontal: left count is five .count =" + count  +", points = " + points.toString());
            return true;
        }

        //计算中心棋子 → 右边是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
                Log.i(TAG, "checkHorizontal: right count ++ ,count =" + count );

            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            Log.i(TAG, "checkHorizontal: right count is five .count =" + count +", points = " + points.toString());
            return true;
        }
        return false;
    }


    /**
     * 计算竖直方向 五子连珠
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x, int y, List<Point> points) {

        int count = 1;
        //检测中心棋子 ↑ 上部分是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            return true;
        }
        //检测中心棋子 ↓ 下部分是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            return true;
        }
        return false;
    }


    /**
     *
     * 检测棋子 左斜边 五子连珠
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {

        int count = 1;
        //检测 ↙ 方向是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            return true;
        }
        //检测 ↗ 方向是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            return true;
        }
        return false;
    }


    /**
     *
     * 检测棋子 右斜边 五子连珠
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {

        int count = 1;
        //检测 ↖ 方向是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            return true;
        }
        //检测 ↘ 方向是否五子连珠
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }

        if (MAX_COUNT_IN_LINE == count) {
            return true;
        }
        return false;
    }

}
