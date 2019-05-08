package com.atguigu.gmall.manager.handler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManagerWebHandler {

    @RequestMapping("index")
    public String toIndex(){

        return "index";
    }

    @RequestMapping("attrListPage")
    public String toAttrListPage(){

        return "attrListPage";
    }

    @RequestMapping("spuListPage")
    public String toSpuListPage(){

        return "spuListPage";
    }


}
