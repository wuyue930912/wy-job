package com.ts.constant;

import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class JobConstant {
    public static final SchedulerFactory SCHEDULER_FACTORY = new StdSchedulerFactory();
    public static final String TRIGGER_GROUP_NAME = "ts-job";
}
