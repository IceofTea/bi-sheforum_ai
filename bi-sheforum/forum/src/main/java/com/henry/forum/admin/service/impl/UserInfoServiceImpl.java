package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.mapper.UserInfoMapper;
import com.henry.forum.admin.service.BadgeService;
import com.henry.forum.admin.service.UserInfoService;
@Service
public class UserInfoServiceImpl implements UserInfoService {

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private BadgeService badgeService;
	public UserInfo login(UserInfo userInfo) throws Exception {
		//1.账户密码格式是否正确
		if (userInfo.getUsername()==null ||
				userInfo.getUsername().length() < 3 ||
				userInfo.getUsername().length() > 16){
			throw new Exception("账号格式不对");
		}
		if (userInfo.getPassword( )==null ||
				userInfo.getPassword().length() < 3 ||
				userInfo.getPassword(). length() >16){
			throw new Exception("密码格式不对");
		}

		//2. 查询账户信息
		UserInfo selecRoot=userInfoMapper.selectByName(userInfo.getUsername());
		//3.判断账户是否存在
		if (selecRoot==null) {
			throw new Exception("账户不存在");
		}
		//4.判断密码是否正确
		if (!selecRoot.getPassword().equals(userInfo.getPassword())) {
			throw new Exception("密码错误");
		}
		//5.判断账户是否可用
		if (selecRoot.getStatus()!=0) {
			throw new Exception("账户被禁用");
		}
		
		return selecRoot;
	}
	
	@Override
	public UserInfo register(UserInfo userInfo) throws Exception {
		
		
		UserInfo selecRoot=userInfoMapper.selectByName(userInfo.getUsername());
		//3.判断账户是否存在
		if (selecRoot!=null) {
			throw new Exception("账户已存在");
		}
		if (userInfo.getUsername()==null ||
				userInfo.getUsername().length() < 3 ||
				userInfo.getUsername().length() > 16){
			throw new Exception("账号格式不对");
		}
		if (userInfo.getPassword( )==null ||
				userInfo.getPassword().length() < 3 ||
				userInfo.getPassword(). length() >16){
			throw new Exception("密码格式不对");
		}
		userInfo.setExperience(0);
		userInfo.setLevel(1);
		userInfo.setPoints(0);
		userInfo.setThreadCount(0);
		userInfo.setCommentCount(0);
		userInfo.setLoginCount(0);
		userInfo.setContinuousLoginDays(0);
		userInfo.setRegisterTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
		userInfoMapper.insert(userInfo);
		return userInfoMapper.selectByPrimaryKey(userInfo.getId());
	}
	
	@Override
	public List selectAll() {
		return userInfoMapper.selectAll();
	}

	@Override
	public List getRankings(String type) {
		return userInfoMapper.selectAll();
	}

	@Override
	public UserInfo selectById(Integer id) {
		
		return userInfoMapper.selectByPrimaryKey(id);
	}

	@Override
	public UserInfo remove(Integer id) {
		
		return null;
	}

	@Override
	public UserInfo edit(UserInfo userInfo) {
		
		userInfoMapper.updateByPrimaryKey(userInfo);
		return userInfoMapper.selectByPrimaryKey(userInfo.getId());
	}

	@Override
	public PageInfo getForPage(Integer pageIndex, Integer pageSize) {
		PageHelper.startPage(pageIndex, pageSize);
		List users = userInfoMapper.selectAll();
		PageInfo pageInfo = new PageInfo(users);
		return pageInfo;
	}
	
	@Override
	public void addExperience(Integer userId, Integer exp) {
		if (userId == null || exp == null || exp <= 0) return;
		UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
		if (user != null) {
			int newExp = (user.getExperience() == null ? 0 : user.getExperience()) + exp;
			user.setExperience(newExp);
			user.setLevel(calculateLevel(newExp));
			userInfoMapper.updateByPrimaryKey(user);
			badgeService.checkAndAwardBadges(userId);
		}
	}
	
	@Override
	public void addPoints(Integer userId, Integer points) {
		if (userId == null || points == null || points == 0) return;
		UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
		if (user != null) {
			int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + points;
			user.setPoints(newPoints);
			userInfoMapper.updateByPrimaryKey(user);
			badgeService.checkAndAwardBadges(userId);
		}
	}
	
	@Override
	public UserInfo updateLoginStats(Integer userId) {
		if (userId == null) return null;
		UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
		if (user != null) {
			int loginCount = (user.getLoginCount() == null ? 0 : user.getLoginCount()) + 1;
			user.setLoginCount(loginCount);
			user.setLastLoginTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
			int oldDays = user.getContinuousLoginDays() == null ? 0 : user.getContinuousLoginDays();
			user.setContinuousLoginDays(oldDays + 1);
			userInfoMapper.updateByPrimaryKey(user);
			badgeService.checkAndAwardBadges(userId);
		}
		return user;
	}
	
	@Override
	public int calculateLevel(int experience) {
		if (experience < 100) return 1;
		if (experience < 300) return 2;
		if (experience < 600) return 3;
		if (experience < 1000) return 4;
		if (experience < 1500) return 5;
		if (experience < 2100) return 6;
		if (experience < 2800) return 7;
		if (experience < 3600) return 8;
		if (experience < 4500) return 9;
		if (experience < 5500) return 10;
		if (experience < 6600) return 11;
		if (experience < 7800) return 12;
		if (experience < 9100) return 13;
		if (experience < 10500) return 14;
		if (experience < 12000) return 15;
		if (experience < 13600) return 16;
		if (experience < 15300) return 17;
		if (experience < 17100) return 18;
		if (experience < 19000) return 19;
		return 20;
	}

@Override
	public UserInfo selectByUsername(String username) {
		return userInfoMapper.selectByName(username);
	}
	
	@Override
	public void incrementThreadCount(Integer userId) {
		if (userId != null) {
			userInfoMapper.updateThreadCount(userId);
			badgeService.checkAndAwardBadges(userId);
		}
	}

	@Override
	public void incrementCommentCount(Integer userId) {
		if (userId != null) {
			userInfoMapper.updateCommentCount(userId);
			badgeService.checkAndAwardBadges(userId);
		}
	}

	@Override
	public void updateContinuousLoginDays(Integer userId, Integer days) {
		if (userId != null && days != null) {
			userInfoMapper.updateContinuousLoginDays(userId, days);
		}
	}
}
