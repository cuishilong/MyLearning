## Hbase

### 问题

1、多条件查询问题，可以用二级索引解决

HBase本身只提供基于行键和全表扫描的查询，而行键索引单一，对于多维度的查询困难(如：对于价格+天数+酒店+交通的多条件组合查询困难)，全表扫描效率低下。

二级索引的本质就是建立各列值与行键之间的映射关系。