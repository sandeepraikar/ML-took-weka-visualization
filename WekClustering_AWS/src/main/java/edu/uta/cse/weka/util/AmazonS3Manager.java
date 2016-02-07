package edu.uta.cse.weka.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

public class AmazonS3Manager {

	private static Logger LOGGER = LoggerFactory
			.getLogger(AmazonS3Manager.class);
	private static AmazonS3 s3Client;

	static {
		try {
			AWSCredentials awscredentials = new BasicAWSCredentials(Constants.AWS_ACCESS_KEY,Constants.AWS_SECRET_KEY);
			//s3Client = new AmazonS3Client();
			s3Client = new AmazonS3Client(awscredentials);
			Region usWest2 = Region.getRegion(Regions.US_WEST_2);
			s3Client.setRegion(usWest2);

		} catch (AmazonServiceException ase) {
			LOGGER.info("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			LOGGER.info("Error Message:    " + ase.getMessage());
			LOGGER.info("HTTP Status Code: " + ase.getStatusCode());
			LOGGER.info("AWS Error Code:   " + ase.getErrorCode());
			LOGGER.info("Error Type:       " + ase.getErrorType());
			LOGGER.info("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			LOGGER.info("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			LOGGER.info("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			LOGGER.info("Exception occured: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void checkIfBucketExists(){
		if (!s3Client.doesBucketExist(Constants.AWS_S3_BUCKET_NAME)) {
			createBucket();
		}else{
			LOGGER.info("Bucket already exists!!!");
		}
	}
	
	public static void createBucket() {
		long startTime, endTime;
		try {
			startTime = System.currentTimeMillis();
			if (!s3Client.doesBucketExist(Constants.AWS_S3_BUCKET_NAME)) {
				Bucket bucket = s3Client
						.createBucket(Constants.AWS_S3_BUCKET_NAME);
				endTime = System.currentTimeMillis();
				LOGGER.info(bucket.getName() + "created Successfully");
				LOGGER.info("Total Time taken in milli seconds: "
						+ (endTime - startTime));
			} else {
				LOGGER.info("Bucket alread exist!!");
			}

		} catch (AmazonServiceException e) {
			LOGGER.error("Amazon Service Exception occured while creating new Bucket : "
					+ e);
			e.printStackTrace();
		} catch (AmazonClientException e) {
			LOGGER.error("Amazon Client Exception occured while creating new Bucket : "
					+ e);
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.info("Exception occured" + e);
			e.printStackTrace();
		}
	}
	
	public static void uploadDataSet(InputStream inputStream, String fileName) {

		try {
			long startTime = System.currentTimeMillis();
			
			if (s3Client.doesBucketExist(Constants.AWS_S3_BUCKET_NAME)) {
				LOGGER.info("Uploading the csv data set :"+fileName);
				
				/*
				PutObjectRequest putObjectRequest = new PutObjectRequest(
						bucketName + "/", uploadFile.getName(), uploadFile);*/
				/*PutObjectRequest putObjectRequest = new PutObjectRequest(
						Constants.AWS_S3_BUCKET_NAME , uploadFile.getName(), uploadFile);
				*/
				PutObjectRequest putObjectRequest = new PutObjectRequest(Constants.AWS_S3_BUCKET_NAME, fileName, inputStream,new ObjectMetadata());
				PutObjectResult result = s3Client.putObject(putObjectRequest);

				LOGGER.info("Version ID : " + result.getVersionId());
				LOGGER.info("Etag:" + result.getETag() + "-->" + result);

				long endTime = System.currentTimeMillis();
				LOGGER.info("Total Time taken to Upload in milli seconds: "
						+ (endTime - startTime));

			} else {
				LOGGER.error("Specified Bucket does not exist!!");
			}
		
		} catch (Exception e) {
			LOGGER.info("Exception occured" + e);
			e.printStackTrace();
		}
	}
	public static InputStream getObject(String key){
		InputStream inputStream = null;
		try{
			S3Object s3object = s3Client.getObject(new GetObjectRequest(Constants.AWS_S3_BUCKET_NAME, key));
			inputStream=s3object.getObjectContent();
			
		}catch(Exception e){
			LOGGER.error("Exception occured while retrieving object"+e.getMessage());
			e.printStackTrace();
		}
		return inputStream;
	}
	
	@SuppressWarnings("deprecation")
	public static URL uploadFileWithURL(File uploadFile) {
		URL url=null;
		
		try {
			long startTime = System.currentTimeMillis();
			
			if (s3Client.doesBucketExist(Constants.AWS_S3_CLUSTERED_BUCKET_NAME)) {
				LOGGER.info("Uploading the file");
				
				/*
				PutObjectRequest putObjectRequest = new PutObjectRequest(
						bucketName + "/", uploadFile.getName(), uploadFile);*/
				PutObjectRequest putObjectRequest = new PutObjectRequest(
						Constants.AWS_S3_CLUSTERED_BUCKET_NAME , uploadFile.getName(), uploadFile);
				
				PutObjectResult result = s3Client.putObject(putObjectRequest);
			
				
				LOGGER.info("Version ID : " + result.getVersionId());
				LOGGER.info("Etag:" + result.getETag() + "-->" + result);

				long endTime = System.currentTimeMillis();
				LOGGER.info("Total Time taken to Upload in milli seconds: "
						+ (endTime - startTime));
				
				//Retrieve the URL of the object stored in Amazon S3
				s3Client.setObjectAcl(Constants.AWS_S3_CLUSTERED_BUCKET_NAME, uploadFile.getName(),
						CannedAccessControlList.PublicRead);
				GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
						Constants.AWS_S3_CLUSTERED_BUCKET_NAME, uploadFile.getName());
				//request.setExpiration(new Date(1435231568));
				url = s3Client.generatePresignedUrl(request);
				
				
			} else {
				LOGGER.error("Specified Bucket does not exist!!");
			}
		
		} catch (Exception e) {
			LOGGER.info("Exception occured" + e);
			e.printStackTrace();
		}
		return url;
	}


}
