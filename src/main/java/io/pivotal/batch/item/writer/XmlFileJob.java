package io.pivotal.batch.item.writer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.oxm.xstream.XStreamMarshaller;

@Configuration
@Profile("xml-format-job")
public class XmlFileJob {

	private JobBuilderFactory jobBuilderFactory;

	private StepBuilderFactory stepBuilderFactory;

	public XmlFileJob(JobBuilderFactory jobBuilderFactory,
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
	@StepScope
	public StaxEventItemWriter<Customer> xmlCustomerWriter(
			@Value("#{jobParameters['outputFile']}") Resource outputFile) throws Exception {

		Map<String, Class> aliases = new HashMap<>();
		aliases.put("customer", Customer.class);

		XStreamMarshaller marshaller = new XStreamMarshaller();

		marshaller.setAliases(aliases);

		marshaller.afterPropertiesSet();

		return new StaxEventItemWriterBuilder<Customer>()
				.name("customerItemWriter")
				.resource(outputFile)
				.marshaller(marshaller)
				.rootTagName("customers")
				.build();
	}

	@Bean
	public Step xmlFormatStep() throws Exception {
		return this.stepBuilderFactory.get("xmlFormatStep")
				.<Customer, Customer>chunk(10)
				.reader(customerFileReader(null))
				.writer(xmlCustomerWriter(null))
				.build();
	}

	@Bean
	public Job xmlFormatJob() throws Exception {
		return this.jobBuilderFactory.get("xmlFormatJob")
				.start(xmlFormatStep())
				.build();
	}
}

