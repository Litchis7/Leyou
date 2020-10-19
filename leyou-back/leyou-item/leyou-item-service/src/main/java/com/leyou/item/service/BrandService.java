package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.com.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;


    /**
     * 根据查询条件分页并排序查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //根据name模糊查询，或者根据首字母查询
       /* if (StringUtils.isNullOrEmpty(key))*/ //用了MySQL的StringUtils,没有isNotEmpty。应该用tk.mapper的
        if (StringUtil.isNotEmpty(key))
        {
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }

        //添加分页条件，获取第page页，rows条内容
        PageHelper.startPage(page,rows);

        //添加排序条件
        /*if (StringUtils.isNullOrEmpty(sortBy))*/
        if (StringUtil.isNotEmpty(sortBy))
        {
            /*默认写法的是
                example.setOrderByClause("id desc")
                当id和desc为参数时，需要把id和desc，需要把它们写到外面
            * */
           example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }

        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装成pageinfo
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brands);
        //包装成分页结果集返回
        return new PageResult<>(brandPageInfo.getTotal(),brandPageInfo.getList());
       /* return new PageResult<>(brandPageInfo.getTotal(),brands);*/


    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional //加了事务就不用判断了，出错自动回滚
    public void saveBrand(Brand brand, List<Long> cids) {

        //先新增brand
        /*Boolean flag =  this.brandMapper.insertSelective(brand) == 1;*/
        this.brandMapper.insertSelective(brand);

        //再新增中间表
        /*if (flag){
            cids.forEach(cid->{
                //集成mybatis的通用mapper（只能单表操作），其它表操作需要自己写sql语句
                this.brandMapper.insertCategoryAndBrand(cid,brand.getId());
            });
        }*/
        cids.forEach(cid->{
            //集成mybatis的通用mapper（只能单表操作），其它表操作需要自己写sql语句
            this.brandMapper.insertCategoryAndBrand(cid,brand.getId());
        });

    }

    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.selectBrandByCid(cid);
    }


    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
