package com.master.dao;

/***********************************************************************
 * Module:  CourseScheduleDao.java
 * Author:  Administrator
 * Purpose: Defines the Interface CourseScheduleDao
 ***********************************************************************/

import java.util.List;
import java.util.Map;

import com.master.model.CourseSchedule;

public interface CourseScheduleDao {
   /** @param courseSchedule */
   void insertCourseSchedule(CourseSchedule courseSchedule);
   /** @param paraMap */
   List selectCourseSchedule(Map paraMap);

}