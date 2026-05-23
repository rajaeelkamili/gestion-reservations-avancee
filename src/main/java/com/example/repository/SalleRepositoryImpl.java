package com.example.repository;

import com.example.model.Salle;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalleRepositoryImpl implements SalleRepository {

    private EntityManager entityManager;

    public SalleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Trouve les salles NON réservées sur le créneau [start, end].
     * Logique : une salle est occupée si une réservation existante chevauche
     * l'intervalle demandé, i.e. r.dateDebut < end ET r.dateFin > start.
     */
    @Override
    public List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end) {
        String jpql = "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN " +
                      "(SELECT r.salle.id FROM Reservation r " +
                      " WHERE r.dateDebut < :end AND r.dateFin > :start)";

        return entityManager.createQuery(jpql, Salle.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    /**
     * Recherche multi-critères via l'API Criteria JPA.
     * Critères supportés :
     *   - nom         (String)  : recherche partielle (LIKE)
     *   - capaciteMin (Integer) : capacité >= valeur
     *   - capaciteMax (Integer) : capacité <= valeur
     *   - batiment    (String)  : égalité exacte
     *   - etage       (Integer) : égalité exacte
     *   - equipement  (Long)    : ID de l'équipement possédé
     */
    @Override
    public List<Salle> findByCriteria(Map<String, Object> criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> query = cb.createQuery(Salle.class);
        Root<Salle> salle = query.from(Salle.class);

        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "nom":
                    predicates.add(cb.like(
                        cb.lower(salle.get("nom")),
                        "%" + value.toString().toLowerCase() + "%"
                    ));
                    break;
                case "capaciteMin":
                    predicates.add(cb.greaterThanOrEqualTo(salle.get("capacite"), (Integer) value));
                    break;
                case "capaciteMax":
                    predicates.add(cb.lessThanOrEqualTo(salle.get("capacite"), (Integer) value));
                    break;
                case "batiment":
                    predicates.add(cb.equal(salle.get("batiment"), value));
                    break;
                case "etage":
                    predicates.add(cb.equal(salle.get("etage"), value));
                    break;
                case "equipement":
                    // Jointure avec la table des équipements
                    Join<Object, Object> equipements = salle.join("equipements");
                    predicates.add(cb.equal(equipements.get("id"), value));
                    query.distinct(true);
                    break;
                default:
                    System.out.println("[WARN] Critère inconnu ignoré : " + key);
            }
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(cb.asc(salle.get("nom")));
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Récupère une page de salles (1-indexed).
     * page=1, size=2 → éléments 0 et 1.
     */
    @Override
    public List<Salle> findAllPaginated(int page, int size) {
        if (page < 1) page = 1;
        return entityManager.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                .getSingleResult();
    }

    @Override
    public Salle findById(Long id) {
        return entityManager.find(Salle.class, id);
    }

    @Override
    public List<Salle> findAll() {
        return entityManager.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                .getResultList();
    }

    @Override
    public void save(Salle salle) {
        entityManager.persist(salle);
    }

    @Override
    public void update(Salle salle) {
        entityManager.merge(salle);
    }

    @Override
    public void delete(Salle salle) {
        if (!entityManager.contains(salle)) {
            salle = entityManager.merge(salle);
        }
        entityManager.remove(salle);
    }
}
