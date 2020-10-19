package com.leyou.cart.service;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.com.utils.JsonUtils;
import com.leyou.common.pojo.UserInfo;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundGeoOperations;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

/*    这里我们不访问数据库，而是直接操作Redis。基本思路：
    - 先查询之前的购物车数据
    - 判断要添加的商品是否存在
    - 存在：则直接修改数量后写回Redis
    - 不存在：新建一条数据，然后写入Redis*/


    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;//可读性高

    static final String KEY_PREFIX = "user:cart:";


    public void addCart(Cart cart) {

        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();

        //查询购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());


        //key传过来的是json对象，需要转化成string存储。Map<string,Map<string,string>>
        //Map<userId,Map<skuId,cart>>
        String key = cart.getSkuId().toString();
        Integer num = cart.getNum();

        //判断当前的商品是否在购物车中
        if (hashOperations.hasKey(key)){
            //在，更新数量
            //取数据类型为string的redis值 得使用getObject, getString类型会在值中带有 "" 导致判断时会有问题
            String cartJson = hashOperations.get(key).toString();//取出来的是object值
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(cart.getNum() + num);
            /*
            * ex:
            * hashOps.put("name","jack");
            * hashOps.put("age","21");
            *
            *
            * Map<String,Map<String,String>>
              Map<userId,Map<skuId,cart>>
            * */
            //在Redis中，所有数据都保存为字符串
            //hashOperations.put(key, JsonUtils.serialize(cart));

        }else {
            //不在，新增购物车

            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setUserId(userInfo.getId());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setPrice(sku.getPrice());
            //hashOperations.put(key, JsonUtils.serialize(cart));
        }

        hashOperations.put(key, JsonUtils.serialize(cart));
    }

    /**查询
     * @return
     */
    public List<Cart> queryCarts() {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();

        // 判断是否存在购物车
        String key = KEY_PREFIX + user.getId();
        if(!this.redisTemplate.hasKey(key)){
            // 不存在，直接返回
            return null;
        }
        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        //获取购物车Map所有Cart值
        List<Object> cartsJson = hashOps.values();//object类型
        // 判断是否有数据
        if(CollectionUtils.isEmpty(cartsJson)){
            return null;
        }
        // 查询购物车数据,把List<Object>集合转化为List<Cart集合>
        return cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(), Cart.class)).collect(Collectors.toList());//返回一个新的数组
    }

    public void updateCarts(Cart cart) {
        // 获取登陆信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();

        String key = KEY_PREFIX + userInfo.getId();
        if(!this.redisTemplate.hasKey(key)){
            // 不存在，直接返回
            return ;
        }

        Integer num = cart.getNum();

        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        // 获取购物车信息
        //Object carjJson2 = hashOperations.get(cart.getSkuId().toString()) ;
        String cartJson = hashOperations.get(cart.getSkuId().toString()).toString();

        cart = JsonUtils.parse(cartJson, Cart.class);

        cart.setNum(num);

        hashOperations.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));

    }

    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + user.getId();
        if(!this.redisTemplate.hasKey(key)){
            // 不存在，直接返回
            return ;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }
}
