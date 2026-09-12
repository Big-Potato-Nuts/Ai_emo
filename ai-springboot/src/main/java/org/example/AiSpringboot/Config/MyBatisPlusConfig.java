package org.example.AiSpringboot.Config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置类
 * 职责：
 * 1. 扫描 Mapper 接口所在的包，让 Spring 容器能管理所有的 Mapper Bean
 * 2. 配置 SqlSessionFactory（MyBatis 的核心工厂，负责创建 SqlSession）
 * 3. 注册 MyBatis-Plus 分页插件，使 selectPage 等分页查询能够真正生效
 */
@Configuration
@MapperScan("org.example.AiSpringboot.Mapper")
public class MyBatisPlusConfig {

    /**
     * 创建 MyBatis 的 SqlSessionFactory
     * 说明：手动构建工厂 Bean，并在其上挂载分页插件拦截器。
     *      分页插件（PaginationInnerInterceptor）是 MyBatis-Plus 分页查询（IPage/Page）能正确
     *      生成 LIMIT 语句并回填 total 的必备条件，没有它分页接口将返回全量数据。
     *
     * @param dataSource 数据源（由 Spring Boot 自动注入）
     * @return SqlSessionFactory MyBatis 会话工厂
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        // 1. 创建 MyBatis-Plus 专用的 SqlSessionFactoryBean
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        // 2. 设置数据源，后续 Mapper 都基于该数据源执行 SQL
        factoryBean.setDataSource(dataSource);

        // 3. 构建 MyBatis-Plus 插件拦截器链
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 4. 添加分页插件，指定数据库类型为 MySQL，分页方言会按 MySQL 语法生成
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 5. 设置单页最大条数限制为 500，防止一次查询拉取过多数据（可配置项，默认无限制）
        paginationInnerInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 6. 将插件链挂载到 SqlSessionFactory 上
        factoryBean.setPlugins(interceptor);

        // 7. 构建并返回最终的 SqlSessionFactory
        return factoryBean.getObject();
    }
}
