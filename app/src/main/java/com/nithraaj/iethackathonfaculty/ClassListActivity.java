package com.nithraaj.iethackathonfaculty;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class ClassListActivity extends AppCompatActivity {
    Context context;
    FirebaseDatabase database;
    DatabaseReference teacherRef;
    DatabaseReference classRef;
    ListView list;
    ArrayAdapter arrayAdapter;
    ArrayList<String> class_ids;
    ArrayList<String> classname;
    TextView textview;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);
        context = this;
        progressBar = findViewById(R.id.progressBar3);
        progressBar.setIndeterminate(true);
        textview = findViewById(R.id.textView);
        database = FirebaseDatabase.getInstance();
        list = findViewById(R.id.listview);
        class_ids = new ArrayList<>();
        classname = new ArrayList<>();
        getClasses();

    }
    private void getClasses(){
        Log.d("database","geting classes");
        String teacher_id = "002";
        teacherRef = database.getReference("teachers");
        Query query = teacherRef.orderByChild("id").equalTo(teacher_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String temp = data.child("classid").getValue().toString();
                        temp = temp.replace("[","");
                        temp = temp.replace("]","");
                        class_ids.addAll(Arrays.asList(temp.split(", ")));
                    }
                    Log.d("Class ids",class_ids.toString());
                    getClassNames(class_ids);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getClassNames(ArrayList<String> ids){
        Log.d("classnames",ids.toString());
        classRef = database.getReference("class");
        ArrayList<Query> query = new ArrayList<>();
        int i = -1;
        for(String clas: class_ids) {
            Log.d("test", clas);
            query.add(classRef.orderByChild("classid").equalTo(clas));
        }
        for(Query quer: query){
            quer.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("test","received data");
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            classname.add(data.child("subject").getValue().toString());
                            arrayAdapter.notifyDataSetChanged();
                        }
                        Log.d("Class ids",classname.toString());
                    }else{
                        Log.d("test","no data"+dataSnapshot.toString());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        showClasses(classname);
        Log.d("classname","exited loop");

    }
    private void showClasses(final ArrayList<String> classes) {
        Log.d("database", "showing classes "+classes.toString());
        arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, classes);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
        textview.setVisibility(View.GONE);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, AttendanceActivity.class);
                intent.putExtra("class_id", class_ids.get(position));
                intent.putExtra("class_name",classname.get(position));
                startActivity(intent);
            }
        });
    }
}
