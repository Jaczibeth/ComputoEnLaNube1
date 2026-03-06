package com.jaczibeth.alumnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaczibeth.alumnos.model.Alumno;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

}
