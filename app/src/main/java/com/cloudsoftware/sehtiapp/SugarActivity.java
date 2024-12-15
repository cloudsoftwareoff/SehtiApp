package com.cloudsoftware.sehtiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SugarActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText  measurementEditText;

    private GraphView graphView;
    String selectedDate;
  //  long ms;
    private LineGraphSeries<DataPoint> series;
    DatabaseReference Healthdb;
    double height,weight;
    String unit="mmol/L";
    String db="sugar",uid;
    private  boolean imc_mode=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugar);
        // views declaration
        measurementEditText = findViewById(R.id.sugar_edit);
        ImageView goback = findViewById(R.id.go_back);
        graphView = findViewById(R.id.graph);
        TextView title = findViewById(R.id.title);
        Button add = findViewById(R.id.add_sugar);
        // get user id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
         uid=currentUser.getUid();}
       // remove actionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // get Request type check
        measurementEditText.setHint("Saisir un mesure (mmol/L)");
        if(getIntent().getStringExtra("task").equals("tension")) {
            measurementEditText.setHint("Saisir votre Pression artérielle (mmHg)");
            db = "tension";
            unit="mmHg";
            title.setText("Tension");
        }

        //  assign root db
        Healthdb = FirebaseDatabase.getInstance().getReference(db+"/"+uid);

        // Add the series to the graph
        series = new LineGraphSeries<>();
        graphView.addSeries(series);

        // Load data from firebase
        Query query = Healthdb.orderByChild("date");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for(DataSnapshot _snapshot : snapshot.getChildren()) {

                        String _value = Objects.requireNonNull(_snapshot.child("value").getValue()).toString();
                       long _time=Long.parseLong(Objects.requireNonNull(_snapshot.child("date").getValue(String.class)));
                       addDataPoint(_value,_time,false);
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SugarActivity.this);
                builder.setTitle("Error")
                        .setMessage(error.getMessage())
                        .setPositiveButton("OK", null)
                        .show();

            }
        });

        // Go back button
        goback.setOnClickListener(view -> finish());
        add.setOnClickListener(view -> {
            if(!measurementEditText.getText().toString().equals(""))
                    showDatePicker();
            else
                measurementEditText.setError(" ");
    });

    }
        // show date picker dialog function
    private void showDatePicker() {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new DatePickerDialog instance
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, dayOfMonth);
        // Show the dialog
        datePickerDialog.show();
    }

    // add data point to graph function
    private void addDataPoint(String value,long time,boolean addtoDB) {

        double measurementValue = Double.parseDouble(value);

        // Create a new data point
        DataPoint dataPoint = new DataPoint(time, measurementValue);

        // Set the X-axis label formatter to display the date
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graphView.getContext()) {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // Convert the value (ms) to a date
                    Date date = new Date((long) value);
                    // Format the date as desired
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return dateFormat.format(date);
                }
                return super.formatLabel(value, isValueX);
            }
        });

        // Set the viewport to show the entire graph
        graphView.getViewport().setMinX(series.getLowestValueX());
        graphView.getViewport().setMaxX(series.getHighestValueX());

        // Adjust the padding to fit the labels properly
        graphView.getGridLabelRenderer().setPadding(64);

        // Add the data point to the graph series
        series.appendData(dataPoint, true, 7); // Assuming you have a LineGraphSeries<DataPoint> called series
        // Set up the series styling
        series.setColor(Color.BLUE);
        series.setThickness(4);

        // Add the series to the graph
        graphView.addSeries(series);


        // Customize the appearance of the graph

        if(db.equals("tension"))
            graphView.setTitle("Pression artérielle");
        else
            graphView.setTitle("Taux de sucre");

        graphView.setTitleTextSize(24);
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("jour");
        if(db.equals("tension")){
            graphView.getGridLabelRenderer().setVerticalAxisTitle("Pression artérielle (mmHg)");
        }
        if(db.equals("sugar")){
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Taux de sucre (mmol/L)");
            }

        graphView.getGridLabelRenderer().setTextSize(16);
        // Refresh the graph to display the updated data points
        graphView.invalidate();
       if(addtoDB) {
           HashMap<String, Object> data = new HashMap<>();
           data.put("value", value);
           data.put("date", String.valueOf(time));
           Healthdb.child(String.valueOf(time)).updateChildren(data)
                   .addOnSuccessListener(aVoid -> {
                       // Data updated successfully
                       Toast.makeText(SugarActivity.this, "Données ajouter", Toast.LENGTH_SHORT).show();

                   })
                   .addOnFailureListener(e ->
                           Toast.makeText(SugarActivity.this, e.toString(), Toast.LENGTH_SHORT).show());

       }
    }

 // on date picked
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Update the TextView with the selected date
        Calendar calendar1 = Calendar.getInstance();
         selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
         calendar1.set(Calendar.DAY_OF_MONTH,dayOfMonth);
         calendar1.set(Calendar.MONTH,month);
         calendar1.set(Calendar.YEAR,year);
         long ms=calendar1.getTimeInMillis();
        addDataPoint(measurementEditText.getText().toString(),ms,true);
    }
}
