package com.example.schedulemeeting;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.R.layout.*;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    ProgressDialog pd;
    ArrayList<String> startTimeArray;
    ArrayList<String> endTimeArray;
    ArrayList<String> allTimeArray;
    ListView listView;
    SimpleDateFormat sdf;
    Button prevBtn,nextBtn,scheduleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView date=(TextView)findViewById(R.id.dateTextView);
        intialize();
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        final String currentDate = sdf.format(new Date());
        date.setText(currentDate);
        fetchresults(currentDate);
    //on the click of prev button
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove 1 day from current selected date
                Date d1 = null;
                try {
                    d1 = sdf.parse(date.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(d1);
                cal.add(Calendar.DATE, -1);
                String fstr=null;

                if(String.valueOf(cal.get(Calendar.DATE)).length()<2)
                {
                    try
                    {
                        fstr="0"+cal.get(Calendar.DATE);
                        cal.set(Calendar.DATE, Integer.parseInt("0"+cal.get(Calendar.DATE)));
                    }catch (Exception e)
                    {
                        Log.i("sds","Asda");
                    }
                }
                else
                {
                    fstr=String.valueOf(cal.get(Calendar.DATE));
                }
                String pdate= fstr+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR);
                date.setText(pdate);

                startTimeArray.clear();
                fetchresults(pdate);

                //disable if date selected is less than current date
                if(pdate.compareTo(currentDate)>=0)
                {
                    //fstr > current date
                    scheduleBtn.setEnabled(true);
                }
                else
                {
                    scheduleBtn.setEnabled(false);
                }
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //add 1 day from current selected date
                Date d1 = null;
                try {
                    d1 = sdf.parse(date.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(d1);
                cal.add(Calendar.DATE, +1);
                String fstr=null;

                if(String.valueOf(cal.get(Calendar.DATE)).length()<2)
                {
                    try
                    {
                        fstr="0"+cal.get(Calendar.DATE);
                        cal.set(Calendar.DATE, Integer.parseInt("0"+cal.get(Calendar.DATE)));

                    }catch (Exception e)
                    {
                        Log.i("sds","Asda");
                    }

                }
                else
                {
                    fstr=String.valueOf(cal.get(Calendar.DATE));
                }
                String pdate= fstr+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR);
                date.setText(pdate);
                startTimeArray.clear();
                fetchresults(pdate);
                //disable if date selected is less than current date
                if(pdate.compareTo(currentDate)>=0)
                {
                    scheduleBtn.setEnabled(true);
                }
                else
                {
                    scheduleBtn.setEnabled(false);
                }
            }
        });

        scheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent in=new Intent(getApplicationContext(),ScheduleMeetingForm.class);
                in.putExtra("dateSelected",date.getText().toString());
                startActivity(in);
            }
        });

    }

    private void fetchresults(String date) {
        com.example.schedulemeeting.JsonTask jsonTask=new com.example.schedulemeeting.JsonTask(new Async_Response() {
            @Override
            public void processFinish(Object output) throws JSONException {
                if(output!=null)
                {
                    Log.i("sdfs",output.toString());
                    parseJson(output.toString());
                }
            };
        },this);
        jsonTask.execute("http://fathomless-shelf-5846.herokuapp.com/api/schedule?date=%22"+date+"%22");
    }
    private void parseJson(String result) {

        if(allTimeArray!=null)
        {
            allTimeArray.clear();
        }
        //parse json
        try {
            if(result!=null)
            {
                JSONArray array=new JSONArray(result);
                Log.i("test",array.toString());
                for(int i=0;i<array.length();i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    String start_time = jsonObject.getString("start_time");
                    String end_time = jsonObject.getString("end_time");
                    //to format time in double digit only
                    if(start_time.length()<5)
                    {
                        start_time="0"+start_time;
                    }
                    if(end_time.length()<5)
                    {
                        end_time="0"+end_time;
                    }
                    allTimeArray.add(start_time);
                    allTimeArray.add(end_time);
                }
                Collections.sort(allTimeArray);
                for(int i=0;i<allTimeArray.size();i+=2)
                {
                    String combined=allTimeArray.get(i)+"  -  "+allTimeArray.get(i+1);
                    startTimeArray.add(combined);
                }
                ArrayAdapter<String>adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.support_simple_spinner_dropdown_item,startTimeArray);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }
            else
            {
                Log.i("test","Result json is null");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void intialize() {

        startTimeArray=new ArrayList<String>();
        endTimeArray=new ArrayList<String>();
        allTimeArray=new ArrayList<String>();
        listView = (ListView) findViewById(R.id.meetingListView);
        prevBtn=(Button)findViewById(R.id.prevBtn);
        nextBtn=(Button)findViewById(R.id.nextBtn);
        scheduleBtn=(Button)findViewById(R.id.scheduleBtn);

    }
}
