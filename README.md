# my-weibo-crawler

基于 Java 21 的微博图片/视频扫描与下载工具。项目保留旧版扫描历史、下载历史和日志格式，同时把扫描、解析、历史日志、下载等代码拆到不同 package 里，便于维护。

## 环境要求

- JDK 21
- Maven 3.9+
- 可访问微博接口的网络环境
- `src/main/resources/weibo_json_url_headers.txt` 中需要维护有效的微博请求头和 Cookie

## 日志兼容

项目继续兼容旧文件：

- `pics_videos.log`：按 `yyyy-MM-dd-weiboId` 作为 key 的媒体日志。
- `done.list`：已下载 URL 记录，livephoto 的 `.mov` 会继续按旧逻辑截断 query 后判断。
- `failed.list`：下载失败 URL。
- `weibo.log` / `list.log`：保留文件名常量，方便继续兼容旧历史。

## 包结构

- `com.my.crawler.weibo.app`：入口类。
- `com.my.crawler.weibo.config`：配置、账号分组和搜索关键词。
- `com.my.crawler.weibo.client`：微博接口请求。
- `com.my.crawler.weibo.parser`：微博 JSON 到 `pics_videos.log` 的解析。
- `com.my.crawler.weibo.history`：历史日志读写和合并。
- `com.my.crawler.weibo.download`：媒体下载和 `done.list` / `failed.list` 维护。
- `com.my.crawler.weibo.util`：文件名、日期、JSON 工具。

## 旧式扫描入口

扫描入口是：

```text
com.my.crawler.weibo.app.ScanWeiboMain
```

这个类保留旧 `根据UID扫描所有weibo并解析pic和video.java` 的使用方式：直接编辑 `main` 方法，注释或取消注释历史任务，然后运行类。

默认日志根目录：

```text
F:\weibo_log
```

单账号扫描方法仍保留：

```java
start("2017376052", new File(folderBasePath, "郭四火-"));
```

`开始扫描()` 里的旧手工扫描历史也已保留。

## 旧式下载入口

下载入口是：

```text
com.my.crawler.weibo.app.DownloadWeiboMain
```

这个类保留旧 `根据picvideolog开始下载.java` 的使用方式：直接编辑 `main` 方法，注释或取消注释历史任务，然后运行类。

已保留：

- `下载MOVs()`
- `下载部分生图()`
- `下载部分明星图片()`
- `下载部分日本写真()`
- `下载()`
- `下载明星1()` / `下载明星2()` / `下载明星3()`

旧下载目录仍按历史代码使用，例如：

```text
F:\weibo_log
H:\weibo
H:\weibo-mov
H:\weibo-日本写真
```

## 命令行聚合入口

也可以使用聚合入口快速扫描单个账号：

```text
com.my.crawler.weibo.app.WeiboCrawlerApp
```

示例：

```powershell
mvn -q exec:java "-Dexec.args=scan --uid 2017376052 --name 郭四火- --log-dir D:/temp/weibo_log --output-dir D:/temp/weibo_log_output --max-pages 2 --download"
```

常用参数：

- `--uid`：微博 UID。
- `--name`：账号目录名。
- `--log-dir`：日志根目录。
- `--output-dir`：下载输出根目录。
- `--begin-page`：起始页，默认 `0`。
- `--max-pages`：最多扫描页数，`0` 表示不限制。
- `--delay-ms`：分页请求间隔。
- `--download`：扫描后继续下载。
- `--no-video`：只下载图片。

## 测试

普通构建：

```powershell
mvn -q test
```

项目包含一个手动集成测试：

```text
src/test/java/com/my/crawler/weibo/app/GuoSihuoIntegrationTest.java
```

该测试用于扫描和下载：

```text
uid: 2017376052
name: 郭四火-
log: D:\temp\weibo_log
output: D:\temp\weibo_log_output
```

该测试会真实访问微博并写入 `D:\temp`，运行前请确认微博 Cookie 有效。只运行该测试可使用：

```powershell
mvn -q -Dtest=GuoSihuoIntegrationTest test
```
