package io.pivotal.batch.item.writer;

import javax.jms.ConnectionFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.adapter.PropertyExtractingDelegatingItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import ch.qos.logback.classic.pattern.MessageConverter;

@Configuration
@Profile("jms-resource-config")
public class JmsResourceConfiguration {

	@Autowired
    private JobBuilderFactory jobBuilderFactory;

	@Autowired
    private StepBuilderFactory stepBuilderFactory;

    public JmsResourceConfiguration(JobBuilderFactory jobBuilderFactory,
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
    public PropertyExtractingDelegatingItemWriter<Customer> itemWriter(CustomerService customerService) {
    	PropertyExtractingDelegatingItemWriter<Customer> itemWriter =
    			new PropertyExtractingDelegatingItemWriter<>();

    	itemWriter.setTargetObject(customerService);
    	itemWriter.setTargetMethod("logCustomerAddress");
    	itemWriter.setFieldsUsedAsTargetMethodArguments(
    			new String[] {"address", "city", "state", "zip"});

    	return itemWriter;
    }

    @Bean
    public Step formatStep() throws Exception {
    	return this.stepBuilderFactory.get("formatStep")
    			.<Customer, Customer>chunk(10)
    			.reader(customerFileReader(null))
    			.writer(itemWriter(null))
    			.build();
    }

    @Bean
    public Job propertiesFormatJob() throws Exception {
    	return this.jobBuilderFactory.get("propertiesFormatJob")
    			.start(formatStep())
    			.build();
    }

    @Bean // Serialize message content to json using TextMessage
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
    	MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    	converter.setTargetType(MessageType.TEXT);
    	converter.setTypeIdPropertyName("_type");
    	return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
    	CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
    	cachingConnectionFactory.afterPropertiesSet();

    	JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
    	jmsTemplate.setDefaultDestinationName("customers");
    	jmsTemplate.setReceiveTimeout(5000L);

    	return jmsTemplate;
    }


}
