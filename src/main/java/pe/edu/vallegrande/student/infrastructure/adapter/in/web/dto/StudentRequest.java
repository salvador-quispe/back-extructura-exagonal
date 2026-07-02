package pe.edu.vallegrande.student.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada (request body) para POST/PUT.
 * Cambia validaciones o agrega campos aqui sin tocar el dominio.
 */
public class StudentRequest {

    @NotBlank(message = "dni es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "dni debe tener 8 digitos")
    private String dni;

    @NotBlank(message = "firstName es obligatorio")
    private String firstName;

    @NotBlank(message = "lastName es obligatorio")
    private String lastName;

    @NotNull(message = "promotion es obligatorio")
    private Integer promotion;

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getPromotion() {
        return promotion;
    }

    public void setPromotion(Integer promotion) {
        this.promotion = promotion;
    }
}
