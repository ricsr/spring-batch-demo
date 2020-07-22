package com.ricsr.springbootdemo;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class ChocolateTypeStepExecutionListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Before arranging chocolates");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("After arranging chocolates");
        String chocolateType = stepExecution.getJobParameters().getString("chocolateType");
        return chocolateType.equals("Almonds") ? new ExitStatus("NUTTY_CHOCOLATES") : new ExitStatus("NORMAL_CHOCOLATES");
    }
}
