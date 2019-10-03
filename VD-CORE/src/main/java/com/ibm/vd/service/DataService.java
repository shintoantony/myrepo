package com.ibm.vd.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.stereotype.Service;

@Service
public class DataService 
{

	public Connection connectToDB()
	{
		Connection con=null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			con=DriverManager.getConnection("jdbc:mysql://prod-mysqldb:3306/sample?autoReconnect=true&useSSL=false","root","admin");
		/*"jdbc:mysql://prod-mysqldb:3306/sample?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root","admin");*/
			
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
				System.out.println(status);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return status;
		
	}
}
