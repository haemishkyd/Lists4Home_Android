package com.sppp.lists4home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class WeatherAdapter extends ArrayAdapter<Weather>
{

    Context context;
    int layoutResourceId;
    Weather data[] = null;
    public String Old_Category;
    private int CurrentColour;
    private int red_comp = 100;
    private int green_comp = 150;
    private int blue_comp = 50;
    private int flag = 0;

    public WeatherAdapter(Context context, int layoutResourceId, Weather[] passeddata)
    {
        super(context, layoutResourceId, passeddata);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = passeddata;
        CurrentColour = Color.argb(50, 70, 70, 70);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        WeatherHolder holder = null;
        Weather weather = data[position];

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }


        holder.txtTitle.setText(weather.title);
        holder.imgIcon.setImageResource(weather.icon);

        if (!weather.category.equals(Old_Category))
        {
        	if (flag == 1)
        	{
	        	red_comp = 0;
	        	blue_comp = 128;
	        	green_comp = 255;
	        	flag = 0;
        	}
        	else
        	{
        		red_comp =0;
	        	blue_comp = 255;
	        	green_comp = 128;
	        	flag = 1;
        	}
        	CurrentColour = Color.argb(80, red_comp, green_comp, blue_comp);
        }
        holder.txtTitle.setBackgroundColor(CurrentColour);

        Old_Category = weather.category;
        if (weather.isTicked)
        {
        	holder.txtTitle.setTextColor(Color.RED);
        }
        else
        {
        	holder.txtTitle.setTextColor(Color.WHITE);
        }

        return row;
    }

    static class WeatherHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}