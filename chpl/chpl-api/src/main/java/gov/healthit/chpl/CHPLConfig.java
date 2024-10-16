package gov.healthit.chpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.filter.APIKeyAuthenticationFilter;
import gov.healthit.chpl.ratelimiting.RateLimitingInterceptor;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.CacheControlHandlerInterceptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebMvc
@EnableAsync
@EnableAspectJAutoProxy
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:/environment.properties"),
    @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/lookup.properties"),
    @PropertySource(value = "classpath:/lookup-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/email.properties"),
    @PropertySource(value = "classpath:/email-override.properties", ignoreResourceNotFound = true),
})
@ComponentScan(basePackages = {
        "gov.healthit.chpl.**"
})
@Log4j2
public class CHPLConfig implements WebMvcConfigurer, EnvironmentAware {
    private static final long MAX_UPLOAD_FILE_SIZE = 5242880;
    private static final int MAX_COOKIE_AGE_SECONDS = 3600;
    private String chplServiceUrl;
    private String apiLicenseUrl;
    private String apiVersion;
    private String apiDescriptionHtml;
    private String feedbackFormUrl;
    private Boolean tryItOutEnabled;

    @Autowired
    private Environment env;

    @Lazy
    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @Lazy
    @Autowired
    private ErrorMessageUtil errorUtil;

    private Integer rateLimitRequestCount;
    private Integer rateLimitTimePeriod;

    @Override
    public void setEnvironment(Environment e) {
        this.chplServiceUrl = e.getProperty("chplUrlBegin") + e.getProperty("basePath");
        this.apiLicenseUrl = e.getProperty("api.licenseUrl");
        this.apiVersion = e.getProperty("api.version");
        this.apiDescriptionHtml = e.getProperty("api.description");
        this.feedbackFormUrl = e.getProperty("contact.publicUrl");
        this.tryItOutEnabled = BooleanUtils.toBooleanObject(e.getProperty("api.tryItOutEnabled"));

        this.rateLimitRequestCount = Integer.parseInt(e.getProperty("rateLimitRequestCount"));
        this.rateLimitTimePeriod = Integer.parseInt(e.getProperty("rateLimitTimePeriod"));
    }

    @Autowired
    void configureObjectMapper(ObjectMapper mapper) {
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = 2803278488940499378L;

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                Boolean returnDeprecatedFields = Boolean.valueOf(env.getProperty("response.returnDeprecatedFields"));
                if (_findAnnotation(m, JsonIgnore.class) != null) {
                    return true;
                } else {
                    return _findAnnotation(m, DeprecatedResponseField.class) != null && !returnDeprecatedFields;
                }
            }
        });
   }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();
        bean.setPrefixJson(false);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        bean.setSupportedMediaTypes(mediaTypes);
        return bean;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("errors-override");

        ResourceBundleMessageSource parentMessageSource = new ResourceBundleMessageSource();
        parentMessageSource.setBasename("errors");

        messageSource.setParentMessageSource(parentMessageSource);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("my-locale-cookie");
        localeResolver.setCookieMaxAge(MAX_COOKIE_AGE_SECONDS);
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor() {
        RateLimitingInterceptor interceptor = new RateLimitingInterceptor(apiKeyDAO, errorUtil, rateLimitRequestCount, rateLimitTimePeriod);
        return interceptor;
    }

    @Bean
    public CacheControlHandlerInterceptor cacheControlHandlerInterceptor() {
        CacheControlHandlerInterceptor interceptor = new CacheControlHandlerInterceptor();
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor());
        registry.addInterceptor(rateLimitingInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns(APIKeyAuthenticationFilter.ALLOWED_REQUEST_PATHS);
        registry.addInterceptor(cacheControlHandlerInterceptor());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        LOGGER.info("Get BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OpenAPI chplOpenAPI() {
        OpenAPI api = new OpenAPI()
                .info(new Info().title("Certified Health IT Product Listing API")
                .version(apiVersion)
                .description(String.format(apiDescriptionHtml, feedbackFormUrl, feedbackFormUrl))
                .license(new License().name("BSD License").url(apiLicenseUrl)))
                .addServersItem(new Server().url(chplServiceUrl));
        if (BooleanUtils.isTrue(tryItOutEnabled)) {
            api.setComponents(new Components()
                .addSecuritySchemes(SwaggerSecurityRequirement.API_KEY,
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(In.HEADER).name("API-Key").scheme("API-Key"))
                .addSecuritySchemes(SwaggerSecurityRequirement.BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).in(In.HEADER).name("Bearer").scheme("Bearer").bearerFormat("JWT")));
        }
        return api;
    }

    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> openApi.setTags(openApi.getTags()
                .stream()
                .sorted(Comparator.comparing(tag -> StringUtils.stripAccents(tag.getName())))
                .collect(Collectors.toList()));
    }

    @Bean
    public OpenApiCustomizer sortSchemasAlphabetically() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        };
    }
}
