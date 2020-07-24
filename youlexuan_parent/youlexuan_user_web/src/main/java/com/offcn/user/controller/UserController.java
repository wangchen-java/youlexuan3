package com.offcn.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbUser;
import com.offcn.user.service.UserService;
import com.offcn.util.PhoneFormatCheckUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * usercontroller
 * @author senqi
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	@Autowired
	private RedisTemplate redisTemplate;
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		try{
			boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);
			if (!phoneLegal){
				return new Result(false,"请输入正确的手机号");
			}
			userService.sendCode(phone);
			return new Result(true,"短信验证码发送成功");
		}catch (Exception e){
			e.printStackTrace();
			return new Result(false,"短信验证码发送失败");
		}
	}
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String code){
		try {
			boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(user.getPhone());
			if (!phoneLegal){
				return new Result(false,"请输入正确的手机号");
			}

			String oldCode = (String) redisTemplate.boundHashOps("phoneCode").get(user.getPhone());
			if (!oldCode.equals(code)){
				return new Result(false,"验证码错误,请重新输入");
			}
			user.setCreated(new Date());//创建日期
			user.setUpdated(new Date());//修改日期
			String password=DigestUtils.md5Hex(user.getPassword());//对密码加密
			user.setPassword(password);
			
			userService.add(user);
			redisTemplate.boundHashOps("phoneCode").delete(user.getPhone());
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	
}
