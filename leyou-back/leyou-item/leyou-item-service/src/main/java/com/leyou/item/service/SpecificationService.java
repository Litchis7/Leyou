package com.leyou.item.service;


import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;


    /**
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupByid(Long cid) {

        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return this.specGroupMapper.select(specGroup);
    }

    /**
     * 更新
     * @param specGroup
     */
    public void updateGroup(SpecGroup specGroup) {

/*        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();*/

        this.specGroupMapper.updateByPrimaryKeySelective(specGroup);
    }

    /**增加规格参数
     * @param specGroup
     */
    public void addGroup(SpecGroup specGroup) {

        this.specGroupMapper.insertSelective(specGroup);

    }

    /**
     * 删除规格组
     * @param id
     */
    public void deleteGroup(Long id) {
        this.specGroupMapper.deleteByPrimaryKey(id);

    }

    /*上面为Group*/
    /**/
    /*下面为Param*/


    /*
    * gid，cid可以合并同项目
    * */

    /**
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.specParamMapper.select(record);
    }


/*    *//**
     * @param gid
     * @return
     *//*
    public List<SpecParam> queryParamsByid(Long gid) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        return this.specParamMapper.select(specParam);
    }*/

/*    *//**
     * @param cid
     * @return
     *//*
    public List<SpecParam> queryParamBycid(Long cid) {

        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        return this.specParamMapper.select(specParam);
    }*/

    /*
     * gid，cid可以合并同项目
     * */

    /**
     * 添加单个规格参数
     * @param specParam
     */
    public void addParam(SpecParam specParam) {

        this.specParamMapper.insertSelective(specParam);
    }


    /**
     * 修改规格参数
     * @param specParam
     */
    public void updateParam(SpecParam specParam) {
        this.specParamMapper.updateByPrimaryKeySelective(specParam);
    }


    /**
     * 删除规格参数
     * @param id
     */
    public void deleteParam(Long id) {
        this.specParamMapper.deleteByPrimaryKey(id);
    }


    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> lists = this.queryGroupByid(cid);
        lists.forEach(list->{
            List<SpecParam> specParams = this.queryParams(list.getId(), null, null, null);
            list.setParams(specParams);
        });
        return lists;
    }
}
