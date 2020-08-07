package com.ricsr.springbootdemo;

import com.sun.tools.javac.util.Pair;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowStep;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.List;

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
				.split(new SimpleAsyncTaskExecutor())
				.add(packagingAndDeliveryFlow(), updateGiftRepositoryFlow())
				.end()
				.build();
	}

	@Bean
	public Job orderChocolateBoxJob(){
		return this.jobBuilderFactory.get("orderChocolateBoxJob")
				.start(selectChocolatesStep())
					.on("NUTTY_CHOCOLATES").to(addNoteStep()).next(arrangeChocolatesStep())
				.from(selectChocolatesStep()).on("NORMAL_CHOCOLATES").to(arrangeChocolatesStep())
				.from(arrangeChocolatesStep()).on("*").to(packagingAndDeliveryFlow())
				.end()
				.build();
	}

	@Bean
	public Flow packagingAndDeliveryFlow(){
		return new FlowBuilder<SimpleFlow>("packageAndDeliveryFlow").start(packageStep())
				.on("FAILED").fail()
				.from(packageStep())
				.on("*").to(pickupOrDeliveryDecider())
				.on("DELIVER").to(deliveryStep())
				.from(pickupOrDeliveryDecider())
				.on("PICKUP").to(pickupStep())
				.build();
	}

	@Bean
	public Flow updateGiftRepositoryFlow(){
		return new FlowBuilder<SimpleFlow>("updateGiftRepositoryFlow")
				.start(checkGiftRepositoryStep())
				.next(orderNewGiftsStep())
				.next(giftRepositoryUpdatedStep())
				.build();
	}

	@Bean
	public Step checkGiftRepositoryStep(){
		return this.stepBuilderFactory.get("updateGiftRepositoryStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Checking if Gift Repository is short of gifts");
				System.out.println("Gift Repository is short of gifts");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step orderNewGiftsStep(){
		return this.stepBuilderFactory.get("orderNewGiftsStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Ordering new gifts");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step giftRepositoryUpdatedStep(){
		return this.stepBuilderFactory.get("giftRepositoryUpdatedStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Gift Repository is full");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public StepExecutionListener chocolateTypeStepExecutionListener(){
		return new ChocolateTypeStepExecutionListener();
	}

	@Bean
	public Step selectChocolatesStep(){
		return this.stepBuilderFactory.get("selectChocolatesStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Ordering Chocolate : "+ chunkContext.getStepContext().getJobParameters().get("chocolateType").toString());
				return RepeatStatus.FINISHED;
			}
		}).listener(chocolateTypeStepExecutionListener()).build();
	}

	@Bean
	public Step arrangeChocolatesStep(){
		return this.stepBuilderFactory.get("arrangeChocolatesStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Ordering Chocolate : "+ chunkContext.getStepContext().getJobParameters().get("chocolateType").toString());
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step addNoteStep(){
		return this.stepBuilderFactory.get("addNoteStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Chocolate box contains nuts");
				return RepeatStatus.FINISHED;
			}
		}).build();
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
