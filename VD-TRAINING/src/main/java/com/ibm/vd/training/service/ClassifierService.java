package com.ibm.vd.training.service;
import java.io.File;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.watson.developer_cloud.natural_language_classifier.v1.NaturalLanguageClassifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.CreateClassifierOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.DeleteClassifierOptions;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
/* Classifier/Watson Model related activities are specified here */
@Service
public class ClassifierService 
{
	@Autowired
	DataService db;
	
	final static Logger logger = Logger.getLogger(ClassifierService.class);
	/* Create a Watson Model for the Client */
	public String createClassifier(String file,String username,String password)
	{
		String classifierID=null;
		try
		{
			IamOptions options = new IamOptions.Builder().apiKey(password).build();
			NaturalLanguageClassifier naturalLanguageClassifier = new NaturalLanguageClassifier(options);
			naturalLanguageClassifier.setEndPoint(username);
			CreateClassifierOptions createOptions = new CreateClassifierOptions.Builder().metadata(new File("./metadata.json"))
			.trainingData(new File(file)).build();
			Classifier classifier = naturalLanguageClassifier.createClassifier(createOptions).execute();
			JSONObject jsonObj = new JSONObject(classifier.toString());
			classifierID=jsonObj.getString("classifier_id");				
		}
		catch(Exception e)
		{
			logger.error(e.toString());	
		}
		return classifierID;
	}
	
	/* Delete the Watson Model created for the Client */
	public String deleteClassifier(String classifierID,String username,String password,Integer projectID)
	{
		String status=null;
		try
		{
			Integer nlcStatus=db.getNLCStatus(projectID);
			if(nlcStatus.equals(0))
			{
				NaturalLanguageClassifier service = new NaturalLanguageClassifier();
				service.setUsernameAndPassword(username, password);				
				DeleteClassifierOptions deleteOptions = new DeleteClassifierOptions.Builder().classifierId(classifierID).build();
				service.deleteClassifier(deleteOptions).execute();
				db.deleteClassifier(classifierID);
				status="classifier deleted";
			}			
			else
			{
				IamOptions options = new IamOptions.Builder().apiKey(password).build();
				NaturalLanguageClassifier naturalLanguageClassifier = new NaturalLanguageClassifier(options);
				naturalLanguageClassifier.setEndPoint(username);
				DeleteClassifierOptions deleteOptions = new DeleteClassifierOptions.Builder().classifierId(classifierID).build();
				naturalLanguageClassifier.deleteClassifier(deleteOptions).execute();
				db.deleteClassifier(classifierID);
				status="classifier deleted";
			}
		}
		catch(Exception e)
		{
			logger.error(e.toString());	
		}
		return status;
	}
}
