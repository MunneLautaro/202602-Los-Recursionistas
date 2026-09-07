package unq.losrecursionistas.backend.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String username, @NotBlank String apiKey) {
}