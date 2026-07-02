package pe.edu.vallegrande.student.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio pura (sin anotaciones de framework).
 * Cambia los campos aqui si tu recurso no es "Student" (ver docs/CUSTOMIZE.md).
 */
public class Student {

    private Long id;
    private String dni;
    private String firstName;
    private String lastName;
    private Integer promotion;
    private LocalDateTime date;

    public Student() {
    }

    public Student(Long id, String dni, String firstName, String lastName, Integer promotion, LocalDateTime date) {
        this.id = id;
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.promotion = promotion;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
