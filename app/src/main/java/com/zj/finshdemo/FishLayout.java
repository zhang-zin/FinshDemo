package com.zj.finshdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

/**
 * @author 79810
 * 控制鱼游动的布局
 */
public class FishLayout extends RelativeLayout {

    private Paint mPaint;
    private ImageView ivFish;
    private DrawableFish fishDrawable;

    private float ripple;
    private int alpha;
    private float touchX;
    private float touchY;

    public FishLayout(Context context) {
        this(context, null);
    }

    public FishLayout(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public FishLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // ViewGroup 默认不执行onDraw()
        setWillNotDraw(false);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);

        ivFish = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ivFish.setLayoutParams(layoutParams);
        fishDrawable = new DrawableFish();
        ivFish.setImageDrawable(fishDrawable);
        addView(ivFish);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setAlpha(alpha);
        canvas.drawCircle(touchX, touchY, ripple * 150, mPaint);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();

        mPaint.setAlpha(100);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "ripple", 0, 1f).setDuration(1000);
        objectAnimator.start();

        makeTrail();
        return super.onTouchEvent(event);
    }

    private void makeTrail() {
        // 鱼的重心：对应ImageView的坐标
        PointF fishRelativeMiddle = fishDrawable.getMiddlePoint();
        // 鱼的重心：绝对坐标
        PointF fishMiddle = new PointF(fishRelativeMiddle.x + ivFish.getX(),
                fishRelativeMiddle.y + ivFish.getY());
        // 鱼的圆心坐标，控制点1
        final PointF headPoint = new PointF(ivFish.getX() + fishDrawable.getHeadPoint().x,
                ivFish.getY() + fishDrawable.getHeadPoint().y);
        // 点击坐标，结束点
        PointF endPoint = new PointF(touchX, touchY);

        float angle = includeAngele(fishMiddle, headPoint, endPoint) / 2;
        float delta = includeAngele(fishMiddle, new PointF(fishMiddle.x + 1, fishMiddle.y), headPoint);

        // 控制点2 的坐标
        PointF controlPoint = fishDrawable.calculatePoint(fishMiddle,
                fishDrawable.getHeadRadius() * 1.6f, angle + delta);

        Path path = new Path();
        path.moveTo(fishMiddle.x - fishRelativeMiddle.x, fishMiddle.y - fishRelativeMiddle.y);
        path.cubicTo(headPoint.x - fishRelativeMiddle.x, headPoint.y - fishRelativeMiddle.y,
                controlPoint.x - fishRelativeMiddle.x, controlPoint.y - fishRelativeMiddle.y,
                touchX - fishRelativeMiddle.x, touchY - fishRelativeMiddle.y);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivFish, "x", "y", path);
        objectAnimator.setDuration(2000);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fishDrawable.setFrequence(1f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fishDrawable.setFrequence(3f);

                // 摆动鱼鳍
                ObjectAnimator finsAnimator = ObjectAnimator.ofFloat(fishDrawable,
                        "finsValue", 0, fishDrawable.getHeadRadius() * 2, 0);
                finsAnimator.setRepeatCount(new Random().nextInt(4));
                finsAnimator.setDuration((new Random().nextInt(1) + 1) * 500);
                finsAnimator.start();
            }
        });

        final PathMeasure pathMeasure = new PathMeasure(path, false);
        final float[] tan = new float[2];
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                animation.getAnimatedValue();
                // 执行了整个周期的百分之多少
                float fraction = animation.getAnimatedFraction();
                pathMeasure.getPosTan(pathMeasure.getLength() * fraction, null, tan);
                float angle = (float) Math.toDegrees(Math.atan2(-tan[1], tan[0]));
                fishDrawable.setFishMainAngle(angle);
            }
        });
        objectAnimator.start();

    }

    public float includeAngele(PointF o, PointF a, PointF b) {
        // cos(AOB)
        // oa * ob = (ax -ox)(bx - ox) + (ay - oy)(by - oy)
        float AOB = (a.x - o.x) * (b.x - o.x) + (a.y - o.y) * (b.y - o.y);
        float oaLength = (float) Math.sqrt((a.x - o.x) * (a.x - o.x) + (a.y - o.y) * (a.y - o.y));
        // ob的长度
        float obLength = (float) Math.sqrt((b.x - o.x) * (b.x - o.x) + (b.y - o.y) * (b.y - o.y));
        float cosAOB = AOB / (oaLength * obLength);

        // 反余弦
        float angleAOB = (float) Math.toDegrees(Math.acos(cosAOB));

        // AB连线与X的夹角的tan值 - OB与x轴的夹角的tan值
        float direction = (a.y - b.y) / (a.x - b.x) - (o.y - b.y) / (o.x - b.x);

        if (direction == 0) {
            if (AOB >= 0) {
                return 0;
            } else {
                return 180;
            }
        } else {
            if (direction > 0) {
                return -angleAOB;
            } else {
                return angleAOB;
            }
        }
    }

    public float getRipple() {
        return ripple;
    }

    public void setRipple(float ripple) {
        // 透明度变化 100 -> 0
        alpha = (int) (100 * (1 - ripple));
        this.ripple = ripple;
    }
}
