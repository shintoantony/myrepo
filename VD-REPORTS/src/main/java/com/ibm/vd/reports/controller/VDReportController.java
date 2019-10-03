package com.ibm.vd.reports.controller;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.ibm.vd.reports.service.DataService;

/* Controller for Virtual Dispatcher Report Service */
@RestController
public class VDReportController 
{
	@Autowired
	DataService db;
	
	static Logger logger = Logger.getLogger(VDReportController.class.getName());
	
	/* Virtual Dispatcher Ticket assignment */
	@RequestMapping("/reports")
    public String assignTicket(@RequestParam("tickettool")String tickettool,@RequestParam("country")String country,@RequestParam("month")String month,@RequestParam("year")String year,@RequestParam("dou")String dou)
    { 
	   String status=null;
	   try
	   {
		   HashMap<String,String> vdticketsummary= new HashMap<String,String>();
		   vdticketsummary=db.getVDTicketSummary(tickettool,country,month,year,dou);
		   status = new Gson().toJson(vdticketsummary);
	   }
	   catch(Exception e)
	   {
		   logger.error(e.toString());
	   }  
	
	return status;			
   }  

}
