package com.example.covidtrackerindia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    List<Entry> x;
    date end;date first;
    String val;
    public int flag=0;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        val=parent.getItemAtPosition(position).toString();
        StringBuilder gom=new StringBuilder();
        for(int i=0;i<val.length();i++)
        {
            char ch=val.charAt(i);
            if(ch=='=')
                break;
            gom.append(ch);
        }
        TextView tem=findViewById(R.id.textView);
        val=gom.toString();
        tem.setText(val);
        singleton ram= singleton.getInstance(this);
        JsonObjectRequest so=new JsonObjectRequest(Request.Method.GET,"https://api.covid19india.org/v4/min/timeseries.min.json",null,new correct(),new wrong());
        ram.requestQueue.add(so);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    return;
    }

    class anime implements Easing.EasingFunction
    {

        @Override
        public float getInterpolation(float input)
        {
            float val=0.0f;
            if(input<=0.5)
                val=input;
            else if(input<=0.9)
                val= (float) (1-input);
            else
                val=9*input-8;

            return val;
            }
        }
    class date
    {
        int d,m,y;
        date(int x,int g,int z)
        {
            d=x;m=g;y=z;
        }
        date(int num,int year)
        {
            int[] mdays={1,-3,1,0,1,0,1,1,0,1,0,1};
            if(year%4==0)mdays[1]=-2;int curr=0;int count=0;
            do
            {
                curr+=mdays[count++];
                curr+=30;
            }while(curr<num);
         int cat=num-curr+mdays[count-1]+30;
         d=cat;m=count;y=year;
        }
        date next()
        {
            int[] mdays={1,-3,1,0,1,0,1,1,0,1,0,1};
            if(y%4==0)mdays[1]=-2;
            if((mdays[m-1]+30)==d)
            {
                d=1;
                if(m==12)
                {
                    y++;
                    m=1;
                }
                else
                    m++;
                return this;
            }
            d++;
            return this;
        }
        date backmonth()
        {
            if(m==1)
            { m=12;y--;d=31; }
            else
            m--;
            return this;
        }
        date backweek()
        {
            if(d>7)
                d-=7;
            else
            {
                if(m>1)
                    m--;
                else
                {
                    m=12;
                    y--;
                }
            }
            return this;
        }


        boolean equals(date d)
        {
            return d.d == this.d && d.m == m && d.y == y;
        }

        String get()
        {
            String dd="";
            String mm ="";
            if(d<10){dd+="0";dd+=d;}else{dd+=d;}
            if(m<10){mm+="0";mm+=m;}else{mm+=m;}
            String s=""+y;s+="-";s+=mm;s+="-";s+=dd;
            return s;
        }

    }
    static class singleton
    {
        private static singleton instance;
        private RequestQueue requestQueue;
        private static Context ctx;
        private singleton(Context c)
        {
            ctx=c;
            requestQueue=(requestQueue==null)? Volley.newRequestQueue(ctx.getApplicationContext()):requestQueue;
        }
        public static synchronized singleton getInstance(Context c)
        {
            if(instance==null)
                instance= new singleton(c);
            return instance;

        }

    }
    class correct implements Response.Listener<JSONObject>
    {

        @Override

        public void onResponse(JSONObject response)
        {
            String state=val;
            TextView tex=findViewById(R.id.textView);
            x.clear();
            tex.setText(flag+" ");
            date copy=new date(end.d,end.m,end.y);
            if(flag==0)
                first=new date(4,5,2020);
            if(flag==1)
                first=copy.backmonth();
            if(flag==2)
                first=copy.backweek();
            try
            {
                JSONObject job = response.getJSONObject(state);
                JSONObject dates = job.getJSONObject("dates");
                int counter=0;
                while (!first.equals(end))
                {
                    tex.setText(x.size()+"");
                    JSONObject time = dates.getJSONObject(first.get());
                    JSONObject total = time.getJSONObject("total");
                    int batman = total.getInt("confirmed");
                    x.add(new Entry(counter,batman));
                    first=first.next();
                    counter++;
                }
                }
                 catch(JSONException e)
                 {e.printStackTrace();tex.setText("failure");}
            LineDataSet set=new LineDataSet(x,"sin");
            set.setColors(R.color.black);
            set.setDrawCircles(false);
            set.setCircleColor(R.color.teal_700);
            LineData xo=new LineData(set);
            LineChart chart=findViewById(R.id.chart);
            chart.setData(xo);
            chart.animateX(2000,new anime());

        }
    }
    public class wrong implements Response.ErrorListener
    {

        @Override
        public void onErrorResponse(VolleyError error)
        {
         TextView tom=findViewById(R.id.textView);
         tom.setText("galat");
        }
    }


    public void timechange(View v)
    {
        date copy=new date(end.d,end.m,end.y);
        switch(v.getId())
        {
            case R.id.one:
                flag=0;
                break;
            case R.id.two:
                flag=1;
                break;
            case R.id.three:
                flag=2;
                break;

        }
        singleton ram= singleton.getInstance(this);
        JsonObjectRequest so=new JsonObjectRequest(Request.Method.GET,"https://api.covid19india.org/v4/min/timeseries.min.json",null,new correct(),new wrong());
        ram.requestQueue.add(so);
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("flag",flag);
        savedInstanceState.putString("val",val);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x=new ArrayList<>();
        if(savedInstanceState!=null)
        {
         flag=savedInstanceState.getInt("flag");
         val=savedInstanceState.getString("val");
        }
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String fdate = df.format(c);
        StringBuilder sr[]=new StringBuilder[3];
        for(int i=0;i<3;i++)sr[i]=new StringBuilder();
        int vax=0;
        for(int i=0;i<fdate.length();i++)
        {
            char ch=fdate.charAt(i);
            if(ch!='-')
                sr[vax].append(ch);
            else
                vax++;
        }
        end = new date(Integer.parseInt(sr[0].toString()), Integer.parseInt(sr[1].toString()),Integer.parseInt(sr[2].toString()));
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.state, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String imp=spinner.getSelectedItem().toString();
        Log.d("oo",imp);
        spinner.setOnItemSelectedListener(this);
        /*singleton ram= singleton.getInstance(this);
        JsonObjectRequest so=new JsonObjectRequest(Request.Method.GET,"https://api.covid19india.org/v4/min/timeseries.min.json",null,new correct(),new wrong());
        ram.requestQueue.add(so);*/
        /*LineDataSet set=new LineDataSet(x,"sin");
        set.setColors(R.color.black);
        set.setDrawCircles(false);
        set.setCircleColor(R.color.teal_700);
        LineData xo=new LineData(set);
        LineChart chart=findViewById(R.id.chart);
        chart.setData(xo);
        chart.animateX(3000,new anime());*/
        /*s.setAdapter(new MyAdapter(data));
        s.setScrubEnabled(true);
       series=new LineGraphSeries<>();
        Handler h=new Handler();

        h.post(new Runnable()
        {
            double x=flag*100;
            @Override
            public void run()
            {
                g.removeAllSeries();
                flag++;
                for(double i=0;i<(x+10);i++)
                {
                    x+=0.1;
                    Math.cos(x);
                    series.appendData(new DataPoint(x,Math.cos(x)),true,100);
                }
                g.addSeries(series);
                if(flag!=5)
                h.postDelayed((Runnable)this,100);
            }
        });*/


    }

}