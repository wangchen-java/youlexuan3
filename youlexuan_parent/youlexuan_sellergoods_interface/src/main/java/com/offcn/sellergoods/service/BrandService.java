package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<TbBrand> findAll();
    PageResult findPage(int pageNum, int pageSize);
    void add(TbBrand tbBrand);
    public void update(TbBrand tbBrand);
    public TbBrand findOne(Long id);
    public void delete(Long[] ids);
    public PageResult findPage(TbBrand brand, int pageNum, int pageSize);

    List<Map> findBrandList();
}
