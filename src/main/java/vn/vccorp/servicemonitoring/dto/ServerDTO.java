package vn.vccorp.servicemonitoring.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerDTO {
    @NotBlank
    @Size(max = 15)
    private String ip;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotBlank
    private String rootPath;
}