package com.ibm.vd.training.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.vd.training.service.ClassifierService;
import com.ibm.vd.training.service.CorpusService;
import com.ibm.vd.training.service.DataService;
import com.ibm.vd.training.service.OnboardingService;

/* Controller Class for Virtual Dispatcher Training Service */
@RestController
public class TrainingController 
{
	final static Logger logger = Logger.getLogger(TrainingController.class);
	
	@Autowired
	OnboardingService ld;
	
	@Autowired
	DataService db;
	
	@Autowired
	ClassifierService cr;
	
	@Autowired
	CorpusService cs;
	
	/* On-board a Client to Virtual Dispatcher with good amount of historical data */
	@RequestMapping("/createClassifier")	
	public String createClassifier(@RequestParam("projectName")String projectName,@RequestParam("csvData") String file)
	{	
	   String status=null;
		try
		{
			String flag="true";
			DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
			Date date = new Date();
			
			String cleaning_path="/tmp/virtual-dispatcher/data/"+projectName+"/cleaned/";
			String cleaning_out_file=projectName+"_cleaned"+dateFormat.format(date)+".csv";				
			String uploadfile="/tmp/virtual-dispatcher/data/"+projectName+"/upload/output.csv";			
			
				/* 1. Load the tables */							
				String load_status=ld.loadMetaData(file);
				if(load_status.equals("Data Loading Completed"))
				{
					/* 2. Cleaning the data */
					logger.info("Data Loading Completed");
					String clean_status=ld.cleanData(file,cleaning_path,cleaning_out_file,uploadfile,flag);		
					if(clean_status.equals("Data Cleaned Successfully"))						
					{
						/* 3. Create Classifier/Corpus*/
						logger.info("Data Cleaned Successfully");
						String meta_status=ld.loadMetadataJson(ld.project);
						
						if(meta_status.equals("success"))
						{							
							ArrayList<String> instanceDetails = new ArrayList<String>();							
							instanceDetails=db.getNLCInstance();
							String username=instanceDetails.get(1);
							String password=instanceDetails.get(2);
							Integer instanceID=Integer.parseInt(instanceDetails.get(0));						
							Integer projectID=db.getProject(ld.project);
							Integer count=db.getClassifierStatus(projectID);
							if(count.equals(0))
							{
								logger.info("Corpus Creation Started");
								cs.createCorpus(file, projectID);
								logger.info("Corpus Creation Completed");								
								String classifierID=null;
								classifierID=cr.createClassifier(uploadfile,username,password);
								logger.info("Classifier Created Successfully");
								if(classifierID !=null)
								{		
									Integer watsonStatus=null;
									watsonStatus=db.loadClassifier(projectID, classifierID,instanceID);		
									if(watsonStatus.equals(1))
									{
										status="Training Completed";
									}									
								}
								else
								{
									status="Training Failed \n Classifier not created";
								}								
							}
							else
							{
								status="Training stopped \n Classifier exists for the project";
							}							
						}									
					}
					else
					{
						logger.info("Data Clean Failed");
					}
				}
				else
				{
					logger.info("Data Loading Failed");
				}
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}  
		return status;
   }
	
	/* Re-train a client with additional historical data */
	@RequestMapping("/retrainClassifier")
	public String retrainClassifier(@RequestParam("projectName")String projectName,@RequestParam("csvData") String file)
	{
		String status=null;
		BufferedReader input =null; 
		try
		{
			String flag="false";
			DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
			Date date = new Date();
			
			String cleaning_path="/tmp/virtual-dispatcher/data/"+projectName+"/cleaned/";
			String cleaning_out_file=projectName+"_cleaned"+dateFormat.format(date)+".csv";				
			String uploadfile="/tmp/virtual-dispatcher/data/"+projectName+"/upload/output.csv";			
			
			/* 1. Load the tables */							
			String load_status=ld.loadMetaData(file);
			Integer projectID=db.getProject(ld.project);
			if(load_status.equals("Data Loading Completed"))
			{
				String clean_status=ld.cleanData(file,cleaning_path,cleaning_out_file,uploadfile,flag);
				if(clean_status.equals("Data Cleaned Successfully"))
				{ 					
					ArrayList<String> classifierDetails = new ArrayList<String>();
					classifierDetails=db.getClassifier(projectID);
					String classifierID=classifierDetails.get(0);
					String username=classifierDetails.get(1);
					String password=classifierDetails.get(2);
					status=cr.deleteClassifier(classifierID, username, password,projectID);
					if(status.equals("classifier deleted"))
					{
						Process pr = Runtime.getRuntime().exec(new String[]{"/home/merge.sh",projectName});
				        input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				        String line = "";
				        while ((line = input.readLine()) != null) 
				        {
				           status=line;
				        } 
				        input.close();
				        if(status.equals("completed"))
				        {
				        	String meta_status=ld.loadMetadataJson(ld.project);
				        	if(meta_status.equals("success"))
							{
				        		ArrayList<String> instanceDetails = new ArrayList<String>();							
								instanceDetails=db.getNLCInstance();		
								String inst_username=instanceDetails.get(1);
								String inst_password=instanceDetails.get(2);
								Integer instanceID=Integer.parseInt(instanceDetails.get(0));								
								Integer count=db.getClassifierStatus(projectID);
								if(count.equals(0))
								{
									String newclassifierID=null;
									newclassifierID=cr.createClassifier(uploadfile,inst_username,inst_password);
									if(newclassifierID !=null)
									{
										logger.info("Corpus Creation Started");
										cs.createCorpus(file, projectID);
										logger.info("Corpus Creation Completed");
										Integer watsonStatus=null;
										watsonStatus=db.loadClassifier(projectID, newclassifierID,instanceID);		
										if(watsonStatus.equals(1))
										{
											status="Re-Training Completed";
										}											
									}
									else
									{
										status="Re-Training Failed \n Classifier not created";
									}	
								}
								else
								{
									status="Re-Training stopped \n Classifier exists for the project";
								}	
							}
				        	else
							{
								status="Metadata Json loading Failed";
							}	
				        }
				        else
				        {
				        	status="Merging the csv failed";
				        }
					}
					else
					{
						status="Classifier Deletion Failed";
					}
				}
				else
				{
					status="Data Cleaning Failed";
				}
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
				input.close();
			} 
			catch (IOException e) 
			{
				logger.error(e.toString());
			}
		}		
		return status;		
	} 	
	/* On-board a Client with no historical data / less historical data */
	@RequestMapping("/onboardClient")
	public String onboardClient(@RequestParam("projectName")String projectName,@RequestParam("dataStatus")String dataStatus,@RequestParam("dou") String dou,@RequestParam("csvData") String file)
	{
		String status =null;
		try
		{
			if(dataStatus.toUpperCase().equals("yes".toUpperCase()) && file !=null)
			{
				String flag="true";
				DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
				Date date = new Date();				
				
				String default_csv_location ="/tmp/virtual-dispatcher/data/DEFAULT/upload/onboard.csv";
				String combined_rawdata_location ="/tmp/virtual-dispatcher/data/"+projectName+"/rawdata/combined.csv";
				String cleaning_path="/tmp/virtual-dispatcher/data/"+projectName+"/cleaned/";
				String cleaning_out_file=projectName+"_cleaned"+dateFormat.format(date)+".csv";				
				String uploadfile="/tmp/virtual-dispatcher/data/"+projectName+"/upload/output.csv";						
			
				String merge_status=ld.mergeData(default_csv_location, file,combined_rawdata_location);					
				if(merge_status.equals("Data Merged Successfully"))
				{
					/* 1. Load the tables */							
					String load_status=ld.loadMetaData(file);
					if(load_status.equals("Data Loading Completed"))
					{
						/* 2. Cleaning the data */
						logger.info("Data Loading Completed");
						String clean_status=ld.cleanData(combined_rawdata_location,cleaning_path,cleaning_out_file,uploadfile,flag);		
						if(clean_status.equals("Data Cleaned Successfully"))						
						{
							/* 3. Create Classifier/Corpus*/
							logger.info("Data Cleaned Successfully");
							String meta_status=ld.loadMetadataJson(ld.project);
							
							if(meta_status.equals("success"))
							{
								ArrayList<String> instanceDetails = new ArrayList<String>();							
								instanceDetails=db.getNLCInstance();
								String username=instanceDetails.get(1);
								String password=instanceDetails.get(2);
								Integer instanceID=Integer.parseInt(instanceDetails.get(0));														
								
								Integer projectID=db.getProject(ld.project);
								Integer count=db.getClassifierStatus(projectID);
								if(count.equals(0))
								{
									logger.info("Corpus Creation Started");
									cs.createCorpus(file, projectID);
									logger.info("Corpus Creation Completed");								
									String classifierID=null;
									classifierID=cr.createClassifier(uploadfile,username,password);
									logger.info("Classifier Created Successfully");
									if(classifierID !=null)
									{		
										Integer watsonStatus=null;
										watsonStatus=db.loadClassifier(projectID, classifierID,instanceID);		
										if(watsonStatus.equals(1))
										{
											status="On-boarding client with less historical data Completed successfully";
											logger.info("On-boarding client with less historical data Completed successfully");
											db.updateHistDataStatus(projectID,1);
										}									
									}
									else
									{
										status="Training Failed \n Classifier not created";										
									}								
								}
								else
								{
									status="Training stopped \n Classifier exists for the project";											
								}							
							}									
						}
						else
						{
							logger.info("Data Clean Failed");
									
						}
					}
					else
					{
						logger.info("Data Loading Failed");
						
					}
				}
				else
				{
					logger.info("Data Merge Failed");
						
				}
				
			}
			else if(dataStatus.toUpperCase().equals("No".toUpperCase()))
			{
				String watsonModel=null;
				watsonModel=db.getDefaultWatsonModel();	
				String classifierID=watsonModel.split("```")[0];
				Integer instanceID=Integer.parseInt(watsonModel.split("```")[1]);
				
				/* Load Tenant */
				Integer tenant_status=null;
				tenant_status=db.loadProjects(projectName);
				Integer projectID=db.getProject(projectName);
				
				if(tenant_status.equals(1))
				{
					/* Load DOU */					
					ArrayList <String> douDetails = new ArrayList<String>();
					douDetails=db.getAgreement(dou);
					String douID=douDetails.get(0);
					String douName=douDetails.get(1);
					String douStartDate=douDetails.get(2);
					String douEndDate=douDetails.get(3);
					String douOwningCountry=douDetails.get(4);	
					Integer dou_status=null;
					dou_status=db.loadAgreements(douID,douName,douStartDate,douEndDate,douOwningCountry,projectID);	
					if(dou_status.equals(1))
					{
						/* Load Categories */
						HashSet<String> defaultCategories=db.getCategories(0);
						Integer catStatus=null;
						for (String defaultCat : defaultCategories) 
						{
							db.loadCategories(defaultCat, projectID);
							catStatus=1;
				        }
						if(catStatus.equals(1))
						{
							/* Load Watson Model */		
							Integer watsonStatus=null;
							watsonStatus=db.loadClassifier(projectID, classifierID, instanceID);
							if(watsonStatus.equals(1))
							{
								status="Onboarding Client with no historical data completed succcessfully";
								logger.info("Onboarding Client with no historical data completed succcessfully");
								db.updateHistDataStatus(projectID,0);
							}
							else
							{
								logger.info("Watson Model not loaded.. Onboarding Client with no historical data failed");
							}							
						}
						else
						{
							logger.info("Category not loaded");
						}
					}
					else
					{
						logger.info("DOU not loaded");
					}				
				}
				else 
				{
					logger.info("Tenant not loaded");
				}			
			}
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		return status;		
		
	}
	/* Build the default Corpus/Classifier for clients with no/less historical data */
	@SuppressWarnings("unused")
	@RequestMapping("/buildDefaultCorpus")
	public String buildDefaultCorpus(@RequestParam("csvData") String file)
	{
		String status=null;
		try
		{
			String flag="true";
			DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
			Date date = new Date();
			
			String cleaning_path="/tmp/virtual-dispatcher/data/DEFAULT/cleaned/";
			String cleaning_out_file="DEFAULT_cleaned"+dateFormat.format(date)+".csv";				
			String uploadfile="/tmp/virtual-dispatcher/data/DEFAULT/upload/output.csv";		
			String onboardfile="/tmp/virtual-dispatcher/data/DEFAULT/upload/onboard.csv";		
			
			String load_status=ld.loadDefaultCategories(file);
			if(load_status.equals("Category Loading Completed"))
			{
				/* 2. Cleaning the data */
				logger.info("Category Loading Completed");
				String clean_status=ld.defaultCleanData(file,cleaning_path,cleaning_out_file,uploadfile,flag);	
				if(clean_status.equals("Data Cleaned Successfully"))						
				{
					/* 3. Create Classifier/Corpus*/
					logger.info("Data Cleaned Successfully");
					ld.defaultCleanData(file,cleaning_path,cleaning_out_file,onboardfile,flag);	
					String meta_status=ld.loadMetadataJson("DEFAULT-CORPUS");
					
					if(meta_status.equals("success"))
					{						
						ArrayList<String> instanceDetails = new ArrayList<String>();							
						instanceDetails=db.getNLCInstance();
						String username=instanceDetails.get(1);
						String password=instanceDetails.get(2);
						Integer instanceID=Integer.parseInt(instanceDetails.get(0));	
						
						if(instanceID !=null)
						{
							logger.info("Corpus Creation Started");
							cs.createCorpus(file,0);
							logger.info("Corpus Creation Completed");
							String classifierID=null;
							classifierID=cr.createClassifier(uploadfile,username,password);
							logger.info("Classifier Created Successfully");
							if(classifierID !=null)
							{		
								Integer watsonStatus=null;
								watsonStatus=db.loadDefaultClassifier(classifierID,instanceID);	
								if(watsonStatus.equals(1))
								{
									status="Default Corpus/Classifier Created";
								}	
							}
							else
							{
								logger.info("Classifier not created");
							}
						}
						else {
							logger.info("NLC Instance not available");
						}
					}
					else
					{
						logger.info("Meta data not loaded");
					}
				}
				else
				{
					logger.info("Data Cleaning Failed");
				}
			}
			else
			{
				logger.info("Category loading failed");
			}
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		return status;
	}
	@RequestMapping("/offboardClient")	
	public String offboardClient(@RequestParam("projectName")String projectName)
	{	
	   String status=null;
		try
		{		
			/* 1. Get the tenant id */			
			Integer projectID=db.getProject(projectName);
			if(projectID != null)
			{
				/* 2. Delete from Engagement */
				Integer engStatus = db.deleteEngagement(projectID);
				if(engStatus.equals(1))
				{
					/* 3. Delete from Category */
					Integer catStatus = db.deleteCategory(projectID);
					if(catStatus.equals(1))
					{							
						Integer corpStatus=db.deleteCorpus(projectID);
						if(corpStatus.equals(1))
						{
							/* 4. Identifying and Invoking the delete classifier url */
							ArrayList<String> classifierDetails = new ArrayList<String>();
							classifierDetails=db.getClassifier(projectID);
							String classifierID=classifierDetails.get(0);
							String username=classifierDetails.get(1);
							String password=classifierDetails.get(2);
							status=cr.deleteClassifier(classifierID, username, password,projectID);
							if(status.equals("classifier deleted"))
							{
								/* 5. Deleting the project */
								Integer projStatus = db.deleteProject(projectID);
								if(projStatus.equals(1))
								{
									status="Offboarding Completed";
									logger.info("Offboarding Completed");
								}
								else
								{
									status="Tenant - Data Deletion Failed";
									logger.info("Tenant - Data Deletion Failed");
								}
							}
							else
							{
								status="Classifier Deletion Failed";
								logger.info("Classifier Deletion Failed");
							}		
						}
						else
						{
							status="Corpus Deletion Failed";
							logger.info("Corpus Deletion Failed");
						}
					}							
					else
					{
						status="Category - Data Deletion Failed";
						logger.info("Category - Data Deletion Failed");
					}	
				}
				else
				{
					status="Engagement - Data Deletion Failed";
					logger.info("Engagement - Data Deletion Failed");
				}				
			}
			else
			{
				status="Invalid Project Name";
				logger.info("Invalid Project Name");					
			}	
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}  
		return status;
   }
}
