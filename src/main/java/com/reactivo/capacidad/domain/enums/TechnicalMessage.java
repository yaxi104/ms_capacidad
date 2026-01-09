package com.reactivo.capacidad.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    INTERNAL_ERROR("500", "Something went wrong, please try again", ""),
    INTERNAL_ERROR_IN_ADAPTERS("PRC501", "Something went wrong in adapters, please try again", ""),
    INVALID_REQUEST("400", "Bad Request, please verify data", ""),
    INVALID_PARAMETERS(INVALID_REQUEST.getCode(), "Bad Parameters, please verify data", ""),
    INVALID_EMAIL("403", "Invalid email, please verify", "email"),
    INVALID_MESSAGE_ID("404", "Invalid Message ID, please verify", "messageId"),
    UNSUPPORTED_OPERATION("501", "Method not supported, please try again", ""),
    TECHNOLOGY_CREATED("201", "Technology created successfully", ""),
    ADAPTER_RESPONSE_NOT_FOUND("404-0", "invalid email, please verify", ""),
    INVALID_CREDENTIALS("401", "Credenciales invalidas", ""),
    CAPACITY_ALREADY_EXISTS("400", "La capacidad ya está registrada", ""),
    CLIENTE_TECHNOLOGY_FAILED("404", "Error al guardar las tecnologías asociadas a la capacidad", "");


    private final String code;
    private final String message;
    private final String param;
}