package com.example.service;

import com.example.model.Salle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalleService {

    /** Salles disponibles pour un créneau donné */
    List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end);

    /** Recherche multi-critères */
    List<Salle> searchRooms(Map<String, Object> criteria);

    /** Listing paginé */
    List<Salle> getPaginatedRooms(int page, int size);

    /** Nombre total de pages pour une taille donnée */
    int getTotalPages(int size);

    // CRUD
    Salle getRoomById(Long id);
    List<Salle> getAllRooms();
    void saveRoom(Salle salle);
    void updateRoom(Salle salle);
    void deleteRoom(Salle salle);
}
