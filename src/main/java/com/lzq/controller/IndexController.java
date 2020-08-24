package com.lzq.controller;

import com.lzq.pojo.Content;
import com.lzq.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
  @Autowired
  private ContentService service;

  @ResponseBody
  @GetMapping("esTest")
  public ArrayList<Map<String,Object>> search(String searchKey, Integer pageNo, Integer pageSize) throws IOException {
      return service.getEstic(pageNo,pageSize,searchKey);
  }
  @ResponseBody
  @GetMapping("search")
  public ArrayList<Map<String,Object>> search1(String searchKey, Integer pageNo, Integer pageSize) throws IOException {
    ArrayList<Map<String, Object>> list = service.searchPageHighlightBulider(pageNo, pageSize, searchKey);
    System.out.println(list);
    return list;
  }
  @GetMapping({"/", "/index"})
  public String getIndex() {
    return "index";
  }

  @GetMapping("eslist")
  @ResponseBody
  public Boolean setEstic(String key) throws Exception {
    return service.parseContent(key);
  }
}
