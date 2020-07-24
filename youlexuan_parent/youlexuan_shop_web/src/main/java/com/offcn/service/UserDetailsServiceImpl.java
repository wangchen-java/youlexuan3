package com.offcn.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {
    @Reference
    private SellerService sellerService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<GrantedAuthority> grantedAuthorities =new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        TbSeller seller =sellerService.findOne(username);
        if (seller != null){
            if (seller.getStatus().equals("1")){
                return new User(username,seller.getPassword(),grantedAuthorities);
            }else {
                return null;
            }

        }else {
            return null;
        }

    }
}
