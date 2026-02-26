package com.example.acacia.dto;

import com.example.acacia.enums.ResponseStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Response<D> {
    private ResponseStatusEnum status;
    private D data;
    private String message;
    private MetaData metaData;
}
