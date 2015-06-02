package x.keepemup;

    import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Timer;

public class Game extends Activity {

    // blocker related


    // animated view
    private ShapeView mShapeView;
    public int mXCenter;
    public int mYCenter;
    public boolean isFalling;

    // screen size
    private int mWidthScreen;
    private int mHeightScreen;

    // motion parameters
    private final float FACTOR_FRICTION = 0.5f; // imaginary friction on the screen
    private final float GRAVITY = 9.8f; // acceleration of gravity
    private float mAx; // acceleration along x axis
    private float mAy; // acceleration along y axis
    private final float mDeltaT = 0.5f; // imaginary time interval between each acceleration updates


    // timer
    private Timer mTimer;
    private Handler mHandler;
    private boolean isTimerStarted = false;
    private long mStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the screen always portait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // obtain screen width and height
        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mWidthScreen = display.getWidth();
        mHeightScreen = display.getHeight();

        // initializing the view that renders the ball
        mShapeView = new ShapeView(this);
        mShapeView.setOvalCenter(500, 500);

        setContentView(mShapeView);


    }


    public void Moving() {
        // obtain the three accelerations from sensors

        float mAz = 1;

        if (mHeightScreen - 50 > 0) {
            isFalling = true;
        }

        mAy = mAy + 1;
        mAx = Math.signum(mAx) * Math.abs(mAx) * (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY);
        mAy = Math.signum(mAy) * Math.abs(mAy) * (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY);

        try {
            Thread.sleep(15);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // start sensor sensing

    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop senser sensing

    }

    // the view that renders the ball
    private class ShapeView extends SurfaceView implements SurfaceHolder.Callback {

        private final int RADIUS = 50;
        private final float FACTOR_BOUNCEBACK = 0.75f;

        private RectF mRectF;
        private final Paint mPaint;
        private ShapeThread mThread;

        private float mVx;
        private float mVy;

        private float x, y;
        private Bitmap bmp;
        private float width = 150;
        private float height = 50.0f;

        private boolean touched = false;


        public ShapeView(Context context) {
            super(context);

            getHolder().addCallback(this);
            mThread = new ShapeThread(getHolder(), this);
            setFocusable(true);

            mPaint = new Paint();
            mPaint.setColor(Color.CYAN);
            mPaint.setAlpha(192);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);

            mRectF = new RectF();

            x = y = 0;
        }

        // set the position of the ball
        public boolean setOvalCenter(int x, int y) {
            mXCenter = x;
            mYCenter = y;
            return true;
        }

        // calculate and update the ball's position
        public boolean updateOvalCenter() {
            mVx -= mAx * mDeltaT;
            mVy += mAy * mDeltaT;

            mXCenter += (int) (mDeltaT * (mVx + 0.5 * mAx * mDeltaT));
            mYCenter += (int) (mDeltaT * (mVy + 0.5 * mAy * mDeltaT));


            if (mXCenter < RADIUS) {
                mXCenter = RADIUS;
                mVx = -mVx * FACTOR_BOUNCEBACK;
            }

            if (mXCenter > mWidthScreen - RADIUS) {
                mXCenter = mWidthScreen - RADIUS;
                mVx = -mVx * FACTOR_BOUNCEBACK;
            }

            if (mYCenter < RADIUS) {
                mYCenter = RADIUS;
                mVy = -mVy * FACTOR_BOUNCEBACK;
            }

            if (mYCenter > mHeightScreen - 2 * RADIUS) {
                mYCenter = mHeightScreen - 2 * RADIUS;
                mVy = -mVy * FACTOR_BOUNCEBACK;
            }

            return true;
        }

        // update the canvas
        protected void onDraw(Canvas canvas) {
            if (mRectF != null) {
                mRectF.set(mXCenter - RADIUS, mYCenter - RADIUS, mXCenter + RADIUS, mYCenter + RADIUS);
                canvas.drawColor(Color.BLACK);
                canvas.drawOval(mRectF, mPaint);
            }


            if (touched) {
                canvas.drawRect(x, y, x + width, y + height, mPaint);

                if ((mYCenter > y-1.5 * RADIUS) && (mXCenter > x - RADIUS  || mXCenter < (x+width)- RADIUS)) {
                    mYCenter = (int)y - 2 * RADIUS;
                    mVy = -mVy * FACTOR_BOUNCEBACK;
                    //canvas.drawColor(Color.BLACK);

                }


            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mThread.setRunning(true);
            mThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            mThread.setRunning(false);
            while (retry) {
                try {
                    mThread.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touched = true;
                    //getting the touched x and y position
                    x = event.getX();
                    y = event.getY();
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:

                    break;

                case MotionEvent.ACTION_UP:


                    break;


                case MotionEvent.ACTION_CANCEL:

                    break;

                default:
                    // do nothing
                    break;
            }
            return super.onTouchEvent(event);

        }
    }

        class ShapeThread extends Thread {
            private SurfaceHolder mSurfaceHolder;
            private ShapeView mShapeView;
            private boolean mRun = false;

            public ShapeThread(SurfaceHolder surfaceHolder, ShapeView shapeView) {
                mSurfaceHolder = surfaceHolder;
                mShapeView = shapeView;
            }

            public void setRunning(boolean run) {
                mRun = run;
            }

            public SurfaceHolder getSurfaceHolder() {
                return mSurfaceHolder;
            }

            @Override
            public void run() {
                Canvas c;
                while (mRun) {
                    mShapeView.updateOvalCenter();
                    Moving();
                    c = null;
                    try {
                        c = mSurfaceHolder.lockCanvas(null);
                        synchronized (mSurfaceHolder) {
                            mShapeView.onDraw(c);
                        }
                    } finally {

                        if (c != null) {
                            mSurfaceHolder.unlockCanvasAndPost(c);
                        }
                    }
                }
            }
        }
    }



