package com.cloudsoftware.sehtiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    // Constants
   final double STEP_LENGTH = 0.7; // Average step length in meters
   final  double STEP_FACTOR = 0.004; // Step factor for calorie calculation

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final int STEP_THRESHOLD = 14; // Adjust this value according to your needs
    private boolean isStepDetected = false;
    private int progressbar_max;
    double calories=0;
    private double meter,calories_burned=0,last_calory_count;
    private int stepCount = 0,goalint=0;
    private FrameLayout progressview;
    private TextView   cal_text,steps,goal,distance_travelled;
    private ProgressBar progressBar;
    private FirebaseDatabase userdb;
    private  Intent intent;
    DecimalFormat df1 = new DecimalFormat("#.######");
    DecimalFormat df2 = new DecimalFormat("#.###");
    DecimalFormat df = new DecimalFormat("#.##");
    String calory_string;
    private String _gender, _name,_weight,_height,uid;
    private boolean viewstep=true;
    SharedPreferences localdb ;
    TextView cal_burnedText;
    SharedPreferences.Editor editor ;
    boolean data_receveid=false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // local db
        localdb= getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor= localdb.edit();

        // Layout assignement
        steps = findViewById(R.id.step);
        progressview=findViewById(R.id.progressview);
        ImageView profile= findViewById(R.id.menu);
        TextView myname=findViewById(R.id.name);
        TextView my_imc=findViewById(R.id.imc_title);
        TextView heart_rate=findViewById(R.id.heart_rate);
        cal_burnedText =findViewById(R.id.cal_burned);

        cal_text=findViewById(R.id.cal_txt);
        LinearLayout nutrition=findViewById(R.id.nutrition);
        LinearLayout hydra=findViewById(R.id.hydra);
         goal = findViewById(R.id.goal);
         distance_travelled= findViewById(R.id.walked);
        progressBar =findViewById(R.id.progressBar);
        LinearLayout imc = findViewById(R.id.imc);
        LinearLayout botlayer = findViewById(R.id.botlayer);
        LinearLayout sugar_view = findViewById(R.id.sugar_view);
        TextView _level= findViewById(R.id._level);

        botlayer.setVisibility(View.GONE);

         progressbar_max=0;

         // check if user is logged
        FirebaseAuth Mauth=FirebaseAuth.getInstance();
       uid=Mauth.getCurrentUser().getUid();
        final int[] cal = {0};

        // Heart Rate Calculation
       Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double estimatedHeartRate = 60 + ((stepCount- cal[0]) * 0.4);
                heart_rate.setText(String.valueOf(Math.round(estimatedHeartRate))+" bpm");
                cal[0] =stepCount;
                if(estimatedHeartRate>100)
                    heart_rate.setTextColor(Color.RED);
                else
                    heart_rate.setTextColor(Color.GRAY);

            }
        }, 1000, 2000);

        Timer cal_per_minute = new Timer();
        cal_per_minute.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    double step_taken_m=calories-last_calory_count;
                    step_taken_m=(step_taken_m/3)*60;
                    last_calory_count=calories;

                    cal_text.setText(df2.format(step_taken_m) + " cal/m");
                });
            }
        }, 1000, 3000);


        // get user personal data
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               _name = snapshot.child("name").getValue(String.class);
                _gender = snapshot.child("gender").getValue(String.class);
                editor.putString(uid+"gender", _gender);
                editor.apply();

                _weight = snapshot.child("weight").getValue(String.class);
                data_receveid=true;
                _height = snapshot.child("height").getValue(String.class);

                // calculate imc
                double bmi = calculateBMI(Double.parseDouble(_weight), Double.parseDouble(_height));


                String formattedBMI = df.format(bmi);
                my_imc.setText(formattedBMI);
                editor.putString(uid+"imc", formattedBMI);
                editor.apply();

                //check for special illness
                if(snapshot.hasChild("illness")){
                    if(snapshot.child("illness").getValue(String.class).equals("sugar")){
                        _level.setText("Diabète");

                    }else if (snapshot.child("illness").getValue(String.class).equals("tension")){
                        _level.setText("Tension");
                    }else{
                        sugar_view.setVisibility(View.GONE);
                    }
                }
                myname.setText("Salut "+_name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // load local user data
        if(localdb.contains(uid+"travelled")) {
  distance_travelled.setText("Distance parcourue\n"+localdb.getString(uid+"travelled","") +"m");
meter=Double.parseDouble(localdb.getString(uid+"travelled","") );
        }
        if(localdb.contains(uid+"cal_burned")) {
            cal_text.setText(localdb.getString(uid+"cal_burned","") +"\n cal brûlées");

        }

        // if goal exist setup UI
        if(localdb.contains(uid+"goal")) {

            goalint=Integer.parseInt(localdb.getString(uid+"goal",""));
        progressBar.setMax(goalint);
        // get current progress
            if(localdb.contains(uid+"step_to_goal")) {
                stepCount=Integer.parseInt(localdb.getString(uid+"step_to_goal",""));
                progressBar.setProgress(stepCount);

            steps.setText(localdb.getString(uid+"step_to_goal",""));
            goal.setText(goalint - stepCount +"\n Reste pour votre objective");
            //updateStepCountUI();

            }
        } else {
           // set goal
            SetGoal();

        }

        // Sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // change view
        progressview.setOnClickListener(view -> {
            //viewstep=!viewstep;
        });

        // go to nutrition menu
        nutrition.setOnClickListener(view -> {
          intent =new Intent(HomeActivity.this,NutritionActivity.class);
          startActivity(intent);
        });
        hydra.setOnClickListener(view -> {
            intent =new Intent(HomeActivity.this,HydritionActivity.class);
            startActivity(intent);
        });

        // Profile action
        profile.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options")
                    .setMessage("Choisir un option:")
                    .setPositiveButton("Deconnection", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform logout action
                            FirebaseAuth.getInstance().signOut();

                            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();


                        }
                    })
                    .setNegativeButton("Editer le Profile", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform edit profile action
                        Intent  intent = new Intent(HomeActivity.this,SignupActivity.class);
                            intent.putExtra("mode","update");
                            startActivity(intent);

                        }
                    })
                    .setCancelable(true)
                    .show();

        });

        // view sugar or tension
        sugar_view.setOnClickListener(view -> {
            intent = new Intent(this,SugarActivity.class);
            if(_level.getText().toString().equals("Diabète")){

            intent.putExtra("task","sugar");

            }else if (_level.getText().toString().equals("Tension"))
                intent.putExtra("task","tension");
                        else
                            intent.putExtra("task","none");

            startActivity(intent);
        });


        // Setting up walking goal
        goal.setOnClickListener(view -> {
            SetGoal();

        });
        // go work out
        imc.setOnClickListener(view -> {
            intent=new Intent(this,IMCActivity.class)  ;
            intent.putExtra("gender",_gender);
            startActivity(intent);
        });

    }

    // calculate imc
    public static double calculateBMI(double weight, double height) {
       height=height/100;
        return weight / (height * height);
    }
    public void SetGoal(){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("But de marche");
        builder.setMessage("combien de pas aimeriez-vous faire?:");

        // Set up the input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the positive button
        builder.setPositiveButton("Fixer un objectif", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String goalText = input.getText().toString();

                if(!goalText.equals("")){

                progressbar_max=Integer.parseInt(goalText);
                progressBar.setMax(progressbar_max);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                long Time = calendar.getTimeInMillis();

                // set last time goal updated
                editor.putString(uid+"goal_last_set",String.valueOf(Time));
                    stepCount=0;
                    updateStepCountUI();

                editor.putString(uid+"goal", goalText);
                editor.apply();

                Toast.makeText(HomeActivity.this, "Objectif enregistré", Toast.LENGTH_SHORT).show();
            }
                else
                    Toast.makeText(HomeActivity.this, "null input", Toast.LENGTH_SHORT).show();

            }
        });

        // Set up the negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the magnitude of the acceleration vector
            double magnitude = Math.sqrt(x * x + y * y + z * z);

            // Check if a step is detected based on the magnitude
            if (magnitude > STEP_THRESHOLD) {
                if (!isStepDetected) {
                   if(data_receveid){
                    isStepDetected = true;
                    stepCount++;

                    // Update the step count in your UI or perform any other desired action
                    updateStepCountUI();}
                }
            } else {
                isStepDetected = false;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateStepCountUI() {
        //Calories burned per minute = (0.035  X body weight in kg) + ((Velocity in m/s ^ 2) / Height in m)) X (0.029) X (body weight in kg)
        int intgoal;
        steps.setText(String.valueOf(stepCount)+" pas");
        if(stepCount>1000)
            steps.setText(stepCount/1000+"k pas");
        if(localdb.contains(uid+"goal")) {
            progressBar.setMax(Integer.parseInt(localdb.getString(uid+"goal","")));
            intgoal=Integer.parseInt(localdb.getString(uid+"goal",""));
            if(stepCount>intgoal ) {
                goal.setText("Objectif atteint\n clicker ici pour le changer");
            }else {
                goal.setText(String.valueOf(intgoal-stepCount)+" Pas \npour votre objectif");
            }
        }else progressBar.setMax(0);

        progressBar.setProgress(stepCount);
        editor.putString(uid+"step_to_goal", String.valueOf(stepCount));
        editor.apply();
        int temp=(Integer.parseInt(_weight));

        //Calories Burned per Step = (Body Weight in kilograms * 0.035) / 1000
        //0.000035
        calories_burned=(0.035*temp)+(0.5*0.029*temp );
        //calories+=STEP_FACTOR*STEP_LENGTH*temp;
        calories+=Integer.parseInt(_weight)*0.035/1000;
        calory_string=df1.format(calories);

        editor.putString(uid+"cal_burned",String.valueOf(calory_string));
        editor.apply();
        if(localdb.contains(uid+"calories"))
            cal_burnedText.setText(calory_string+"/"+localdb.getString(uid+"calories","")+"\n cal brûlées");
            else
        cal_burnedText.setText(calory_string+"\n cal brûlées");

        // to km
        meter=meter+0.75;
        editor.putString(uid+"travelled",String.valueOf(meter));
        editor.apply();
        if(meter<2000)
        distance_travelled.setText("Distance parcourue\n"+String.valueOf(meter) +"m");
        else
            distance_travelled.setText("Distance parcourue\n"+String.valueOf(meter/1000) +"km");
    }
}





