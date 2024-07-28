# TMS (Teamwork Management System)

[![Version](https://img.shields.io/badge/version-2.28.0-blue.svg)](https://gitee.com/sageer_return/tms/releases)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://gitee.com/sageer_return/tms/actions)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

<p align="center"><a href="#主要特性">主要特性</a> • <a href="#快速开始">快速开始</a> • <a href="#安装部署">安装部署</a> • <a href="#开发手册">开发手册</a> • <a href="#许可证">许可证</a></p>

TMS是一个响应式的开源团队协作系统，基于频道模式进行团队沟通和轻量级任务管理。它支持Markdown、富文本、在线表格和思维导图的团队博文wiki，以及i18n国际化翻译管理。

![TMS Screenshot](https://images.gitee.com/uploads/images/2020/0524/095513_cf21d89f_19723.png)

演示地址: 🏗️ 施工中 🏗️

## 主要特性

- 📢 团队协作沟通（类似Slack和Bearychat）
- 📝 团队博文wiki（类似精简版Confluence和蚂蚁笔记）
- 🌐 国际化（i18n）翻译管理
- 📱 响应式设计，支持移动端

## 快速开始

📖 [用户手册](https://gitee.com/sageer_return/tms/wikis/pages?sort_id=11385516&doc_id=5756581)

❓ [常见问题](https://gitee.com/sageer_return/tms/wikis/pages?sort_id=11385516&doc_id=5756581)

### 代码仓库

- 🦊 Gitee：[https://gitee.com/sageer_return/tms](https://gitee.com/sageer_return/tms)
- 🐱 GitHub：🏗️ 施工中 🏗️

## 安装部署

### 1.从源码编译部署

#### 环境要求

- JDK 1.8
- MySQL 5.6
- Maven 3.3+

> ⚠️ 注意
高版本的JDK和MySQL可能会导致编译失败或运行异常。如果你想用更高版本的MySQL,请看[这里](https://gitee.com/sageer_return/tms/wikis/pages?sort_id=11385549&doc_id=5756581)。

##### a. 克隆代码仓库

``` bash
git clone https://gitee.com/sageer_return/tms.git
cd tms
```

##### b. 创建数据库

``` sql
CREATE DATABASE ${db_name} DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
```

##### c. 修改配置文件

修改`application.properties`文件的 `spring.profiles.active` 属性为 `prod`，并编辑`application-prod.properties`文件中的数据库配置:

``` properties
spring.datasource.url=jdbc:mysql://${ip}:${port}/${db_name}?useUnicode=true&characterEncoding=UTF-8&useSSL=false
spring.datasource.username=${db_username}
spring.datasource.password=${db_password}
```

##### d. 编译打包

``` bash
mvn clean package -Dmaven.test.skip=true
```

### 2.使用 Docker-compose 部署

#### a. 克隆代码仓库

``` bash
git clone https://gitee.com/sageer_return/tms.git
cd tms
```

#### b. 修改配置文件

修改`application.properties`文件的 `spring.profiles.active` 属性为 `prod`，并编辑`application-prod.properties`文件中的数据库配置:

``` properties
spring.datasource.url=jdbc:mysql://db:3306/tms?useUnicode=true&characterEncoding=UTF-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=pingan
```

> ⚠️ 注意
这里数据库端口和密码应均保留默认值，如果需要修改，请同时修改项目根目录下`docker-compose.yml`文件中的`MYSQL_ROOT_PASSWORD`和 `db`服务的`ports`。

#### c. 构建镜像

建议使用JDK 1.8版本构建镜像，否则可能会导致编译失败。

``` bash
cd tms
mvn clean package -Dmaven.test.skip=true
```

#### d. 使用 Docker-compose 启动

``` bash
docker-compose up -d
```

服务将会启动在`http://localhost:8090`。

## 功能概述

### 团队协作沟通

- 🔄 基于WebSocket的实时通讯
- 🗨️ 频道（组团沟通）和私聊（一对一）
- ✍️ Markdown语法支持
- 🔔 @消息、收藏消息、富文本消息目录
- 📊 频道任务看板（可拖拽）
- 📅 日程安排和待办事项
- 📁 文件上传和分享
- 📬 邮件通知、桌面通知、Toastr通知
- 🎨 自定义皮肤色调

### 团队博文 (Wiki)

- 📝 多种创作方式：Markdown、HTML富文本、电子表格、思维导图、图表工具
- 🔄 博文版本控制和协作编辑
- 📤 导出为PDF、Markdown、HTML、Excel、PNG
- 💬 博文评论和实时更新通知

### 国际化（i18n）翻译管理

- 🌍 翻译项目管理
- 🗣️ 翻译语言管理
- 📥📤 翻译导入导出

## 开发手册

🧑‍💻 [开发指导](https://gitee.com/sageer_return/tms/wikis/pages?sort_id=11385558&doc_id=5756581)
⚙️ [配置手册](https://gitee.com/sageer_return/tms/wikis/pages?sort_id=11385569&doc_id=5756581)
🎁 [参与贡献](https://gitee.com/sageer_return/tms/wikis/pages?sort_id=11385542&doc_id=5756581)

## 许可证

TMS采用MIT许可证。请查看[LICENSE](LICENSE)文件了解更多信息。

## 免责声明

TMS项目使用了许多优秀的第三方开源库。如果您计划将TMS用于商业用途，请确保您已获得所有依赖库的适当许可。TMS不对可能发生的任何版权纠纷或侵权问题承担法律责任。