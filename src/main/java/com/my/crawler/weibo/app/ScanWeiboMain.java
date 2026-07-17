package com.my.crawler.weibo.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.my.crawler.weibo.client.WeiboClient;
import com.my.crawler.weibo.config.AppConfig;
import com.my.crawler.weibo.config.Constants;
import com.my.crawler.weibo.config.Constants.IDNameEntity;
import com.my.crawler.weibo.history.HistoryStore;
import com.my.crawler.weibo.parser.PicVideoParser;
import com.my.crawler.weibo.util.JsonSupport;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * 扫描微博并生成/合并 pics_videos.log。
 *
 * <p>
 * 对应旧类“根据UID扫描所有weibo并解析pic和video”的核心流程：
 * 读取旧 pics_videos.log -> 仅扫描新增页 -> 解析图片/视频 -> 按旧 dateId 格式合并保存。
 * </p>
 */
@SuppressWarnings("unused")
public final class ScanWeiboMain {
    static String folderBasePath = "F:\\weibo_log";

    private ScanWeiboMain() {
    }

    /** 历史扫描入口；保留旧 main 中通过注释切换任务的使用方式。 */
    public static void main(String[] args) throws Exception {
        // 扫描生图();
        // 扫描(Constants.半年可见());
        // 扫描(Constants.特别关注());
        // 扫描(Constants.网红());

        // 扫描(Constants.艾米());
        // 扫描(Constants.白鹿());
        // 扫描(Constants.蔡文静());
        // 扫描(Constants.陈都灵());
        // 扫描(Constants.程潇());
        // 扫描(Constants.代斯());
        // 扫描(Constants.黛薇卡());
        // 扫描(Constants.范冰冰());
        // 扫描(Constants.傅菁());
        // 扫描(Constants.关晓彤());

        // 扫描(Constants.韩雪());
        // 扫描(Constants.胡连馨());
        // 扫描(Constants.黄梦莹());
        // 扫描(Constants.黄杨钿甜());
        // 扫描(Constants.江疏影());
        // 扫描(Constants.蒋依依());
        // 扫描(Constants.景甜());
        // 扫描(Constants.鞠婧祎());
        // 扫描(Constants.李沁());
        // 扫描(Constants.李一桐());

        // 扫描(Constants.梁洁());
        // 扫描(Constants.林允());
        // 扫描(Constants.刘诗诗());
        // 扫描(Constants.孟佳());
        // 扫描(Constants.孟子义());
        // 扫描(Constants.娜扎());
        // 扫描(Constants.倪妮());
        // 扫描(Constants.欧阳娜娜());
        // 扫描(Constants.秦岚());
        // 扫描(Constants.热巴());

        // 扫描(Constants.宋妍霏());
        // 扫描(Constants.宋祖儿());
        // 扫描(Constants.孙芮());
        // 扫描(Constants.唐艺昕());
        // 扫描(Constants.田曦薇());
        // 扫描(Constants.王楚然());
        // 扫描(Constants.王鹤润());
        // 扫描(Constants.王鸥());
        // 扫描(Constants.王秀竹());
        // 扫描(Constants.王玉雯());

        // 扫描(Constants.王紫璇());
        // 扫描(Constants.吴宣仪());
        // 扫描(Constants.吴优());
        // 扫描(Constants.辛芷蕾());
        // 扫描(Constants.许佳琪());
        // 扫描(Constants.徐璐());
        // 扫描(Constants.杨超越());
        // 扫描(Constants.杨幂());
        // 扫描(Constants.虞书欣());
        // 扫描(Constants.曾黎());

        // 扫描(Constants.张嘉倪());
        // 扫描(Constants.张婧仪());
        // 扫描(Constants.章若楠());
        // 扫描(Constants.张天爱());
        // 扫描(Constants.张雪迎());
        // 扫描(Constants.张予曦());
        // 扫描(Constants.赵今麦());
        // 扫描(Constants.赵露思());
        // 扫描(Constants.钟楚曦());
        // 扫描(Constants.周洁琼());

        // 扫描(Constants.周也());

        // 扫描(Constants.欧美());
        // 扫描(Constants.韩国());
        // 扫描日本写真();
        // 扫描(Constants.日本演员());
        // 扫描(Constants.明星图片());
        // 扫描(Constants.明星1());
        // 扫描(Constants.明星2());
        // 扫描(Constants.明星3());

        开始扫描();
    }

    /** 批量扫描一个账号分组，扫描后沿用旧逻辑下载 livephoto MOV。 */
    private static void 扫描(Collection<Constants.IDNameEntity> entities) throws Exception {
        for (Constants.IDNameEntity entity : entities) {
            System.out.println("id: " + entity.id + "\t name: " + entity.screen_name);
            start(entity.getId(), new File(folderBasePath, entity.screen_name));
            Constants.randomSleepLong();
            DownloadWeiboMain.下载Mov2(entity);
        }
    }

    /** 扫描日本写真分组，但不触发 MOV 下载。 */
    private static void 扫描日本写真() throws Exception {
        for (Constants.IDNameEntity entity : Constants.日本写真()) {
            System.out.println("id: " + entity.id + "\t name: " + entity.screen_name);
            start(entity.getId(), new File(folderBasePath, entity.screen_name));
            Constants.randomSleepLong();
        }
    }

    /** 扫描生图分组，保留旧代码中独立循环的历史逻辑。 */
    private static void 扫描生图() throws Exception {
        // 扫描(Constants.生图());
        for (Constants.IDNameEntity entity : Constants.生图()) {
            System.out.println("id: " + entity.id + "\t name: " + entity.screen_name);
            start(entity.getId(), new File(folderBasePath, entity.screen_name));
            Constants.randomSleepLong();
        }
    }

    /** 保留旧代码中手工维护的零散账号扫描历史。 */
    private static void 开始扫描() throws IOException {
        // start("5525995118", new File(folderBasePath, "我是一个小朋友小啊小朋友"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("5525995118", "我是一个小朋友小啊小朋友"));
        // start("3603256695", new File(folderBasePath, "梦醒忒远"));
        // start("5077791396", new File(folderBasePath, "黄杨钿甜"));
        // start("7414925267", new File(folderBasePath, "姜珮瑶工作室"));
        // start("6656508997", new File(folderBasePath, "周依然工作室"));
        // start("1517846540", new File(folderBasePath, "王亚飞Yafei"));
        // start("1215621210", new File(folderBasePath, "何琢言"));
        // start("2269438254", new File(folderBasePath, "美娜呀美La"));
        // start("1230184540", new File(folderBasePath, "白庆琳的微博"));
        // start("1312412824", new File(folderBasePath, "林志玲"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1604236990", "林志玲"));
        // start("6072304766", new File(folderBasePath, "林志玲工作室"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1604236990", "林志玲工作室"));
        // start("8402043366", new File(folderBasePath, "歐陽娣娣Didi工作室"));
        // start("1373550167", new File(folderBasePath, "王思懿"));
        // start("1604236990", new File(folderBasePath, "斓曦"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1604236990", "斓曦"));
        // start("1378010100", new File(folderBasePath, "王子文Ava"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1378010100", "王子文Ava"));
        // start("1649540795", new File(folderBasePath, "郭小仙儿-郭珺"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1649540795", "郭小仙儿-郭珺"));
        // start("1973763181", new File(folderBasePath, "徐沐婵Xx"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1973763181", "徐沐婵Xx"));
        // start("1246788271", new File(folderBasePath, "李菲儿love"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1258859614", "李菲儿love"));
        // start("7754982812", new File(folderBasePath, "小艾同学下课了吗"));
        // start("1197354837", new File(folderBasePath, "左小青"));
        // start("1301064830", new File(folderBasePath, "柴蔚"));
        // start("5197213436", new File(folderBasePath, "金佳悦-"));
        // start("1397341394", new File(folderBasePath, "_陳菲"));
        // start("1734442735", new File(folderBasePath, "汪小敏"));
        // start("2007347307", new File(folderBasePath, "孙佳奇Titania"));
        // start("1758929805", new File(folderBasePath, "张馨月Carina"));
        // start("1378010100", new File(folderBasePath, "王子文Ava"));
        // start("1649540795", new File(folderBasePath, "郭小仙儿-郭珺"));
        // start("1240008360", new File(folderBasePath, "王媛可"));
        // start("1727687652", new File(folderBasePath, "章乐韵"));
        // start("2433827884", new File(folderBasePath, "周依然6"));
        // start("2595359142", new File(folderBasePath, "Yakisa彭雅琦"));
        // start("1628482500", new File(folderBasePath, "陆妍淇"));
        // start("1628482500", new File(folderBasePath, "陆妍淇"));
        // start("1262719025", new File(folderBasePath, "毛林林NIKITA"));
        // start("5408683366", new File(folderBasePath, "合诗雨Hermia"));
        // start("7765311497", new File(folderBasePath, "明星生图严选"));
        // start("5783471428", new File(folderBasePath, "刘承羽Natasha"));
        // start("1301064830", new File(folderBasePath, "柴蔚"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("5408683366", "柴蔚"));
        // start("3179885602", new File(folderBasePath, "钟晨瑶"));
        // start("1749646275", new File(folderBasePath, "何雨桐MANGO"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("3179885602", "何雨桐MANGO"));
        // start("7754982812", new File(folderBasePath, "小艾同学下课了吗"));
        // start("3511260684", new File(folderBasePath, "_黃芯靈"));
        // start("5525618252", new File(folderBasePath, "大鹅鹅Ran"));
        // start("3833024804", new File(folderBasePath, "徐艺真的微博"));
        // start("7983000031", new File(folderBasePath, "河北彩伽2"));
        // start("3151770020", new File(folderBasePath, "蓝心妍工作室"));
        // start("1265241322", new File(folderBasePath, "刘雨欣yoyoliu"));
        // start("1752051394", new File(folderBasePath, "劉心悠Annie"));
        // start("1281950453", new File(folderBasePath, "周扬"));
        // start("2034565060", new File(folderBasePath, "许晴随行笔记"));
        // start("5690339577", new File(folderBasePath, "Summer许晴工作室"));
        // start("1214548420", new File(folderBasePath, "郭珍霓"));
        // start("1258859614", new File(folderBasePath, "我是江一燕"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1258859614", "我是江一燕"));
        // start("1890196401", new File(folderBasePath, "不2不叫周淑怡"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1890196401", "不2不叫周淑怡"));
        // start("1890196401", new File(folderBasePath, "不2不叫周淑怡"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1890196401", "不2不叫周淑怡"));
        // start("6650714202", new File(folderBasePath, "李凯馨Eleanor工作室"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("6650714202", "李凯馨Eleanor工作室"));
        // start("6430354194", new File(folderBasePath, "杨采钰工作室"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("6430354194", "杨采钰工作室"));
        // start("1006421732", new File(folderBasePath, "陈昊宇Amy"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1006421732", "陈昊宇Amy"));
        // start("7661259195", new File(folderBasePath, "Official_权恩妃"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("7661259195", "Official_权恩妃"));
        // start("3617364884", new File(folderBasePath, "彭小苒"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("3617364884", "彭小苒"));
        // start("2111083372", new File(folderBasePath, "乔欣Bridgette"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("2111083372", "乔欣Bridgette"));
        // start("3617364884", new File(folderBasePath, "彭小苒"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("3617364884", "彭小苒"));
        // start("6203942537", new File(folderBasePath, "请往我的生活里加满糖谢谢"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("6203942537", "请往我的生活里加满糖谢谢"));
        // start("5503504780", new File(folderBasePath, "吉娜爱丽丝Gina"));
        // start("7772266364", new File(folderBasePath, "Rachelcook1995"));
        // start("5638789392", new File(folderBasePath, "rachelcook14"));
        // start("3807003830", new File(folderBasePath, "张若晞Roxie"));
        // start("7284275957", new File(folderBasePath, "坠落星空·江疏影"));
        // start("1807391080", new File(folderBasePath, "Constence刘彦池"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1807391080", "Constence刘彦池"));
        // start("2595359142", new File(folderBasePath, "Yakisa彭雅琦"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("2595359142", "Yakisa彭雅琦"));
        // start("6521611081", new File(folderBasePath, "食梦鲨"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("6521611081", "食梦鲨"));
        // start("1378010100", new File(folderBasePath, "王子文Ava"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1378010100", "王子文Ava"));
        // start("1722686885", new File(folderBasePath, "王卓淇erin__kay"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1722686885", "王卓淇erin__kay"));
        // start("1231654104", new File(folderBasePath, "刘芸"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1231654104", "刘芸"));
        // start("1240008360", new File(folderBasePath, "王媛可"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1240008360", "王媛可"));
        // start("1258859614", new File(folderBasePath, "我是江一燕"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1258859614", "我是江一燕"));
        // start("6519552504", new File(folderBasePath, "陈梦儿-myYY"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("6519552504", "陈梦儿-myYY"));
        // start("1853627313", new File(folderBasePath, "卓仕琳"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("1853627313", "卓仕琳"));
        // start("6130786243", new File(folderBasePath, "流星牛角"));
        // DownloadWeiboMain.下载Mov2(new IDNameEntity("6130786243", "流星牛角"));
        // start("5044684628", new File(folderBasePath, "嘉行传媒"));
        // start("1715351501", new File(folderBasePath, "辛芷蕾"));
        // start("5945340823", new File(folderBasePath, "辛芷蕾工作室"));
        // start("1264948457", new File(folderBasePath, "董璇粉丝团"));
        // start("2883701284", new File(folderBasePath, "O0笨笨呀0O-梁洁"));
        // start("2283247914", new File(folderBasePath, "JunoTieN"));
        // start("5503504780", new File(folderBasePath, "吉娜爱丽丝Gina"));
        // start("7362539881", new File(folderBasePath, "黄梦莹工作室"));
        // start("1676082433", new File(folderBasePath, "高圆圆"));
        // start("5234965173", new File(folderBasePath, "甘婷婷工作室"));
        // start("7040041221", new File(folderBasePath, "潇骑校尉曹操"));
        // start("6349746169", new File(folderBasePath, "冯文娟工作室"));
        // start("1504964033", new File(folderBasePath, "冯文娟"));
        // start("5658396874", new File(folderBasePath, "安雅泰勒乔伊"));
        // start("5511171110", new File(folderBasePath, "安雅泰勒-乔伊"));
        // start("3561627892", new File(folderBasePath, "EstherHeesch吧"));
        // start("7607376925", new File(folderBasePath, "是你的程儿"));
        // start("2382243211", new File(folderBasePath, "hThT__"));
        // start("1734442735", new File(folderBasePath, "汪小敏"));
        // start("7309784142", new File(folderBasePath, "Penicillin·倪妮"));
        // start("7489613730", new File(folderBasePath, "北纬26度想Ni·倪妮"));
        // start("7457436869", new File(folderBasePath, "KleinBlue·倪妮"));
        // start("2595359142", new File(folderBasePath, "Yakisa彭雅琦"));
        // start("1233965570", new File(folderBasePath, "川外川"));
        // start("7772266364", new File(folderBasePath, "Rachelcook1995"));
        // start("6518263844", new File(folderBasePath, "镜头兄"));
        // start("6467863545", new File(folderBasePath, "tbbhktjj"));
        // start("6472458430", new File(folderBasePath, "SkyPictorial"));
        // start("1197002213", new File(folderBasePath, "男人装"));
        // start("2966904732", new File(folderBasePath, "美好身体bo"));
        // start("6973729551", new File(folderBasePath, "HeartSniper_许佳琪"));
        // start("1874288121", new File(folderBasePath, "王瑞子715"));
        // start("5279748872", new File(folderBasePath, "yesyanbaby"));
        // start("3987343279", new File(folderBasePath, "河北彩伽-元河北彩花"));
        // start("5732211644", new File(folderBasePath, "河北彩伽"));
        // start("7409628440", new File(folderBasePath, "AprilFox_Naran娜然"));
        // start("7415540957", new File(folderBasePath, "月与玫瑰__娜然Naran"));
        // start("7014245539", new File(folderBasePath, "娜然丨NaranDaily"));
        // start("7863307524", new File(folderBasePath, "娜然Naran工作室"));
        // start("6463277233", new File(folderBasePath, "娜然Naran"));
        // start("2646681810", new File(folderBasePath, "林允Jelly"));
        // start("1237313773", new File(folderBasePath, "热依扎"));
        // start("1957663211", new File(folderBasePath, "张芷溪"));
        // start("6368929929", new File(folderBasePath, "王子文工作室微博"));
        // start("1222062284", new File(folderBasePath, "张萌"));
        // start("1313228221", new File(folderBasePath, "李佳桐sep"));
        // start("1307243944", new File(folderBasePath, "李依晓"));
        // start("1741661732", new File(folderBasePath, "顾璇"));
        // start("6521611081", new File(folderBasePath, "食梦鲨"));
        // start("1951027255", new File(folderBasePath, "李如儒"));
        // start("1819744725", new File(folderBasePath, "王乐君"));
        // start("5197213436", new File(folderBasePath, "金佳悦-"));
        // start("3262625014", new File(folderBasePath, "唐艺昕工作室"));
        // start("1862896261", new File(folderBasePath, "松岡李那LinahM"));
        // start("3285031871", new File(folderBasePath, "刘美彤"));
        // start("1345943410", new File(folderBasePath, "刘美含"));
        // start("2742586765", new File(folderBasePath, "孙嘉璐Ruby"));
        // start("5994952329", new File(folderBasePath, "包上恩"));
        // start("2169289455", new File(folderBasePath, "孙雪宁ooo"));
        // start("1280435871", new File(folderBasePath, "YUNAN男男"));
        // start("6079596473", new File(folderBasePath, "老余那些事"));
        // start("1890196401", new File(folderBasePath, "不2不叫周淑怡"));
        // start("7499320208", new File(folderBasePath, "刘芊螢_LQY"));
        // start("1624095323", new File(folderBasePath, "曲尼次仁"));
        // start("1898812132", new File(folderBasePath, "吳千語全球官方後援會"));
        // start("5849619820", new File(folderBasePath, "蔡卓宜工作室"));
        // start("7120634645", new File(folderBasePath, "李若嘉工作室"));
        // start("6576856192", new File(folderBasePath, "火箭少女101官博"));
        // start("1243273752", new File(folderBasePath, "古晨"));
        // start("1714246692", new File(folderBasePath, "傅嘉莉KellyFu"));
        // start("1297916241", new File(folderBasePath, "洋气YOUNGCHIC"));
        // start("7782739992", new File(folderBasePath, "无尽热恋丨1226x1109"));
        // start("6650714202", new File(folderBasePath, "李凯馨Eleanor工作室"));
        // start("7749127987", new File(folderBasePath, "·AllForZendaya·"));
        // start("3635953843", new File(folderBasePath, "MadisonBeerUpdates"));
        // start("5335569701", new File(folderBasePath, "凯瑟琳纽顿0208"));
        // start("3194073462", new File(folderBasePath, "MetFreckle"));
        // start("7832948489", new File(folderBasePath, "周也资讯站"));
        // start("5666687855", new File(folderBasePath, "BellaHadidCN"));
        // start("7329838548", new File(folderBasePath, "SydneySweeneyCN"));
    }

    /** 按 UID 扫描单个账号，并写入该账号目录下的 pics_videos.log。 */
    public static void start(String uid, File folder) throws IOException {
        start(uid, 0, folder);
    }

    /** 从指定页开始扫描单个账号。 */
    private static void start(String uid, int beginPage, File folder) throws IOException {
        scan(uid, beginPage, 0, folder);
    }

    /** 扫描指定页范围，保留旧 start(uid, beginPage, endPage, folder) 调用形式。 */
    private static void start(String uid, int beginPage, int endPage, File folder) throws IOException {
        int maxPages = endPage <= beginPage ? 0 : endPage - beginPage;
        scan(uid, beginPage, maxPages, folder);
    }

    /** 执行实际扫描、解析和旧日志合并。 */
    private static void scan(String uid, int beginPage, int maxPages, File folder) throws IOException {
        try {
            var mapper = JsonSupport.mapper();
            HistoryStore historyStore = new HistoryStore(mapper, folder.toPath());
            TreeMap<String, JsonNode> oldLog = historyStore.readPicVideoLog();
            var latestExistingDate = beginPage == 0 ? historyStore.latestDate(oldLog).orElse(null) : null;

            System.out.printf("scan uid=%s name=%s latestExistingDate=%s%n",
                    uid, folder.getName(), latestExistingDate);
            List<JsonNode> statuses = new WeiboClient(mapper).fetchStatuses(
                    uid,
                    beginPage,
                    maxPages,
                    latestExistingDate,
                    1500L);

            TreeMap<String, JsonNode> newLog = new PicVideoParser(mapper).parse(statuses);
            TreeMap<String, JsonNode> merged = new TreeMap<>(oldLog);
            merged.putAll(newLog);
            removeScreenName(merged);
            historyStore.writePicVideoLog(merged);

            System.out.printf("statuses=%d newMediaPosts=%d totalMediaPosts=%d log=%s%n",
                    statuses.size(), newLog.size(), merged.size(), historyStore.picVideoLog());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("扫描微博时被中断", e);
        } catch (Exception e) {
            throw e instanceof IOException ioException ? ioException : new IOException("扫描微博失败", e);
        }
    }

    /** 给聚合入口使用的扫描方法。 */
    public static void scan(AppConfig config) throws Exception {
        scan(config.uid(), config.beginPage(), config.maxPages(), config.accountLogDir().toFile());
    }

    /** 写回日志前清理旧历史中残留的 screen_name 字段。 */
    private static void removeScreenName(TreeMap<String, JsonNode> log) {
        for (JsonNode item : log.values()) {
            if (item instanceof ObjectNode objectNode) {
                objectNode.remove(Constants.SCREEN_NAME);
            }
        }
    }
}
