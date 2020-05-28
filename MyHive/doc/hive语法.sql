-- 创建库

create database if not exists tmp;

show databases like 'tmp*';

desc database tmp;
-- hive建表模板

-- drop table if exists tmp.tmp_tmp ;
create external table tmp.tmp_tmp
(
    id   int comment "id",
    name string comment "name",
    note string comment "注释"
) comment "构建分区表"
    partitioned by (dt string,status string)
    clustered by (id) into 5 buckets
    row format delimited fields terminated by '|' stored as textfile
    -- row format delimited fields terminated by '|' stored as parquet
    -- row format delimited fields terminated by '|' stored as sequencefile
    location '/user/hadoop/warehouse/log_test';



