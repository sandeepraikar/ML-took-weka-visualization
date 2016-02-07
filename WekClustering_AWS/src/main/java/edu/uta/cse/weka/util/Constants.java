package edu.uta.cse.weka.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author Sandeep_Raikar
 * 
 */
public class Constants {

	public static final String AWS_ACCESS_KEY;
	public static final String AWS_SECRET_KEY;
	public static final String AWS_S3_BUCKET_NAME;
	public static final String WEKA_DUMP;
	public static final String AWS_S3_CLUSTERED_BUCKET_NAME;


	static {

		Configuration config = null;
		try {

			config = new PropertiesConfiguration("config.properties");

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		AWS_S3_BUCKET_NAME = config.getString("s3.bucket.name");
		AWS_ACCESS_KEY = config.getString("aws.accesskey");
		AWS_SECRET_KEY = config.getString("aws.secretkey");
		WEKA_DUMP = config.getString("weka.dump");
		AWS_S3_CLUSTERED_BUCKET_NAME = config.getString("s3.bucket.clustred.name");
	}
}
