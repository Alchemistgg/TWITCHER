package com.parse.starter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    ArrayList<String> users = new ArrayList<String>();
    ArrayAdapter arrayAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater  = new MenuInflater(this);
        menuInflater.inflate(R.menu.tweet_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.tweet){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Compose a tweet");
            final EditText tweetEditText = new EditText(this);
            builder.setView(tweetEditText);
            builder.setPositiveButton("Send tweet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("tweet:", tweetEditText.getText().toString());
                    ParseObject tweet = new ParseObject("Tweet");
                    tweet.put("username", ParseUser.getCurrentUser().getUsername());
                    tweet.put("tweet", tweetEditText.getText().toString());
                    tweet.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(UserListActivity.this, "Tweet has been sent!", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(UserListActivity.this,"Sorry! Tweet could not be sent", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }

            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("tweet","I dont want to send any tweet!");
                }
            });
            builder.show();

        }
        else if (item.getItemId() == R.id.logout){

            ParseUser.logOut();
            Toast.makeText(UserListActivity.this, "You have logged out successfully!", Toast.LENGTH_SHORT).show();
            Intent intent  = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);

        }
        else if(item.getItemId() == R.id.viewFeed){
            Intent intent = new Intent(getApplicationContext(),FeedActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        setTitle("Welcome "+ ParseUser.getCurrentUser().getUsername()+"!");
        final ListView listView  = (ListView)findViewById(R.id.feedListView);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        arrayAdapter  = new ArrayAdapter(this, android.R.layout.simple_list_item_checked,users);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CheckedTextView checkedTextView  = (CheckedTextView)view;
                if(checkedTextView.isChecked()){
                    Log.i("Info", "CHECKED!");
                    Toast.makeText(UserListActivity.this, "You are following "+ users.get(i), Toast.LENGTH_LONG).show();
                    ParseUser.getCurrentUser().add("isFollowing", users.get(i));
                }
                else {
                    Log.i("Info", "NOT CHECKED!");
                    Toast.makeText(UserListActivity.this, "You unfollowed  "+ users.get(i), Toast.LENGTH_LONG).show();
                    ParseUser.getCurrentUser().getList("isFollowing").remove(users.get(i));
                    List temp = ParseUser.getCurrentUser().getList("isFollowing");
                    ParseUser.getCurrentUser().remove("isFollowing");
                    ParseUser.getCurrentUser().put("isFollowing", temp);

                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null && objects.size()>0){
                   for(ParseUser user:objects){
                       users.add(user.getUsername());
                   }
                   listView.setAdapter(arrayAdapter);
                   arrayAdapter.notifyDataSetChanged();

                   for(String username:users){
                       if(ParseUser.getCurrentUser().getList("isFollowing").contains(username)){
                           listView.setItemChecked(users.indexOf(username),true);
                       }
                   }
                }

            }
        });
    }
}
