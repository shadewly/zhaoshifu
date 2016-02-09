package com.security.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import com.bz.model.Authority;
import com.bz.model.Url;
import com.bz.service.AccountServiceI;
import com.bz.service.AuthorityServiceI;

/**
 * @description 资源源数据定义，将所有的资源和权限对应关系建立起来，即定义某一资源可以被哪些角色访问
 * @author yuxinchen
 * 
 */
public class MySecurityMetadataSource implements
		FilterInvocationSecurityMetadataSource {

	private AccountServiceI baseUserService;
	private AuthorityServiceI authorityService;
	/* 保存资源和权限的对应关系 key-资源url value-权限 */
	private Map<String, Collection<ConfigAttribute>> resourceMap = null;
	private AntPathMatcher urlMatcher = new AntPathMatcher();

	public MySecurityMetadataSource(AuthorityServiceI authorityService) {
		this.authorityService = authorityService;
		loadResourcesDefine();
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}

	private void loadResourcesDefine() {

		try {
			resourceMap = new HashMap<String, Collection<ConfigAttribute>>();

			List<Authority> authorityList = authorityService.selectAuthorityList(null);
			for (Authority authority : authorityList) {
				Collection<ConfigAttribute> configAttributes = null;
				ConfigAttribute configAttribute = new SecurityConfig(
						authority.getAuthName());
				for (Url url : authority.getUrlList()) {
					if (resourceMap.containsKey(url.getUrl())) {
						configAttributes = resourceMap.get(url.getUrl());
						configAttributes.add(configAttribute);
					} else {
						configAttributes = new ArrayList<ConfigAttribute>();
						configAttributes.add(configAttribute);
						resourceMap.put(url.getUrl(), configAttributes);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 根据请求的资源地址，获取它所拥有的权限
	 */
	@Override
	public Collection<ConfigAttribute> getAttributes(Object obj)
			throws IllegalArgumentException {
		// 获取请求的url地址
		String url = ((FilterInvocation) obj).getRequestUrl();
		url = url.substring(1);

		Iterator<String> it = resourceMap.keySet().iterator();
		while (it.hasNext()) {
			String _url = it.next();
//			if (_url.indexOf("?") != -1) {
//				_url = _url.substring(0, _url.indexOf("?"));
//
//			}

			if (urlMatcher.match(_url, url))
				return resourceMap.get(url);
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> arg0) {
		return true;
	}

	public AccountServiceI getBaseUserService() {
		return baseUserService;
	}

	public void setBaseUserService(AccountServiceI baseUserService) {
		this.baseUserService = baseUserService;
	}

	public AuthorityServiceI getAuthorityService() {
		return authorityService;
	}

	public void setAuthorityService(AuthorityServiceI authorityService) {
		this.authorityService = authorityService;
	}

}