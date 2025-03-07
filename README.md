# movierecommend
基于Spring Boot的大数据电影推荐系统，采用协同过滤算法实现个性化推荐

### 如何在本地开发
```
# 环境依赖
1. java环境
2. gradle项目，建议通过Intellij IDEA打开，运行build.gradle下载依赖，具体参考gradle教程
3. IDEA下载开启 lombok插件
4. 如果需要正常运行，需要使用mysql数据库和redis，具体配置可根据自己的项目配置在application.yml中
5. 发送短信和照片上传需要一些token和access_key，可以参考代码`configService.getConfigValue`获取配置和阿里云短信
```


### 架构
- 项目组织： 前端后端分离，通过Restful接口传递数据
- 代码组织：基于SpringBoot，采用maven进行依赖管理
- 推荐算法： 采用Mahout基于用户的协同过滤算法和基于内容的协同过滤算法  

![项目结构图](http://ydschool-online.nos.netease.com/1582746970143Snipaste_2020-02-26_22-19-39.png)

### 技术栈
* spring boot
* docker
* mysql
* es
* redis
* maven

### 其他说明及文档
项目持续更新中，目前基本项目已经搭建好了



