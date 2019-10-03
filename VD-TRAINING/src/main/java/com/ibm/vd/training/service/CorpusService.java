package com.ibm.vd.training.service;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
/* Corpus related activities are specified here */
@Service
public class CorpusService 
{
	@Autowired
	DataService db;
	
	final static Logger logger = Logger.getLogger(CorpusService.class);
	/* Build a Corpus for the Client */
	public String createCorpus(String file,Integer projectID)
	{
		String status=null;	
		BufferedReader br =null;
		String line ="";
		try
		{
			HashMap<String,HashMap<String,String>> keywordHashMap=new HashMap<String,HashMap<String,String>>();
			HashSet<String> categories = new HashSet<String>();
			ArrayList<String> tickets = new ArrayList<String>();
			categories = db.getCategories(projectID);			
			br = new BufferedReader(new FileReader(file));			
			while ((line = br.readLine()) != null) 
			{
				String[] ticket_feed =line.split(",");				
				tickets.add(ticket_feed[0]+"```"+ticket_feed[1]+"```"+ticket_feed[4]);				
			}			
			for (int i=0;i<tickets.size();i++)
			{
				Iterator<String> itr = categories.iterator();
				while(itr.hasNext())
				{
					String cat_name =(String)itr.next();
					if(tickets.get(i).split("```")[2].equalsIgnoreCase(cat_name))
					{
						String ticketID=tickets.get(i).split("```")[0];
						String ticket_desc=tickets.get(i).split("```")[1];
						ticket_desc=ticket_desc.replaceAll("[^a-zA-Z0-9]"," ");
						ticket_desc=ticket_desc.trim();
						ticket_desc=ticket_desc.replaceAll("   "," ");
						ticket_desc=ticket_desc.replaceAll("  "," ");
									
						keywordHashMap=getKeywords(ticketID,ticket_desc,cat_name);
						Iterator<Map.Entry<String,HashMap<String,String>>> it=keywordHashMap.entrySet().iterator();	
						while (it.hasNext())
						{
							Map.Entry<String,HashMap<String,String>> pair = it.next();
							HashMap<String,String>printvalues=pair.getValue();
							String pattern=printvalues.get("TICKETID");
							Integer frequency=0;						
							for (int k = 0; k < pattern.length(); k++) 
							{
							    if (pattern.charAt(k) == ';') 
							    {
							    	frequency=frequency+1;
							    }
							}
							frequency=frequency+1;
							if(!projectID.equals(0))
							{
								status=db.buildCorpus(pair.getKey(), printvalues.get("RELEVANCE").toString(), frequency.toString(), printvalues.get("CATEGORY").toString(),projectID);
							}
							else
							{
								/* Default Corpus - Clients with no/less historical data */ 
								status=db.buildDefaultCorpus(pair.getKey(), printvalues.get("RELEVANCE").toString(), frequency.toString(), printvalues.get("CATEGORY").toString());
							}
						}
					}
				}
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
				br.close();
			} 
			catch (IOException e) 
			{
				logger.error(e.toString());
			}
		}
		return status;
	}
	/* Extract the keywords & relevance for the ticket description */
	public HashMap<String,HashMap<String,String>> getKeywords(String ticketID,String ticketDesc,String category)
	{
		HashMap<String,HashMap<String,String>> keywordHashMap=new HashMap<String,HashMap<String,String>>();
		try
		{
			ArrayList<String> nlu_credentials = new ArrayList<String>();
			nlu_credentials=db.getNLUCredentials();
			String username =nlu_credentials.get(0);
			String password =nlu_credentials.get(1);
			
			NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2018-04-16",username,password);
			KeywordsOptions keywords= new KeywordsOptions.Builder().sentiment(false).emotion(false).limit(3).build();
			Features features = new Features.Builder().keywords(keywords).build();
			AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(ticketDesc).features(features).build();
			AnalysisResults response = service.analyze(parameters).execute();
			JSONObject jsonObj = new JSONObject(response);				
								
			JSONArray arr = jsonObj.getJSONArray("keywords");	
			for (int i = 0; i < arr.length(); i++)
			{
				String keyword 	 = arr.getJSONObject(i).getString("text");
				Double relevance = arr.getJSONObject(i).getDouble("relevance");		
				if(keywordHashMap.containsKey(keyword))
				{
					HashMap<String,String>val=keywordHashMap.get(keyword);
					String currentTicket=val.get("TICKETID");
					String newTicket=currentTicket.concat(";").concat(ticketID);
					float currentRelevance=Float.valueOf(val.get("RELEVANCE"));
					keywordHashMap.put(keyword, initValues(new String(Double.toString(currentRelevance+Double.valueOf(relevance))),newTicket,category));
				}		    
				else
				{
					keywordHashMap.put(keyword, initValues(new String(Double.toString(relevance)),ticketID,category));
				}
			}
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		return keywordHashMap;
	}
	
	/* Sub function to extract the keywords & relevance for the ticket description */
	public  HashMap<String,String>initValues(String relevance,String ticketId, String category)
	{
		HashMap<String,String> defaultHashMap=new HashMap<String,String>();
		try
		{
			defaultHashMap.put("RELEVANCE",relevance);
			defaultHashMap.put("TICKETID",ticketId);
			defaultHashMap.put("CATEGORY",category);			
		}
		catch(Exception e)
		{
			logger.error(e.toString());		
		}
		return defaultHashMap;
	}	

}
