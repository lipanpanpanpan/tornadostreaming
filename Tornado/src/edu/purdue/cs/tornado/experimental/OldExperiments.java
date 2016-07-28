package edu.purdue.cs.tornado.experimental;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.storm.Config;
import org.apache.storm.metric.LoggingMetricsConsumer;

import edu.purdue.cs.tornado.SpatioTextualToplogyBuilder;
import edu.purdue.cs.tornado.SpatioTextualToplogySubmitter;
import edu.purdue.cs.tornado.experimental.baseline.BaselineEvaluator;
import edu.purdue.cs.tornado.helper.IndexCell;
import edu.purdue.cs.tornado.helper.IndexCellCoordinates;
import edu.purdue.cs.tornado.helper.KillTopology;
import edu.purdue.cs.tornado.helper.PartitionsHelper;
import edu.purdue.cs.tornado.helper.Point;
import edu.purdue.cs.tornado.helper.QueryType;
import edu.purdue.cs.tornado.helper.Rectangle;
import edu.purdue.cs.tornado.helper.SpatioTextualConstants;
import edu.purdue.cs.tornado.helper.TextualPredicate;
import edu.purdue.cs.tornado.index.global.GlobalIndexType;
import edu.purdue.cs.tornado.index.local.LocalIndexType;
import edu.purdue.cs.tornado.loadbalance.Cell;
import edu.purdue.cs.tornado.loadbalance.LoadBalanceMessage;
import edu.purdue.cs.tornado.loadbalance.Partition;
import edu.purdue.cs.tornado.messages.CombinedTuple;
import edu.purdue.cs.tornado.messages.Control;
import edu.purdue.cs.tornado.messages.DataObject;
import edu.purdue.cs.tornado.messages.DataObjectList;
import edu.purdue.cs.tornado.messages.Query;
import edu.purdue.cs.tornado.messages.ResultSetChange;
import edu.purdue.cs.tornado.performance.ClusterInformationExtractor;
import edu.purdue.cs.tornado.serializer.CellSerializer;
import edu.purdue.cs.tornado.serializer.CombinedTupleSerializer;
import edu.purdue.cs.tornado.serializer.ControlSerializer;
import edu.purdue.cs.tornado.serializer.DataObjectListSerializer;
import edu.purdue.cs.tornado.serializer.DataObjectSerializer;
import edu.purdue.cs.tornado.serializer.IndexCellCoordinatesSerializer;
import edu.purdue.cs.tornado.serializer.IndexCellSerializer;
import edu.purdue.cs.tornado.serializer.LoadBalanceMessageSerializer;
import edu.purdue.cs.tornado.serializer.PartitionSerializer;
import edu.purdue.cs.tornado.serializer.PointSerializer;
import edu.purdue.cs.tornado.serializer.QuerySerializer;
import edu.purdue.cs.tornado.serializer.RectangleSerializer;
import edu.purdue.cs.tornado.serializer.ResultSetChangeSerializer;
import edu.purdue.cs.tornado.spouts.FileSpout;
import edu.purdue.cs.tornado.spouts.QueriesFileSystemSpout;
import edu.purdue.cs.tornado.spouts.TweetsFSSpout;
import edu.purdue.cs.tornado.storage.POILFSDataSource;

public class OldExperiments{
	//static String javaArgs = "-Xmx5g -Xms5g ";
	static String javaArgs = "  -Xmx2g -Xms2g -Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ";
	static Integer fineGridGran = 64;

	public static void main(String[] args) {

		//getStats();
		experiment2();

	}

	public static void experiment2() {
		javaArgs = "-Xmx2600m -Xms2600m -Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ";
		//testGridGlobalGridLocaltest("results/GlobalGridLocalGrid.csv", 0, 1, 16);
		//testBaseLinePlain("results/baseline.csv", 0, 0, 36);
		//testBaseLine("results/baseline.csv", 0, 0, 36);
		buildRangeQueryToplogyBackPressure("Tornado", 64+1/* evaluatorParrellism */, 10/* routingParrellism */,
				5/* spout parrallism */, 3/* initial emitsleep duration */,
				0/* number of ackers */, 1000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
				20/* minutes to stats */, LocalIndexType.HYBRID_GRID /* localIndexType */, GlobalIndexType.DYNAMIC_OPTIMIZED/* globalIndexType */
				, "resources/partitions64_1024.ser"/* partitionsPath */,
				1/* query Spout parrellisim */, 1, 1024, 0);
	}
	//texting various baselines
	public static void experiment3_5million() {
			javaArgs = "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:+PrintGCDetails -Xloggc:/home/apache-storm-1.0.0/logs/gc-storm-worker-%ID%-" + (new Date()).getTime()
					+ ".log  -Xmx2600m -Xms2600m -Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ";
			testPartitonedTextAWAREGlobalGridLocalLocalGranularityEffect("results/GlobalPartitionedTextLocalGrid.csv", 0, 10, 36);

			//testGridGlobalGridLocal5million("results/GlobalGridLocalGrid.csv", 0, 10, 36);
			//testGridGlobalNoLocal_5million("results/GlobalGridNoLocal.csv", 0, 10, 36);
			testPartitonedGlobalGridLocal5million("results/GlobalPartitionedLocalGrid.csv", 0, 10, 36);
			testBaseLine("results/baseline.csv", 0, 10, 36);
	}
	//texting various baselines
	public static void experiment1_5million() {
		javaArgs = "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:+PrintGCDetails -Xloggc:/home/apache-storm-1.0.0/logs/gc-storm-worker-%ID%-" + (new Date()).getTime()
				+ ".log  -Xmx2600m -Xms2600m -Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ";
				//testPartitonedTextAWAREGlobalGridLocalLocalGranularityEffect("results/GlobalPartitionedTextLocalGrid.csv", 0, 10, 36);

		//testGridGlobalGridLocal5million("results/GlobalGridLocalGrid.csv", 0, 10, 36);
		//testGridGlobalNoLocal_5million("results/GlobalGridNoLocal.csv", 0, 10, 36);
		testPartitonedGlobalGridLocal5million("results/GlobalPartitionedLocalGrid.csv", 0, 10, 36);
		testBaseLine("results/baseline.csv", 0, 10, 36);
	}

	public static void testGridGlobalNoLocal_5million(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";
		result = buildRangeQueryToplogy("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
				1/* spout parrallism */, 3/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				5000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
				15/* minutes to stats */, LocalIndexType.NO_LOCAL_INDEX /* localIndexType */, GlobalIndexType.GRID/* globalIndexType */
				, "resources/partitionsDataAndQueries36_512.ser"/* partitionsPath */,
				1/* query Spout parrellisim */, 1, 1024, 9);
		appendToFile(fileName, result);

	}

	public static void testGridGlobalGridLocaltest(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";

		result = buildRangeQueryToplogy("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
				100/* spout parrallism */, 1/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				100000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
				30/* minutes to stats */, LocalIndexType.HYBRID_GRID /* localIndexType */, GlobalIndexType.GRID/* globalIndexType */
				, "resources/partitionsDataAndQueries36_1024.ser"/* partitionsPath */,
				1/* query Spout parrellisim */, 1, 1024, 95);
		appendToFile(fileName, result);
	}

	public static void testGridGlobalGridLocal5million(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";

		for (int i = 0; i < 3; i++) {
			result = buildRangeQueryToplogy("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
					1/* spout parrallism */, 3/* initial emitsleep duration */,
					numberOfAckers/* number of ackers */,
					5000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
					15/* minutes to stats */, LocalIndexType.HYBRID_GRID /* localIndexType */, GlobalIndexType.GRID/* globalIndexType */
					, "resources/partitionsDataAndQueries36_1024.ser"/* partitionsPath */,
					1/* query Spout parrellisim */, 1, 1024, 95);
			appendToFile(fileName, result);
		}

	}

	public static void testPartitonedGlobalGridLocal5million(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {
		String result = "";
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		for (int j = 0; j < 4; j++) {
			result = buildRangeQueryToplogy("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
					4/* spout parrallism */, 3/* initial emitsleep duration */,
					numberOfAckers/* number of ackers */,
					5000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
					15/* minutes to stats */, LocalIndexType.HYBRID_GRID /* localIndexType */, GlobalIndexType.PARTITIONED/* globalIndexType */
					, "resources/partitionsDataAndQueries36_1024.ser"/* partitionsPath */,
					1/* query Spout parrellisim */, 1, 1024, 0);

			appendToFile(fileName, result);
		}

	}

	public static void testPartitonedTextAWAREGlobalGridLocalLocalGranularityEffect(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";
		for (int i = 5; i <= 5; i++) {
			for (int j = 0; j < 2; j++) {
				result = buildRangeQueryToplogy("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
						i/* spout parrallism */,
						6/* initial emitsleep duration */,
						numberOfAckers/* number of ackers */,
						5000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
						18/* minutes to stats */, LocalIndexType.HYBRID_GRID /* localIndexType */, GlobalIndexType.PARTITIONED_TEXT_AWARE/* globalIndexType */
						, "resources/partitionsDataAndQueries36_1024.ser"/* partitionsPath */,
						1/* query Spout parrellisim */, 1, 1024, 0);
				appendToFile(fileName, result);
			}
		}
	}

	public static void testFineGridGranularityEffect(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";
		result = buildRangeQueryToplogy("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
				20/* spout parrallism */, 2/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				1000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
				17/* minutes to stats */, LocalIndexType.HYBRID_GRID /* localIndexType */, GlobalIndexType.GRID/* globalIndexType */
				, "resources/partitionsDataAndQueries36_1024.ser"/* partitionsPath */,
				1/* query Spout parrellisim */, 1, 1024, 0);
		appendToFile(fileName, result);
	}

	public static void testRangeQueriesNumberOfQueries(String fileName, LocalIndexType localIndexType, GlobalIndexType globalIndexType, Integer numberOfAckers) {
		appendToFile(fileName, "-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";

		result = buildRangeQueryToplogy("Tornado", 64/* evaluatorParrellism */, 50/* routingParrellism */,
				4/* spout parrallism */, 3/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				100000/* number of queries */, 5/* numberOfQueryKeywords */, 50.0/* spatialRange */,
				20/* minutes to stats */, localIndexType /* localIndexType */, globalIndexType/* globalIndexType */
				, "resources/partitionsDataAndQueries64.ser"/* partitionsPath */,
				1/* query Spout parrellisim */, 1, fineGridGran, 0);
		appendToFile(fileName, result);

		result = buildRangeQueryToplogy("Tornado", 64/* evaluatorParrellism */, 50/* routingParrellism */,
				4/* spout parrallism */, 300000/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				1000000/* number of queries */, 5/* numberOfQueryKeywords */, 50.0/* spatialRange */,
				20/* minutes to stats */, localIndexType /* localIndexType */, globalIndexType/* globalIndexType */
				, "resources/partitionsDataAndQueries64.ser"/* partitionsPath */,
				1/* query Spout parrellisim */, 1, fineGridGran, 0);
		appendToFile(fileName, result);
	}

	public static void testWorkerScalability(String fileName, LocalIndexType localIndexType, GlobalIndexType globalIndexType, Integer numberOfAckers) {
		appendToFile(fileName, "Scalability test,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,");
		String result = "";

		result = buildRangeQueryToplogy("Tornado", 64/* evaluatorParrellism */, 16/* routingParrellism */,
				3/* spout parrallism */, 3/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				200000/* number of queries */, 5/* numberOfQueryKeywords */, 50.0/* spatialRange */,
				20 /* minutes to stats */, localIndexType /* localIndexType */, globalIndexType/* globalIndexType */
				, "resources/partitions64.ser"/* partitionsPath */,
				5/* Spout parrellisim */, 1, fineGridGran, 0);
		appendToFile(fileName, result);

	}

	public static String buildRangeQueryToplogy(String topologyName, Integer evaluatorParrellism, Integer routingParrellism, Integer spoutParallesim, Integer initialSpoutSleepDuration, Integer numberOfackers, Integer numberOfQueries,
			Integer numberOfQueryKeywords, Double spatialRange, Integer minutesToStats, LocalIndexType localIndexType, GlobalIndexType globalIndexType, String partitionsPath, Integer querySpoutParrellisim, Integer spoutBatch,
			Integer fineGridGranularity, Integer spoutSleepDuration) {
		String tweetsSource = "Tweets";
		String querySource = "querySource";

		String toRetun = "numberOfackers," + numberOfackers + ",evaluatorParrellism," + evaluatorParrellism + ",routingParrellism," + routingParrellism + ",spoutParallesim," + spoutParallesim * spoutBatch + ",numberOfQueryKeywords,"
				+ numberOfQueryKeywords + ",numberOfQueries," + numberOfQueries + ",spatialRange," + spatialRange + ",localIndexType," + localIndexType.name() + ",globalIndexType," + globalIndexType.name() + ",";
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(SpatioTextualConstants.CLUSTER_CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			System.exit(1);
		}

		SpatioTextualToplogyBuilder builder = new SpatioTextualToplogyBuilder();
		DataAndQueriesSources.addLFSTweetsSpout(tweetsSource, builder, properties, spoutParallesim, spoutSleepDuration, initialSpoutSleepDuration * 60000, spoutBatch);
		DataAndQueriesSources.addRangeQueries(tweetsSource, querySource, builder, properties, querySpoutParrellisim, spatialRange, numberOfQueries, numberOfQueryKeywords, 0, 0, FileSpout.LFS);
		HashMap<String, String> staticSourceConf = new HashMap<String, String>();
		staticSourceConf.put(POILFSDataSource.POI_FOLDER_PATH, properties.getProperty("LFS_POI_FOLDER_PATH"));

		ArrayList<Cell> partitions = PartitionsHelper.readSerializedPartitions(partitionsPath);
		if (GlobalIndexType.DYNAMIC_OPTIMIZED == globalIndexType || GlobalIndexType.DYNAMIC_AQWA == globalIndexType)
			builder.addDynamicSpatioTextualProcessor(topologyName, routingParrellism, evaluatorParrellism, partitions, globalIndexType, localIndexType, fineGridGranularity).addVolatileSpatioTextualInput(tweetsSource)
					.addContinuousQuerySource(querySource);
		else
			builder.addSpatioTextualProcessor(topologyName, routingParrellism, evaluatorParrellism, partitions, globalIndexType, localIndexType, fineGridGranularity).addVolatileSpatioTextualInput(tweetsSource)
					.addContinuousQuerySource(querySource);

		try {
			properties.load(new FileInputStream(SpatioTextualConstants.CLUSTER_CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			System.exit(1);
		}
		Config conf = new Config();
		conf.setDebug(false);
		String nimbusHost = properties.getProperty(SpatioTextualConstants.NIMBUS_HOST);
		Integer nimbusPort = Integer.parseInt(properties.getProperty(SpatioTextualConstants.NIMBUS_THRIFT_PORT).trim());
		conf.setNumAckers(numberOfackers);
		conf.put(Config.JAVA_LIBRARY_PATH, "/home/tornadojars/:/usr/local/lib:/opt/local/lib:/usr/lib");
		//conf.put(Config.TOPOLOGY_BACKPRESSURE_ENABLE, true);
		conf.put(Config.BACKPRESSURE_DISRUPTOR_HIGH_WATERMARK, .7);
		conf.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 300);
		conf.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 1048576);
		conf.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 1048576);
		//		
		conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 100000);
		conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, javaArgs);//-XX:+UseG1GC");
		conf.put(Config.STORM_ZOOKEEPER_SESSION_TIMEOUT, 250000);
		conf.put(Config.STORM_ZOOKEEPER_CONNECTION_TIMEOUT, 250000);
		conf.registerMetricsConsumer(LoggingMetricsConsumer.class, 1);
		conf.put(Config.NIMBUS_HOST, nimbusHost);
		conf.put(Config.NIMBUS_THRIFT_PORT, nimbusPort);
		conf.put(Config.STORM_ZOOKEEPER_PORT, Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_PORT)));
		ArrayList<String> zookeeperServers = new ArrayList(Arrays.asList(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_SERVERS).split(",")));
		conf.put(Config.STORM_ZOOKEEPER_SERVERS, zookeeperServers);
		conf.setNumWorkers(Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_NUMBER_OF_WORKERS).trim()));
		System.setProperty("storm.jar", properties.getProperty(SpatioTextualConstants.STORM_JAR_PATH));
		try {
			SpatioTextualToplogySubmitter.submitTopology(topologyName, conf, builder.createTopology());
			String[] nimbusInfo = new String[3];
			nimbusInfo[0] = nimbusHost;
			nimbusInfo[1] = "" + nimbusPort;
			nimbusInfo[2] = "" + topologyName;
			Thread.sleep(1000 * 60 * minutesToStats);
			//		Thread.sleep(1000 *  minutesToStats);
			toRetun += ClusterInformationExtractor.getStats(nimbusInfo);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				KillTopology.killToplogy(topologyName, nimbusHost, nimbusPort);
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return toRetun;
	}

	public static String buildRangeQueryToplogyBackPressure(String topologyName, Integer evaluatorParrellism, Integer routingParrellism, Integer spoutParallesim, Integer initialSpoutSleepDuration, Integer numberOfackers,
			Integer numberOfQueries, Integer numberOfQueryKeywords, Double spatialRange, Integer minutesToStats, LocalIndexType localIndexType, GlobalIndexType globalIndexType, String partitionsPath, Integer querySpoutParrellisim,
			Integer spoutBatch, Integer fineGridGranularity, Integer spoutSleepDuration) {
		String tweetsSource = "Tweets";
		String querySource = "querySource";

		String toRetun = "numberOfackers," + numberOfackers + ",evaluatorParrellism," + evaluatorParrellism + ",routingParrellism," + routingParrellism + ",spoutParallesim," + spoutParallesim * spoutBatch + ",numberOfQueryKeywords,"
				+ numberOfQueryKeywords + ",numberOfQueries," + numberOfQueries + ",spatialRange," + spatialRange + ",localIndexType," + localIndexType.name() + ",globalIndexType," + globalIndexType.name() + ",";
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(SpatioTextualConstants.CLUSTER_CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			System.exit(1);
		}

		SpatioTextualToplogyBuilder builder = new SpatioTextualToplogyBuilder();
		DataAndQueriesSources.addLFSTweetsSpout(tweetsSource, builder, properties, spoutParallesim, spoutSleepDuration, initialSpoutSleepDuration * 60000, spoutBatch);
		DataAndQueriesSources.addRangeQueries(tweetsSource, querySource, builder, properties, querySpoutParrellisim, spatialRange, numberOfQueries, numberOfQueryKeywords, 0, 0, FileSpout.LFS);
		HashMap<String, String> staticSourceConf = new HashMap<String, String>();
		staticSourceConf.put(POILFSDataSource.POI_FOLDER_PATH, properties.getProperty("LFS_POI_FOLDER_PATH"));

		ArrayList<Cell> partitions = PartitionsHelper.readSerializedPartitions(partitionsPath);

		if (GlobalIndexType.DYNAMIC_OPTIMIZED == globalIndexType || GlobalIndexType.DYNAMIC_AQWA == globalIndexType)
			builder.addDynamicSpatioTextualProcessor(topologyName, routingParrellism, evaluatorParrellism, null, globalIndexType, localIndexType, fineGridGranularity).addVolatileSpatioTextualInput(tweetsSource)
					.addContinuousQuerySource(querySource);
		else
			builder.addSpatioTextualProcessor(topologyName, routingParrellism, evaluatorParrellism, partitions, globalIndexType, localIndexType, fineGridGranularity).addVolatileSpatioTextualInput(tweetsSource)
					.addContinuousQuerySource(querySource);

		try {
			properties.load(new FileInputStream(SpatioTextualConstants.CLUSTER_CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			System.exit(1);
		}
		Config conf = new Config();
		conf.setDebug(false);
		String nimbusHost = properties.getProperty(SpatioTextualConstants.NIMBUS_HOST);
		Integer nimbusPort = Integer.parseInt(properties.getProperty(SpatioTextualConstants.NIMBUS_THRIFT_PORT).trim());
		conf.setNumAckers(numberOfackers);
		conf.put(Config.JAVA_LIBRARY_PATH, "/home/tornadojars/:/usr/local/lib:/opt/local/lib:/usr/lib");
		//conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ");
		conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, " -XX:+PrintGCDetails -verbose:gc -Xloggc:/home/apache-storm-1.0.0/logs/gc-storm-worker-%ID%-" + (new Date()).getTime()
				+ ".log  -XX:+PrintGCTimeStamps -XX:GCLogFileSize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/apache-storm-1.0.0/logs/heapdump  -Xmx2600m -Xms2600m -Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ");

		conf.put(Config.STORM_ZOOKEEPER_SESSION_TIMEOUT, 300000);
		conf.put(Config.STORM_ZOOKEEPER_CONNECTION_TIMEOUT, 300000);
		conf.registerMetricsConsumer(LoggingMetricsConsumer.class, 1);
		conf.put(Config.NIMBUS_HOST, nimbusHost);
		conf.put(Config.NIMBUS_THRIFT_PORT, nimbusPort);
		conf.put(Config.STORM_ZOOKEEPER_PORT, Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_PORT)));
		ArrayList<String> zookeeperServers = new ArrayList(Arrays.asList(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_SERVERS).split(",")));
		conf.put(Config.STORM_ZOOKEEPER_SERVERS, zookeeperServers);
		conf.setNumWorkers(80);//Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_NUMBER_OF_WORKERS).trim()));
		System.setProperty("storm.jar", properties.getProperty(SpatioTextualConstants.STORM_JAR_PATH));

		try {
			SpatioTextualToplogySubmitter.submitTopology(topologyName, conf, builder.createTopology());
			String[] nimbusInfo = new String[3];
			nimbusInfo[0] = nimbusHost;
			nimbusInfo[1] = "" + nimbusPort;
			nimbusInfo[2] = "" + topologyName;
			Thread.sleep(1000 * 60 * minutesToStats);
			toRetun += ClusterInformationExtractor.getStats(nimbusInfo);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				KillTopology.killToplogy(topologyName, nimbusHost, nimbusPort);
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return toRetun;

	}

	public static String getStats() {
		String toReturn = "";
		try {
			String[] nimbusInfo = new String[3];
			nimbusInfo[0] = "172.18.11.208";
			nimbusInfo[1] = "" + 6627;
			nimbusInfo[2] = "" + "Tornado";

			toReturn += ClusterInformationExtractor.getStats(nimbusInfo);

		} catch (Exception e) {
			e.printStackTrace();

		}

		return toReturn;
	}

	public static void testGlobalParitionedLocalIndexRoutingRangeQuery() {

	}

	public static void testBaseLinePlain(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {

		String result = "";

		for (int i = 1; i <= 5; i++) {
			result = runBaseLineToplogySpatialRangeManual("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
					30/* spout parrallism */, 60/* initial emitsleep duration */,
					numberOfAckers/* number of ackers */,
					5000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
					75/* minutes to stats */, 1/* query Spout parrellisim */, 1, 0);
			appendToFile(fileName, result);

		}

	}

	public static void testBaseLine(String fileName, Integer numberOfAckers, Integer numberOfRouting, Integer numberOfEvaluators) {

		String result = "";

		//	for (int i = 10; i <= 90; i++) {
		result = runBaseLineToplogySpatialRange("Tornado", numberOfEvaluators/* evaluatorParrellism */, numberOfRouting/* routingParrellism */,
				20/* spout parrallism */, 3/* initial emitsleep duration */,
				numberOfAckers/* number of ackers */,
				5000000/* number of queries */, 3/* numberOfQueryKeywords */, 5.0/* spatialRange */,
				23/* minutes to stats */, 1/* query Spout parrellisim */, 1, 0);
		appendToFile(fileName, result);

		//	}

	}

	public static String runBaseLineToplogySpatialRange(String topologyName, Integer evaluatorParrellism, Integer routingParrellism, Integer spoutParallesim, Integer initialSpoutSleepDuration, Integer numberOfackers,
			Integer numberOfQueries, Integer numberOfQueryKeywords, Double spatialRange, Integer minutesToStats, Integer querySpoutParrellisim, Integer spoutBatch, Integer spoutSleepDuration) {
		//	String toplogyName = "BaselineTornado";
		String toRetun = "Numberof ackers," + numberOfackers + ",numberOfEvaluators," + evaluatorParrellism + ",spoutParallesim," + spoutParallesim + ",initialSpoutSleepDuration," + initialSpoutSleepDuration + ",numberOfQueries,"
				+ numberOfQueries + ",spatialRange," + spatialRange + ",numberOfQueryKeywords," + numberOfQueryKeywords + ",";

		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(SpatioTextualConstants.CLUSTER_CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			ioException.printStackTrace();
		}

		SpatioTextualToplogyBuilder builder = new SpatioTextualToplogyBuilder();

		String tweetsSource = "Tweets";
		String querySource = "querySource";

		DataAndQueriesSources.addLFSTweetsSpout(tweetsSource, builder, properties, spoutParallesim, spoutSleepDuration, initialSpoutSleepDuration * 60000, spoutBatch);
		DataAndQueriesSources.addRangeQueries(tweetsSource, querySource, builder, properties, querySpoutParrellisim, spatialRange, numberOfQueries, numberOfQueryKeywords, 0, 0, FileSpout.LFS);

		builder.setBolt("BaseLineEvaluator", new BaselineEvaluator(), evaluatorParrellism + routingParrellism).shuffleGrouping(tweetsSource).allGrouping(querySource);//.allGrouping("BaseLineEvaluator", "sharedData");
		Config conf = new Config();
		conf.setDebug(false);
		String nimbusHost = properties.getProperty(SpatioTextualConstants.NIMBUS_HOST);
		Integer nimbusPort = Integer.parseInt(properties.getProperty(SpatioTextualConstants.NIMBUS_THRIFT_PORT).trim());
		conf.setNumAckers(numberOfackers);
		conf.put(Config.JAVA_LIBRARY_PATH, "/home/tornadojars/:/usr/local/lib:/opt/local/lib:/usr/lib");
		//conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ");
		conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, " -XX:+PrintGCDetails -verbose:gc -Xloggc:/home/apache-storm-1.0.0/logs/gc-storm-worker-%ID%-" + (new Date()).getTime()
				+ ".log  -XX:+PrintGCTimeStamps -XX:GCLogFileSize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/apache-storm-1.0.0/logs/heapdump ");

		conf.put(Config.STORM_ZOOKEEPER_SESSION_TIMEOUT, 300000);
		conf.put(Config.STORM_ZOOKEEPER_CONNECTION_TIMEOUT, 300000);
		conf.registerMetricsConsumer(LoggingMetricsConsumer.class, 1);
		conf.put(Config.NIMBUS_HOST, nimbusHost);
		conf.put(Config.NIMBUS_THRIFT_PORT, nimbusPort);
		conf.put(Config.STORM_ZOOKEEPER_PORT, Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_PORT)));
		ArrayList<String> zookeeperServers = new ArrayList(Arrays.asList(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_SERVERS).split(",")));
		conf.put(Config.STORM_ZOOKEEPER_SERVERS, zookeeperServers);
		conf.setNumWorkers(80);//Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_NUMBER_OF_WORKERS).trim()));
		System.setProperty("storm.jar", properties.getProperty(SpatioTextualConstants.STORM_JAR_PATH));

		try {
			SpatioTextualToplogySubmitter.submitTopology(topologyName, conf, builder.createTopology());
			String[] nimbusInfo = new String[3];
			nimbusInfo[0] = nimbusHost;
			nimbusInfo[1] = "" + nimbusPort;
			nimbusInfo[2] = "" + topologyName;
			Thread.sleep(1000 * 60 * minutesToStats);
			toRetun += ClusterInformationExtractor.getStats(nimbusInfo);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				KillTopology.killToplogy(topologyName, nimbusHost, nimbusPort);
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return toRetun;
	}

	public static String runBaseLineToplogySpatialRangeManual(String topologyName, Integer evaluatorParrellism, Integer routingParrellism, Integer spoutParallesim, Integer initialSpoutSleepDuration, Integer numberOfackers,
			Integer numberOfQueries, Integer numberOfQueryKeywords, Double spatialRange, Integer minutesToStats, Integer querySpoutParrellisim, Integer spoutBatch, Integer spoutSleepDuration) {
		//	String toplogyName = "BaselineTornado";
		String toRetun = "Numberof ackers," + numberOfackers + ",numberOfEvaluators," + evaluatorParrellism + ",spoutParallesim," + spoutParallesim + ",initialSpoutSleepDuration," + initialSpoutSleepDuration + ",numberOfQueries,"
				+ numberOfQueries + ",spatialRange," + spatialRange + ",numberOfQueryKeywords," + numberOfQueryKeywords + ",";

		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(SpatioTextualConstants.CLUSTER_CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			ioException.printStackTrace();
		}

		//TopologyBuilder builder = new TopologyBuilder();
		SpatioTextualToplogyBuilder builder = new SpatioTextualToplogyBuilder();

		String tweetsSource = "Tweets";
		String querySource = "querySource";

		//builder.setSpout(tweetsSource, new  DummyTweetGenerator(10),spoutParallesim);

		Map<String, Object> tweetsSpoutConf = new HashMap<String, Object>();
		tweetsSpoutConf.put(FileSpout.FILE_PATH, properties.getProperty("LFS_TWEETS_FILE_PATH"));
		tweetsSpoutConf.put(FileSpout.FILE_SYS_TYPE, FileSpout.LFS);
		tweetsSpoutConf.put(FileSpout.EMIT_SLEEP_DURATION_NANOSEC, new Integer(spoutSleepDuration));
		builder.setSpout(tweetsSource, new TweetsFSSpout(tweetsSpoutConf, initialSpoutSleepDuration * 60000, spoutBatch), spoutParallesim);

		Map<String, Object> queriesSpoutConf = new HashMap<String, Object>();
		queriesSpoutConf.put(FileSpout.FILE_PATH, properties.getProperty("QUERIES_FILE_PATH"));
		queriesSpoutConf.put(FileSpout.FILE_SYS_TYPE, FileSpout.LFS);
		queriesSpoutConf.put(FileSpout.CORE_FILE_PATH, properties.getProperty("CORE_FILE_PATH"));
		queriesSpoutConf.put(QueriesFileSystemSpout.SPATIAL_RANGE, spatialRange);
		queriesSpoutConf.put(QueriesFileSystemSpout.TOTAL_QUERY_COUNT, numberOfQueries);
		queriesSpoutConf.put(QueriesFileSystemSpout.KEYWORD_COUNT, numberOfQueryKeywords);
		queriesSpoutConf.put(SpatioTextualConstants.dataSrc, tweetsSource);
		queriesSpoutConf.put(SpatioTextualConstants.queryTypeField, QueryType.queryTextualRange);
		queriesSpoutConf.put(SpatioTextualConstants.textualPredicate, TextualPredicate.OVERlAPS);
		queriesSpoutConf.put(FileSpout.EMIT_SLEEP_DURATION_NANOSEC, 0);
		builder.setSpout(querySource, new QueriesFileSystemSpout(queriesSpoutConf, 0), querySpoutParrellisim);

		builder.setBolt("BaseLineEvaluator", new BaselineEvaluator(), evaluatorParrellism + routingParrellism).shuffleGrouping(tweetsSource).allGrouping(querySource);//.allGrouping("BaseLineEvaluator", "sharedData");
		Config conf = new Config();
		conf.setDebug(false);
		String nimbusHost = properties.getProperty(SpatioTextualConstants.NIMBUS_HOST);
		Integer nimbusPort = Integer.parseInt(properties.getProperty(SpatioTextualConstants.NIMBUS_THRIFT_PORT).trim());
		conf.setNumAckers(numberOfackers);
		conf.put(Config.JAVA_LIBRARY_PATH, "/home/tornadojars/:/usr/local/lib:/opt/local/lib:/usr/lib");
		//conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ");
		conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, " -XX:+PrintGCDetails -verbose:gc -Xloggc:/home/apache-storm-1.0.0/logs/gc-storm-worker-%ID%-" + (new Date()).getTime()
				+ ".log  -XX:+PrintGCTimeStamps -XX:GCLogFileSize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/apache-storm-1.0.0/logs/heapdump ");

		conf.put(Config.STORM_ZOOKEEPER_SESSION_TIMEOUT, 300000);
		conf.put(Config.STORM_ZOOKEEPER_CONNECTION_TIMEOUT, 300000);
		conf.registerMetricsConsumer(LoggingMetricsConsumer.class, 1);
		conf.put(Config.NIMBUS_HOST, nimbusHost);
		conf.put(Config.NIMBUS_THRIFT_PORT, nimbusPort);
		conf.put(Config.STORM_ZOOKEEPER_PORT, Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_PORT)));
		ArrayList<String> zookeeperServers = new ArrayList(Arrays.asList(properties.getProperty(SpatioTextualConstants.STORM_ZOOKEEPER_SERVERS).split(",")));
		conf.put(Config.STORM_ZOOKEEPER_SERVERS, zookeeperServers);
		conf.setNumWorkers(80);//Integer.parseInt(properties.getProperty(SpatioTextualConstants.STORM_NUMBER_OF_WORKERS).trim()));
		System.setProperty("storm.jar", properties.getProperty(SpatioTextualConstants.STORM_JAR_PATH));

		((Config) conf).registerSerialization(Query.class, QuerySerializer.class);
		((Config) conf).registerSerialization(DataObject.class, DataObjectSerializer.class);
		((Config) conf).registerSerialization(DataObjectList.class, DataObjectListSerializer.class);
		((Config) conf).registerSerialization(LoadBalanceMessage.class, LoadBalanceMessageSerializer.class);
		((Config) conf).registerSerialization(Control.class, ControlSerializer.class);
		((Config) conf).registerSerialization(Point.class, PointSerializer.class);
		((Config) conf).registerSerialization(Rectangle.class, RectangleSerializer.class);
		((Config) conf).registerSerialization(CombinedTuple.class, CombinedTupleSerializer.class);
		((Config) conf).registerSerialization(ResultSetChange.class, ResultSetChangeSerializer.class);
		((Config) conf).registerSerialization(Partition.class, PartitionSerializer.class);
		((Config) conf).registerSerialization(Cell.class, CellSerializer.class);
		((Config) conf).registerSerialization(IndexCell.class, IndexCellSerializer.class);
		((Config) conf).registerSerialization(IndexCellCoordinates.class, IndexCellCoordinatesSerializer.class);

		try {
			SpatioTextualToplogySubmitter.submitTopology(topologyName, conf, builder.createTopology());
			String[] nimbusInfo = new String[3];
			nimbusInfo[0] = nimbusHost;
			nimbusInfo[1] = "" + nimbusPort;
			nimbusInfo[2] = "" + topologyName;
			Thread.sleep(1000 * 60 * minutesToStats);
			toRetun += ClusterInformationExtractor.getStats(nimbusInfo);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				KillTopology.killToplogy(topologyName, nimbusHost, nimbusPort);
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return toRetun;
	}

	static void appendToFile(String fileName, String data) {
		BufferedWriter bw = null;

		try {
			// APPEND MODE SET HERE
			bw = new BufferedWriter(new FileWriter(fileName, true));
			bw.write(data);
			bw.newLine();
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { // always close the file
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
					ioe2.printStackTrace();
				}
		}
	}
}
