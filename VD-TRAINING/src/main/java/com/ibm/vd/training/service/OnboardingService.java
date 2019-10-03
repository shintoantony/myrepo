package com.ibm.vd.training.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/* Data loading from CSV Files are specified here */
@Service
public class OnboardingService 
{
	@Autowired
	DataService db;

	public String project=null;
	final static Logger logger = Logger.getLogger(OnboardingService.class);
	
	/* Load Project/Engagement/Categories from the CSV Historical File */
	public String loadMetaData(String file)
	{
		String status=null;
		BufferedReader br=null;
		BufferedReader br1=null;
		BufferedReader  br2 =null;
		try
		{
			HashSet<String> projects = new HashSet<String>();	
			HashSet<String> agreements = new HashSet<String>();	
			HashSet<String> categories = new HashSet<String>();
			
			/* Identify Projects */
			String line=null;
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) 
			{
				String[] ticket_feed =line.split(",");
				projects.add(ticket_feed[2]);
			}
			/* Identify Agreements & Categories*/
			Iterator<String> itr = projects.iterator();
			
			while (itr.hasNext()) 
			{		
				Integer st=null;
				project=(String)itr.next();				
				st=db.loadProjects(project);
				if(st.equals(1))
				{
					Integer projectID=db.getProject(project);
					String line1=null;
					br1 = new BufferedReader(new FileReader(file));
					while ((line1 = br1.readLine()) != null) 
					{
						String[] ticket_feed =line1.split(",");
						if(ticket_feed[2].equals(project))
						{
							agreements.add(ticket_feed[3]);											
						}						
					}			
					Iterator<String> itr1 = agreements.iterator();
					while (itr1.hasNext())
					{
						String agreement=(String)itr1.next();	
						ArrayList<String> agreementDetails = new ArrayList<String>();
						agreementDetails= db.getAgreement(agreement);
						String agreementID=agreementDetails.get(0);
						String agreementName=agreementDetails.get(1);
						String agreementStartDate=agreementDetails.get(2);
						String agreementEndDate=agreementDetails.get(3);
						String agreementOwningCompany=agreementDetails.get(4);	
						@SuppressWarnings("unused")
						Integer agreementstatus=db.loadAgreements(agreementID,agreementName,agreementStartDate,agreementEndDate,agreementOwningCompany,projectID);						
					}					
					String line2=null;
					br2 = new BufferedReader(new FileReader(file));
					while ((line2 = br2.readLine()) != null) 
					{
						String[] ticket_feed =line2.split(",");
						if(ticket_feed[2].equals(project))
						{
							categories.add(ticket_feed[4]);							
						}				
					}					
					Iterator<String> itr2 = categories.iterator();
					while (itr2.hasNext())
					{
						String category=(String)itr2.next();	
						db.loadCategories(category,projectID);						
					}
				}
			}
		status ="Data Loading Completed";	
		}
		catch(Exception e)
		{
			logger.error(e.toString());
			status ="Data Loading Failed";
		}
		finally
		{
			try 
			{
				br.close();
				br1.close();
				br2.close();
			} 
			catch (IOException e) 
			{
				logger.error(e.toString());
			}
		}
		return status;
	}
	/* Load Default Categories from the CSV Historical File for build the common Corpus/Classifier */
	public String loadDefaultCategories(String file)
	{
		String status=null;
		BufferedReader br=null;
		HashSet<String> categories = new HashSet<String>();
		try
		{
			String line=null;
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) 
			{
				String[] ticket_feed =line.split(",");
				categories.add(ticket_feed[4]);							
							
			}					
			Iterator<String> itr2 = categories.iterator();
			while (itr2.hasNext())
			{
				String category=(String)itr2.next();	
				db.loadDefaultCategories(category);						
			}
			status="Category Loading Completed";
		}
		catch(Exception e)
		{
			logger.error(e.toString());
			status="Category Loading Failed";
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
	
	/* Preparing the JSON String for creating a classifier. 
	 * Project Name differentiates the different classifiers  */
	public String loadMetadataJson(String projectname)
	{
		String status=null;
		try
		{
			String metadata="{\"language\":\"en\",\"name\":\""+projectname+"\"}";			
			File file=new File("./metadata.json");  
            file.createNewFile();  
            FileWriter fileWriter = new FileWriter(file);  
            fileWriter.write(metadata.toString());  
            fileWriter.flush();  
            fileWriter.close();  
            status="success";
            
		}		
		catch(Exception e)
		{
			status="failed";
			logger.error(e.toString());
		}
		return status;
	}
	
	/* Cleans each ticket description in the historical data in CSV format.
	 * Create a new cleaned CSV file to upload to NLC */
	public String cleanData(String file,String path,String out_file,String uploadfile,String flag)	
	{
		String status=null;
		BufferedReader br=null;
		OutputStreamWriter bw = null;
		try
		{		
			File directory = new File(path);
			if (! directory.exists())
			{
			   directory.mkdirs();			        
			}
			out_file=path+"/"+out_file;
			br = new BufferedReader(new FileReader(file));
			bw = new OutputStreamWriter(new FileOutputStream(out_file), StandardCharsets.UTF_8);
			
			String line =null;
			String[] cols = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null)
			{
				cols = line.split(",");
				String	ticketdesc = cols[1].replaceAll("[^a-zA-Z0-9]"," ");
				String  ticketcat=cols[4];	
						
				sb.append(ticketdesc+",");    
				sb.append(ticketcat);
				sb.append("\n");  
			}		
			bw.write(sb.toString());
			bw.close();					
			if(flag.equals("true"))
			{
				File source = new File(out_file);
				File dest = new File(uploadfile);			
				FileUtils.copyFile(source, dest);
			}			
			status="Data Cleaned Successfully";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			status="Data Clean Failed";
		}
		finally
		{
			try 
			{
				br.close();
				bw.close();
			} 
			catch (IOException e) 
			{
				logger.error(e.toString());
			}
		}
		return status;
	}	
	
	/* Cleans each ticket description in the historical data in CSV format.
	 * Create a new cleaned CSV file to upload to NLC for Default Corpus */
	public String defaultCleanData(String file,String path,String out_file,String uploadfile,String flag)	
	{
		String status=null;
		BufferedReader br=null;
		OutputStreamWriter bw = null;
		try
		{		
			File directory = new File(path);
			if (! directory.exists())
			{
			   directory.mkdirs();			        
			}
			out_file=path+"/"+out_file;
			br = new BufferedReader(new FileReader(file));
			bw = new OutputStreamWriter(new FileOutputStream(out_file), StandardCharsets.UTF_8);
			
			String line =null;
			String[] cols = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null)
			{
				cols = line.split(",");
				String	ticketdesc = cols[1].replaceAll("[^a-zA-Z0-9]"," ");
				String  ticketcat=cols[4];	
						
				sb.append("TICKETID"+",");
				sb.append(ticketdesc+",");    
				sb.append("PROJECT"+",");
				sb.append("DOU"+",");
				sb.append(ticketcat);
				sb.append("\n");  
			}		
			bw.write(sb.toString());
			bw.close();					
			if(flag.equals("true"))
			{
				File source = new File(out_file);
				File dest = new File(uploadfile);			
				FileUtils.copyFile(source, dest);
			}			
			status="Data Cleaned Successfully";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			status="Data Clean Failed";
		}
		finally
		{
			try 
			{
				br.close();
				bw.close();
			} 
			catch (IOException e) 
			{
				logger.error(e.toString());
			}
		}
		return status;
	}	
	
	/* Merge the default historical data & project specific historical data and creates a new Combined CSV file
	 * It is used for on-boarding clients with less historical data	 */
	public String mergeData (String defaultFile,String projectFile,String combinedFile)
	{
		String status=null;
		try
		{	
		    List<Path> paths = Arrays.asList(Paths.get(defaultFile), Paths.get(projectFile));
		    HashSet<String> mergedLines = getMergedLines(paths);
		    Path target = Paths.get(combinedFile);
		    Files.write(target, mergedLines, Charset.forName("UTF-8"));				
		    status="Data Merged Successfully";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			status="Data Merge Failed";
		}
		return status;
	}
	
	/* Sub Method to merge two CSV files */
	public HashSet<String> getMergedLines(List<Path> paths) throws IOException 
	{
	    HashSet<String> mergedLines = new HashSet<String> ();
	    for (Path p : paths)
	    {
	       List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
	       if (!lines.isEmpty()) 
	       {
	         mergedLines.addAll(lines.subList(0, lines.size()));
	       }
	    }
	 return mergedLines;
	}
	
}
