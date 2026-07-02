package pe.edu.vallegrande.student.infrastructure.config;

import pe.edu.vallegrande.student.domain.model.Student;
import pe.edu.vallegrande.student.domain.port.out.StudentRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * Carga un registro inicial en H2/MySQL al arrancar, para que
 * GET /v1/api/student nunca devuelva vacio en la demo.
 *
 * CAMBIA estos valores por tus datos (ver docs/CUSTOMIZE.md).
 */
@Configuration
public class DataSeeder implements CommandLineRunner {

    private final StudentRepositoryPort repository;

    public DataSeeder(StudentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (!repository.existsByDni("87654321")) {
            Student student = new Student();
            student.setDni("87654321");
            student.setFirstName("tus nombres");
            student.setLastName("tus apellidos");
            student.setPromotion(232);
            student.setDate(LocalDateTime.now());
            repository.save(student);
        }
    }
}
