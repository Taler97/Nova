package com.nova.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nova.config.RedisBloomFilter;
import com.nova.constant.MessageConstant;
import com.nova.constant.StatusConstant;
import com.nova.converter.CategoryConverter;
import com.nova.dto.CategoryDTO;
import com.nova.dto.CategoryPageQueryDTO;
import com.nova.entity.Category;
import com.nova.exception.BaseException;
import com.nova.mapper.CategoryMapper;
import com.nova.mapper.DishMapper;
import com.nova.mapper.SetmealMapper;
import com.nova.result.PageResult;
import com.nova.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final CategoryConverter categoryConverter;
    private final CacheManager cacheManager;
    private final RedisBloomFilter bloomFilter;

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void save(CategoryDTO dto) {
        Category category = categoryConverter.toEntity(dto);
        category.setStatus(StatusConstant.ENABLE);
        categoryMapper.insert(category);
        bloomFilter.add("category:id", category.getId());
        evictAllCategoryCache();
    }

    @Override
    public void update(CategoryDTO dto) {
        Category category = categoryConverter.updateEntity(dto);
        categoryMapper.update(category);
        evictAllCategoryCache();
    }

    @Override
    public void deleteById(Long id) {
        Integer dishCount = dishMapper.countByCategoryId(id);
        if (dishCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        categoryMapper.deleteById(id);
        evictAllCategoryCache();
    }

    @Override
    public List<Category> list(Integer type) {
        // 清除 PageHelper 线程残留，避免影响查询
        com.github.pagehelper.PageHelper.clearPage();
        if (type == null) {
            // type 为 null 时清除旧缓存，防止之前 PageHelper 残留导致的分页数据被缓存
            evictAllCategoryCache();
        }
        List<Category> list = getCachedCategoryList(type);
        return list;
    }

    @Cacheable(value = "category:list", key = "#type")
    public List<Category> getCachedCategoryList(Integer type) {
        return categoryMapper.list(type);
    }

    private void evictAllCategoryCache() {
        org.springframework.cache.Cache cache = cacheManager.getCache("category:list");
        if (cache != null) {
            cache.clear();
        }
    }
}
