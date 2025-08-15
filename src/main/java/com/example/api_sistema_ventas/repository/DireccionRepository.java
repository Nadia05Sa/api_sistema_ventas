package com.example.api_sistema_ventas.repository;

import com.example.api_sistema_ventas.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {

}
