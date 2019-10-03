package application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.service.Database;

@RestController	
public class jcontroller 
{
    @Autowired
	Database db;
    
    @RequestMapping("/status")
    public String getDBConnectionStatus()
    {
    	String status=null;
    	String status_op=null;
    	try
    	{
    		status=db.getDBStatus();
    		if (status.equals("success"))
    		{
    			status_op="connected";
    			
    		}
    		else
    		{
    			status_op="Not Connected";
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		status_op="Not Connected";
    	}
    	return status_op;
    }
   
}
