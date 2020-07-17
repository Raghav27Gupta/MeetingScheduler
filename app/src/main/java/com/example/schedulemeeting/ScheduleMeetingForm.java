package com.example.schedulemeeting;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ScheduleMeetingForm extends AppCompatActivity {

    EditText sTime,eTime,selectedDate;
    Button submitBtn;
    String dateForMeeting=null;

    ProgressDialog pd;
    ArrayList<String> allTimeArray;


    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_meeting_form);
        allTimeArray=new ArrayList<String>();
        dateForMeeting=getIntent().getStringExtra("dateSelected");
        sTime=(EditText)findViewById(R.id.chooseStartTime);
        eTime=(EditText)findViewById(R.id.chooseEndTime);
        selectedDate=(EditText)findViewById(R.id.chooseDate);
        submitBtn=(Button)findViewById(R.id.submitbtn);

        sTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStartTimeDialog(sTime);
            }
        });

        eTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEndTimeDialog(sTime);
            }
        });

        selectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(selectedDate);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sel_date=selectedDate.getText().toString();
                String sel_startTime=sTime.getText().toString();
                String sel_endTime=eTime.getText().toString();

                new JsonTask().execute("http://fathomless-shelf-5846.herokuapp.com/api/schedule?date=%22"+sel_date+"%22");

            }
        });


    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ScheduleMeetingForm.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
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
                    //perform logic to find slots
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    Date d1 = null;
                    Date d2 = null;
                    try {
                        d1 = format.parse(sTime.getText().toString());
                        d2 = format.parse(eTime.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long diff = d2.getTime() - d1.getTime();
                    long meetingDuration = diff / (60 * 1000);
                    Log.i("sdf", String.valueOf(meetingDuration));

                    for(int i=0;i<allTimeArray.size();i+=2)
                    {
                        if(sTime.getText().toString().compareTo(allTimeArray.get(i))<0)
                        {
                            if(allTimeArray.get(i-1).compareTo(sTime.getText().toString())>=0)
                            {
                                Log.i("asdasd","slot not availabele");
                                Toast.makeText(ScheduleMeetingForm.this, "Slot Not Available", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else
                            {
                                Date d3=format.parse(allTimeArray.get(i));
                                Date d4=format.parse(allTimeArray.get(i-1));
                                long diffe = d3.getTime() - d4.getTime();
                                long emptyTime = diffe / (60 * 1000);

                                if(meetingDuration<emptyTime)
                                {
                                    Log.i("asdasd","slot availabele");
                                    Toast.makeText(ScheduleMeetingForm.this, "Slot Available", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(ScheduleMeetingForm.this, "Slot Not Available", Toast.LENGTH_SHORT).show();
                                    Log.i("asdasd","slot not availabele");
                                }

                                break;
                            }

                        }
                    }
                    //allTimeArray.clear();
                }
                else
                {
                    Log.i("test","Result json is null");
                }


            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }
    }

    //Selecting DATE
    private void showDateDialog(final EditText sTime) {
        final Calendar calendar=Calendar.getInstance();

        DatePickerDialog.OnDateSetListener dateSetListener= new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                datePicker.setMinDate(System.currentTimeMillis() - 1000);
                int nyear = datePicker.getYear();
                int nmonth = datePicker.getMonth();
                int nday = datePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(nyear, nmonth, nday);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                String strDate = format.format(calendar.getTime());
                if(nmonth < 10){

                    nmonth = Integer.parseInt("0" + nmonth);
                }
                if(nday < 10){

                    nday  = Integer.parseInt("0" + nday);
                }
                calendar.set(Calendar.YEAR,nyear);
                calendar.set(Calendar.MONTH,nmonth);
                calendar.set(Calendar.DAY_OF_MONTH,nday);
                selectedDate.setText(strDate);
            }
        };

       // new DatePickerDialog(ScheduleMeetingForm.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        new DatePickerDialog(ScheduleMeetingForm.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    //selecting Start TIME

    private void showStartTimeDialog(final EditText sTime) {
        final Calendar calendar=Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener StimeSetListener=new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int nhour = view.getHour();
                int nmin = view.getMinute();
                //Calendar calendar = Calendar.getInstance();

                if(nhour < 10){

                    nhour = Integer.parseInt("0" + nhour);
                }
                if(nmin < 10){

                    nmin= Integer.parseInt("0" + nmin);
                }
                calendar.set(Calendar.HOUR,nhour);
                calendar.set(Calendar.MINUTE,nmin);
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String strDate = format.format(calendar.getTime());
                sTime.setText(strDate);
            }
        };

        new TimePickerDialog(this,StimeSetListener,calendar.get(Calendar.HOUR),calendar.get(Calendar.MINUTE)+5,false).show();
    }

    //Selecting End time
    private void showEndTimeDialog(final EditText sTime) {
        final Calendar ecalendar=Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener EtimeSetListener=new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int ehour = view.getHour();
                int emin = view.getMinute();
                //Converting in 2 digit format
                if(ehour < 10){

                    ehour = Integer.parseInt("0" + ehour);
                }
                if(emin < 10){

                    emin= Integer.parseInt("0" + emin);
                }
                ecalendar.set(Calendar.HOUR,ehour);
                ecalendar.set(Calendar.MINUTE,emin);
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String streDate = format.format(ecalendar.getTime());
                eTime.setText(streDate);
            }
        };

        new TimePickerDialog(this,EtimeSetListener,ecalendar.get(Calendar.HOUR),ecalendar.get(Calendar.MINUTE)+5,false).show();
    }


}
