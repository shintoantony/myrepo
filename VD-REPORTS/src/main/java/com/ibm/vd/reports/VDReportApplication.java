package com.ibm.vd.reports;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/* Main Class for Virtual Dispatcher Report Service */
@SpringBootApplication 
public class VDReportApplication extends SpringBootServletInitializer
{	
   public static void main(String[] args) throws Exception 
   {
	  SpringApplication.run(VDReportApplication.class);
   } 
      
}
