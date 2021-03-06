package com.dm.master.service.impl;

/***********************************************************************
 * Module:  UserMasterServiceImpl.java
 * Author:  Administrator
 * Purpose: Defines the Class UserMasterServiceImpl
 ***********************************************************************/

import java.util.List;

import com.dm.master.common.MasterConstant;
import com.dm.master.dao.UserMasterDao;
import com.dm.master.model.UserMaster;
import com.dm.master.service.CarInfoServiceI;
import com.dm.master.service.UserMasterServiceI;
import com.lsfrpc.annotation.RPCComponent;

/**
 * 师傅用户信息服务实现类
 */
@RPCComponent(UserMasterServiceI.class)
public class UserMasterServiceImpl implements UserMasterServiceI {
	// 师傅用户dao接口对象
	private UserMasterDao userMasterDao; // 车辆信息dao接口对象
	private CarInfoServiceI carInfoService;

	@Override
	public void addUserMaster(UserMaster userMaster) throws Exception {
		// 校验师傅是否已经注册
		if(userMasterDao.validateUserMaster(userMaster)>0){
			throw new Exception("师傅信息已经注册");
		}
				// 校验教练车信息是否已经注册
		List<String> autoNoList=carInfoService.validateCarInfoByAutoNo(userMaster.getCarInfoList());
		if(autoNoList.size()>0){
			StringBuffer sb=new StringBuffer();
			for(String autoNo:autoNoList){
				sb.append(autoNo);
				sb.append(",");
			}

			throw new Exception(sb.substring(0, sb.length()-1)+"车辆已经注册");
		}
				// 校验提交信息是否满足审核条件l
				if(userMaster.getCarInfoList().size()>0 && userMaster.getAvatar()!=null &&){
					//满足设置为待审
					userMaster.setMasterStatus(MasterConstant.UserMasterStatus.Pending.getValue());
				}else{
					//不满足设置为待完善
					userMaster.setMasterStatus(MasterConstant.UserMasterStatus.Incomplete.getValue());
				}
				//保存师傅信息并校验结果
				if(userMasterDao.insertUserMaster(userMaster)!=1){
					throw new Exception("插入师傅信息出错");
				}
				//上传师傅图片到服务器并校验结果
				//保存教练车信息并校验结果
				carInfoService.addCarInfoList(userMaster.getCarInfoList());



	}

	private boolean checkUserMaster(UserMaster userMaster) {

		if (userMaster.getCarInfoList().size() <= 0) {

		} else if (strNullCheck(userMaster.getAvatar())) {

		} else if (strNullCheck(userMaster.getRealName())) {

		} else if (strNullCheck(userMaster.getIdentity())) {

		} else if (strNullCheck(userMaster.getIdentityFront())) {

		} else if (strNullCheck(userMaster.getIdentityReverse())) {

		} else if (intNullCheck(userMaster.getExperience())) {

		} else if (strNullCheck(userMaster.getLicenseFront())) {

		} else if (strNullCheck(userMaster.getLicenseReverse())) {

		} else if (strNullCheck(userMaster.getLicenseAssociateFront())) {

		} else if (strNullCheck(userMaster.getLicenseAssociateReverse())) {

		}
	}

	private boolean strNullCheck(String str) {
		if (str == null || "".equals(str.trim())) {
			return true;
		}
		return false;
	}

	private boolean intNullCheck(Integer integer) {
		if (integer == null || integer.intValue() == 0) {
			return true;
		}
		return false;
	}
}