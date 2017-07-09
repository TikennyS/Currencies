package ytr31401313.zucc.com.currencies;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * Created by yangzimu on 2017/7/4.
 */

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> ForCode = new ArrayList<>();
    private ArrayList<String> HomCode = new ArrayList<>();
    private ArrayList<String> time = new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();

    private SearchView mSearchView;
    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mListView = (ListView) findViewById(R.id.list_view);
        mSearchView = (SearchView) findViewById(R.id.search_view);

        showlist();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    mListView.setFilterText(s);
                } else {
                    mListView.clearTextFilter();
                }
                return false;
            }
        });

        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("选择操作");
                menu.add(0, 0, 0, "删除该条");
            }
        });
    }

    //给菜单项添加事件
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        String id = String.valueOf(info.id);
        switch (item.getItemId()) {
            case 0:
                //System.out.println("删除"+info.id);
                SQLiteDatabase db = openOrCreateDatabase("History.db", Context.MODE_PRIVATE, null);
                deleteData(db, id);  //删除事件的方法
                mData.clear();
                showlist();
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    public void showlist(){

        SQLiteDatabase db = openOrCreateDatabase("History.db", Context.MODE_PRIVATE, null);
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

        for (int i = 0; i < id.size(); i++) {
            mData.add(id.get(i) + "          " + ForCode.get(i) + "         " + HomCode.get(i) + "  " + time.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(HistoryActivity.this, android.R.layout.simple_list_item_1, mData);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(true);
        id.clear();ForCode.clear();HomCode.clear();time.clear();
    }

    public void deleteData(SQLiteDatabase db,String id){
        String[] mid = mData.get(Integer.parseInt(id)).split(" ");
        String[] sqlid = {mid[0]};
        db.delete("history", "id=?", sqlid);
        db.close();
    }
}
