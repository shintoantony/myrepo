package base.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.stereotype.Service;

@Service
public class Database 
{

	public Connection connectToDB()
	{
		Connection con=null;
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
			con=DriverManager.getConnection("jdbc:mysql://mysql-service:2206/sample?autoReconnect=true&useSSL=false","root","admin");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return con;
	}
	
	public String getDBStatus()
	{
		String status = null;
		
		ResultSet r = null;
		Connection con= null;
		Statement s= null;
		try
		{
			con=this.connectToDB();
			s=con.createStatement();
			String query="select status from STATUS_INFO";
			r=s.executeQuery(query);
			while(r.next())
			{
				status=r.getString("status");
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return status;
		
	}
}
