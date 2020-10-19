package com.leyou.item.controller;


import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import com.sun.corba.se.impl.orbutil.LogKeywords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByid(@PathVariable("cid") Long cid){

        List<SpecGroup> list =  this.specificationService.queryGroupByid(cid);

        if (CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);

    }

    /**
     * 更新规格组件名
     * @param specGroup
     * @return
     */
   // @PostMapping("group")
    @PutMapping("group")
   /* @ResponseBody*/
    public ResponseEntity<Void> updateGroup(@RequestBody SpecGroup specGroup){

          this.specificationService.updateGroup(specGroup);
          /*return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();*/
            return ResponseEntity.status(HttpStatus.OK).build();

    }


    /**
     * 增加规格
     * @param specGroup
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> addGroup(@RequestBody SpecGroup specGroup){
        this.specificationService.addGroup(specGroup);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     *删除规格组
     * @param id
     * @return
     */
    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") Long id){
        this.specificationService.deleteGroup(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

/*上面为Group*/
    /**/
        /*下面为Param*/


    /*
    * gid,cid属于相同方法，但是传入参数不一样，可以合并，不需要单独写
    * */

    /**
     *根据条件查询规格参数
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid", required = false)Long gid,
            @RequestParam(value = "cid", required = false)Long cid,
            @RequestParam(value = "generic", required = false)Boolean generic,
            @RequestParam(value = "searching", required = false)Boolean searching
    ){

        List<SpecParam> params = this.specificationService.queryParams(gid, cid, generic, searching);

        if (CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }



    /**
     * 根据gid查询规格参数
     * @param gid
     * @return
     */
/*    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamsByid(@RequestParam("gid") Long gid){

        List<SpecParam> list = this.specificationService.queryParamsByid(gid);

        if (CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }*/

/*    *//**
     * @param cid
     * @return
     *//*
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamBycid(@RequestParam("cid") Long cid){

        List<SpecParam> list = this.specificationService.queryParamBycid(cid);

        if (CollectionUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);

    }*/

    /*
     * gid,cid属于相同方法，但是传入参数不一样，可以合并，不需要单独写
     * */

    /**
     * 添加单个规格参数
     * @param specParam
     */
    @PostMapping("param")
    public ResponseEntity<Void> addParam(@RequestBody SpecParam specParam){

        this.specificationService.addParam(specParam);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 修改规格参数
     * @param specParam
     * @return
     */

    @PutMapping("param")
    /* @ResponseBody*/
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam specParam){

        this.specificationService.updateParam(specParam);
        /*return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();*/
        return ResponseEntity.status(HttpStatus.OK).build();

    }

    /**
     *删除规格参数
     * @param id
     * @return
     */
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteParam(@PathVariable("id") Long id){
        this.specificationService.deleteParam(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsWithParam(@PathVariable("cid") Long cid){
        List<SpecGroup> list =this.specificationService.queryGroupsWithParam(cid);
        if (StringUtils.isEmpty(list)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }



}
