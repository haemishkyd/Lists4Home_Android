package com.sppp.lists4home;

public class UserData {
	public String name;
    public String pass;
    
    public UserData(){
        super();
    }
    
    public UserData(String lname, String lpass){
        super();
        name=lname;
        pass=lpass;
    }
    
    public String getName()
    {
    	return name;
    }
    
    public String getPass()
    {
    	return pass;
    }
}
