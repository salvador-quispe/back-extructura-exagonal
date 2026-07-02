package pe.edu.vallegrande.student.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByDni(String dni);

    boolean existsByDni(String dni);

    void deleteByDni(String dni);
}
