package io.pivotal.batch.item.writer;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
@Profile("jpa-format-job")
public class JpaImportJob {

	private JobBuilderFactory jobBuilderFactory;

	private StepBuilderFactory stepBuilderFactory;

	public JpaImportJob(JobBuilderFactory jobBuilderFactory,
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

	// Listing 9-39. formatJob Configured to Use JpaItemWriter
	@Bean
	public JpaItemWriter<Customer> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
		JpaItemWriter<Customer> jpaItemWriter = new JpaItemWriter<>();

		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

		return jpaItemWriter;
	}

	@Bean
	public Step jpaFormatStep() throws Exception {
		return this.stepBuilderFactory.get("jpaFormatStep")
				.<Customer, Customer>chunk(10)
				.reader(customerFileReader(null))
				.writer(jpaItemWriter(null))
				.build();
	}

	@Bean
	public Job jpaFormatJob() throws Exception {
		return this.jobBuilderFactory.get("jpaFormatJob")
				.start(jpaFormatStep())
				.build();
	}
}

