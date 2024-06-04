//package config;
//
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//@Configuration
//public class DataSourceConfig {
//    @Bean
//    public DataSource getDataBaseDataSource() {
//        return DataSourceBuilder.create()
//                .driverClassName("org.h2.Driver")
//                .url("jdbc:h2:mem:testDb")
//                .username("sa")
//                .password("1234")
//                .build();
//    }
//}
