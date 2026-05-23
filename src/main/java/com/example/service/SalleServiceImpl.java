package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SalleServiceImpl implements SalleService {

    private final EntityManager entityManager;
    private final SalleRepository salleRepository;

    public SalleServiceImpl(EntityManager entityManager, SalleRepository salleRepository) {
        this.entityManager = entityManager;
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("Les dates ne peuvent pas être nulles.");
        if (!end.isAfter(start)) throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        return salleRepository.findAvailableRooms(start, end);
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return salleRepository.findAll();
        }
        return salleRepository.findByCriteria(criteria);
    }

    @Override
    public List<Salle> getPaginatedRooms(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        return salleRepository.findAllPaginated(page, size);
    }

    @Override
    public int getTotalPages(int size) {
        if (size < 1) size = 10;
        long count = salleRepository.count();
        return (int) Math.ceil((double) count / size);
    }

    @Override
    public Salle getRoomById(Long id) {
        return salleRepository.findById(id);
    }

    @Override
    public List<Salle> getAllRooms() {
        return salleRepository.findAll();
    }

    @Override
    public void saveRoom(Salle salle) {
        executeInTransaction(() -> salleRepository.save(salle));
    }

    @Override
    public void updateRoom(Salle salle) {
        executeInTransaction(() -> salleRepository.update(salle));
    }

    @Override
    public void deleteRoom(Salle salle) {
        executeInTransaction(() -> salleRepository.delete(salle));
    }

    /** Utilitaire pour encapsuler les appels dans une transaction. */
    private void executeInTransaction(Runnable action) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            action.run();
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
    }
}
