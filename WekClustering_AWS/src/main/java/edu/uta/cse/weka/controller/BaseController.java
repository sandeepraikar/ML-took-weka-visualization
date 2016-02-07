package edu.uta.cse.weka.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;


import edu.uta.cse.weka.util.AmazonS3Manager;
import edu.uta.cse.weka.util.WekaUtil;

@Controller
public class BaseController {

	private static Logger LOGGER = LoggerFactory
			.getLogger(BaseController.class);

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String login(ModelMap map) {
		LOGGER.info("Redirecting to landing page");
		return "LandingPage";
	}

	@RequestMapping(value = "/uploadImpl", method = RequestMethod.POST)
	public String uploadDataSet(MultipartRequest request,ModelMap map) {
		List<String> params = new ArrayList<String>();
		try {

			LOGGER.info("inside upload method!!");
			MultipartFile mpf = null;
			Iterator<String> itr = request.getFileNames();
			String fileNameWithExtension=null;
			while (itr.hasNext()) {
				mpf = request.getFile(itr.next());
				LOGGER.info("fileName : "+mpf.getOriginalFilename());
				LOGGER.info("inputstream : "+mpf.getInputStream());
				InputStream data = new ByteArrayInputStream(mpf.getBytes());
				LOGGER.info("input stream : "+data);
				fileNameWithExtension= mpf.getOriginalFilename();
				String fileName=fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
				AmazonS3Manager.uploadDataSet(data,
						mpf.getOriginalFilename());
				Thread.sleep(100);

				// Read the data set from Amazon S3
				AmazonS3Manager.getObject(mpf.getOriginalFilename());
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(AmazonS3Manager.getObject(fileNameWithExtension)));

				
				String firstLine = reader.readLine();

				LOGGER.info("first line from csv file : " + firstLine);
				for (String columnName : firstLine.split(",")) {					
					params.add(columnName);
				}
				
				WekaUtil.convertCSV2Arff(AmazonS3Manager.getObject(fileNameWithExtension), new File(edu.uta.cse.weka.util.Constants.WEKA_DUMP+fileName+".arff"));
				map.addAttribute("attributeList",params);
				map.addAttribute("fileName",fileName+".arff");
			}

		} catch (Exception ex) {
			LOGGER.error("Exception occured : " + ex.getMessage());
			ex.printStackTrace();
		}
		return "DataCluster";
	}
	
	@RequestMapping(value = "/createClusterImpl", method = RequestMethod.POST)
	public @ResponseBody String createCluster(@RequestParam(value = "numOfClusters") String numOfClusters,@RequestParam(value = "arffFileName")  String arffFileName) {
		URL url= null;
		String fileName=arffFileName.substring(0, arffFileName.lastIndexOf('.'));
		LOGGER.info("inside create Cluster!!");
		LOGGER.info("No. of clusters "+ numOfClusters);
		LOGGER.info("file name:"+ fileName);
		
		if(Files.exists(Paths.get(edu.uta.cse.weka.util.Constants.WEKA_DUMP+arffFileName))){
			 url = WekaUtil.createCluster(arffFileName, Integer.parseInt(numOfClusters));	
		}
		//return fileName+".csv";
		return url.toString();
	}	
}
