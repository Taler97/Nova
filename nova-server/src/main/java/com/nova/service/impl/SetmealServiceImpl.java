package com.nova.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nova.config.RedisBloomFilter;
import com.nova.converter.SetmealConverter;
import com.nova.constant.MessageConstant;
import com.nova.constant.StatusConstant;
import com.nova.dto.SetmealDTO;
import com.nova.entity.Dish;
import com.nova.entity.Setmeal;
import com.nova.entity.SetmealDish;
import com.nova.exception.BaseException;
import com.nova.mapper.DishMapper;
import com.nova.mapper.SetmealDishMapper;
import com.nova.mapper.SetmealMapper;
import com.nova.result.PageResult;
import com.nova.service.SetmealService;
import com.nova.utils.AliOssUtil;
import com.nova.vo.SetmealVO;
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
public class SetmealServiceImpl implements SetmealService {

    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final DishMapper dishMapper;
    private final AliOssUtil aliOssUtil;
    private final SetmealConverter setmealConverter;
    private final CacheManager cacheManager;
    private final RedisBloomFilter bloomFilter;

    @Override
    @Transactional
    public void save(SetmealDTO dto) {
        Setmeal setmeal = setmealConverter.toEntity(dto);
        setmealMapper.insert(setmeal);

        List<SetmealDish> dishes = dto.getSetmealDishes();
        if (dishes != null && !dishes.isEmpty()) {
            dishes.forEach(d -> d.setSetmealId(setmeal.getId()));
            setmealDishMapper.insertBatch(dishes);
        }
        bloomFilter.add("setmeal:id", setmeal.getId());
        evictSetmealCache(dto.getCategoryId());
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) return null;
        SetmealVO vo = setmealConverter.toVO(setmeal);
        vo.setImage(aliOssUtil.convertToSignedUrl(vo.getImage()));
        vo.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        return vo;
    }

    @Override
    @Transactional
    public void update(SetmealDTO dto) {
        Setmeal setmeal = setmealConverter.updateEntity(dto);
        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(dto.getId());
        List<SetmealDish> dishes = dto.getSetmealDishes();
        if (dishes != null && !dishes.isEmpty()) {
            dishes.forEach(d -> d.setSetmealId(dto.getId()));
            setmealDishMapper.insertBatch(dishes);
        }
        evictSetmealCache(dto.getCategoryId());
    }

    @Override
    public PageResult pageQuery(SetmealDTO dto) {
        PageHelper.startPage(dto.getPage() > 0 ? dto.getPage() : 1,
                dto.getPageSize() > 0 ? dto.getPageSize() : 10);
        Page<SetmealVO> page = setmealMapper.pageQuery(dto);
        List<SetmealVO> records = page.getResult().stream()
                .peek(vo -> vo.setImage(aliOssUtil.convertToSignedUrl(vo.getImage())))
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal oldSetmeal = setmealMapper.getById(id);
        if (status == StatusConstant.ENABLE) {
            List<SetmealDish> dishes = setmealDishMapper.getBySetmealId(id);
            for (SetmealDish sd : dishes) {
                Dish dish = dishMapper.getById(sd.getDishId());
                if (dish != null && dish.getStatus() == StatusConstant.DISABLE) {
                    throw new BaseException("套餐包含未起售菜品，无法起售");
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
        if (oldSetmeal != null) {
            evictSetmealCache(oldSetmeal.getCategoryId());
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        Set<Long> categoryIds = new HashSet<>();
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal != null && setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new BaseException(MessageConstant.SETMEAL_ON_SALE);
            }
            if (setmeal != null) {
                categoryIds.add(setmeal.getCategoryId());
            }
            setmealDishMapper.deleteBySetmealId(id);
            setmealMapper.deleteById(id);
        }
        categoryIds.forEach(this::evictSetmealCache);
    }

    @Override
    public List<SetmealVO> listByCategoryId(Long categoryId) {
        // 布隆过滤器检查：如果 categoryId 一定不存在，直接返回空列表
        if (categoryId != null && !bloomFilter.mightContain("category:id", categoryId)) {
            return List.of();
        }
        List<SetmealVO> list = getCachedSetmealList(categoryId);
        list.forEach(vo -> vo.setImage(aliOssUtil.convertToSignedUrl(vo.getImage())));
        return list;
    }

    @Cacheable(value = "setmeal:list", key = "#categoryId")
    public List<SetmealVO> getCachedSetmealList(Long categoryId) {
        List<Setmeal> setmeals = setmealMapper.listByCategoryId(categoryId);
        return setmeals.stream().map(s -> {
            SetmealVO vo = setmealConverter.toVO(s);
            vo.setSetmealDishes(setmealDishMapper.getBySetmealId(s.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private void evictSetmealCache(Long categoryId) {
        if (categoryId != null) {
            org.springframework.cache.Cache cache = cacheManager.getCache("setmeal:list");
            if (cache != null) {
                cache.evict(categoryId);
            }
        }
    }
}
