package com.lzq.utils;

import com.lzq.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlParseUtil {
 public List<Content> parseJD(String key) throws Exception {
     //获取请求 https://search.jd.com/Search?keyword=java
     String url = "https://search.jd.com/Search?keyword="+key;
     //解析网页  Document对象
     Document document = Jsoup.parse(new URL(url), 30000);
     //所有在js中可以使用的方法，在这都可以使用
     Element element = document.getElementById("J_goodsList");
     //获取所有li标签
     Elements li = element.getElementsByTag("li");
     List<Content> list = new ArrayList<>();
     for (Element el: li) {
         String img  = el.getElementsByTag("img").eq(0).attr("src");
         String price  = el.getElementsByClass("p-price").eq(0).text();
         String title  = el.getElementsByClass("p-name").eq(0).text();
         list.add(new Content(img,title,price));
     }
     return list;
 }

  public static void main(String[] args) throws Exception {
      //
      new HtmlParseUtil().parseJD("笔记本").forEach(System.out::println);
    System.out.println(new HtmlParseUtil().parseJD("python").size());
  }
}
