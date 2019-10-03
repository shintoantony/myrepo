package com.ibm.vd.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/* Main Class for Virtual Dispatcher Training Service */
@SpringBootApplication
public class TrainingApplication extends SpringBootServletInitializer
{
   public static void main(String[] args) throws Exception 
   {
	  SpringApplication.run(TrainingApplication.class);
   }    
}
