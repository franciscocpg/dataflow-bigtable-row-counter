# Cloud Dataflow BigTable row counter

A Java script to count rows in a bigtable table. Especially useful for large
tables. For smaller tables
[cbt](https://cloud.google.com/bigtable/docs/cbt-reference#count_rows_in_a_table)
may be more worthwhile.

This script was extracted from
[dataflow-connector-examples](https://github.com/GoogleCloudPlatform/cloud-bigtable-examples/tree/7673bd4e740b26d5d47114582032e521ae04629f/java/dataflow-connector-examples).

## Project setup

### Provision your project for Cloud Dataflow

* Follow the Cloud Dataflow [getting started](https://cloud.google.com/dataflow/getting-started) instructions. (if required) Including:
  * Create a project
  * Enable Billing
  * Enable APIs
  * Create a Google Cloud Storage Bucket
  * Development Environment Setup
      * Install Google Cloud SDK
      * Install Java
      * Install Maven

### Provision a Bigtable Instance

* Create a [Cloud Bigtable cluster](https://cloud.google.com/bigtable/docs/creating-cluster) using the [Developer Console](https://cloud.google.com/console) by clicking on the **Storage** > **Cloud Bigtable** > **New Instance** button.  After that, enter the **Instance name**, **ID**, **zone**, and **number of nodes**. Once you have entered those values, click the **Create** button.

### Create a Google Cloud Storage Bucket

* Using the [Developer Console](https://cloud.google.com/console) click on **Storage** > **Cloud Storage** > **Browser** then click on the **Create Bucket** button.  You will need a globally unique name for your bucket, such as your projectID.

## Running

SourceRowCount performs a simple row count using the Cloud Bigtable Source and writes the count to a file in Google Storage.

    mvn package exec:exec \
        -DSourceRowCount \
        -Dbigtable.projectID=<projectID> \
        -Dbigtable.instanceID=<instanceID> \
        -Dbigtable.table=<tableID> \
        -Dgs=<your_bucket>
        
You can watch the job progress (including cancelling it) in the
[dataflow console](https://console.cloud.google.com/dataflow/jobs).

You can verify the results by first typing:

    gsutil ls 'gs://<your_bucket>/*rowcount*'

There should be a file that looks like
`<tableID>-rowcount-<job>-XXXXXX-of-YYYYYY`.
To get row count:

    gsutil cp gs://<your_bucket>/<tableID>-rowcount-<job>-XXXXXX-of-YYYYYY .
    cat <tableID>-rowcount-<job>-XXXXXX-of-YYYYYY
