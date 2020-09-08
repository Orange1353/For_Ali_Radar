package com.example.firestorerecycleradapterdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private Button MapsBtn;
    private Button Logout;
    private FirebaseAuth mAuth;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    EditText textsend;
    ChatAdapter chatadapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference chatref = db.collection("chat");
    FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textsend = findViewById(R.id.textsend);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Chat chat = new Chat(user.getUid(),user.getDisplayName(), textsend.getText().toString(), new Date());*/
                Chat chat = new Chat(user.getUid(), Objects.requireNonNull(user.getEmail()).replace("@mail.ru", ""), textsend.getText().toString(), new Date());
                chatref.add(chat);
                textsend.setText("");
            }
        });

        recyclerView = findViewById(R.id.chatrecyclerview);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Query query = FirebaseFirestore.getInstance().collection("chat").orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>().setQuery(query, Chat.class).build();
        chatadapter = new ChatAdapter(options);
        chatadapter.registerAdapterDataObserver(    new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                    recyclerView.scrollToPosition(chatadapter.getItemCount() - 1);
            }
        });
        recyclerView.setAdapter(chatadapter);


        mAuth = FirebaseAuth.getInstance();
        Logout = (Button) findViewById(R.id.logout_btn);
        MapsBtn = (Button)findViewById (R.id.button2);



        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();

                LogOutUser();
            }
        });

        MapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ChatActivity.this, CustomersMapsActivity.class);
                startActivity(intent);
            }
        });

    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getTitle().toString()){
            case "logout":
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(ChatActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
        }

        return true;
    }
*/



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Обработайте нажатие, верните true, если обработка выполнена


        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Обработайте отпускание клавиши, верните true, если обработка выполнена
        return false;
    }
    private void LogOutUser() {


        Intent startPageIntent = new Intent(ChatActivity.this, CustomerLoginRegisterActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

  @Override
  public void onBackPressed() {

      //DriversMap!
      Intent intent = new Intent(ChatActivity.this, CustomersMapsActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
      finish();
  }

    @Override
    protected void onStart() {
        super.onStart();
        chatadapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatadapter.stopListening();
    }
}
