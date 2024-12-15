package com.cloudsoftware.sehtiapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkOutActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> workoutguide = new ArrayList<>();
    private int randomNumber,counter;
    private ArrayList<Integer> clickedIndexes = new ArrayList<>();

    private String gender;
    private Timer _timer = new Timer();
    private TimerTask trainer_img,trianer_count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_out);
        Random random = new Random();

        LinearLayout move_linear=findViewById(R.id.train_linear);
        TextView count_text=findViewById(R.id.move_count);
        ImageView move_image=findViewById(R.id.image);

        ProgressBar loading=findViewById(R.id.load);

        ListView workout_list= findViewById(R.id.workout_list);
        Button start =findViewById(R.id.start);
        ImageView go_back = findViewById(R.id.go_back);
        gender= getIntent().getStringExtra("gender");
        if(gender.equals("female"))
            gender="women";
        else gender="men";
        move_linear.setVisibility(View.GONE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // ----- get Workout list-------
        DatabaseReference _workoutDB = FirebaseDatabase.getInstance().getReference("workout/"+gender);

        _workoutDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
                };

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    HashMap<String, Object> product = productSnapshot.getValue(genericTypeIndicator);

                    randomNumber = random.nextInt(2);
                    if(randomNumber == 0)
                        workoutguide.add(product);
                    else
                        workoutguide.add(0,product);

                }
                // product_list.setAdapter(new ProductListAdapter(products));
                FitnessListAdapter adapter = new FitnessListAdapter(workoutguide);
                workout_list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                loading.setVisibility(View.GONE);

                //  Toast.makeText(HomeActivity.this,"data loaded", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log.e(TAG, "Error getting products from Firebase", databaseError.toException());
            }
        });





        go_back.setOnClickListener(view -> {
            finish();
        });

        start.setOnClickListener(view -> {
            workout_list.setVisibility(View.GONE);
            start.setVisibility(View.GONE);
            move_linear.setVisibility(View.VISIBLE);

            AtomicInteger counter = new AtomicInteger(0);

            TimerTask trainer_count = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            count_text.setText(String.valueOf(counter.get()));
                            int currentValue = counter.getAndIncrement();
                            if (currentValue == 5) {
                                counter.set(0);
                            }

                        }
                    });
                }
            };

            _timer.scheduleAtFixedRate(trainer_count, 0, 1000);




            Timer timer = new Timer();
            int delay = 0; // Initial delay before starting the task
            int period = 5000; // Interval between each task execution (5 seconds)

            for (int i = 0; i < clickedIndexes.size(); i++) {
                int index = clickedIndexes.get(i);
                Toast.makeText(WorkOutActivity.this, String.valueOf(index), Toast.LENGTH_SHORT).show();

                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String imageUrl = workoutguide.get(index).get("image").toString();
                                    Glide.with(WorkOutActivity.this)
                                            .asGif()
                                            .load(imageUrl)
                                            .into(move_image);
                                }catch (Exception e){

                                }
                    }
                        });
                    }
                };

                // Schedule the TimerTask with the specified delay and period
                timer.schedule(timerTask, delay, period);

                // Increment the delay for the next task to ensure they are executed 5 seconds apart
                delay += period;
            }


            count_text.setText("Good job");

        });
    }
 public class FitnessListAdapter extends BaseAdapter{

     ArrayList<HashMap<String, Object>> _data;

     public FitnessListAdapter(    ArrayList<HashMap<String, Object>> data){
         _data= data;
     }

     @Override
     public int getCount() {
         return _data.size();
     }

     @Override
     public Object getItem(int i) {
         return null;
     }

     @Override
     public long getItemId(int i) {
         return i;
     }

     @Override
     public View getView(int i, View view, ViewGroup viewGroup) {
         LayoutInflater _inflater= getLayoutInflater();
         View _view=view;
         if (_view == null){
             _view = _inflater.inflate(R.layout.workout_list,null);
         }
         final TextView name = _view.findViewById(R.id.name);
         final ImageView icon =_view.findViewById(R.id.icon);
         final CheckBox checkbox= _view.findViewById(R.id.checkbox);
         final String url=_data.get(i).get("image").toString();


         name.setText(_data.get(i).get("FR").toString());
         Glide.with(WorkOutActivity.this)
                 .asGif()
                 .load(url) // Replace with your GIF resource
                 .into(icon);

         checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
             if(b){
                 clickedIndexes.add(i);
             }
         });

         return _view;
     }
 }
}