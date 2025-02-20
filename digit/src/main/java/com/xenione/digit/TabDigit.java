package com.xenione.digit;
/**
 * ----
 // android
 drawBackground 0
 drawBackground Matrix : [1.0, 0.0, 0.0][0.0, 0.0, 0.0][0.0, -0.0017361111, 1.0]
 drawBackground 1
 drawBackground Matrix : [1.0, 0.0, 0.0][0.0, 1.0, 0.0][0.0, 0.0, 1.0]
 drawBackground 2
 drawBackground Matrix : [1.0, 0.0, 0.0][0.0, 0.89100635, 0.0][0.0, 7.881782E-4, 1.0]

 //hmos
 drawBackground: 0
 drawBackground: Matrix{[1.0, 0.0, 0.0][0.0, -1.0, 0.0][0.0, 0.0, 1.0]}
 drawBackground: 1
 drawBackground: Matrix{[1.0, 0.0, 0.0][0.0, 1.0, 0.0][0.0, 0.0, 1.0]}
 drawBackground: 2
 drawBackground: Matrix{[1.0, 0.0, 0.0][0.0, 0.30901697, 0.0][0.0, -0.0016511398, 1.0]}
 * ----
 *
 * TODO: find out why the top is not rendering
 *
 * TODO: find out why hmos draw pattern is slow ? that's why the animation is slow
 *
 */

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.AttrHelper;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.impl.AgpEngineFactory;
import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Rect;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Eugeni on 16/10/2016.
 */
public class TabDigit extends Component implements Runnable, Component.DrawTask,
        Component.EstimateSizeListener, Component.LayoutRefreshedListener {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "-MainAbility-");

    private static final String TAG = "TabDigit";
    /*
     * false: rotate upwards
     * true: rotate downwards
     */
    private boolean mReverseRotation = false;

    private Tab mTopTab;

    private Tab mBottomTab;

    private Tab mMiddleTab;
    Engine engine;
    TaskDispatcher uiTaskDispatcher;

    //#region internal variables
    private List<Tab> tabs = new ArrayList<>(3);

    private AbstractTabAnimation tabAnimation;

    private Matrix mProjectionMatrix = new Matrix();

    private int mCornerSize;

    private Paint mNumberPaint;

    private Paint mDividerPaint;

    private Paint mBackgroundPaint;
    private Paint mBackgroundPaint1;
    private Paint mBackgroundPaint2;
    private Paint mBackgroundPaint3;

    private Rect mTextMeasured = new Rect();

    private int mPadding = 0;

    private char[] mChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    //#endregion internal variables
    EventHandler eventHandler;
    //#region constructor

    public TabDigit(Context context) {
        super(context);
        init(context, null);
    }

    public TabDigit(Context context, AttrSet attrSet) {
        super(context, attrSet);
        init(context, attrSet);
    }

    public TabDigit(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        init(context, attrSet);
    }

    public TabDigit(Context context, AttrSet attrSet, int resId) {
        super(context, attrSet, resId);
        init(context, attrSet);
    }

    //#endregion constructor

    //#region initialize

    public void init(Context context, AttrSet attrs) {
        addDrawTask(this);
        setEstimateSizeListener(this);
        setLayoutRefreshedListener(this);
        eventHandler=new EventHandler(EventRunner.getMainEventRunner());
        initPaints();


        int padding = AttrHelper.fp2px(10,getContext());
        int textSize = 165;//AttrHelper.fp2px(60,getContext());
        int cornerSize = 30;
        int textColor = 1;
        int backgroundColor = 1;
        boolean reverseRotation = false;
        /**
         TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TabDigit, 0, 0);
         final int num = ta.getIndexCount();
         for (int i = 0; i < num; i++) {
         int attr = ta.getIndex(i);
         if (attr == R.styleable.TabDigit_textSize) {
         textSize = ta.getDimensionPixelSize(attr, -1);
         Log.d(TAG, "init: textSize " + textSize);
         } else if (attr == R.styleable.TabDigit_padding) {
         padding = ta.getDimensionPixelSize(attr, -1);
         } else if (attr == R.styleable.TabDigit_cornerSizeZ) {
         cornerSize = ta.getDimensionPixelSize(attr, -1);
         } else if (attr == R.styleable.TabDigit_textColor) {
         textColor = ta.getColor(attr, 1);
         } else if (attr == R.styleable.TabDigit_backgroundColor) {
         backgroundColor = ta.getColor(attr, 1);
         } else if (attr == R.styleable.TabDigit_reverseRotation) {
         reverseRotation = ta.getBoolean(attr, false);
         }
         }
         ta.recycle();
         *
         */

        HiLog.warn(LABEL_LOG, "TabDigit: textSize "+textSize);
        if (textSize > 0) {
            mNumberPaint.setTextSize(textSize);
        }
        if (padding > 0) {
            mPadding = padding;
        }
        if (cornerSize > 0) {
            mCornerSize = cornerSize;
        }


        /** set values
         *



         if (textColor < 1) {
         mNumberPaint.setColor(textColor);
         }

         if (backgroundColor < 1) {
         mBackgroundPaint.setColor(backgroundColor);
         }
         */

        mReverseRotation = reverseRotation;
        mReverseRotation = true;

        Optional<Engine> optionalEngine = AgpEngineFactory.createEngine();
        engine = optionalEngine.get();
        uiTaskDispatcher = getContext().getUITaskDispatcher();
        initTabs();

    }


    private void initPaints() {
        mNumberPaint = new Paint();
        mNumberPaint.setAntiAlias(true);
        mNumberPaint.setStyle(Paint.Style.FILLANDSTROKE_STYLE);
        mNumberPaint.setColor(Color.WHITE);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStyle(Paint.Style.FILLANDSTROKE_STYLE);
        mDividerPaint.setColor(Color.WHITE);
        mDividerPaint.setStrokeWidth(1);

        mBackgroundPaint = new Paint();
        mBackgroundPaint1 = new Paint();
        mBackgroundPaint2 = new Paint();
        mBackgroundPaint3 = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint1.setColor(Color.BLUE);
        mBackgroundPaint2.setColor(Color.YELLOW);
        mBackgroundPaint3.setColor(Color.MAGENTA);

    }

    private void initTabs() {
        animatorValue.setDelay(40);
        animatorValue.setDuration(1000);
        animatorValue.setLoopedCount(Animator.INFINITE);
        // top Tab
        mTopTab = new Tab();
        mTopTab.rotate(180);
        tabs.add(mTopTab);

        // bottom Tab
        mBottomTab = new Tab();
        tabs.add(mBottomTab);

        // middle Tab
        mMiddleTab = new Tab();
        tabs.add(mMiddleTab);

        tabAnimation = mReverseRotation ? new TabAnimationDown(mTopTab, mBottomTab, mMiddleTab) : new TabAnimationUp(mTopTab, mBottomTab, mMiddleTab);

        tabAnimation.initMiddleTab();

        setInternalChar(0);
    }
    //#endregion initialize

    //#region some calculation not important

    public void setChar(int index) {
        setInternalChar(index);
        invalidate();
    }

    private void setInternalChar(int index) {
        for (Tab tab : tabs) {
            tab.setChar(index);
        }
    }


    private void setupProjectionMatrix() {
        mProjectionMatrix.reset();
        int centerY = getHeight() / 2;
        int centerX = getWidth() / 2;
        HiLog.warn(LABEL_LOG, String.format("TabDigit: setupProjectionMatrix(centerX: %s, centerY: %s)", centerX,centerY));
        MatrixHelper.translate(mProjectionMatrix, centerX,centerY , 0);
    }

    private void measureTabs(int width, int height) {
        for (Tab tab : tabs) {
            tab.measure(width, height);
        }
    }

    private void drawTabs(Canvas canvas) {
        int i = 0;
        for (Tab tab : tabs) {
            tab.draw(canvas, i);
            i++;
        }
    }

    private void drawDivider(Canvas canvas) {
        canvas.save();
        canvas.concat(mProjectionMatrix);
        canvas.drawLine(
                -canvas.getLocalClipBounds().getWidth() / 2,
                0,
                canvas.getLocalClipBounds().getWidth() / 2,
                0,
                mDividerPaint
        );
        canvas.restore();
    }

    private void calculateTextSize(Rect rect) {
       Rect rect1 = mNumberPaint.getTextBounds(
                "8"
                //, 0, 1, rect
        );
       HiLog.warn(LABEL_LOG, "TabDigit: calculateTextSize "+rect1);
        rect.modify(rect1);
    }
    //#endregion some calculation not important


    //#region getter & setter

    public void setTextSize(int size) {
        mNumberPaint.setTextSize(size);
        invalidate();
//        requestLayout();
    }

    public int getTextSize() {
        return (int) mNumberPaint.getTextSize();
    }

    public void setPadding(int padding) {
        mPadding = padding;
        invalidate();
//        requestLayout();
    }

    /**
     * Sets chars that are going to be displayed.
     * Note: <b>That only one digit is allow per character.</b>
     *
     * @param chars
     */
    public void setChars(char[] chars) {
        mChars = chars;
    }

    public char[] getChars() {
        return mChars;
    }


    public void setDividerColor(Color color) {
        mDividerPaint.setColor(color);
    }

    // todo: fix the clash
    public int getMPadding() {
        return mPadding;
    }

    public void setTextColor(Color color) {
        mNumberPaint.setColor(color);
    }

    public Color getTextColor() {
        return mNumberPaint.getColor();
    }

    public void setCornerSize(int cornerSize) {
        mCornerSize = cornerSize;
        invalidate();
    }

    public int getCornerSize() {
        return mCornerSize;
    }

    public void setBackgroundColor(Color color) {
        mBackgroundPaint.setColor(color);
    }

    public Color getBackgroundColor() {
        return mBackgroundPaint.getColor();
    }
    //#endregion getter & setter

    public void start() {
        HiLog.warn(LABEL_LOG, "TabDigit: start");
        internalCounter=0;
        tabAnimation.start();
        invalidate();
    }

    int internalCounter = 0;
    @Override
    public void run() {
        HiLog.warn(LABEL_LOG, "TabDigit: run");

        tabAnimation.run();
        invalidate();
    }

    public void sync() {
        tabAnimation.sync();
        invalidate();
    }


    /**
     * Measure dimension from estimate dimension.
     *
     * @param defaultSize Default width/height.
     * @param measureSpec estimate width/height.
     * @return Measured dimension.
     */
    private int measureDimension(int defaultSize, int measureSpec) {
        int result;
        int specMode = EstimateSpec.getMode(measureSpec);
        int specSize = EstimateSpec.getSize(measureSpec);
        if (specMode == EstimateSpec.PRECISE) {
            result = specSize;
        } else {
            result = defaultSize; // UNSPECIFIED
            if (specMode == EstimateSpec.NOT_EXCEED) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    //#region listeners

    int oldWidth = -1;
    int oldHeight = -1;

    @Override
    public void onRefreshed(Component component) {
        HiLog.warn(LABEL_LOG, "TabDigit: onRefreshed");
        if (oldWidth != component.getWidth() || oldHeight != component.getHeight()) {
            setupProjectionMatrix();
        }else{
//            setupProjectionMatrix();
            HiLog.warn(LABEL_LOG, "TabDigit: onRefreshed skip...");

        }
        oldWidth = component.getWidth();
        oldHeight = component.getHeight();
        HiLog.warn(LABEL_LOG, String.format("TabDigit: onRefreshed(height: %s,width: %s)", oldHeight,oldWidth));
        HiLog.warn(LABEL_LOG, "----------------------------");
        HiLog.warn(LABEL_LOG, String.format("TabDigit: onRefreshed(height: %s,width: %s)", getHeight(),getWidth()));
    }

    //    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        Log.d(TAG, "onSizeChanged: ");
//        if (w != oldw || h != oldh) {
//            setupProjectionMatrix();
//        }
//    }


    @Override
    public boolean onEstimateSize(int widthMeasureSpec, int heightMeasureSpec) {
        HiLog.warn(LABEL_LOG, "TabDigit: onEstimateSize "+mTextMeasured);
        calculateTextSize(mTextMeasured);
        HiLog.warn(LABEL_LOG, "TabDigit: onEstimateSize "+mTextMeasured);

        int childWidth = mTextMeasured.getWidth() + mPadding;
        int childHeight = mTextMeasured.getHeight() + mPadding;
        measureTabs(childWidth, childHeight);
        HiLog.warn(LABEL_LOG, String.format("TabDigit: onEstimateSize measureTabs(%s,%s)", childWidth, childHeight));

        int maxChildWidth = mMiddleTab.maxWith();
        int maxChildHeight = 2 * mMiddleTab.maxHeight();


        // int resolvedWidth = resolveSize(maxChildWidth, widthMeasureSpec);
        // int resolvedHeight = resolveSize(maxChildHeight, heightMeasureSpec);
        int width = measureDimension(maxChildWidth, widthMeasureSpec);
        int height = measureDimension(maxChildHeight, heightMeasureSpec);


        //Do Size Estimation here and don't forgot to call setEstimatedSize(width, height)
        setEstimatedSize(EstimateSpec.getSizeWithMode(width, EstimateSpec.PRECISE),
                EstimateSpec.getSizeWithMode(height, EstimateSpec.PRECISE));
        // setMeasuredDimension(resolvedWidth, resolvedHeight);

        HiLog.warn(LABEL_LOG, String.format("TabDigit: onEstimateSize setEstimatedSize(width:%s, height:%s)", width,height));
        HiLog.warn(LABEL_LOG, String.format("TabDigit: onEstimateSize setEstimatedSize(maxChildWidth:%s, maxChildHeight:%s)", maxChildWidth,maxChildHeight));
        HiLog.warn(LABEL_LOG, String.format("TabDigit: onEstimateSize setEstimatedSize(widthMeasureSpec:%s, heightMeasureSpec:%s)", widthMeasureSpec,heightMeasureSpec));
//        setComponentMinSize(200, 200);
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setRgbColor(new RgbColor(150,150,80));
        setBackground(shapeElement);
        return true;
    }


    AnimatorValue animatorValue = new AnimatorValue();
    @Override
    public void onDraw(Component component, Canvas canvas) {
        internalCounter++;
        HiLog.warn(LABEL_LOG, "TabDigit: onDraw"+internalCounter);
        drawTabs(canvas);
        drawDivider(canvas);
        // ViewCompat.postOnAnimationDelayed(this, this, 40);

//        uiTaskDispatcher.delayDispatch(this, 40);
        eventHandler.postTask(this,100);

    }
    //#endregion listeners

    public class Tab {

        //#region internal variable

        private final Matrix mModelViewMatrix = new Matrix();

        private final Matrix mModelViewProjectionMatrix = new Matrix();

        private final Matrix mRotationModelViewMatrix = new Matrix();

        private final RectF mStartBounds = new RectF();

        private final RectF mEndBounds = new RectF();

        private int mCurrIndex = 0;

        private int mAlpha;

        private Matrix mMeasuredMatrixHeight = new Matrix();

        private Matrix mMeasuredMatrixWidth = new Matrix();
        //#endregion internal variable

        //#region calculation
        public void measure(int width, int height) {
            Rect area = new Rect(-width / 2, 0, width / 2, height / 2);
            mStartBounds.set(area);
            mEndBounds.set(area);
            mEndBounds.offset(0, -height / 2);
        }

        public int maxWith() {
            RectF rect = new RectF(mStartBounds);
            Matrix projectionMatrix = new Matrix();
            MatrixHelper.translate(projectionMatrix, mStartBounds.left, -mStartBounds.top, 0);
            mMeasuredMatrixWidth.reset();
            mMeasuredMatrixWidth.setConcat(projectionMatrix, MatrixHelper.ROTATE_X_90);
            mMeasuredMatrixWidth.mapRect(rect.toRectFloat());
            return (int) rect.getWidth();
        }

        public int maxHeight() {
            RectF rect = new RectF(mStartBounds);
            Matrix projectionMatrix = new Matrix();
            mMeasuredMatrixHeight.reset();
            mMeasuredMatrixHeight.setConcat(projectionMatrix, MatrixHelper.ROTATE_X_0);
            mMeasuredMatrixHeight.mapRect(rect.toRectFloat());
            return (int) rect.getHeight();
        }
        //#endregion calculation

        public void setChar(int index) {
            mCurrIndex = index > mChars.length ? 0 : index;
        }

        public void next() {
            HiLog.warn(LABEL_LOG, "Tab: next");
            mCurrIndex++;
            if (mCurrIndex >= mChars.length) {
                mCurrIndex = 0;
            }
        }

        public void rotate(int alpha) {
            HiLog.warn(LABEL_LOG, String.format("Tab: rotate (%s)", alpha));
            mAlpha = alpha;
            MatrixHelper.rotateX(mRotationModelViewMatrix, alpha);
        }

        public void draw(Canvas canvas, int i) {
            drawBackground(canvas, i);
            drawText(canvas);
        }

        //#region draw

        private void drawBackground(Canvas canvas, int i) {
            Paint p = mBackgroundPaint;
            switch (i) {
                case 1:
                    p = mBackgroundPaint2;
                    break;
                case 2:
                    p = mBackgroundPaint3;
                    break;
                default:
                case 0:
                    p = mBackgroundPaint1;
                    break;

            }
            canvas.save();
            HiLog.warn(LABEL_LOG, "Tab: drawBackground rotate "+mRotationModelViewMatrix);
            mModelViewMatrix.setMatrix(mRotationModelViewMatrix);
            applyTransformation(canvas, mModelViewMatrix);
            canvas.drawRoundRect(mStartBounds.toRectFloat(), mCornerSize, mCornerSize, p);
            canvas.restore();
        }

        private void drawText(Canvas canvas) {
            canvas.save();
            mModelViewMatrix.setMatrix(mRotationModelViewMatrix);
            RectF clip = mStartBounds;
            if (mAlpha > 90) {
                mModelViewMatrix.setConcat(mModelViewMatrix, MatrixHelper.MIRROR_X);
                clip = mEndBounds;
            }

            applyTransformation(canvas, mModelViewMatrix);
            canvas.clipRect(clip.toRectFloat());
            canvas.drawText(
                    mNumberPaint,
                    Character.toString(mChars[mCurrIndex]),
                    -mTextMeasured.getCenterX(),
                    -mTextMeasured.getCenterY()
            );
            canvas.restore();
        }
        //#endregion draw

        private void applyTransformation(Canvas canvas, Matrix matrix) {
            mModelViewProjectionMatrix.reset();
            mModelViewProjectionMatrix.setConcat(mProjectionMatrix, matrix);
            canvas.concat(mModelViewProjectionMatrix);
        }
    }

}
