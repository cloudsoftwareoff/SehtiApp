package com.cloudsoftware.sehtiapp;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SleepTrackerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracker);
        GraphView graphView = findViewById(R.id.graph);
        TextView debug=findViewById(R.id.debug_date);
        String d="";
        // Check if permission is granted
        if (!hasUsageStatsPermission(this)) {
            // Request permission from the user
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

// Get the UsageStatsManager instance
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);




        long endTime = System.currentTimeMillis();

        // Set the start time to 7 days ago
        long startTime = endTime - (7 * 24 * 60 * 60 * 1000);
        // Initialize the calendar and set it to the start time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }



// Retrieve the usage statistics for the last week
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

// Create a list to store the sleep time data
        List<DataPoint> sleepData = new ArrayList<>();


// Iterate through the usage stats and calculate sleep duration
        for (UsageStats usageStats : usageStatsList) {
            // Exclude system and idle time usage
           if (!isSystemUsage(usageStats) && !isIdleTimeUsage(usageStats)) {
                // Calculate sleep duration for each day
                long sleepDuration = usageStats.getTotalTimeInForeground()*4;

                // Convert sleep duration to hours
                double sleepHours = sleepDuration / (1000 * 60 * 60.0); // Convert milliseconds to hours

               Date date = new Date(calendar.getTimeInMillis());

// Step 2: Format the Date object into a readable date string
               SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
               String formattedDate = dateFormat.format(date);

// Step 3: Display the formatted date string using a Toast
              d=d+"\n"+formattedDate;
              debug.setText(usageStats.getPackageName());
                // Add the sleep time data to the list
                sleepData.add(new DataPoint(calendar.getTime(), sleepHours));
           }

            // Move to the previous day
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

// Reverse the sleep data list to display in chronological order
        Collections.reverse(sleepData);


// Create a LineGraphSeries using the sleep data
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(sleepData.toArray(new DataPoint[0]));

// Set up the series styling
        series.setColor(Color.BLUE);
        series.setThickness(4);

// Add the series to the graph
        graphView.addSeries(series);

// Customize the appearance of the graph
        graphView.setTitle("Sleep Tracker (Last Week)");
        graphView.setTitleTextSize(24);
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Day");
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Sleep Time (hours)");
        graphView.getGridLabelRenderer().setTextSize(16);


    }
    // TRACK
    private boolean isSystemUsage(UsageStats usageStats) {
        // Check if the package name of the usageStats belongs to system apps
        return (usageStats.getPackageName().startsWith("com.android.") || usageStats.getPackageName().startsWith("android"));

    }

    private boolean isIdleTimeUsage(UsageStats usageStats) {
        return (usageStats.getTotalTimeInForeground() == 0);

        // Return true if it is idle time usage, false otherwise
    }


    // Check usage permission
    private boolean hasUsageStatsPermission(Context context) {
        // Get the app package name
        String packageName = context.getPackageName();

        // Get the app ops service
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        // Check if the app has permission using the AppOpsManager
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName);

        // Return true if the app has permission, false otherwise
        return mode == AppOpsManager.MODE_ALLOWED;
    }





}