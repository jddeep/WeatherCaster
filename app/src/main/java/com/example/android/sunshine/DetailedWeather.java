package com.example.android.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class DetailedWeather extends AppCompatActivity {
    private TextView location_textview;
    private TextView weather_textview;
    private String location;
    private String weather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_weather);


        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(this.getString(R.string.Weather_details_label));
        location_textview = findViewById(R.id.location_text_view);
        weather_textview = findViewById(R.id.detailed_weather_text_view);
        Bundle bundle = getIntent().getExtras();
        location = bundle.getString("location");
        weather = bundle.getString("weather");
        location_textview.setText(location);
        weather_textview.setText(weather);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detailedact_menu, menu);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.share:
                Intent shareintent = ShareCompat.IntentBuilder.from(this)
                                      .setChooserTitle("Share with: ")
                                      .setType("text/plain")
                                      .setText(location+"\n"+weather)
                                      .getIntent();
                if(shareintent.resolveActivity(getPackageManager())!=null){
                    startActivity(shareintent);
                }
                return true;
            case R.id.map:
                Intent mapintent = new Intent(Intent.ACTION_VIEW);
                mapintent.setData(Uri.parse("geo:0,0?q="+location));
                if(!(mapintent.resolveActivity(getPackageManager())==null)){
                    startActivity(mapintent);
                }else {
                    Toast.makeText(this, "Couldn't locate your place", Toast.LENGTH_SHORT).show();
                }
              return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }
}
