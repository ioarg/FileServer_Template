/*
*   The application's configuration class
*   Used to define file server beans
*   and static resource handlers
 */
package jarg.templates.FileServer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySource("classpath:fileserver.properties")
public class FileServerConfig implements WebMvcConfigurer {

    @Autowired
    private Environment env;

    /************************************************
     *   Fileserver Beans
     ************************************************/
    @Bean
    public String storageDirectory(){
        return env.getProperty("fileserver.storageDirectory");
    }

    @Bean
    public ExecutorService execService() {
        return Executors.newFixedThreadPool(Integer.parseInt(env.getProperty("fileserver.maxPoolSize")));
    }

    @Bean(name="multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multResolver = new CommonsMultipartResolver();
        multResolver.setMaxUploadSize(100000000);
        return multResolver;
    }

    /************************************************
     *   Static Resources
     ************************************************/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/styles/**").addResourceLocations("classpath:/static/styles/");
        registry.addResourceHandler("/scripts/**").addResourceLocations("classpath:/static/scripts/");
    }

}
