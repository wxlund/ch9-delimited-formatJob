package io.pivotal.batch.item.writer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
@Profile("item-writer-adapter")
public class ItemWriterAdapterConfiguration {

	@Autowired
    private JobBuilderFactory jobBuilderFactory;

	@Autowired
    private StepBuilderFactory stepBuilderFactory;

    public ItemWriterAdapterConfiguration(JobBuilderFactory jobBuilderFactory,
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
    public ItemWriterAdapter<Customer> itemWriter(CustomerService customerService) {
    	ItemWriterAdapter<Customer> customerItemWriterAdapter = new ItemWriterAdapter<>();

    	customerItemWriterAdapter.setTargetObject(customerService);
    	customerItemWriterAdapter.setTargetMethod("logCustomer");

    	return customerItemWriterAdapter;
    }


    @Bean
    public Step formatStep() throws Exception {
    	return this.stepBuilderFactory.get("jpaFormatStep")
    			.<Customer, Customer>chunk(10)
    			.reader(customerFileReader(null))
    			.writer(itemWriter(null))
    			.build();
    }

    @Bean
    public Job itemWriterAdapterFormatJob() throws Exception {
    	return this.jobBuilderFactory.get("itemWriterAdapterFormatJob")
    			.start(formatStep())
    			.build();
    }

}
