package com.ibm.vd.reports.service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.vd.reports.utils.Config;
/* Database related activies are specified here */
@Service
public class DataService 
{
	@Autowired
	Config cf;
	
	static Logger logger = Logger.getLogger(DataService.class.getName());
	/* Connection - Virtual Dispatcher Native DB */
 	public  Connection connectToDB()
	{
		 Connection con=null;		 
		 try
		 {
			Class.forName(cf.getDb_driver());			
			con = DriverManager.getConnection(cf.getDb(),cf.getDb_username(),cf.getDb_passcode());			
		 }
		 catch(Exception e)
		 {
			logger.error(e.toString());
		 }	
		return con;
	}
	/* Retrieve the VD ticket summary */
	public HashMap<String,String> getVDTicketSummary(String tickettool,String country,String month,String year,String dou)
	{
		HashMap<String,String> ticketSummary = new HashMap<String,String>();
		ResultSet r=null;	
		Connection con=null;
		Statement s = null;
		try
		{		
			con=this.connectToDB();
			s=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT DISTINCT (a.adminTickets/a.totalTickets)*100 as adminTicketsPercentage,"
					+ "(a.ticketPatternNotInCorpus/a.totalTickets)*100 as ticketPatternNotInCorpusPercentage,"
					+ "(a.assigneeNotInDots/a.totalTickets)*100 as assigneeNotInDotsPercentage,"
					+ "a.totalTickets,a.AdminTickets,a.ticketPatternNotInCorpus,a.assigneeNotInDots "
					+ "FROM ( SELECT SUM((SELECT COUNT(1) FROM REPORTS.TICKETS T1 WHERE T1.TICKET_ID=T.TICKET_ID)) AS totalTickets,"
					+ "SUM((SELECT COUNT(1) FROM REPORTS.TICKETS T1 WHERE T1.SL_NO=T.SL_NO AND "
					+ "T1.ASSIGNEE NOT IN (SELECT DISTINCT SOC_FUNCTIONAL_ID FROM TENANT))) AS adminTickets,"
					+ "SUM((SELECT  COUNT(1) FROM REPORTS.TICKETS T2 WHERE T2.SL_NO=T.SL_NO "
					+ "AND T2.ASSIGNEE IN (SELECT SOC_FUNCTIONAL_ID FROM TENANT) "
					+ "AND REMARKS LIKE '%Ticket pattern not available in historical data%' AND T2.CATEGORY='NF'))AS ticketPatternNotInCorpus,"
					+ "SUM((SELECT  COUNT(1) FROM REPORTS.TICKETS T2 WHERE T2.SL_NO=T.SL_NO AND T2.ASSIGNEE IN "
					+ "(SELECT  SOC_FUNCTIONAL_ID FROM TENANT) AND REMARKS LIKE '%Assignee Not Found in Roster%' "
					+ "AND T2.CATEGORY<>'NF' ))AS assigneeNotInDots "
					+ "FROM REPORTS.TICKETS T INNER JOIN ENGAGEMENT E ON E.ENGAGEMENT_NAME=T.ENGAGEMENT "
					+ "WHERE UPPER(E.ENGAGEMENT_OWNING_COUNTRY)=COALESCE("+country.toUpperCase()+",E.ENGAGEMENT_OWNING_COUNTRY) "
					+ "AND MONTH(T.ASSIGNED_TIME)=COALESCE("+month+",MONTH(T.ASSIGNED_TIME)) "
					+ "AND YEAR(T.ASSIGNED_TIME)=COALESCE("+year+",YEAR(T.ASSIGNED_TIME)) "
					+ "AND UPPER(T.ENGAGEMENT) =COALESCE("+dou.toUpperCase()+",T.ENGAGEMENT) "
					+ "AND UPPER(T.TICKET_SOURCE)=COALESCE("+tickettool.toUpperCase()+",T.TICKET_SOURCE)"
					+ ")a";
			r = s.executeQuery(query);		
			while(r.next())
			{
				ticketSummary.put("adminTicketsPercentage",r.getString("adminTicketsPercentage"));	
				ticketSummary.put("ticketPatternNotInCorpusPercentage",r.getString("ticketPatternNotInCorpusPercentage"));
				ticketSummary.put("assigneeNotInDotsPercentage",r.getString("assigneeNotInDotsPercentage"));	
				ticketSummary.put("totalTickets",r.getString("totalTickets"));
				ticketSummary.put("adminTickets",r.getString("adminTickets"));	
				ticketSummary.put("ticketPatternNotInCorpus",r.getString("ticketPatternNotInCorpus"));
				ticketSummary.put("assigneeNotInDots",r.getString("assigneeNotInDots"));
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
		return ticketSummary;
	}
	
	
}
