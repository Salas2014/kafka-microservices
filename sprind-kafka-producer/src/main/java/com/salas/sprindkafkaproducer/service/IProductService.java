package com.salas.sprindkafkaproducer.service;


import com.salas.sprindkafkaproducer.service.dto.CreateProductDto;

import java.util.concurrent.ExecutionException;

public interface IProductService {

    String createProduct(CreateProductDto dto) throws ExecutionException, InterruptedException;
}
