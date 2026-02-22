package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    private Long memberId;
    private String name;
    private String email;
    private String role;
}

