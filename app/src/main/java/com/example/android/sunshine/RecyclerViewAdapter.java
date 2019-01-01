package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private List<String> mWeathertexts;
    private Context mContext;
    private int Display_id;

    private final ForecastAdapterOnClickHandler mClickHandler;

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private String date;
    private String weather;
    private String highTemp;
    private String lowTemp;
    private int id;
    private int image_res_id;

    private int hypenIndex1;
    private int hypenIndex2;
    private int SlashIndex;
    private int hypenIndex3;
    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickHandler {
        void onClick(String location, String weather);
    }



        public RecyclerViewAdapter(Context mContext, List<String> mWeathertexts, ForecastAdapterOnClickHandler clickHandler, int displayID) {
        this.mWeathertexts = mWeathertexts;
        this.mContext = mContext;
        this.mClickHandler = clickHandler;
        this.Display_id = displayID;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        int layoutId;

        switch (viewType) {

            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.today_weather_item;
                break;
            }

            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.weather_list_item;
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);

        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder,int position) {
        String weatherPerPos = mWeathertexts.get(position);


for(int k=0;k<weatherPerPos.length();k++){
    if(weatherPerPos.charAt(k) == '-') {
        date = weatherPerPos.substring(0, k).trim();
        hypenIndex1 = k;
        break;
    }
}
        for(int f=hypenIndex1+1;f<weatherPerPos.length();f++){
                 if(weatherPerPos.charAt(f) == '-'){
                     weather = weatherPerPos.substring((hypenIndex1+1),f).trim();
                     hypenIndex2 = f;
                     break;
                 }
        }

        for(int g=hypenIndex2+1;g<weatherPerPos.length();g++){
    if(weatherPerPos.charAt(g) == '/'){
        highTemp = weatherPerPos.substring((hypenIndex2+1),g).trim();
        SlashIndex = g;
        break;
    }
        }

        for(int m=SlashIndex+1;m<weatherPerPos.length();m++){
    if(weatherPerPos.charAt(m) == '-'){
        lowTemp = weatherPerPos.substring((SlashIndex+1),m).trim();
        hypenIndex3 = m;
        break;
    }
        }
        id = Integer.parseInt(weatherPerPos.substring((hypenIndex3+1), weatherPerPos.length()).trim());

int viewtype = getItemViewType(position);

switch (viewtype){
    case VIEW_TYPE_TODAY:
        image_res_id = SunshineWeatherUtils.getArtResourceForWeatherCondition(id);
        viewHolder.today_weatherImage.setImageResource(image_res_id);
        viewHolder.today_date_text.setText(date);
        viewHolder.today_weather_text.setText(weather);
        viewHolder.today_temperature1_view.setText(highTemp);
        viewHolder.today_temp2_view.setText(lowTemp);
        break;
    case VIEW_TYPE_FUTURE_DAY:
        image_res_id = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(id);
        viewHolder.weatherImage.setImageResource(image_res_id);
        viewHolder.date_text.setText(date);
        viewHolder.weather_text.setText(weather);
        viewHolder.temperature1_view.setText(highTemp);
        viewHolder.temp2_view.setText(lowTemp);
        break;
        default:
            throw new IllegalArgumentException("Invalid view type, value of " + viewtype);
}


//Log.d(TAG, "Display Id = "+Display_id);

//        viewHolder.parentRecyclerLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext, mWeathertexts.get(i), Toast.LENGTH_SHORT).show();
//
//
//            }
//        });

    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return VIEW_TYPE_TODAY;
        }else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    @Override
    public int getItemCount() {
        return mWeathertexts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView parentCardLayout;
        ImageView weatherImage;
        TextView date_text;
        TextView weather_text;
        TextView temperature1_view;
        TextView temp2_view;

        ConstraintLayout parentConstrLayout;
        ImageView today_weatherImage;
        TextView today_date_text;
        TextView today_weather_text;
        TextView today_temperature1_view;
        TextView today_temp2_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentConstrLayout = itemView.findViewById(R.id.today_weaher_view_parent);
            today_weatherImage = itemView.findViewById(R.id.today_imageView);
            today_date_text = itemView.findViewById(R.id.today_date_View);
            today_weather_text = itemView.findViewById(R.id.today_weather_text_View);
            today_temperature1_view = itemView.findViewById(R.id.today_highTemp_text_view);
            today_temp2_view = itemView.findViewById(R.id.today_lowTemp_text_view);

            weatherImage = itemView.findViewById(R.id.item_image_view);
            date_text = itemView.findViewById(R.id.date_view);
            weather_text = itemView.findViewById(R.id.weather_text_view);
            temperature1_view = itemView.findViewById(R.id.temp1);
            temp2_view = itemView.findViewById(R.id.temp2);
            parentCardLayout = itemView.findViewById(R.id.item_parent_card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String weathercurr;
            if(weather_text != null) {
                weathercurr = weather_text.getText().toString();
            }else{
                weathercurr = today_weather_text.getText().toString();
            }
            mClickHandler.onClick(SunshinePreferences.DEFAULT_MAP_LOCATION,weathercurr);
        }
    }



}

