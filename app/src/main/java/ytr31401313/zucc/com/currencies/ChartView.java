package ytr31401313.zucc.com.currencies;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by yangzimu on 2017/7/5.
 */
public class ChartView extends View{

    private ArrayList<Double> mRates = new ArrayList<>();
    private ArrayList<String> mTimes = new ArrayList<>();
    // 创建画笔
    Paint p = new Paint();
    private int cur_x;
    private int cur_y;

    public ChartView(Context context) {
        super(context);
    }
    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void setData(ArrayList<Double> rate, ArrayList<String> time){
        this.mRates = rate;
        this.mTimes = time;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        p.setStrokeWidth(5);
        p.setTextSize(50);

        p.setColor(Color.BLUE);// 设置蓝色
        p.setTextSize(80);
        canvas.drawText("汇率", 150, 300, p);
        canvas.drawText("时间", 900, 900, p);
        canvas.drawLine(100, 300, 100, 800, p);// 画线 Y坐标
        canvas.drawLine(100, 800, 1000, 800, p);// 画线 X坐标


        p.setTextSize(25);//定义字体大小
        canvas.drawLine(100,400,1000,400,p); //7.0上限
        canvas.drawText("6.9", 50, 400, p);

        canvas.drawText("6.7", 60, 800+10, p);


        if(mRates.isEmpty())
            Log.i("+++++++++++","数据为空，无法创建折线图");
        else if(mRates.size()==1)
            Log.i("+++++++++++","数据太少，无法创建折线图");
        else {

            int limit = mRates.size()+100;
            

            for (int i = 100; i < limit; i = i + 100) {
                limit = limit+100;
                for(int j = 0;j<mRates.size();j++) {
                    int limit2 = 100;
                    p.setColor(Color.BLUE);// 设置蓝色
                    canvas.rotate(90,limit2+j*20, 850);
                    canvas.drawText(mTimes.get(j).substring(8), limit2+j*20, 850, p); //X坐标值
                    canvas.rotate(-90,limit2+j*20, 850);
                    if (j<mRates.size()-1) {
                        p.setColor(Color.RED);
                        p.setStrokeWidth(10);
                        canvas.drawPoint(limit2+j*20,1000-(Float.valueOf(mRates.get(j).toString())*1000-6600)*2,p);
                        p.setColor(Color.BLACK);// 设置黑色
                        p.setStrokeWidth(5);
                        canvas.drawLine(limit2+j*20, 1000-(Float.valueOf(mRates.get(j).toString())*1000-6600)*2, limit2+(j+1)*20, 1000-(Float.valueOf(mRates.get(j + 1).toString())*1000-6600)*2, p); //画线
                       // canvas.drawText(mRates.get(j).toString(),limit2+j*20, Float.valueOf(mRates.get(j).toString())*50+50,p);
                    }
                    else
                        canvas.drawText(mRates.get(j).toString(),limit2+j*20,1000-(Float.valueOf(mRates.get(j).toString())*1000-6600)*2,p);
                }
                Log.i("+++++++++++","绘制完成");
                break;
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cur_x = (int) event.getX();
                cur_y = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x =  (int) event.getX();
                int y =  (int) event.getY();
                moveView(x-cur_x, y-cur_y);
                cur_x = x;
                cur_y = y;
        }
        return true;
    }

    public void moveView(int offsetX, int offsetY){
        if(offsetX ==0 && offsetY==0)
            return;

        int left = this.getLeft() + offsetX;
        int top = this.getTop() + offsetY;
        int right = this.getRight() + offsetX;
        int bottom = this.getBottom() + offsetY;
        this.layout(left, top, right, bottom);
    }


}
