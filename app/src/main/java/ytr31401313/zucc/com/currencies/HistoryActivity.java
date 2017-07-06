package ytr31401313.zucc.com.currencies;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * Created by yangzimu on 2017/7/4.
 */

public class HistoryActivity extends AppCompatActivity{

    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> ForCode = new ArrayList<>();
    private ArrayList<String> HomCode = new ArrayList<>();
    private ArrayList<String> time = new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();

    private SearchView mSearchView;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mListView = (ListView)findViewById(R.id.list_view);
        mSearchView = (SearchView)findViewById(R.id.search_view);

        SQLiteDatabase db =openOrCreateDatabase("History.db", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select id,ForCode,HomCode,time from history", null);

        cursor.moveToFirst();
        if (cursor.isFirst()) {
            id.add(cursor.getString(0));
            ForCode.add(cursor.getString(1));
            HomCode.add(cursor.getString(2));
            time.add(cursor.getString(3));
        }
        while (cursor.moveToNext()) {
            id.add(cursor.getString(0));
            ForCode.add(cursor.getString(1));
            HomCode.add(cursor.getString(2));
            time.add(cursor.getString(3));
        }

        cursor.close();
        db.close();

        for (int i=0;i<id.size();i++){
            mData.add(id.get(i)+" "+ForCode.get(i)+" "+HomCode.get(i)+" "+time.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(HistoryActivity.this,android.R.layout.simple_list_item_1,mData);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(true);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)){
                    mListView.setFilterText(s);
                }else{
                    mListView.clearTextFilter();
                }
                return false;
            }
        });
    }

}
