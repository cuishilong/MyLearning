

```shell script
hive
# 从控制台接收一个sql语句
-e "sql"

# 从一个sql文件接收一个sql
-f

# 接收一个自定义变量
-d

# 显示详情
-v
```

```shell script
hive -v -d dt='20200518' -f demo.sql
```

```hiveql
-- dt变量引用上面的自定义变量
select * from table where dt = ${dt}
```


