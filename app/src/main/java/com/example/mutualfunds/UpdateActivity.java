package com.example.mutualfunds;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

    }
    private class NewThread extends Thread{
        public NewThread(String tableName) {
            this.tableName = tableName;
        }

        String tableName;
        @Override
        public void run() {
           new MoneyControl().execute(tableName);
        }
    }

    public void updateBtn(View view) {
        NewThread t[]=new NewThread[3];
        int count=0;
        for (String s : new String[]{"large_cap", "mid_cap", "small_cap"})
        {
            t[count]=new NewThread(s);
            t[count].start();
           count+=1;
        }
        for(int i=0;i<count;i++) {
            try {
                t[i].join();
            } catch (InterruptedException e) {
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class MoneyControl extends AsyncTask<String, Void, HashMap<String, Integer>> {
        int max = 0;
        String TABLE_NAME;
        ProgressBar progressBar;

        private MoneyControl() {
            TABLE_NAME = null;
            progressBar = findViewById(R.id.progressBar);
        }

        @Override
        protected void onPreExecute() {
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected HashMap<String, Integer> doInBackground(String... tableName) {
            TABLE_NAME = tableName[0];
            String url = "https://www.moneycontrol.com/mutual-funds/performance-tracker/returns/" + TABLE_NAME.replaceAll("_", "-") + "-fund.html";

            try {
                return findCommon(url);
            } catch (IOException e) {
                return null;
            }
        }

        private List<String> getFunds(String url) throws IOException {
            List<String> fundCodes = new ArrayList<>();
            Document document = Jsoup.connect(url).get();
            Elements mutualFunds = document.select(".robo_medium");
            for (Element a : mutualFunds)
                try {
                    String[] href = a.attr("href").split("/");
                    String domain = href[2], code = href[href.length - 1];
                    if (domain.equals("www.moneycontrol.com"))
                        fundCodes.add(code);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            max = mutualFunds.size();
            progressBar.setMax(max);

            return fundCodes;
        }

        private List<String> getShares(String code) throws IOException {
            List<String> stockList = new ArrayList<>();
            Document document = Jsoup.connect("https://www.moneycontrol.com/mutual-funds/hdfc-top-100-fund-direct-plan/portfolio-holdings/" + code).get();
            Elements stocks = document.select(".check");
            for (Element span : stocks) {
                String stock = span.text().trim();
                stockList.add(stock);
            }
            return stockList;

        }


        private HashMap<String, Integer> findCommon(String url) throws IOException {
            HashMap<String, Integer> hashMap = new HashMap<>();
            HashSet<String> tmp = new HashSet<>();
            List<String> fundCodes = getFunds(url);
            int count = 0;
            for (String code : fundCodes) {
                count += 1;
                tmp.clear();
                tmp.addAll(getShares(code));
                for (String stock : tmp)
                    if (hashMap.get(stock) != null)
                        hashMap.put(stock, hashMap.get(stock) + 1);
                    else
                        hashMap.put(stock, 1);
                progressBar.setProgress(count);
            }
            return hashMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer> common) {
            if (common != null) {
                common.put("#MAX#", max);
                progressBar.setProgress(progressBar.getMax());
                Database db = new Database(getApplicationContext());
                db.refresh(TABLE_NAME);
                for (String s : common.keySet())
                    db.insert(TABLE_NAME, s, common.get(s));
                Toast.makeText(getApplicationContext(), "Updated: " + TABLE_NAME, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
