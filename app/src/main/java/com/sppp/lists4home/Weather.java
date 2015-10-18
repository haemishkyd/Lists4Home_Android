package com.sppp.lists4home;

public class Weather {
    public int icon;
    public String title;
    public String category;
    public boolean isTicked;
    
    public Weather(){
        super();
    }
    
    public Weather(int icon, String title,boolean isTicked,String passed_category) {
        super();
        this.icon = icon;
        this.title = title;
        this.isTicked = isTicked;
        this.category = passed_category;
        
    }
    
    @Override
    public String toString() {
        return this.title;
    }
}