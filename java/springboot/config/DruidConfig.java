package com.zzmx.crawler.configuration;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ${DESCRIPTION}
 *
 * @CreateUser Kevin.Zhou
 * @CreateTime 2018/1/10 23:21
 * @ModifyUser Kevin.Zhou
 * @ModifiedTime 2018/1/10 23:21
 * @Version 1.0
 */
@Configuration
public class DruidConfig {

    /**
     * 注册StatViewServlet
     * @return
     */
    @Bean
    public ServletRegistrationBean statViewServlet(){
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),"/druid/*");
        //添加初始化参数：initParams
        /** 白名单，如果不配置或value为空，则允许所有 */
        servletRegistrationBean.addInitParameter("allow","");
        /** 黑名单，与白名单存在相同IP时，优先于白名单 */
        servletRegistrationBean.addInitParameter("deny","");
        /** 用户名 */
        servletRegistrationBean.addInitParameter("loginUsername","admin");
        /** 密码 */
        servletRegistrationBean.addInitParameter("loginPassword","yuntao");
        /** 禁用页面上的“Reset All”功能 */
        servletRegistrationBean.addInitParameter("resetEnable","false");
        return servletRegistrationBean;
    }

    /**
     * 注册WebStatFilter
     * @return
     */
    @Bean
    public FilterRegistrationBean statFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());

        /** 过滤规则 */
        filterRegistrationBean.addUrlPatterns("/*");
        /** 忽略资源 */
        filterRegistrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
        return filterRegistrationBean;
    }
}
