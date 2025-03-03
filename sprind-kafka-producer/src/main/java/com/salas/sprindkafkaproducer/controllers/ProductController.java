package com.salas.sprindkafkaproducer.controllers;


import com.salas.sprindkafkaproducer.service.IProductService;
import com.salas.sprindkafkaproducer.service.dto.CreateProductDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;

@RestController
@RequestMapping("/product")
public class ProductController {

    private IProductService productService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody CreateProductDto dto) {
        String productId = null;

        try {
            productId = productService.createProduct(dto);
        }catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }

    @GetMapping
    public String greeting(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        System.out.println("Client IP: " + clientIp);

        // Получение метода запроса (GET, POST и т.д.)
        String method = request.getMethod();
        System.out.println("Request Method: " + method);

        // Получение URL запроса
        String requestUrl = request.getRequestURL().toString();
        System.out.println("Request URL: " + requestUrl);

        // Получение строки запроса (query string)
        String queryString = request.getQueryString();
        System.out.println("Query String: " + queryString);

        // Получение всех заголовков запроса
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        }

        // Получение всех параметров запроса
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            System.out.println(paramName + ": " + paramValue);
        }
        return "Hello";
    }
}
