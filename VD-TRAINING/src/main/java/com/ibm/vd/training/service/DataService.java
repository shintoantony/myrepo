package com.ibm.vd.training.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.vd.training.utils.Config;
/* Database related activities are specified here */
@Service
public class DataService
{
	@Autowired
	Config cf;		
	final static Logger logger = Logger.getLogger(DataService.class);	
	
	/* Connection - Virtual Dispatcher Native DB */
	private Connection connectToDB()
	{
   		 Connection con=null;		 
		 try
		 {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(cf.getDb(),cf.getDb_username(),cf.getDb_passcode());	
		 }
		 catch(Exception e)
		 {
			logger.error(e.toString());
		 }	
		 return con;
	}
	/* Connection - Virtual Dispatcher Corpus DB */
	private Connection connectToCorpus()
	{
   		 Connection con=null;		 
		 try
		 {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(cf.getCorpus_db(),cf.getCorpus_db_username(),cf.getCorpus_db_passcode());	
		 }
		 catch(Exception e)
		 {
			logger.error(e.toString());
		 }	
		 return con;
	}

	/* Connection - Report DB */
	private  Connection connectToReportDB()
	{
		 Connection con=null;		 
		 try
		 {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			con = DriverManager.getConnection(cf.getReportdb(),cf.getReportdb_username(),cf.getReportdb_passcode());			
		 }
		 catch(Exception e)
		 {
			logger.error(e.toString());
		 }	
		 return con;
	}  
	/* Get the Watson Model/Classifer name for the Project */
	public ArrayList<String> getClassifier(Integer projectID)
	{
		ArrayList<String> ClassifierDetails = new ArrayList<String>();
		ResultSet r =null;
		Connection con =null;
		Statement s =null;
		try
		{	
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT W.MODEL_ID AS MODELID,N.NLC_USERNAME AS UNAME,"
					+ "N.NLC_PASSWORD AS PWD "
					+ "FROM WATSON_MODEL W INNER JOIN "
					+ "NLC_SERVICE_CREDENTIALS N ON N.NLC_ID=W.NLC_ID "
					+ "WHERE W.TENANT_ID="+projectID;
			r = s.executeQuery(query);			
			while(r.next())
			{
				ClassifierDetails.add(r.getString("MODELID"));
				ClassifierDetails.add(r.getString("UNAME"));
				ClassifierDetails.add(r.getString("PWD"));				
			}		
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{	    
			try 
	        {
	        	if (r != null) 
	        	{
	        		r.close();
	        	}
	        } 
	        catch (SQLException e) 
	        { 
	        	logger.error(e.toString());
	        }
			 try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }	
		    try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		   	  
		}
		return ClassifierDetails;
	}
	
	/* Insert the Watson Model/Classifier details against the particular Project after onboarding a Client */
	public Integer loadClassifier(Integer projectID,String classifierID,Integer instanceID)
	{
		Connection con = null;
		Integer status=null;
		Statement s = null;
		try
		{   
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "INSERT INTO WATSON_MODEL VALUES("+projectID+",'"+classifierID+"',"+instanceID+")";
			s.executeUpdate(query);		
			status=1;
		}
		catch (Exception e)
		{
			status=0;
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }	
		    try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		    	  
		}
		return status;		
	}
	
	/* Insert the Default Classifer/Watson Model for Clients with no/less historical data */
	public Integer loadDefaultClassifier(String classifierID,Integer instanceID)
	{
		Connection con = null;
		Integer status=null;
		Statement s=null;
		try
		{   
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "INSERT INTO WATSON_MODEL_DEFAULT VALUES('"+classifierID+"',"+instanceID+")";
			s.executeUpdate(query);		
			status=1;
		}
		catch (Exception e)
		{
			status=0;
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		    
		}
		return status;		
	}
	/* Retrieve Project ID after loading the Metadata from the CSV File */ 
	public Integer getProject(String project)
	{
		Integer projectID=null;
		ResultSet r=null;		
		Connection con=null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT TENANT_ID AS ID FROM TENANT WHERE UPPER(TENANT_NAME)='"+project.toUpperCase()+"'";
			r = s.executeQuery(query);			
			while(r.next())
			{
				projectID=r.getInt("ID");					
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		    
		}
		return projectID;
	}
	/* Retrieve the default Watson Model/ Classifier Details */
	public String getDefaultWatsonModel()
	{
		String model=null;
		ResultSet r=null;		
		Connection con=null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT MODEL_ID,NLC_ID FROM WATSON_MODEL_DEFAULT";
			r = s.executeQuery(query);			
			while(r.next())
			{
				model=r.getString("MODEL_ID")+"```"+r.getString("NLC_ID");					
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return model;
	}
	
	/* Insert the project details - Onboarding a Client */
	public Integer loadProjects(String projectName)
	{
		Integer status=null;
		Connection con =null;
		Statement s=null;
		try
		{
			con=this.connectToDB();
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Date date = new Date();
			dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); 			
			String CurrentTime=dateFormat.format(date);
	        s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);	        
	        String query ="INSERT INTO TENANT (TENANT_ID,TENANT_NAME,SOC_FUNCTIONAL_ID,MODIFIED_DATE,MODIFIED_BY)"
	        		+ "VALUES(DEFAULT,'"+projectName.toUpperCase()+"',NULL,'"+CurrentTime+"','TrainingService')"
	        		+ "ON DUPLICATE KEY UPDATE TENANT_NAME = '"+projectName.toUpperCase()+"'";
	       	s.executeUpdate(query);
			status=1;			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
			status=0;
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
	/* Insert Agreements/DOUs - Onboarding a Client */
	public Integer loadAgreements(String agreementID,String agreement_name,String agreement_startDate,String agreement_endDate,String agreement_owningCountry,Integer projectID)
	{
		Connection con = null;
		Integer status = null;
		Statement s =null;
		try
		{
			con=this.connectToDB();
	        s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query="INSERT INTO ENGAGEMENT (ENGAGEMENT_ID,ENGAGEMENT_NAME,ENGAGEMENT_START_DT,ENGAGEMENT_END_DT,"
	        		+ "ENGAGEMENT_OWNING_COUNTRY,TENANT_ID) "
	        		+ "VALUES ("+agreementID+",'"+agreement_name.toUpperCase()+"','"+agreement_startDate+"','"+agreement_endDate+"',"
	        		+ "'"+agreement_owningCountry+"',"+projectID+") "
	        		+ "ON DUPLICATE KEY UPDATE ENGAGEMENT_NAME ='"+agreement_name.toUpperCase()+"'";
	     	s.executeUpdate(query);	
	     	status=1;
		}
		catch(Exception e)
		{
			logger.error(e.toString());
			status=0;
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;		
	}
	
	/* Check if the Classifier/Watson Model already exists for the onboarding Client */
	public Integer getClassifierStatus(Integer projectID)
	{
		ResultSet r =null;
		Integer status=null;
		Connection con=null;
		Statement s = null;
		try
		{			
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT COUNT(1) AS STATUS FROM WATSON_MODEL WHERE TENANT_ID="+projectID;
			r = s.executeQuery(query);			
			while(r.next())
			{
				status=r.getInt("STATUS");					
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
		
	}
	/* Retrieve the Categories against a particular project */
	public HashSet<String> getCategories(Integer projectID)
	{
		HashSet<String> categories = new HashSet<String>();
		ResultSet r=null;
		Connection con=null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT CAT_NAME FROM CATEGORY WHERE TENANT_ID="+projectID+" UNION "
					+ "SELECT CAT_NAME FROM CATEGORY_DEFAULT";
			r = s.executeQuery(query);			
			while(r.next())
			{
				categories.add(r.getString("CAT_NAME"));
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return categories;
	}
	/* Retrieve the NLU Credentials */
	public ArrayList<String> getNLUCredentials()
	{
		ArrayList<String> credentials = new ArrayList<String>();
		ResultSet r=null;
		Connection con =null;
		Statement s =null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT NLU_USERNAME,NLU_PASSWORD AS NLU_PASSWORD FROM NLU_SERVICE_CREDENTIALS";
			r = s.executeQuery(query);			
			while(r.next())
			{
				credentials.add(r.getString("NLU_USERNAME"));
				credentials.add(r.getString("NLU_PASSWORD"));				
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return credentials;
	}
	
	/* Delete the Watson Model/Classifier created for a client */
	public void deleteClassifier(String classifierID)
	{
		Connection con = null;
		Statement s =null;
		try
		{   			
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "DELETE FROM WATSON_MODEL WHERE MODEL_ID='"+classifierID+"'";
			s.executeUpdate(query);			
		}
		catch (Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		
	}
	/* Retrieve the DOU details from the Report DB */
	public ArrayList<String> getAgreement(String agreement)
	{
		ArrayList<String>  agreementDetails=new ArrayList<String>();
		ResultSet r =null;
		Connection con=null;
		Statement s =null;
		try
		{
			con=this.connectToReportDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT ENGAGEMENT_ID,UPPER(ENGAGEMENT_NM) AS ENGAGEMENT_NM,"
					+ "ENGAGEMENT_START_DT,ENGAGEMENT_END_DT,ENGAGEMENT_OWNING_COUNTRY_NM "
					+ "FROM TRANSACT.V_ENGAGEMENT_STATUS WHERE UPPER(ENGAGEMENT_NM) ='"+agreement.toUpperCase()+"'";
			r = s.executeQuery(query);			
			while(r.next())
			{
				agreementDetails.add(r.getString("ENGAGEMENT_ID"));	
				agreementDetails.add(r.getString("ENGAGEMENT_NM"));	
				agreementDetails.add(r.getString("ENGAGEMENT_START_DT"));	
				agreementDetails.add(r.getString("ENGAGEMENT_END_DT"));	
				agreementDetails.add(r.getString("ENGAGEMENT_OWNING_COUNTRY_NM"));				
			}			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return agreementDetails;
	}
	
	/* Retrieve the NLC Instance Details which has less than 8 classifiers */
	public ArrayList<String> getNLCInstance()
	{
		ArrayList<String> instanceDetails = new ArrayList<String>();
		ResultSet r =null;
		Connection con =null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT  N.NLC_ID AS ID,N.NLC_USERNAME AS UNAME,N.NLC_PASSWORD AS PWD FROM NLC_SERVICE_CREDENTIALS N WHERE N.NLC_ID IN "
					+ "(SELECT N.NLC_ID  FROM NLC_SERVICE_CREDENTIALS N LEFT JOIN WATSON_MODEL W ON N.NLC_ID=W.NLC_ID "
					+ "GROUP BY N.NLC_ID HAVING COUNT(N.NLC_ID+(SELECT COUNT(NLC_ID) FROM WATSON_MODEL_DEFAULT))<8 ) "
					+ "ORDER BY N.NLC_ID ASC  LIMIT 1";
			r = s.executeQuery(query);			
			while(r.next())
			{
				instanceDetails.add(r.getString("ID"));
				instanceDetails.add(r.getString("UNAME"));
				instanceDetails.add(r.getString("PWD"));				
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (r != null) 
		 		{
		   			r.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return instanceDetails;
	}
	/* Insert the Categories against a particular Project */
	public void loadCategories(String category,Integer projectID)
	{
		Connection con =null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
	        s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query="INSERT INTO CATEGORY (CAT_ID,CAT_NAME,TENANT_ID) VALUES(DEFAULT,'"+category.toUpperCase()+"',"+projectID+") "
	        		+ "ON DUPLICATE KEY UPDATE CAT_NAME = '"+category.toUpperCase()+"'";
	    	s.executeUpdate(query);						
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}		
	}
	
	/* Insert the default Categories while building a default Corpus */
	public void loadDefaultCategories(String category)
	{
		Connection con =null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
	        s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query="INSERT INTO CATEGORY_DEFAULT (CAT_ID,CAT_NAME) VALUES(DEFAULT,'"+category.toUpperCase()+"') "
	        		+ "ON DUPLICATE KEY UPDATE CAT_NAME = '"+category.toUpperCase()+"'";
	    	s.executeUpdate(query);						
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}		
	}
	
	/* Retrieve the NLC Status */
	public Integer getNLCStatus(Integer ProjectID)
	{
		Integer nlcStatus=null;
		ResultSet r =null;
		Connection con=null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query = "SELECT IS_IAM_ENABLED FROM TENANT WHERE TENANT_ID="+ProjectID;
	        r = s.executeQuery(query);			
			while(r.next())
			{
				nlcStatus=r.getInt("IS_IAM_ENABLED");					
			}			
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
	        {
	        	if (r != null) 
			    {
	        		r.close();
			    }
	        } 
	        catch (SQLException e) 
	        { 
	        	logger.error(e.toString());
	        }
	        try 
	        {
	        	if (s != null) 
			    {
	        		s.close();
			    }
	        } 
	        catch (SQLException e) 
	        { 
	        	logger.error(e.toString());
	        }
	        try 
	        {
	        	if (con != null) 
	 		    {
	        		con.close();
	 		    }
	        } 
	        catch (SQLException e) 
	        { 
	        	logger.error(e.toString());
	        }		   
		}
		return nlcStatus;
	}	
	
	/* Mark the historical data status for the client onboarded */
	public void updateHistDataStatus(Integer projectID,Integer histStatus)
	{
		Connection con =null;
		Statement s = null;
		try
		{
			con=this.connectToDB();
	        s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query="UPDATE TENANT SET HISTORICAL_DATA_STATUS="+histStatus+",IS_IAM_ENABLED=1 WHERE TENANT_ID="+projectID;
	    	        s.executeUpdate(query);						
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}		
	}
	
	/* Insert the Keyword details as part of building a Corpus */
	public String buildCorpus(String keyword, String relevance, String frequency,String category,Integer projectID)
	{
		String status=null;
		Connection con = null;
		Statement s = null;
		try
		{
			con=this.connectToCorpus();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query = "INSERT INTO CORPUS VALUES('"+keyword+"',"+ relevance+","+frequency+",'"+category+"',"+projectID+") \r\n" 
	        		+ "ON DUPLICATE KEY UPDATE RELEVANCE = RELEVANCE +"+ relevance+",FREQUENCY=FREQUENCY+1";
	        s.executeUpdate(query);					
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
	/* Delete the Corpus data against Client */
	public Integer deleteCorpus(Integer projectID)
	{
		Integer status=null;
		Connection con = null;
		Statement s = null;
		try
		{
			con=this.connectToCorpus();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query = "DELETE FROM CORPUS WHERE TENANT_ID="+projectID;
	        s.executeUpdate(query);	
	        status=1;
		}
		catch(Exception e)
		{
			logger.error(e.toString());
			status=0;
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
	/* Insert the Keyword details as part of building a default Corpus */
	public String buildDefaultCorpus(String keyword, String relevance, String frequency,String category)
	{
		String status=null;
		Connection con = null;
		Statement s = null;
		try
		{
			con=this.connectToCorpus();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	        String query = "INSERT INTO CORPUS_DEFAULT VALUES('"+keyword+"',"+ relevance+","+frequency+",'"+category+"') \r\n" 
	        		+ "ON DUPLICATE KEY UPDATE RELEVANCE = RELEVANCE +"+ relevance+",FREQUENCY=FREQUENCY+1";
	        s.executeUpdate(query);					
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
	/* Delete the Engagement against Client - Off-boarding */
	public int deleteEngagement(Integer projectID)
	{
		Connection con = null;
		int status;
		Statement s = null;
		try
		{   			
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "DELETE FROM ENGAGEMENT WHERE TENANT_ID="+projectID;
			s.executeUpdate(query);	
			status=1;
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			status=0;
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
	/* Delete category against a Client -Off-boarding */
	public int deleteCategory(Integer projectID)
	{
		Connection con = null;
		int status;
		Statement s = null;
		try
		{   			
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "DELETE FROM CATEGORY WHERE TENANT_ID="+projectID;
			s.executeUpdate(query);		
			status=1;
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			status=0;
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
	public int deleteProject(Integer projectID)
	{
		Connection con = null;
		int status;
		Statement s = null;
		try
		{   			
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "DELETE FROM TENANT WHERE TENANT_ID="+projectID;
			s.executeUpdate(query);		
			status=1;
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			status=0;
		}
		finally 
		{
			try 
		    {
		   		if (s != null) 
		 		{
		   			s.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
			try 
		    {
		   		if (con != null) 
		 		{
		   			con.close();
		 	    }
		    } 
		    catch (SQLException e) 
		    { 
		        logger.error(e.toString());
		    }
		}
		return status;
	}
	
}
