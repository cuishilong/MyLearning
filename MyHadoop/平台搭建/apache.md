# apache大数据平台搭建

1、使用3台虚拟机即可

2、主节点：1/4内核，,6内存，50硬盘

从节点：1/2内核，4内存，硬盘



## 零、Linux相关配置 1611

#### 1、Ip相关

注意：所有服务器上配置

```shell
# 注意：可以在root用户下进行以下操作
# ifcfg-ens33
# /etc/sysconfig/network-scripts/ifcfg-ens33
BOOTPROTO=static
IPADDR=192.168.137.201
PREFIX=24
GATEWAY=192.168.137.2
DNS=192.168.137.2
ONBOOT=yes

# /etc/resolv.conf
nameserver 192.168.137.2

# /etc/hostname
s201
```

#### 2、安装Jdk环境

注意：所有服务器

```shell
# java cuishilong 20181213
export JAVA_HOME=/apps/jdk
export PATH=$PATH:$JAVA_HOME/bin
```

#### 3、配置ssh

注意：配置从主服务器到其他所有服务器的免密登录

```shell
# 生成秘钥文件
ssh-keygen

# 修改.ssh目录权限
chmod 700 ~/.ssh

# 分发公钥，分发到集群所有服务器
ssh-copy-id centos@localhost
```

#### 4、安装Ntp服务

注意：所有服务器

```shell
# 安装ntp服务
> yum -y install ntp
# 启动ntp服务
> systemctl start ntpd
# 校准时间
> ntpdate pool.ntp.org
# 查看校准结果
> date
# 注意：校准后的时间，所有服务器应该是一致的

# 配置开机启动
systemctl enable ntpd
systemctl status ntpd
```

#### 5、关闭防火墙

注意：所有服务器

```shell
# 关闭防火墙
> systemctl stop firewalld
# 禁止开机启动
> systemctl disable firewalld
# 查看防火墙状态
> systemctl status firewalld
```

#### 6、安装常用包

```shell
# 基础包
yum install -y lrzsz openssh-server rsync gcc vim net-tools wget
```

#### 7、/etc/hosts文件修改

```shell
172.16.215.135 hdp135
172.16.215.136 hdp136
172.16.215.137 hdp137
172.16.215.138 hdp138
172.16.215.139 hdp139
```





## 一、安装Hadoop 2.7.3

### 1、hadoop相关配置

#### core-site.xml配置

```xml
<configuration>

    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://s201</value>
    </property>
    <property>
        <name>hadoop.proxyuser.centos.hosts</name>
        <value>*</value>
    </property>
    <property>
        <name>hadoop.proxyuser.centos.groups</name>
        <value>*</value>
    </property>

</configuration>
```

#### hdfs-site.xml配置

```xml
<configuration>

    <property>
        <name>dfs.replication</name>
        <value>2</value>
    </property>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>/home/admin/hadoop/full/hdfs/name</value>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>/home/admin/hadoop/full/hdfs/data</value>
    </property>
    <property>
        <name>dfs.namenode.checkpoint.dir</name>
        <value>/home/admin/hadoop/full/hdfs/namesecondary</value>
    </property>
    <property>
        <name>dfs.permissions</name>
        <value>false</value>
    </property>

</configuration>

```

#### mapred-site.xml配置

```xml
<configuration>

    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>

    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>s201:10020</value>
    </property>

</configuration>
```

#### yarn-site.xml配置

```xml
<configuration>

    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>s201</value>
    </property>
    <property>
        <name>yarn.nodemanager.local-dirs</name>
        <value>/home/centos/hadoop/full/nm-local-dir</value>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>

    <property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>4096</value>
    </property>
    <property>
        <name>yarn.nodemanager.resource.cpu-vcores</name>
        <value>4</value>
    </property>
    <property>
        <name>yarn.nodemanager.pmem-check-enabled</name>
        <value>false</value>
    </property>
    <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
    </property>

</configuration>
```

#### slaves配置

```
s202
s203
```

#### hadoop-env.sh配置

```shell
export JAVA_HOME=/apps/jdk
```

#### capacity-scheduler.xml配置

```xml
<property>
    <name>yarn.scheduler.capacity.resource-calculator</name>
    <value>org.apache.hadoop.yarn.util.resource.DominantResourceCalculator</value>
</property>
```



### 2、配置环境变量

profile配置

```shell
# hadoop cuishilong 20190122
export HADOOP_HOME=/apps/hadoop-2.7.3
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```



### 3、启动hadoop

#### 1、初始化文件系统

```shell
hdfs namenode -format
```

#### 2、启动hadoop

```shell
start-all.sh
```



## 二、安装Mysql 5.7.25

### 1、配置yum仓库

```shell
// 下载mysql的源文件
#wget http://dev.mysql.com/get/mysql-community-release-el7-5.noarch.rpm
wget https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm
// 把mysql源添加到本地
yum localinstall mysql57-community-release-el7-11.noarch.rpm -y
// 查看是否添加成功
# repolist enabled | grep "mysql.*-community.*"
```

### 2、安装Mysql

```shell
// 安装musql服务
yum install -y mysql-community-server
// 启动服务
service mysqld start
// 查看服务状态
service mysqld status
```

### 3、Mysql相关配置

```mysql
// 获取超级用户root的密码
grep 'temporary password' /var/log/mysqld.log

// 登录mysql
mysql -uroot -p

// 修改密码限制
set global validate_password_policy=0; 
set global validate_password_mixed_case_count=0; 
set global validate_password_number_count=0;  
set global validate_password_special_char_count=0;  
set global validate_password_length=3;
SHOW VARIABLES LIKE 'validate_password%';

// 修改root密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';

// 修改其他服务器可登录
use mysql;
update user set host ='%' where user ='root';

CREATE DATABASE `hive_meta` CHARACTER SET utf8 COLLATE utf8_general_ci;
```

### 4、其他信息

```mysql
// 一些基础操作
CREATE USER 'root'@'%' IDENTIFIED BY 'root';
update user set host = '%' where user = 'root';

grant all privileges on *.* to 'root'@'%' identified by 'root';
grant all privileges on *.* to 'root'@'%' identified by 'root' WITH GRANT OPTION;

flush privileges;

/etc/my.cnf mysql的主配置文件 
/var/lib/mysql mysql数据库文件存放位置 
/var/log mysql日志输出存放目录
```



## 三、安装Hive 2.1.0

### 1、hive相关配置

```shell
cp hive-default.xml hive-site.xml
cp hive-env.sh.template hive-env.sh
```

#### hive-site.xml

```xml
<property>
    <name>javax.jdo.option.ConnectionURL</name>
    <value>jdbc:mysql://hdp137:3306/hive_meta?createDatabaseIfNotExist=true&amp;useUnicode=true&amp;characterEncoding=UTF-8</value>
</property>
<property>
    <name>javax.jdo.option.ConnectionDriverName</name>
    <value>com.mysql.jdbc.Driver</value>
</property>
<property>
    <name>javax.jdo.option.ConnectionUserName</name>
    <value>root</value>
</property>
<property>
    <name>javax.jdo.option.ConnectionPassword</name>
    <value>root</value>
</property>
<property>
    <name>hive.metastore.warehouse.dir</name>
    <value>/apps/hive/warehouse</value>
</property>

<!-- 修改以下变量为常量 -->
${system:java.io.tmpdir}=/home/admin/app/hive/tmp
${system:user.name}=admin
```

#### hive-env.sh

```shell
export JAVA_HOME=/apps/jdk
export HIVE_HOME=/apps/hive
export HADOOP_HOME=/apps/hadoop
```

#### mysql驱动

把mysql的驱动jar包放到hive的lib下

#### hive元数据库配置

```mysql
create database if not exists hive001 charset=utf8;
```



### 2、配置环境变量

```shell
#hive cuishilong 20190124
export HIVE_HOME=/apps/hive
export PATH=$PATH:$HIVE_HOME/bin
```



### 3、初始化hive

```shell
schematool -initSchema -dbType mysql

-- 处理中文注释问题
alter table COLUMNS_V2 modify column COMMENT varchar(256) character set utf8;
alter table TABLE_PARAMS modify column PARAM_VALUE varchar(4000) character set utf8;
alter table PARTITION_PARAMS modify column PARAM_VALUE varchar(4000) character set utf8;
alter table PARTITION_KEYS modify column PKEY_COMMENT varchar(4000) character set utf8;
alter table INDEX_PARAMS modify column PARAM_VALUE varchar(4000) character set utf8;
```



## 四、安装Spark 2.1.0

### 1、tar开

```shell
> tar -xzvf spark-2.1.0-bin-hadoop2.7.tgz -C /soft/

> ln -s spark-2.1.0-bin-hadoop2.7 spark
```

### 2、配置

- /soft/spark/conf/（只需配置本服务器上的spark，不需要配置其他服务器）

```shell
> cp spark-env.sh.template spark-env.sh
export JAVA_HOME=/apps/jdk
export HADOOP_CONF_DIR=/apps/hadoop/etc/hadoop

> cp spark-defaults.conf.template spark-defaults.conf
spark.yarn.jars hdfs://s201/apps/spark/lib/*

> hdfs dfs -put /soft/spark/jars/* /apps/spark/lib/
> 
```

- 配置yarn-site.xml，并分发至每台机器

```xml
<property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>4096</value>
</property>
<property>
        <name>yarn.nodemanager.resource.cpu-vcores</name>
        <value>4</value>
</property>
<property>
        <name>yarn.nodemanager.pmem-check-enabled</name>
        <value>false</value>
</property>
<property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
</property>
```

- 配置spark读取hive表

```shell
> ln -s /apps/hive/conf/hive-site.xml /apps/spark/conf/hive-site.xml
# 如果出现兼容问题，则修改hive-site.xml
<property>
	<name>hive.metastore.schema.verification</name>
	<value>false</value>
</property>

> cp /soft/hive/lib/mysql-connector-java-5.1.15.jar /soft/spark/jars/
```

- 配置hadoop文件capacity-scheduler.xml（处理spark使用内核问题）

```xml
<property>
  <name>yarn.scheduler.capacity.resource-calculator</name>
  <!--<value>org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator</value>-->
  <value>org.apache.hadoop.yarn.util.resource.DominantResourceCalculator</value>
  <description>
    The ResourceCalculator implementation to be used to compare 
    Resources in the scheduler.
    The default i.e. DefaultResourceCalculator only uses Memory while
    DominantResourceCalculator uses dominant-resource to compare 
    multi-dimensional resources such as Memory, CPU etc.
  </description>
</property>
```



### 3、启动spark

```shell
> nohup hive --service metastore >> hivemeta.log &
> nohup hiveserver2 >> hivethrift.log &
> ./bin/spark-shell --master yarn
> ./bin/spark-sql --master yarn
```





## 五、安装ZK 3.4.8

### 1、zk相关配置

#### zoo.cfg

```shell
# zoo.cfg
dataDir=/home/centos/zk/data
dataLogDir=/home/centos/zk/logs
server.1=192.168.137.201:2888:3888
server.2=192.168.137.202:2888:3888
server.3=192.168.137.203:2888:3888
```

#### myid

```shell
# 创建工作目录
mkdir -p /home/admin/zk/data
mkdir -p /home/admin/zk/logs

# 在data目录下，创建myid文件，每台服务器都做，分别写入
1 2 3
```



### 2、配置环境变量

```shell
# zk cuishilong 20190124
export ZK_HOME=/apps/zk
export PATH=$PATH:$ZK_HOME/bin
```



### 3、启动zk

```shell
# 启动zk
xzk start

# 查看状态
xzk status
```



## 六、安装Hbase 1.3.1

### 1、hbase相关配置

#### hbase-site.xml

```xml
<property>
    <name>hbase.cluster.distributed</name>
    <value>true</value>
</property>
<property>
    <name>hbase.rootdir</name>
    <value>hdfs://s201/hbase</value>
</property>
<property>
    <name>dfs.replication</name>
    <value>2</value>
</property>
<property>
    <name>hbase.zookeeper.quorum</name>
    <value>s201:2181,s202:2181,s203:2181</value>
</property>
```

#### hbase-env.sh

```shell
export JAVA_HOME=/apps/jdk
export HBASE_MANAGES_ZK=false
export HBASE_CLASSPATH=/apps/hadoop/etc/hadoop
```

#### regionservers

```shell
s202
s203
```



### 2、配置环境变量

```shell
# hbase cuishilong 20190124
export HBASE_HOME=/apps/hbase
export PATH=$PATH:$HBASE_HOME/bin
```



### 3、启动hhbase

```shell
start-hbase.sh
```





## 七、安装Kafka 1.1.0

#### 1、kafka相关配置

```shell
broker.id=101
log.dirs=/apps/kafka/logs
zookeeper.connect=s201:2181,s202:2181,s203:2181
```

#### 2、配置环境变量

```shell
# kafka cuishilong 20190124
export KAFKA_HOME=/apps/kafka
export PATH=$PATH:$KAFKA_HOME/bin
```

#### 3、启动kafka

```shell
kafka-server-start.sh -daemon /apps/kafka/config/server.properties
kafka-server-stop.sh
```



## 八、安装Flume 1.7.0

#### 1、配置环境变量

```shell
# flume cuishilong 20190124
export FLUME_HOME=/apps/kafka
export PATH=$PATH:$FLUME_HOME/bin
```





## 十、涉及脚本

#### 1、xcall.sh

```shell
#!/bin/bash
params=$@
hosts=`cat /apps/hadoop/etc/full/hosts`
for  h in ${hosts} ; do
    tput setaf 2
    echo ============= $h =============
    tput setaf 7
    #echo $h
    ssh -4 $h "source /etc/profile;  $1"
done
```



#### 2、xsync.sh

```shell
#!/bin/bash
param=$1
dir=`dirname $param`
fullpath=`pwd -P`
user=`whoami`
filename=`basename $param`
cd $dir
for host in `cat /apps/hadoop/etc/full/slaves` ; do
echo ==================== $host ===========================
rsync -lr $filename $user@$host:$fullpath ;
done
```



#### 3、xzk.sh

```shell
#! /bin/bash
ssh s201 "source /etc/profile ; zkServer.sh ${1}"
ssh s202 "source /etc/profile ; zkServer.sh ${1}"
ssh s203 "source /etc/profile ; zkServer.sh ${1}"
```



#### 4、xkkstart.sh

```shell
#! /bin/bash
ssh s201 "source /etc/profile ; kafka-server-start.sh -daemon /apps/kafka/config/server.properties"
ssh s202 "source /etc/profile ; kafka-server-start.sh -daemon /apps/kafka/config/server.properties"
ssh s203 "source /etc/profile ; kafka-server-start.sh -daemon /apps/kafka/config/server.properties"
```



#### 5、xkkstop.sh

```shell
#! /bin/bash
ssh s201 "source /etc/profile ; kafka-server-stop.sh"
ssh s202 "source /etc/profile ; kafka-server-stop.sh"
ssh s203 "source /etc/profile ; kafka-server-stop.sh"
```



#### 6、getSize.sh

```shell
#!/bin/bash
hdfs dfs -ls /apps/hive/warehouse/$1.db/$2/par_day=$3|awk -F ' ' '{print $5}'|awk '{a+=$1}END {print a/(1024*1024)}'
```


