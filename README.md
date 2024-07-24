# TMS (Teamwork Management System)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub stars](https://img.shields.io/github/stars/xiweicheng/tms.svg)](https://github.com/xiweicheng/tms/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/xiweicheng/tms.svg)](https://github.com/xiweicheng/tms/network)
[![GitHub issues](https://img.shields.io/github/issues/xiweicheng/tms.svg)](https://github.com/xiweicheng/tms/issues)

TMS是一个响应式的开源团队协作系统，基于频道模式进行团队沟通和轻量级任务管理。它支持Markdown、富文本、在线表格和思维导图的团队博文wiki，以及i18n国际化翻译管理。

![TMS Screenshot](https://images.gitee.com/uploads/images/2020/0524/095513_cf21d89f_19723.png)

## 目录

- [主要特性](#主要特性)
- [快速开始](#快速开始)
- [安装与部署](#安装与部署)
- [功能概述](#功能概述)
- [贡献](#贡献)
- [赞助](#赞助)
- [许可证](#许可证)
- [免责声明](#免责声明)

## 主要特性

- 📢 团队协作沟通（类似Slack和Bearychat）
- 📝 团队博文wiki（类似精简版Confluence和蚂蚁笔记）
- 🌐 国际化（i18n）翻译管理
- 📱 响应式设计，支持移动端

## 快速开始

### 文档

📖 [用户手册](https://gitee.com/xiweicheng/tms/wikis/%E7%9D%80%E9%99%86%E9%A1%B5?sort_id=3692705)

### 代码仓库

- 🦊 Gitee：[https://gitee.com/xiweicheng/tms](https://gitee.com/xiweicheng/tms)
- 🐱 GitHub：[https://github.com/xiweicheng/tms](https://github.com/xiweicheng/tms)

## 安装与部署

选择适合您的部署方式：

1. [在开发工具中运行](https://gitee.com/xiweicheng/tms/wikis/%E5%A6%82%E4%BD%95%E5%9C%A8%E5%BC%80%E5%8F%91%E5%B7%A5%E5%85%B7%E4%B8%AD%E8%BF%90%E8%A1%8C?sort_id=351959)
2. [传统方式部署](https://gitee.com/xiweicheng/tms/wikis/TMS%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%EF%BC%88%E4%BC%A0%E7%BB%9F%E6%96%B9%E5%BC%8F%EF%BC%89?sort_id=21982)
3. [使用Docker Compose部署](https://gitee.com/xiweicheng/tms/wikis/TMS%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%EF%BC%88docker-compose%EF%BC%89?sort_id=21977)
4. [在Kubernetes上部署](https://gitee.com/xiweicheng/tms/wikis/TMS%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2%EF%BC%88k8s%E6%96%B9%E5%BC%8F%EF%BC%89?sort_id=3201498)

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

## 贡献

我们欢迎任何形式的贡献！如果您发现了bug或有新功能建议，请[提交issue](https://github.com/xiweicheng/tms/issues/new)或[创建pull request](https://github.com/xiweicheng/tms/compare)。

## 赞助

如果您觉得TMS对您有帮助，可以考虑赞助我们：

<details>
<summary>点击查看赞助二维码</summary>

![赞助二维码](path_to_qr_code_image)

</details>

## 许可证

TMS采用MIT许可证。请查看[LICENSE](LICENSE)文件了解更多信息。

## 免责声明

TMS项目使用了许多优秀的第三方开源库。如果您计划将TMS用于商业用途，请确保您已获得所有依赖库的适当许可。TMS不对可能发生的任何版权纠纷或侵权问题承担法律责任。