/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;

import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.android.sunshine.RecyclerViewAdapter.ForecastAdapterOnClickHandler;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ForecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String[]>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";

    private RecyclerView mWeatherRecyclerView;

    // COMPLETED (6) Add a TextView variable for the error message display
    private TextView mErrorMessageDisplay;

    // COMPLETED (16) Add a ProgressBar variable to show and hide the progress bar
    private ProgressBar mLoadingIndicator;

    private List<String> mWeather_texts;
    private String location_changed;

    private DrawerLayout mDrawerLayout;

    private SharedPreferences.Editor prefEditor;
    public SharedPreferences sharedPreferences;

    private int Today_Weather_Display_ID = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);


        setupSharedPrefs();
        prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (location_changed == null || location_changed.isEmpty()) {
            prefEditor.putString("location", "Kolkata, India");
            prefEditor.apply();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);


        mDrawerLayout = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);

                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.change_location:
                                mDrawerLayout.closeDrawers();
                                final AlertDialog.Builder locationdialog = new AlertDialog.Builder(MainActivity.this);
                                locationdialog.setTitle(getApplicationContext().getString(R.string.LOCATION_label));
                                locationdialog.setMessage(getApplicationContext().getString(R.string.Dialog_msg));

                                final EditText input = new EditText(MainActivity.this);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT

                                );
                                lp.setMargins(10, 0, 10, 0);

                                input.setLayoutParams(lp);
                                input.setText(sharedPreferences.getString("location", "Kolkata, India"));
                                locationdialog.setView(input);
                                locationdialog.setIcon(R.drawable.ic_location);
                                locationdialog.setNeutralButton(getApplicationContext().getString(R.string.Done_label),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String new_loc = input.getText().toString();
                                                if (location_changed.equals(null)) {
                                                    prefEditor.putString("location", "Kolkata, India").apply();
//                                                    startActivityForResult(new Intent(getApplicationContext(),MainActivity.class),0);
                                                    getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                                                } else {
                                                    prefEditor.putString("location", new_loc).apply();
//                                                    startActivityForResult(new Intent(getApplicationContext(),MainActivity.class),0);
                                                    getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                                                }
                                            }
                                        }
                                );

                                locationdialog.show();
                                return true;
                        }
                        return true;
                    }
                }
        );
        mWeatherRecyclerView = findViewById(R.id.weather_recycler_view);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);


        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);



        /* Once all of our views are setup, we can load the weather data. */

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void setupSharedPrefs() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        location_changed = sharedPreferences.getString("location", "Kolkata, India");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initRecycler() {
        RecyclerView recyclerView = findViewById(R.id.weather_recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mWeather_texts, this, 1);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
//    private void loadWeatherData() {
//        mWeather_texts = new ArrayList<>();
//
//
//        // COMPLETED (20) Call showWeatherDataView before executing the AsyncTask
//        showWeatherDataView();
//
//        String location = SunshinePreferences.getPreferredWeatherLocation(this);
//        new FetchWeatherTask().execute(location);
//
//    }

    // COMPLETED (8) Create a method called showWeatherDataView that will hide the error message and show the weather data

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mWeatherRecyclerView.setVisibility(View.VISIBLE);
    }

    // COMPLETED (9) Create a method called showErrorMessage that will hide the weather data and show the error message

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public void showErrorMessage() {
        /* First, hide the currently visible data */
        mWeatherRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(String location, String weather) {
        location = sharedPreferences.getString("location", "Kolkata, India");
//        Log.d(TAG, "Heyb"+location);
        Intent intent = new Intent(MainActivity.this, DetailedWeather.class);
        intent.putExtra("location", location);
        intent.putExtra("weather", weather);
        startActivity(intent);
    }


//    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        // COMPLETED (18) Within your AsyncTask, override the method onPreExecute and show the loading indicator
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected String[] doInBackground(String... params) {
//
//            /* If there's no zip code, there's nothing to look up. */
//            if (params.length == 0) {
//                return null;
//            }
//
//            String location = params[0];
//            URL weatherRequestUrl = NetworkUtils.buildUrl(location);
//
//            try {
//                String jsonWeatherResponse = NetworkUtils
//                        .getResponseFromHttpUrl(weatherRequestUrl);
//
//                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
//                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);
//
//                return simpleJsonWeatherData;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String[] weatherData) {
//            // COMPLETED (19) As soon as the data is finished loading, hide the loading indicator
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            if (weatherData != null) {
//                // COMPLETED (11) If the weather data was not null, make sure the data view is visible
//                showWeatherDataView();
//                /*
//                 * Iterate through the array and append the Strings to the TextView. The reason why we add
//                 * the "\n\n\n" after the String is to give visual separation between each String in the
//                 * TextView. Later, we'll learn about a better way to display lists of data.
//                 */
//                for (String weatherString : weatherData) {
//                    mWeather_texts.add((weatherString));
//                }
//                initRecycler();
//            } else {
//                // COMPLETED (10) If the weather data was null, show the error message
//                showErrorMessage();
//            }
//        }
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:

                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_refresh:
                mWeatherRecyclerView.setVisibility(View.INVISIBLE);
                getSupportLoaderManager().restartLoader(0, null, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

//        if (id == R.id.action_refresh) {
//            mWeatherRecyclerView.setVisibility(View.INVISIBLE);
//            getSupportLoaderManager().restartLoader(0,null,this);
//            return true;
//        }if(id == R.id.home){
//            Log.d(TAG, "Heyb");
//            mDrawerLayout.openDrawer(GravityCompat.START);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int i, @Nullable Bundle bundle) {

        return new AsyncTaskLoader<String[]>(this) {

            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String[] loadInBackground() {

                String location = location_changed;
                URL weatherRequestUrl = NetworkUtils.buildUrl(location);
                Log.d(TAG, "WeatherData = " + weatherRequestUrl);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);


                    return simpleJsonWeatherData;


                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(String[] data) {
                if (data != null) {
                    mWeatherData = data;
                    super.deliverResult(data);
                } else {
                    showErrorMessage();
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mWeather_texts = Arrays.asList(data);
        if (mWeather_texts == null || mWeather_texts.isEmpty()) {
            showErrorMessage();
        } else {

            showWeatherDataView();
            initRecycler();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        location_changed = sharedPreferences.getString("location", "Kolkata, India");

    }
}