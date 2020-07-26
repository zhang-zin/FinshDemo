package com.zj.finshdemo;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author 79810
 */
public class DrawableFish extends Drawable {

    private Path mPath;
    private Paint mPaint;
    //透明度
    private int otherAlpha = 110;
    private int bodyAlpha = 200;

    /* 鱼的数据 start */
    //鱼的重心
    private PointF middlePoint;

    //鱼的朝向
    private float fishMainAngle = 0;
    //鱼头半径
    private float headRadius = 100;
    //鱼的身体长度
    private float bodyLength = 3.2f * headRadius;
    //寻找鱼鳍起始坐标的线长
    private float findFishLength = 0.9f * headRadius;
    //鱼鳍的长度
    private float finsLength = 1.3f * headRadius;
    //大圆的半径
    private float bigCircleRadius = 0.7f * headRadius;
    //中圆的半径
    private float middleCircleRadius = 0.6f * bigCircleRadius;
    //小圆半径
    private float smallCircleRadius = 0.4f * middleCircleRadius;
    //寻找尾部中圆圆心的线长
    private final float findMiddleCircleLength = bigCircleRadius * (0.6f + 1);
    //寻找尾部小圆圆心的线长
    private final float findSmallCircleLength = middleCircleRadius * (0.4f + 2.7f);
    // --寻找大三角形底边中心点的线长
    private final float findTriangleLength = middleCircleRadius * 2.7f;

    public DrawableFish() {
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setARGB(otherAlpha, 244, 92, 71);

        middlePoint = new PointF(4.19f * headRadius, 4.19f * headRadius);

    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (4.19f * 2 * headRadius);
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (4.19f * 2 * headRadius);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float fishAngle = fishMainAngle;
        mPaint.setStrokeWidth(10);
        canvas.drawPoint(middlePoint.x, middlePoint.y, mPaint);

        // 鱼头的圆心坐标
        PointF headPoint = calculatePoint(middlePoint, bodyLength / 2, fishAngle);
        canvas.drawCircle(headPoint.x, headPoint.y, headRadius, mPaint);
        canvas.drawPoint(headPoint.x, headPoint.y, mPaint);

        // 画右鱼鳍
        PointF rightFinPoint = calculatePoint(headPoint, findFishLength, fishAngle - 110);
        canvas.drawPoint(rightFinPoint.x, rightFinPoint.y, mPaint);
        makeFins(canvas, rightFinPoint, fishAngle, true);

        // 画左鱼鳍
        PointF leftPoint = calculatePoint(headPoint, findFishLength, fishAngle + 110);
        makeFins(canvas, leftPoint, fishAngle, false);

        PointF bodyBottomCenterPoint = calculatePoint(headPoint, bodyLength, fishAngle - 180);
        // 画节肢1
        PointF middleCenterPoint = makeSegment(canvas, bodyBottomCenterPoint, bigCircleRadius, middleCircleRadius
                , findMiddleCircleLength, fishAngle, true);
        // 绘制节肢2
        makeSegment(canvas, middleCenterPoint, middleCircleRadius, smallCircleRadius,
                findSmallCircleLength, fishAngle, false);
        // 尾巴
        makeTail(canvas, middleCenterPoint, findTriangleLength, bigCircleRadius, fishAngle);
        makeTail(canvas, middleCenterPoint, findTriangleLength - 10,
                bigCircleRadius - 20, fishAngle);

        // 绘制身体
        makeBody(canvas, headPoint, bodyBottomCenterPoint, fishAngle);
    }

    private void makeBody(Canvas canvas, PointF headPoint, PointF bodyBottomCenterPoint, float fishAngle) {
        // 身体的四个点求出来
        PointF topLeftPoint = calculatePoint(headPoint, headRadius, fishAngle + 90);
        PointF topRightPoint = calculatePoint(headPoint, headRadius, fishAngle - 90);
        PointF bottomLeftPoint = calculatePoint(bodyBottomCenterPoint, bigCircleRadius,
                fishAngle + 90);
        PointF bottomRightPoint = calculatePoint(bodyBottomCenterPoint, bigCircleRadius,
                fishAngle - 90);

        // 二阶贝塞尔曲线的控制点 --- 决定鱼的胖瘦
        PointF controlLeft = calculatePoint(headPoint, bodyLength * 0.56f,
                fishAngle + 130);
        PointF controlRight = calculatePoint(headPoint, bodyLength * 0.56f,
                fishAngle - 130);

        // 绘制
        mPath.reset();
        mPath.moveTo(topLeftPoint.x, topLeftPoint.y);
        mPath.quadTo(controlLeft.x, controlLeft.y, bottomLeftPoint.x, bottomLeftPoint.y);
        mPath.lineTo(bottomRightPoint.x, bottomRightPoint.y);
        mPath.quadTo(controlRight.x, controlRight.y, topRightPoint.x, topRightPoint.y);
        mPaint.setAlpha(bodyAlpha);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 绘制尾巴
     */
    private void makeTail(Canvas canvas, PointF startPoint, float findCenterLength,
                          float findEdgeLength, float fishAngle) {
        // 三角形底边的中心坐标
        PointF centerPoint = calculatePoint(startPoint, findCenterLength, fishAngle - 180);
        // 三角形底边两点
        PointF leftPoint = calculatePoint(centerPoint, findEdgeLength, fishAngle + 90);
        PointF rightPoint = calculatePoint(centerPoint, findEdgeLength, fishAngle - 90);

        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.lineTo(leftPoint.x, leftPoint.y);
        mPath.lineTo(rightPoint.x, rightPoint.y);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * @param bottomCenterPoint     下底的中心点
     * @param bigRadius             大圆半径
     * @param smallRadius           小圆半径
     * @param findSmallCircleLength 梯形的高
     * @param hasBigCircle          是否有大圆
     */
    private PointF makeSegment(Canvas canvas, PointF bottomCenterPoint, float bigRadius, float smallRadius,
                               float findSmallCircleLength, float fishAngle, boolean hasBigCircle) {
        // 梯形上底圆的圆心
        PointF upperCenterPoint = calculatePoint(bottomCenterPoint, findSmallCircleLength,
                fishAngle - 180);
        // 梯形的四个点
        PointF bottomLeftPoint = calculatePoint(bottomCenterPoint, bigRadius, fishAngle + 90);
        PointF bottomRightPoint = calculatePoint(bottomCenterPoint, bigRadius, fishAngle - 90);
        PointF upperLeftPoint = calculatePoint(upperCenterPoint, smallRadius, fishAngle + 90);
        PointF upperRightPoint = calculatePoint(upperCenterPoint, smallRadius, fishAngle - 90);

        if (hasBigCircle) {
            // 绘制大圆
            canvas.drawCircle(bottomCenterPoint.x, bottomCenterPoint.y, bigRadius, mPaint);
        }
        // 绘制小圆
        canvas.drawCircle(upperCenterPoint.x, upperCenterPoint.y, smallRadius, mPaint);

        // 绘制梯形
        mPath.reset();
        mPath.moveTo(upperLeftPoint.x, upperLeftPoint.y);
        mPath.lineTo(upperRightPoint.x, upperRightPoint.y);
        mPath.lineTo(bottomRightPoint.x, bottomRightPoint.y);
        mPath.lineTo(bottomLeftPoint.x, bottomLeftPoint.y);
        canvas.drawPath(mPath, mPaint);

        return upperCenterPoint;

    }

    /**
     * 绘制鱼鳍
     *
     * @param startPoint 鱼鳍起始点
     * @param isRight    是否为右鱼鳍
     */
    private void makeFins(Canvas canvas, PointF startPoint, float fishAngle, boolean isRight) {
        // 鱼鳍的终点，二阶贝塞尔曲线的终点
        PointF endPoint = calculatePoint(startPoint, finsLength, fishAngle - 180);
        //控制点
        PointF controlPoint = calculatePoint(startPoint, finsLength * 1.8f,
                isRight ? fishAngle - 115 : fishAngle + 115);
        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 求角的坐标
     *
     * @param startPoint 起始点
     * @param length     起始点与终点的长度
     * @param angle      起始点与终点的夹角
     * @return 终点的坐标
     */
    private PointF calculatePoint(PointF startPoint, float length, float angle) {
        float daltaX = (float) (Math.cos(Math.toRadians(angle)) * length);
        float daltaY = (float) (Math.sin(Math.toRadians(angle - 180)) * length);
        return new PointF(daltaX + startPoint.x, daltaY + startPoint.y);
    }
}
