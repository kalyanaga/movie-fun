package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    @Value("${VCAP_SERVICES}")
    String vcapService;

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials getDatabaseServiceCredentials() {
        return new DatabaseServiceCredentials(vcapService);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariDataSource hk = new HikariDataSource();
        hk.setDataSource(dataSource);
        return hk;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariDataSource hk = new HikariDataSource();
        hk.setDataSource(dataSource);
        return hk;
    }

    @Bean
    public HibernateJpaVendorAdapter getHibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setDatabase(Database.MYSQL);
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getMoviesLocalContainerEntityManagerFactoryBean(@Qualifier("moviesDataSource") DataSource moviesDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factory =  new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(moviesDataSource);
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setPackagesToScan("org.superbiz.moviefun.movies");
        factory.setPersistenceUnitName("movies-database");

        return factory;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getAlbumsLocalContainerEntityManagerFactoryBean(@Qualifier("albumsDataSource") DataSource albumsDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factory =  new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(albumsDataSource);
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setPackagesToScan("org.superbiz.moviefun.albums");
        factory.setPersistenceUnitName("albums-database");

        return factory;
    }

    @Bean
    @Qualifier("getMoviesPlatformTransactionManager")
    public PlatformTransactionManager getMoviesPlatformTransactionManager(@Qualifier("getMoviesLocalContainerEntityManagerFactoryBean") EntityManagerFactory getMoviesLocalContainerEntityManagerFactoryBean) {
        return new JpaTransactionManager(getMoviesLocalContainerEntityManagerFactoryBean);
    }

    @Bean
    @Qualifier("getAlbumsPlatformTransactionManager")
    public PlatformTransactionManager getAlbumsPlatformTransactionManager(@Qualifier("getAlbumsLocalContainerEntityManagerFactoryBean") EntityManagerFactory getAlbumsLocalContainerEntityManagerFactoryBean) {
        return new JpaTransactionManager(getAlbumsLocalContainerEntityManagerFactoryBean);
    }
}
