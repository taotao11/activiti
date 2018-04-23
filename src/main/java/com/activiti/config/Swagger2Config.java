package com.activiti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 *
 *@Api：修饰整个类，描述Controller的作用
 *@ApiOperation：描述一个类的一个方法，或者说一个接口
 *@ApiParam：单个参数描述
 *@ApiModel：用对象来接收参数
 *@ApiProperty：用对象接收参数时，描述对象的一个字段
 *@ApiResponse：HTTP响应其中1个描述
 *@ApiResponses：HTTP响应整体描述
 *@ApiIgnore：使用该注解忽略这个API
 *@ApiError ：发生错误返回的信息
 *@ApiImplicitParam：一个请求参数
 *@ApiImplicitParams：多个请求参数
 * @author hedonglin
 * @company 睿思科技
 * @email 1048791780@qq.com
 * @date 2018年03月01
 */
@Configuration
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("api")  // 定义组
                .apiInfo(apiInfo())   // 配置说明
                .select()  // 选择那些路径和 api 会生成 document
                .apis(RequestHandlerSelectors.basePackage("com.activiti.controller")) // 拦截的包路径
                .paths(PathSelectors.any()) // 拦截的接口路径
                .build();  // 创建
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("activiti测试文档") // 标题
                .description("测试activiti") // 描述
                .termsOfServiceUrl("http://blog.csdn.net/saytime")
                .version("1.0")
                .build();
    }
}
