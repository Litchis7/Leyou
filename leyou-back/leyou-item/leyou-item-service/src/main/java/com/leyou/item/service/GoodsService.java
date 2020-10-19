package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.com.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 分页查询商品
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();


        //查询字段
        //添加查询条件
        if (StringUtils.isNotEmpty(key)) {

            // criteria.andLike("key","%" + key + "%");
            criteria.andLike("title", "%" + key + "%");//property是查询对象
        }

        //上下架查询
        //添加上下架的过滤条件
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        //添加分页
        PageHelper.startPage(page, rows);

        //执行查询Spu
        //执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        //PageInfo放在查询结果之后，中间不能放其他，否则就会出现获取的total河实际的total不相等的情况
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);


        //Spu转SpuBo
        //Spu集合转化成SpuBo集合
        /*List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu->{
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuBo);
            // 查询分类名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));

            // 查询品牌的名称
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

            spuBos.add(spuBo);
        });
*/

             List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuBo);

            //查询品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());

            //查询分类名称
            List<String> strings = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            spuBo.setCname(StringUtils.join(strings, "-"));

            return spuBo;//必须要有返回值
        }).collect(Collectors.toList());

/*        错误写法,返回数据不正确
        PageInfo<SpuBo> spuBoPageInfo = new PageInfo<>(spuBos);
        return new PageResult<>(spuBoPageInfo.getTotal(), spuBoPageInfo.getList());*/


        //返回PageResult<SpuBo>
        /*
        * 是这样的方法：public PageResult(Long total, List<T> items)
        * */
        return new PageResult<>(spuPageInfo.getTotal(),spuBos);

    }

    /**
     * 添加商品
     * @param spuBo
     */
    @Transactional //开启事务，防止产生垃圾数据
    public void saveGoods(SpuBo spuBo) {

        //先增spu
        spuBo.setId(null);//防止sql注入
        //这些传入参数没有，需要自己添加
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        //再去新增spudetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());//这个id不是自增的，是继承了spu表
        this.spuDetailMapper.insertSelective(spuDetail);


        saveSkuAndStock(spuBo);

        sendMsg("insert",spuBo.getId());
    }


    /** mq
     * @param type
     * @param id
     */
    private void sendMsg(String type, Long id) {
        try {
            this.amqpTemplate.convertAndSend("item." + type , id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**新增sku和stock
     * ctrl+alt+M抽取方法
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {

            // 新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());//没传入spuid外键，需要自己添加
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());//继承sku表的id，不是自增
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }


    /**更新商品信息
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {//shift+F6批量修改名字，ctrl+shift+u大小写改变

        //根据spuId查询要删除的sku

        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku->{
            //删除stock
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });

        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        this.skuMapper.delete(sku);

        //新增sku和stock
        saveSkuAndStock(spuBo);



        // 更新spu
        spuBo.setCreateTime(null);//设置null，不能让别人更新
        spuBo.setLastUpdateTime(new Date());
        spuBo.setValid(null);//设置null，不能让别人更新
        spuBo.setSaleable(null);//设置null，不能让别人更新
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        // 更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update", spuBo.getId());

    }









    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {

        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spuId查询sku的集合
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(sku);
        //若直接return，则回显不显示库存量。因此需要把stock表的stock赋值sku的stock
        skus.forEach(sku1 -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku1.getId());//stock表的主键就是sku的主键
            sku1.setStock(stock.getStock());
        });
        return skus;
    }


    /**根据spuid查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    public Sku querySkuBySkuId(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }
}
