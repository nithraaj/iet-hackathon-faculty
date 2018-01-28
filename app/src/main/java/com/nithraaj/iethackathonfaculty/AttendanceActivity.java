package com.nithraaj.iethackathonfaculty;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class AttendanceActivity extends AppCompatActivity {

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    PublishOptions.Builder builder;
    Context context;
    FirebaseDatabase database;
    DatabaseReference attendanceRef;
    DatabaseReference classRef;
    ArrayAdapter arrayAdapter;
    ListView studentList;
    String class_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        //class_id = getIntent().getExtras().getString("class_id");
        class_id = "class_2";
        Log.d("class_id",class_id);
        context = this;
        final String class_key = getRandomString(15);
        database = FirebaseDatabase.getInstance();
        classRef = database.getReference("class");

        final Button button_start = findViewById(R.id.start);
        final Button button_stop = findViewById(R.id.stop);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcastAudio(class_key);
                button_start.setVisibility(View.INVISIBLE);
                button_stop.setVisibility(View.VISIBLE);
                updateList();
            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nearby.getMessagesClient(context).unpublish(new Message(class_key.getBytes()));
                button_stop.setVisibility(View.GONE);
            }
        });
        studentList = findViewById(R.id.presentList);
    }

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
    private void broadcastAudio(String message){
        Strategy.Builder sBuilder = new Strategy.Builder();
        sBuilder.setDiscoveryMode(Strategy.DISCOVERY_MODE_BROADCAST);
        sBuilder.setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT);
        builder = new PublishOptions.Builder();
        builder.setStrategy(sBuilder.build());
        Log.d("AudioBroadcast","broadcasting key "+message);
        Nearby.getMessagesClient(this).publish(new Message(message.getBytes()),builder.build());
    }
    private void updateList(){
        attendanceRef = database.getReference("attendance");
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Query query = attendanceRef.orderByChild("date").equalTo(format.format(date));
        final ArrayList<String> students = new ArrayList<>();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        if(data.hasChild("classid")) {
                            Log.d("data-classid", data.child("classid").getValue().toString());
                            Log.d("data-id", data.child("id").getValue().toString());
                            if (Objects.equals(data.child("classid").getValue().toString(), class_id)) {
                                if(!students.contains(data.child("id").getValue().toString())) {
                                    students.add(data.child("id").getValue().toString());
                                }
                                Log.d("students", students.toString());
                            }
                        }
                    }
                    arrayAdapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1,students);
                    arrayAdapter.notifyDataSetChanged();
                    studentList.setAdapter(arrayAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
