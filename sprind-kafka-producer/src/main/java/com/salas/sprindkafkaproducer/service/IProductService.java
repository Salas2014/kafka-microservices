package com.salas.sprindkafkaproducer.service;


import com.salas.sprindkafkaproducer.service.dto.CreateProductDto;

public interface IProductService {

    String createProduct(CreateProductDto dto);
}
