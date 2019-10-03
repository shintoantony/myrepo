package com.ibm.vd.controller;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.vd.service.DataService;

import base.service.Database;

/* Controller for Virtual Dispatcher Core Service */
@RestController
public class VDController 
{

    @Autowired
	DataService db;
    
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
