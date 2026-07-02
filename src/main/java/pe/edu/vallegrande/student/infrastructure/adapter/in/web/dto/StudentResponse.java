package pe.edu.vallegrande.student.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida. Copia exacta del contrato pedido:
 * dni, firstName, lastName, promotion, date.
 */
public class StudentResponse {

    private String dni;
    private String firstName;
    private String lastName;
    private Integer promotion;
    private LocalDateTime date;

    public StudentResponse() {
    }

    public StudentResponse(String dni, String firstName, String lastName, Integer promotion, LocalDateTime date) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.promotion = promotion;
        this.date = date;
    }

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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
