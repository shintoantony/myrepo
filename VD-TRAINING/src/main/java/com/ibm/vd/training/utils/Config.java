package com.ibm.vd.training.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/* Configuration values from the application.yml file */
@Component
public class Config 
{
	    @Value("${app.db_username}")
	    private String db_username;
	    
	    @Value("${app.db}")
	    private String db;
	    
	    @Value("${app.db_passcode}")
	    private String db_passcode;

	    @Value("${app.report_db}")
	    private String reportdb;

	    @Value("${app.report_db_username}")
	    private String reportdb_username;

	    @Value("${app.report_db_passcode}")
	    private String reportdb_passcode;
	    
	    @Value("${app.corpus_db}")
	    private String corpus_db;

	    @Value("${app.corpus_db_username}")
	    private String corpus_db_username;

	    @Value("${app.corpus_db_passcode}")
	    private String corpus_db_passcode;
	    

		public String getCorpus_db() {
			return corpus_db;
		}

		public void setCorpus_db(String corpus_db) {
			this.corpus_db = corpus_db;
		}

		public String getCorpus_db_username() {
			return corpus_db_username;
		}

		public void setCorpus_db_username(String corpus_db_username) {
			this.corpus_db_username = corpus_db_username;
		}

		public String getCorpus_db_passcode() {
			return corpus_db_passcode;
		}

		public void setCorpus_db_passcode(String corpus_db_passcode) {
			this.corpus_db_passcode = corpus_db_passcode;
		}

		public String getDb_username() {
			return db_username;
		}

		public void setDb_username(String db_username) {
			this.db_username = db_username;
		}

		public String getDb() {
			return db;
		}

		public void setDb(String db) {
			System.out.println("confign values"+this.db);
			this.db = db;
		}

		public String getDb_passcode() {
			return db_passcode;
		}

		public void setDb_passcode(String db_passcode) {
			this.db_passcode = db_passcode;
		}

		public String getReportdb() {
			return reportdb;
		}

		public void setReportdb(String reportdb) {
			this.reportdb = reportdb;
		}

		public String getReportdb_username() {
			return reportdb_username;
		}

		public void setReportdb_username(String reportdb_username) {
			this.reportdb_username = reportdb_username;
		}

		public String getReportdb_passcode() {
			return reportdb_passcode;
		}

		public void setReportdb_passcode(String reportdb_passcode) {
			this.reportdb_passcode = reportdb_passcode;
		}    
}
