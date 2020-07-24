package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.mapper.TbTypeTemplateMapper;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.pojo.TbTypeTemplate;
import com.offcn.pojo.TbTypeTemplateExample;
import com.offcn.pojo.TbTypeTemplateExample.Criteria;
import com.offcn.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * type_template服务实现层
 * @author senqi
 *
 */
@Service(timeout = 3000)
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);

		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate != null){			
						if(typeTemplate.getName() != null && typeTemplate.getName().length() > 0){
				criteria.andNameLike("%" + typeTemplate.getName() + "%");
			}			if(typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0){
				criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
			}			if(typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0){
				criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
			}			if(typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0){
				criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
			}
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
		List<TbTypeTemplate> typeTemplates =typeTemplateMapper.selectByExample(null);
		for (TbTypeTemplate template :typeTemplates){
			List<Map> brandList = JSON.parseArray(template.getBrandIds(),Map.class);
			redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);

			List<Map> specAndOptionList = findSpecAndOptionList(template.getId());
			redisTemplate.boundHashOps("specList").put(template.getId(), specAndOptionList);
		}
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findTypeTemplateList() {
		return typeTemplateMapper.findTypeTemplateList();
	}

	@Override
	public List<Map> findSpecAndOptionList(Long id) {
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		List<Map> maps = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		for (Map map :maps){
			Integer specId = (Integer) map.get("id");
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria c = example.createCriteria();

			c.andSpecIdEqualTo(specId.longValue());
			List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(example);
			map.put("options",tbSpecificationOptions);

		}
		return maps;
	}

}
