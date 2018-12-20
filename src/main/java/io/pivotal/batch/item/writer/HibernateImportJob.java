package io.pivotal.batch.item.writer;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
@Profile("hibernate-format-job")
public class HibernateImportJob {

	private JobBuilderFactory jobBuilderFactory;

	private StepBuilderFactory stepBuilderFactory;

	public HibernateImportJob(JobBuilderFactory jobBuilderFactory,
			StepBuilderFactory stepBuilderFactory) {

		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	@Bean
	@StepScope
	public FlatFileItemReader<Customer> customerFileReader(
			@Value("#{jobParameters['customerFile']}")Resource inputFile) {

		return new FlatFileItemReaderBuilder<Customer>()
				.name("customerFileReader")
				.resource(inputFile)
				.delimited()
				.names(new String[] {"firstName",
						"middleInitial",
						"lastName",
						"address",
						"city",
						"state",
						"zip"})
				.targetType(Customer.class)
				.build();
	}

	@Bean
	public HibernateItemWriter<Customer> hibernateItemWriter(EntityManagerFactory entityManager) {
		return new HibernateItemWriterBuilder<Customer>()
				.sessionFactory(entityManager.unwrap(SessionFactory.class))
				.build();
	}

	@Bean
	public Step hibernateFormatStep() throws Exception {
		return this.stepBuilderFactory.get("jdbcFormatStep")
				.<Customer, Customer>chunk(10)
				.reader(customerFileReader(null))
				.writer(hibernateItemWriter(null))
				.build();
	}

	@Bean
	public Job hibernateFormatJob() throws Exception {
		return this.jobBuilderFactory.get("hibernateFormatJob")
				.start(hibernateFormatStep ())
				.build();
	}
}

