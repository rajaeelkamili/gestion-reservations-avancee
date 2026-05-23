package com.example.repository;

import com.example.model.Salle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalleRepository {

    /** Trouver les salles disponibles pour un créneau horaire donné */
    List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end);

    /** Recherche multi-critères dynamique */
    List<Salle> findByCriteria(Map<String, Object> criteria);

    /** Liste paginée des salles */
    List<Salle> findAllPaginated(int page, int size);

    /** Nombre total de salles (pour la pagination) */
    long count();

    // CRUD de base
    Salle findById(Long id);
    List<Salle> findAll();
    void save(Salle salle);
    void update(Salle salle);
    void delete(Salle salle);
}
