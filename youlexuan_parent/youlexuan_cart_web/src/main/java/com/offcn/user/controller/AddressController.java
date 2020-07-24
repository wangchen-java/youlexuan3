package com.offcn.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbAddress;
import com.offcn.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * addresscontroller
 * @author senqi
 *
 */
@RestController
@RequestMapping("/address")
public class AddressController {

	@Reference
	private AddressService addressService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findByUserId")
	public List<TbAddress> findByUserId(){
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return addressService.findByUserId(userId);
	}

}
