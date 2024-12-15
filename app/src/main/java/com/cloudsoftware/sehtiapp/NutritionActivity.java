package com.cloudsoftware.sehtiapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;

public class NutritionActivity extends AppCompatActivity {
    private int total_calories=0;
    private TextView cal_txt;

    private ArrayList<HashMap<String, Object>> foodlist = new ArrayList<>();
    private HashMap<String,Object> fooditem =new HashMap<>();
    SharedPreferences localdb ;
    String  uid;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);
        ImageView go_back = findViewById(R.id.go_back);
        ListView listView= findViewById(R.id.listview);
        cal_txt=findViewById(R.id.cal_text);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        FirebaseAuth Mauth=FirebaseAuth.getInstance();
     uid=Mauth.getCurrentUser().getUid();

        // local db
        localdb = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = localdb.edit();

        // Static data
        fooditem=new HashMap<>();
        fooditem.put("name","Couscous");
        fooditem.put("calories","400");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Makrouna");
        fooditem.put("calories","350");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Riz Djerbien");
        fooditem.put("calories","340");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Shakshuka");
        fooditem.put("calories","250");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Tunisian Tagine");
        fooditem.put("calories","400");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Mechouia Salad ");
        fooditem.put("calories","150");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Mloukhia");
        fooditem.put("calories","200");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Chorba");
        fooditem.put("calories","200");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Ommok Houria");
        fooditem.put("calories","150");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Assida");
        fooditem.put("calories","300");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Ojja ");
        fooditem.put("calories","250");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Fricassé");
        fooditem.put("calories","450");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Baguette Sandwich");
        fooditem.put("calories","450");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Lablabi");
        fooditem.put("calories","250");
        fooditem.put("check","no");

        foodlist.add(fooditem);

        fooditem=new HashMap<>();
        fooditem.put("name","Marqa");
        fooditem.put("calories","300");
        fooditem.put("check","no");

        foodlist.add(fooditem);


        // set listview adapter
        FoodListAdapter adapter = new FoodListAdapter(foodlist);
        listView.setAdapter(adapter);












        go_back.setOnClickListener(view -> {
            Intent intent = new Intent(this,HomeActivity.class) ;
       startActivity(intent);
        });

    }

    // FOOD LISTVIEW ADAPTER


    public class FoodListAdapter extends BaseAdapter {
        private ArrayList<HashMap<String, Object>> foodList;

        public FoodListAdapter(ArrayList<HashMap<String, Object>> foodList) {
            this.foodList = foodList;
        }

        @Override
        public int getCount() {
            return foodList.size();
        }

        @Override
        public Object getItem(int position) {
            return foodList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.custom_list, parent, false);
            }

            // Get the data for the current item
            HashMap<String, Object> foodItem = foodList.get(position);
            String foodName = (String) foodItem.get("name");
            String calories = (String) foodItem.get("calories");

            // Bind the data to the views in the list item layout
            TextView textViewFoodName = convertView.findViewById(R.id.food_name);
            TextView textViewCalories = convertView.findViewById(R.id.calorie_value);
            CheckBox check = convertView.findViewById(R.id.checkbox);

            if(foodItem.get("check").equals("yes")){
                check.setChecked(true);

            }else{
                check.setChecked(false);

            }
            check.setOnCheckedChangeListener((compoundButton, b) -> {

            });

            check.setOnClickListener(view -> {
                if (check.isChecked()){
                    foodItem.put("check","no");
                    total_calories+=Integer.parseInt(foodItem.get("calories").toString());
                    editor.putString(uid+"calories", String.valueOf(total_calories));
                    editor.apply();
                    cal_txt.setText(String.valueOf(total_calories)+" cal");
                    foodList.set(position,foodItem);

                }else{
                    foodItem.put("check","yes");
                    total_calories-=Integer.parseInt(foodItem.get("calories").toString());
                    editor.putString(uid+"calories", String.valueOf(total_calories));
                    editor.apply();
                    cal_txt.setText(String.valueOf(total_calories)+" cal");
                    foodList.set(position,foodItem);

                }


            });

            textViewFoodName.setText(foodName);
            textViewCalories.setText(calories);

            return convertView;
        }
    }

}