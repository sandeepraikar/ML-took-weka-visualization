package edu.uta.cse.weka.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.gui.explorer.ClustererPanel;
import weka.gui.visualize.PlotData2D;

/**
 * @author Sandeep Raikar
 *
 */
public class WekaUtil {

	private static Logger LOGGER = LoggerFactory.getLogger(WekaUtil.class);

	public static void convertArff2CSV(File inputFile, File outputFile) {

		try {

			// load ARFF file
			ArffLoader arffLoader = new ArffLoader();
			arffLoader.setSource(inputFile);
			Instances data = arffLoader.getDataSet();

			// save to csv file
			CSVSaver csvSaver = new CSVSaver();
			csvSaver.setInstances(data);
			csvSaver.setFile(outputFile);

			csvSaver.writeBatch();
			
			LOGGER.info("CSV file created Successfully!!");
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static void convertCSV2Arff(InputStream stream, File outputFile) {
		try {
			
			File parentDirectory = outputFile.getParentFile();
			
			if (!parentDirectory.exists())
			  {
				LOGGER.info("Creating directory: " + parentDirectory);
			    parentDirectory.mkdir();
			}
			// load CSV
			CSVLoader loader = new CSVLoader();
			// loader.setSource(inputFile);
			loader.setSource(stream);

			Instances data = loader.getDataSet();

			// save ARFF
			
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(outputFile);

			// saver.setDestination(outputFile);
			saver.writeBatch();
			
			LOGGER.info("Arff file created successfully!!");
		} catch (Exception e) {
			LOGGER.error("Exception occurred"+e.getMessage());
			e.printStackTrace();
		}
	}

	public static URL createCluster(String fileNameWithExtension, int clusterSize) {
		URL url =null;
		try {

			
			String fileName=fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
			// read the .arff file
			BufferedReader inputReader = new BufferedReader(new FileReader(Constants.WEKA_DUMP+fileNameWithExtension));
			Instances data = new Instances(inputReader);
			
			inputReader.close();

			// create the model
			SimpleKMeans kMeans = new SimpleKMeans();
			kMeans.setNumClusters(clusterSize);
			kMeans.buildClusterer(data);
			

			// print out the cluster centroids
			Instances centroids = kMeans.getClusterCentroids();
			System.out.println("number of centroid instances : "
					+ centroids.numInstances());
			for (int i = 0; i < centroids.numInstances(); i++) {
				System.out.println("Centroid " + i + 1 + ": "
						+ centroids.instance(i));
			}

			// get cluster membership for each instance
			/*for (int i = 0; i < data.numInstances(); i++) {
				System.out.println(data.instance(i) + " is in cluster "
						+ kMeans.clusterInstance(data.instance(i)) + 1);
			}*/

			// evaluate cluster
			ClusterEvaluation eval = new ClusterEvaluation();
			eval.setClusterer(kMeans);
			eval.evaluateClusterer(data);

			BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.WEKA_DUMP+fileName+"_clustered.arff"));

			PlotData2D predData = ClustererPanel.setUpVisualizableInstances(
					data, eval);

			// save data to a file
			writer.write(predData.getPlotInstances().toString());
			// writer.write(data.toString());
			writer.newLine();
			writer.flush();
			writer.close();

			System.out
					.println("evaluated cluster written to file successfully!!");

			
			if(Files.exists(Paths.get(edu.uta.cse.weka.util.Constants.WEKA_DUMP+fileName+"_clustered.arff"))){
				convertArff2CSV(new File(Constants.WEKA_DUMP+fileName+"_clustered.arff"),new File(Constants.WEKA_DUMP+fileName+".csv"));
				url=AmazonS3Manager.uploadFileWithURL(new File(Constants.WEKA_DUMP+fileName+".csv"));
				LOGGER.info("File Uploaded to S3 successfully!!");
			}else{
				LOGGER.error(".arff file is not created!");
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("Exception occurred"+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Exception occurred"+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error("Exception occurred"+e.getMessage());
			e.printStackTrace();
		}
		return url;
	}
}
