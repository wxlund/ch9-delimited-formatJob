package io.pivotal.batch.item.writer;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
@Profile("jdbc-format-job")
public class JdbcFormatJob {

	private JobBuilderFactory jobBuilderFactory;

	private StepBuilderFactory stepBuilderFactory;

	public JdbcFormatJob(JobBuilderFactory jobBuilderFactory,
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
	public JdbcBatchItemWriter<Customer> jdbcCustomerWriter(DataSource dataSource) throws Exception {
		return new JdbcBatchItemWriterBuilder<Customer>()
				.dataSource(dataSource)
				.sql("INSERT INTO CUSTOMER (first_name, " +
						"middle_initial, " +
						"last_name, " +
						"address, " +
						"city, " +
						"state, " +
						"zip) VALUES (:firstName, :middleInitial, :lastName, :address, :city, :state, :zip)")
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.build();
	}
	
//	@Bean
//	@StepScope
//	public JdbcBatchItemWriter<Customer> jdbcCustomerWriter(DataSource dataSource) throws Exception {
//		return new JdbcBatchItemWriterBuilder<Customer>()
//				.dataSource(dataSource)
//				.sql("INSERT INTO CUSTOMER (first_name, " +
//						"middle_initial, " +
//						"last_name, " +
//						"address, " +
//						"city, " +
//						"state, " +
//						"zip) VALUES (?, ?, ?, ?, ?, ?, ?)")
//				.itemPreparedStatementSetter(new CustomerItemPreparedStatementSetter())
//				.build();
//	}

	@Bean
	public Step xmlFormatStep() throws Exception {
		return this.stepBuilderFactory.get("xmlFormatStep")
				.<Customer, Customer>chunk(10)
				.reader(customerFileReader(null))
				.writer(jdbcCustomerWriter(null))
				.build();
	}

	@Bean
	public Job xmlFormatJob() throws Exception {
		return this.jobBuilderFactory.get("xmlFormatJob")
				.start(xmlFormatStep())
				.build();
	}
}
