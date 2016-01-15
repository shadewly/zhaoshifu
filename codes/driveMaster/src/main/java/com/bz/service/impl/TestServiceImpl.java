package com.bz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bz.dao.TestDao;
import com.bz.service.TestServiceI;

/**
 * 目录服务类
 * 
 * @author yuxinchen
 * 
 */
@Service(value = "testService")
@Transactional
public class TestServiceImpl implements TestServiceI {

	@Autowired
	@Qualifier("testDao")
	protected TestDao testDao;

	@Override
	public void aa() throws Exception {
		testDao.selectTest();
		
	}


	
}