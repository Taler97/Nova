package com.nova.config;

import com.nova.entity.Category;
import com.nova.entity.Dish;
import com.nova.mapper.CategoryMapper;
import com.nova.mapper.DishMapper;
import com.nova.mapper.SetmealMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时从数据库加载已有数据初始化布隆过滤器。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BloomFilterInitRunner implements ApplicationRunner {

    private final RedisBloomFilter bloomFilter;
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public void run(ApplicationArguments args) {
        // 分类 ID — 对应 bloom:category:id
        List<Category> categories = categoryMapper.list(null);
        for (Category c : categories) {
            bloomFilter.add("category:id", c.getId());
        }
        log.info("布隆过滤器 [category:id] 已初始化，共 {} 个 ID", categories.size());

        // 菜品 ID — 对应 bloom:dish:id
        List<Dish> dishes = dishMapper.list(null);
        for (Dish d : dishes) {
            bloomFilter.add("dish:id", d.getId());
        }
        log.info("布隆过滤器 [dish:id] 已初始化，共 {} 个 ID", dishes.size());

        // 套餐 ID — 对应 bloom:setmeal:id
        List<Long> setmealIds = setmealMapper.getAllIds();
        for (Long id : setmealIds) {
            bloomFilter.add("setmeal:id", id);
        }
        log.info("布隆过滤器 [setmeal:id] 已初始化，共 {} 个 ID", setmealIds.size());
    }
}
