package ytr31401313.zucc.com.currencies;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by yangzimu on 2017/7/5.
 */

public class ChartActivity extends AppCompatActivity {

    private ChartView mChartView;
    private ArrayList<Double> CNYrate = new ArrayList<>();
    private ArrayList<String> time = new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();
    private Button mReset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        mChartView = (ChartView) findViewById(R.id.chartView);
        mReset=(Button) findViewById(R.id.btn_reset);

        final SQLiteDatabase db =openOrCreateDatabase("History.db", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select Rate,time from CNY_Rate", null);



        cursor.moveToFirst();
        if (cursor.isFirst()) {
            CNYrate.add(Double.valueOf(cursor.getString(0)));
            time.add(cursor.getString(1));
        }
        while (cursor.moveToNext()) {
            CNYrate.add(Double.valueOf(cursor.getString(0)));
            time.add(cursor.getString(1));
        }

        cursor.close();
        db.close();

        mChartView.setData(CNYrate,time);
    }

}
