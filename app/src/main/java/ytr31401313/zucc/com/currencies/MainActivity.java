package ytr31401313.zucc.com.currencies;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button mCalcButton;
    private TextView mConvertedTextView;
    private EditText mAmountEditText;
    private Spinner mForSpinner,mHomSpinner;
    private String[] mCurrencies;
    private Button mHistory;
    private Button mclcHistory;

    public static final String FOR = "FOR_CURRENCY";
    public static final String HOM = "HOM_CURRENCY";

    //this will contain my developers key
    private String mKey;
    //used to fetch the 'rates' json object from openexchangerates.org
    public static final String RATES = "rates";
    public static final String URL_BASE = "http://openexchangerates.org/api/latest.json?app_id=";
    //used to format data from openexchangerates.org
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    public int nFor;
    public int nHom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SQLiteDatabase db = openOrCreateDatabase("History.db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS history (id integer primary key autoincrement,ForCode varchar(20),HomCode varchar(20),time varchar(30))");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConvertedTextView = (TextView)findViewById(R.id.txt_converted);
        mAmountEditText = (EditText)findViewById(R.id.edt_amount);
        mCalcButton = (Button)findViewById(R.id.btn_calc);
        mForSpinner = (Spinner)findViewById(R.id.spn_for);
        mHomSpinner = (Spinner)findViewById(R.id.spn_hom);
        mHistory = (Button)findViewById(R.id.btn_his);
        mclcHistory = (Button)findViewById(R.id.btn_clchis);
        final ArrayList<String> arrayList = ((ArrayList<String>)getIntent().getSerializableExtra(SplashActivity.KEY_ARRAYLIST));
        Collections.sort(arrayList);
        mCurrencies = arrayList.toArray(new String[arrayList.size()]);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.spinner_closed,mCurrencies);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHomSpinner.setAdapter(arrayAdapter);
        mForSpinner.setAdapter(arrayAdapter);
        mHomSpinner.setOnItemSelectedListener(this);
        mForSpinner.setOnItemSelectedListener(this);

        if(savedInstanceState == null && (PrefsMgr.getString(this,FOR) == null&&PrefsMgr.getString(this,HOM) == null)){
            mForSpinner.setSelection(findPositionGivenCode("USD",mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode("CNY",mCurrencies));

            PrefsMgr.setString(this,FOR,"USD");
            PrefsMgr.setString(this,HOM,"CNY");
        }
        else{
            mForSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this,FOR),mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this,HOM),mCurrencies));
        }

        mCalcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAmountEditText.getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(),"请输入数值",Toast.LENGTH_LONG).show();
                else
                    new CurrencyConverterTask().execute(URL_BASE+mKey);

            }
        });
        mKey = getKey("open_key");

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,HistoryActivity.class);
                startActivity(intent);
            }
        });

        mclcHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.execSQL("delete from history");
                Toast toast = Toast.makeText(MainActivity.this, "清除成功", Toast.LENGTH_SHORT);
                toast.show();
            }
        });


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mnu_invert:
                invertCurrencies();
                break;
            case R.id.mnu_codes:
                launchBrowser(SplashActivity.URL_CODES);
                break;
            case R.id.mnu_exit:
                finish();
                break;
        }
        return true;
        // return super.onPrepareOptionsMenu(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    private void launchBrowser(String strUri) {
        if (isOnline()) {
            Uri uri = Uri.parse(strUri);
//call an implicit intent
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {

            case R.id.spn_for:
                PrefsMgr.setString(this, FOR,extractCodeFromCurrency((String)mForSpinner.getSelectedItem()));
                break;

            case R.id.spn_hom:
                PrefsMgr.setString(this, HOM, extractCodeFromCurrency((String)mHomSpinner.getSelectedItem()));
                break;

            default:
                break;
        }

        mConvertedTextView.setText("");

    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private int findPositionGivenCode(String code, String[] currencies) {

        for (int i = 0; i < currencies.length; i++) {
            if (extractCodeFromCurrency(currencies[i]).equalsIgnoreCase(code)) {
                return i;
            }
        }
        //default
        return 0;
    }

    private String extractCodeFromCurrency(String currency) {
        return (currency).substring(0,3);
    }

    public void invertCurrencies() {
        nFor = mForSpinner.getSelectedItemPosition();
        nHom = mHomSpinner.getSelectedItemPosition();
        mForSpinner.setSelection(nHom);
        mHomSpinner.setSelection(nFor);
        mConvertedTextView.setText("");

        PrefsMgr.setString(this, FOR, extractCodeFromCurrency((String)
                mForSpinner.getSelectedItem()));
        PrefsMgr.setString(this, HOM, extractCodeFromCurrency((String)
                mHomSpinner.getSelectedItem()));
    }

    private String getKey(String keyName){
        AssetManager assetManager = this.getResources().getAssets();
        Properties properties = new Properties();
        try {
            InputStream inputStream = assetManager.open("keys.properties");
            properties.load(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  properties.getProperty(keyName);

    }


    //转换
    private class CurrencyConverterTask extends AsyncTask<String,Void,JSONObject> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Calculating Result...");
            progressDialog.setMessage("One moment please...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CurrencyConverterTask.this.cancel(true);
                            progressDialog.dismiss();
                        }
                    });
            progressDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            double dCalculated = 0.0;

            String strForCode =
                    extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]);
            String strHomCode = extractCodeFromCurrency(mCurrencies[mHomSpinner.
                    getSelectedItemPosition()]);
            String strAmount = mAmountEditText.getText().toString();
            try {
                if (jsonObject == null){
                    throw new JSONException("no data available.");
                }
                JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                if (strHomCode.equalsIgnoreCase("USD")){
                    dCalculated = Double.parseDouble(strAmount) / jsonRates.getDouble(strForCode);
                }
                else if (strForCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode);
                }
                else {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode)
                            / jsonRates.getDouble(strForCode);
                }
            } catch (JSONException e) {
                Toast.makeText(
                        MainActivity.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                mConvertedTextView.setText("");
                e.printStackTrace();
            }

            mConvertedTextView.setText(DECIMAL_FORMAT.format(dCalculated) + " " + strHomCode);
            progressDialog.dismiss();

            save();
        }

    }

    //保存数据
    private void save(){

        boolean result = false;


        //创建数据库
        SQLiteDatabase db = openOrCreateDatabase("History.db", Context.MODE_PRIVATE, null);

        //创建一张表
        String sql = "select count(*) as c from sqlite_master where type ='table' and name ='history' ";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if(cursor.moveToFirst()){
            int count = cursor.getInt(0);
            if(count>0){
                result = true;
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日    HH:mm:ss     ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        if (!result) {
            db.execSQL("CREATE TABLE IF NOT EXISTS history (id integer primary key autoincrement,ForCode varchar(20),HomCode varchar(20),time varchar(30))");
            ContentValues values =new ContentValues();
            values.put("ForCode",mAmountEditText.getText().toString()+" "+extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]));
            values.put("HomCode",mConvertedTextView.getText().toString());
            values.put("time",str);
            db.insert("history",null,values);
            values.clear();
        }else {
            Log.i("+++++++++++","已经创建了，无需再创建");

            ContentValues values =new ContentValues();
            values.put("ForCode",mAmountEditText.getText().toString()+" "+extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]));
            values.put("HomCode",mConvertedTextView.getText().toString());
            values.put("time",str);
            db.insert("history",null,values);
            values.clear();
        }
    }



}
