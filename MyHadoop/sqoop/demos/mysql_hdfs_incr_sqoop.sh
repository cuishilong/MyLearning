#!/usr/bin/env bash

sqoop import \
--connect jdbc:mysql://mysql57.rdsmwwoj62k1nq0.rds.bj.baidubce.com:3306/sqooptest \
--username sqoop \
--password sqoop@123 \
--table sqoop \
--fields-terminated-by '|' \
--incremental append \
--check-column id \
--last-value 6 \
--target-dir hdfs://bmr-cluster/sqoop-test/20200501
