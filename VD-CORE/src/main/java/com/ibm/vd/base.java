package com.ibm.vd;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/* Main Class for Virtual Dispatcher Core Service */
@SpringBootApplication 
public class base extends SpringBootServletInitializer
{	
   public static void main(String[] args) throws Exception 
   {
	  SpringApplication.run(base.class);
   } 
      
}
