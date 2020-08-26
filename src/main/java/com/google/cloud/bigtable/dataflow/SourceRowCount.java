package com.google.cloud.bigtable.dataflow;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableScanConfiguration;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;


public class SourceRowCount {

  // Converts a Long to a String so that it can be written to a file.
  static DoFn<Long, String> stringifier = new DoFn<Long, String>() {
    private static final long serialVersionUID = 1L;

    @ProcessElement
    public void processElement(DoFn<Long, String>.ProcessContext context) throws Exception {
      context.output(context.element().toString());
    }
  };

  public static void main(String[] args) {
    CloudBigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(CloudBigtableOptions.class);
    String PROJECT_ID = options.getBigtableProjectId();
    String INSTANCE_ID = options.getBigtableInstanceId();
    String TABLE_ID = options.getBigtableTableId();

    // Workaround to work with java 1.9 or higher
    options.setFilesToStage(
        Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator)).stream()
            .map(entry -> new File(entry).toString()).collect(Collectors.toList()));

    // More meaningful job name
    String normalizedTableId = TABLE_ID.replace(".", "-");
    String jobName = options.getJobName().replace("sourcerow", normalizedTableId + "-row");
    options.setJobName(jobName);

    // [START bigtable_dataflow_connector_scan_config]
    Scan scan = new Scan();
    scan.setCacheBlocks(false);
    scan.setFilter(new FirstKeyOnlyFilter());

    // CloudBigtableTableConfiguration contains the project, zone, cluster and table
    // to connect to.
    // You can supply an optional Scan() to filter the rows that will be read.
    CloudBigtableScanConfiguration config =
        new CloudBigtableScanConfiguration.Builder().withProjectId(PROJECT_ID)
            .withInstanceId(INSTANCE_ID).withTableId(TABLE_ID).withScan(scan).build();

    System.out.println("\n=========================\nCONFIG\n=========================\n");

    System.out.println("Project id: " + config.getProjectId());
    System.out.println("Instance id: " + config.getInstanceId());
    System.out.println("Table id: " + config.getTableId());
    System.out.println("Job name: " + options.getJobName());

    String resultLocation = options.getStagingLocation().replace("staging", jobName);
    System.out.println("Result location: " + resultLocation);

    System.out.println("\n=========================\n");

    Pipeline p = Pipeline.create(options);

    p.apply(Read.from(CloudBigtableIO.read(config))).apply(Count.<Result>globally())
        .apply(ParDo.of(stringifier)).apply(TextIO.write().to(resultLocation));
    // [END bigtable_dataflow_connector_scan_config]

    p.run().waitUntilFinish();
  }
}
