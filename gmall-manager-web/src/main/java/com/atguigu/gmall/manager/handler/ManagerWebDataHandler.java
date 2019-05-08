package com.atguigu.gmall.manager.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;
import com.atguigu.gmall.service.BaseCatalog2Service;
import com.atguigu.gmall.service.BaseCatalog1Service;
import com.atguigu.gmall.service.BaseCatalog3Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ManagerWebDataHandler {

    @Reference
    BaseCatalog1Service baseCatalog1Service;

    @Reference
    BaseCatalog2Service baseCatalog2Service;

    @Reference
    BaseCatalog3Service baseCatalog3Service;

    @RequestMapping("/getAllCatalog1")
    public List<BaseCatalog1> getAll(){

        return baseCatalog1Service.getAll();
    }

    @RequestMapping("/getAllCatalog2")
    public List<BaseCatalog2> getAll2(String ctg1Id){

        return baseCatalog2Service.getAll(ctg1Id);
    }

    @RequestMapping("/getAllCatalog3")
    public List<BaseCatalog3> getAll3(String ctg2Id){

        return baseCatalog3Service.getAll(ctg2Id);
    }
}
