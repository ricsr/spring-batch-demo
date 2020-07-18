package com.ricsr.springbootdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class SpringbootdemoApplication {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job packageJob(){
		return this.jobBuilderFactory.get("giftShopJob").start(readOrderStep())
				.next(packageStep())
				.next(deliveryStep()).build();
	}

	@Bean
	public Step readOrderStep() {
		return this.stepBuilderFactory.get("readOrderStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Order Received");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step packageStep() {
		return this.stepBuilderFactory.get("packageStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				String giftWrapper = chunkContext.getStepContext().getJobParameters().get("giftWrapper").toString();
				System.out.println("Packaging the gift with " + giftWrapper);
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step deliveryStep() {
		return this.stepBuilderFactory.get("deliveryStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Package out for delivery");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringbootdemoApplication.class, args);
	}

}
