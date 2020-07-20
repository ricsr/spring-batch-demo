package com.ricsr.springbootdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
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
					.on("FAILED").stop()
					//.on("FAILED").fail()
				.from(packageStep())
					.on("*").to(pickupOrDeliveryDecider())
						.on("DELIVER").to(deliveryStep())
						.from(pickupOrDeliveryDecider())
							.on("PICKUP").to(pickupStep())
				.end()
				.build();
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

	// Toggle true/false to test rerunning failed jobs
	private boolean throwException = false;
	@Bean
	public Step packageStep() {
		return this.stepBuilderFactory.get("packageStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if(throwException){
					throw new RuntimeException("Exception while Packaging");
				}
				System.out.println("Packaging the gift");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step manualPackageStep() {
		return this.stepBuilderFactory.get("manualPackageStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Manually packaging the gift to be delivered");
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

	@Bean
	public Step pickupStep() {
		return this.stepBuilderFactory.get("pickupStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Package ready for pickup");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public JobExecutionDecider pickupOrDeliveryDecider(){
		return new PickupOrDeliveryDecider();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringbootdemoApplication.class, args);
	}

}
