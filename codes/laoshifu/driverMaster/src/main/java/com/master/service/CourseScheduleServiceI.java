package com.master.service;

/***********************************************************************
 * Module:  CourseScheduleServiceI.java
 * Author:  Administrator
 * Purpose: Defines the Interface CourseScheduleServiceI
 ***********************************************************************/

import java.util.List;
import java.util.Map;

import com.master.model.CourseSchedule;

public interface CourseScheduleServiceI {
   /** @param courseSchedule */
   void deployCourse(CourseSchedule courseSchedule);
   /** @param paraMap */
   List searchCourseSchedule(Map paraMap);

}