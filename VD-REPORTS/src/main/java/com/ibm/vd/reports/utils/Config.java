package com.ibm.vd.reports.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class Config 
{
	     
	    @Value("${app.db}")
	    private String db;

	    @Value("${app.db_username}")
	    private String db_username;

	    @Value("${app.db_passcode}")
	    private String db_passcode;
	    
	    @Value("${app.db_driver}")
	    private String db_driver;
	    
	 
		public String getDb() {
			return db;
		}

		public void setDb(String db) {
			this.db = db;
		}

		public String getDb_username() {
			return db_username;
		}

		public void setDb_username(String db_username) {
			this.db_username = db_username;
		}

		public String getDb_passcode() {
			return db_passcode;
		}

		public void setDb_passcode(String db_passcode) {
			this.db_passcode = db_passcode;
		}



	
		public String getDb_driver() {
			return db_driver;
		}

		public void setDb_driver(String db_driver) {
			this.db_driver = db_driver;
		}

		

	
		
		
		
	    
	 
}
