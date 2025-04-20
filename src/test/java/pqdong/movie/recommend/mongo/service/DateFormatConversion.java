package pqdong.movie.recommend.mongo.service;

import cn.hutool.core.date.DateUtil;
import java.util.Date;

public class DateFormatConversion {
    public static void main(String[] args) {
        String dateStr = "Sat Feb 17 00:00:00 CDT 1945";
        // 解析字符串为Date对象
        Date date = DateUtil.parse(dateStr);
        // 将Date对象格式化为指定格式的字符串
        String formattedDate = DateUtil.format(date, "yyyy-MM-dd");
        System.out.println("格式化后的日期: " + formattedDate);

        // 将格式化后的日期字符串再转换回Date对象
        Date newDate = DateUtil.parse(formattedDate, "yyyy-MM-dd");
        // 获取时间戳
        long timestamp = newDate.getTime();
        System.out.println("对应的时间戳: " + timestamp);
    }
}    