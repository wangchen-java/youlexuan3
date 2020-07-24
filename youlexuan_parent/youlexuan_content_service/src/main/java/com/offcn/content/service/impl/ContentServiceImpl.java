package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * content服务实现层
 * @author senqi
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 分页
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		if (tbContent.getCategoryId() != content.getCategoryId()){
			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
		}
		contentMapper.updateByPrimaryKey(content);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			Long id1 = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(id1);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content != null){			
						if(content.getTitle() != null && content.getTitle().length() > 0){
				criteria.andTitleLike("%" + content.getTitle() + "%");
			}			if(content.getUrl() != null && content.getUrl().length() > 0){
				criteria.andUrlLike("%" + content.getUrl() + "%");
			}			if(content.getPic() != null && content.getPic().length() > 0){
				criteria.andPicLike("%" + content.getPic() + "%");
			}			if(content.getStatus() != null && content.getStatus().length() > 0){
				criteria.andStatusLike("%" + content.getStatus() + "%");
			}
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findContentByCatId(Long catId) {
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(catId);
		if (contentList == null){
			TbContentExample ex = new TbContentExample();
			Criteria c = ex.createCriteria();
			c.andCategoryIdEqualTo(catId);
			c.andStatusEqualTo("1");
			ex.setOrderByClause("sort_order asc");
			List<TbContent> tbContents = contentMapper.selectByExample(ex);
			redisTemplate.boundHashOps("content").put(catId,tbContents);
		}else{

		}
			return contentList;
	}

}
