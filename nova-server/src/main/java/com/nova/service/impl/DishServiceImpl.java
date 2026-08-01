package com.nova.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nova.converter.DishConverter;
import com.nova.config.RedisBloomFilter;
import com.nova.constant.MessageConstant;
import com.nova.constant.StatusConstant;
import com.nova.dto.DishDTO;
import com.nova.entity.Dish;
import com.nova.entity.DishFlavor;
import com.nova.exception.BaseException;
import com.nova.mapper.DishFlavorMapper;
import com.nova.mapper.DishMapper;
import com.nova.mapper.SetmealDishMapper;
import com.nova.result.PageResult;
import com.nova.service.DishService;
import com.nova.utils.AliOssUtil;
import com.nova.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final AliOssUtil aliOssUtil;
    private final DishConverter dishConverter;
    private final CacheManager cacheManager;
    private final RedisBloomFilter bloomFilter;

    @Override
    @Transactional
    public void save(DishDTO dto) {
        Dish dish = dishConverter.toEntity(dto);
        dishMapper.insert(dish);

        List<DishFlavor> flavors = dto.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(f -> f.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
        bloomFilter.add("dish:id", dish.getId());
        evictDishCache(dto.getCategoryId());
    }

    @Override
    public PageResult pageQuery(DishDTO dto) {
        PageHelper.startPage(dto.getPage() > 0 ? dto.getPage() : 1,
                dto.getPageSize() > 0 ? dto.getPageSize() : 10);
        Page<DishVO> page = dishMapper.pageQuery(dto);
        List<DishVO> records = page.getResult().stream()
                .peek(vo -> vo.setImage(aliOssUtil.convertToSignedUrl(vo.getImage())))
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        if (dish == null) return null;
        DishVO vo = dishConverter.toVO(dish);
        vo.setImage(aliOssUtil.convertToSignedUrl(vo.getImage()));
        vo.setFlavors(dishFlavorMapper.getByDishId(id));
        return vo;
    }

    @Override
    @Transactional
    public void update(DishDTO dto) {
        Dish dish = dishConverter.updateEntity(dto);
        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(dto.getId());
        List<DishFlavor> flavors = dto.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(f -> f.setDishId(dto.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
        evictDishCache(dto.getCategoryId());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish oldDish = dishMapper.getById(id);
        if (status == StatusConstant.ENABLE) {
            if (oldDish == null) return;
        }
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
        if (oldDish != null) {
            evictDishCache(oldDish.getCategoryId());
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        Set<Long> categoryIds = new HashSet<>();
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish != null && dish.getStatus() == StatusConstant.ENABLE) {
                throw new BaseException(MessageConstant.DISH_ON_SALE);
            }
            Integer count = setmealDishMapper.countByDishId(id);
            if (count > 0) {
                throw new BaseException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            if (dish != null) {
                categoryIds.add(dish.getCategoryId());
            }
        }
        for (Long id : ids) {
            dishFlavorMapper.deleteByDishId(id);
            dishMapper.deleteById(id);
        }
        categoryIds.forEach(this::evictDishCache);
    }

    public List<DishVO> list(Long categoryId) {
        // 布隆过滤器检查：如果 categoryId 一定不存在，直接返回空列表
        if (categoryId != null && !bloomFilter.mightContain("category:id", categoryId)) {
            return List.of();
        }
        // 从缓存获取原始数据，每次返回时重新签名，保证签名 URL 新鲜
        List<DishVO> list = getCachedDishList(categoryId);
        list.forEach(vo -> vo.setImage(aliOssUtil.convertToSignedUrl(vo.getImage())));
        return list;
    }

    @Cacheable(value = "dish:list", key = "#categoryId")
    public List<DishVO> getCachedDishList(Long categoryId) {
        List<Dish> dishes = dishMapper.list(categoryId);
        return dishes.stream().map(dish -> {
            DishVO vo = dishConverter.toVO(dish);
            vo.setFlavors(dishFlavorMapper.getByDishId(dish.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private void evictDishCache(Long categoryId) {
        if (categoryId != null) {
            org.springframework.cache.Cache cache = cacheManager.getCache("dish:list");
            if (cache != null) {
                cache.evict(categoryId);
            }
        }
    }
}
