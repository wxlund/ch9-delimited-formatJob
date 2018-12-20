package io.pivotal.batch.item.writer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Configuration
@Profile("delimited-text-file-job")
public class DelimitedTextFileJob {

	@Autowired
    private JobBuilderFactory jobBuilderFactory;

	@Autowired
    private StepBuilderFactory stepBuilderFactory;

    public DelimitedTextFileJob(JobBuilderFactory jobBuilderFactory,
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
    public FlatFileItemWriter<Customer> delimitedCustomerItemWriter(
            @Value("#{jobParameters['outputFile']}") Resource outputFile) {

        BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<>();

        fieldExtractor.setNames(new String[] {"zip", "state", "city", "address", "lastName", "firstName"});

        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<Customer> lineAggregator = new DelimitedLineAggregator<>();

        lineAggregator.setFieldExtractor(fieldExtractor);
        lineAggregator.setDelimiter(";");

        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .resource(outputFile)
                .lineAggregator(lineAggregator)
                .append(true)
                .build();
    }


    @Bean
    public Step formatStep() {
        return this.stepBuilderFactory.get("formatStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader(null))
                .writer(delimitedCustomerItemWriter(null))
                .build();
    }

    @Bean
    public Job formatJob() {
        return this.jobBuilderFactory.get("formatJob")
                .start(formatStep())
                .build();
    }
}
