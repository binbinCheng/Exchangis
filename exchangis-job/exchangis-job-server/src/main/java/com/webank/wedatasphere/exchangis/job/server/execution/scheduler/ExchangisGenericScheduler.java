package com.webank.wedatasphere.exchangis.job.server.execution.scheduler;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.scheduler.AbstractScheduler;
import org.apache.linkis.scheduler.SchedulerContext;
import org.apache.linkis.scheduler.executer.ExecutorManager;
import org.apache.linkis.scheduler.queue.ConsumerManager;
import org.apache.linkis.scheduler.queue.GroupFactory;
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOSchedulerContextImpl;

/**
 * Inherited the AbstractScheduler from linkis-scheduler
 */
public class ExchangisGenericScheduler extends AbstractScheduler {

    private static class Constraints{

        private static final CommonVars<Integer> MAX_PARALLEL_PER_TENANCY = CommonVars.apply("wds.exchangis.job.scheduler.consumer.max.parallel.per-tenancy", 1);

        private static final CommonVars<String> TENANCY_PATTERN = CommonVars.apply("wds.exchangis.job.scheduler.consumer.tenancies", "hadoop,log");

        private static final CommonVars<Integer> GROUP_INIT_CAPACITY = CommonVars.apply("wds.exchangis.job.scheduler.group.min.capacity", 1000);

        private static final CommonVars<Integer> GROUP_MAX_CAPACITY = CommonVars.apply("wds.exchangis.job.scheduler.group.max.capacity", 5000);

        private static final CommonVars<Integer> GROUP_MAX_RUNNING_JOBS = CommonVars.apply("wds.exchangis.job.scheduler.group.max.running-jobs", 30);
    }


    private SchedulerContext schedulerContext;

    private ExecutorManager executorManager;

    private ConsumerManager consumerManager;

    public ExchangisGenericScheduler(ExecutorManager executorManager, ConsumerManager consumerManager){
        this.executorManager = executorManager;
        this.consumerManager = consumerManager;
    }

    @Override
    public void init() {
        this.schedulerContext = new ExchangisSchedulerContext(Constraints.MAX_PARALLEL_PER_TENANCY.getValue(), Constraints.TENANCY_PATTERN.getValue());
        GroupFactory groupFactory = this.schedulerContext.getOrCreateGroupFactory();
        if (groupFactory instanceof TenancyParallelGroupFactory){
            TenancyParallelGroupFactory tenancyParallelGroupFactory = (TenancyParallelGroupFactory)groupFactory;
            tenancyParallelGroupFactory.setDefaultInitCapacity(Constraints.GROUP_INIT_CAPACITY.getValue());
            tenancyParallelGroupFactory.setDefaultMaxCapacity(Constraints.GROUP_MAX_CAPACITY.getValue());
            tenancyParallelGroupFactory.setDefaultMaxRunningJobs(Constraints.GROUP_MAX_RUNNING_JOBS.getValue());
        }
        ((FIFOSchedulerContextImpl) this.schedulerContext).setExecutorManager(executorManager);
        ((FIFOSchedulerContextImpl) this.schedulerContext).setConsumerManager(consumerManager);
    }

    @Override
    public String getName() {
        return "Exchangis-Multi-Tenancy-Scheduler";
    }

    @Override
    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

}
