package com.ricsr.springbootdemo;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

public class PickupOrDeliveryDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        Random random = new Random();
        int randomInt = random.nextInt(10);
        String response = randomInt > 5 ? "DELIVER" : "PICKUP";
        System.out.println("Customer choose: "+ response);
        return new FlowExecutionStatus(response);
    }
}
