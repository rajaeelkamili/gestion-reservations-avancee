package com.example;

import com.example.model.*;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe principale de démonstration du TP5 :
 *   - Recherche de salles disponibles par créneau
 *   - Recherche multi-critères (API Criteria JPA)
 *   - Pagination des résultats
 */
public class App {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations");
        EntityManager em = emf.createEntityManager();

        try {
            SalleRepository salleRepository = new SalleRepositoryImpl(em);
            SalleService salleService = new SalleServiceImpl(em, salleRepository);

            // ----- Initialisation -----
            initializeTestData(em);

            // ----- Test 1 : Disponibilité par créneau -----
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  Test 1 : Salles disponibles par créneau        ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            testAvailableRooms(salleService);

            // ----- Test 2 : Recherche multi-critères -----
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  Test 2 : Recherche multi-critères              ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            testMultiCriteriaSearch(salleService);

            // ----- Test 3 : Pagination -----
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  Test 3 : Pagination                            ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            testPagination(salleService);

        } finally {
            em.close();
            emf.close();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Données de test
    // ─────────────────────────────────────────────────────────────
    private static void initializeTestData(EntityManager em) {
        em.getTransaction().begin();

        // Équipements
        Equipement projecteur       = new Equipement("Projecteur", "Projecteur HD 4K");
        Equipement ecran            = new Equipement("Écran interactif", "Écran tactile 65 pouces");
        Equipement visioconference  = new Equipement("Système de visioconférence", "Caméra HD + micro");
        em.persist(projecteur);
        em.persist(ecran);
        em.persist(visioconference);

        // Utilisateurs
        Utilisateur user1 = new Utilisateur("Dupont", "Jean",   "jean.dupont@example.com");
        Utilisateur user2 = new Utilisateur("Martin", "Sophie", "sophie.martin@example.com");
        em.persist(user1);
        em.persist(user2);

        // Salles
        Salle salle1 = new Salle("Salle A101", 30);
        salle1.setDescription("Salle de réunion standard");
        salle1.setBatiment("Bâtiment A"); salle1.setEtage(1);
        salle1.addEquipement(projecteur);

        Salle salle2 = new Salle("Salle B202", 15);
        salle2.setDescription("Petite salle de réunion");
        salle2.setBatiment("Bâtiment B"); salle2.setEtage(2);
        salle2.addEquipement(ecran);

        Salle salle3 = new Salle("Salle C303", 50);
        salle3.setDescription("Grande salle de conférence");
        salle3.setBatiment("Bâtiment C"); salle3.setEtage(3);
        salle3.addEquipement(projecteur); salle3.addEquipement(visioconference);

        Salle salle4 = new Salle("Salle A202", 20);
        salle4.setDescription("Salle de formation");
        salle4.setBatiment("Bâtiment A"); salle4.setEtage(2);
        salle4.addEquipement(projecteur); salle4.addEquipement(ecran);

        Salle salle5 = new Salle("Salle B303", 40);
        salle5.setDescription("Salle polyvalente");
        salle5.setBatiment("Bâtiment B"); salle5.setEtage(3);
        salle5.addEquipement(visioconference);

        em.persist(salle1); em.persist(salle2); em.persist(salle3);
        em.persist(salle4); em.persist(salle5);

        // Réservations
        LocalDateTime now = LocalDateTime.now();

        Reservation res1 = new Reservation(
            now.plusDays(1).withHour(9).withMinute(0).withSecond(0),
            now.plusDays(1).withHour(11).withMinute(0).withSecond(0),
            "Réunion d'équipe"
        );
        res1.setUtilisateur(user1); res1.setSalle(salle1);

        Reservation res2 = new Reservation(
            now.plusDays(2).withHour(14).withMinute(0).withSecond(0),
            now.plusDays(2).withHour(16).withMinute(0).withSecond(0),
            "Entretien"
        );
        res2.setUtilisateur(user2); res2.setSalle(salle2);

        Reservation res3 = new Reservation(
            now.plusDays(3).withHour(10).withMinute(0).withSecond(0),
            now.plusDays(3).withHour(12).withMinute(0).withSecond(0),
            "Présentation client"
        );
        res3.setUtilisateur(user1); res3.setSalle(salle3);

        em.persist(res1); em.persist(res2); em.persist(res3);
        em.getTransaction().commit();
        System.out.println("\n✔ Données de test initialisées : 5 salles, 3 équipements, 2 utilisateurs, 3 réservations.");
    }

    // ─────────────────────────────────────────────────────────────
    //  Test 1 – Disponibilité
    // ─────────────────────────────────────────────────────────────
    private static void testAvailableRooms(SalleService salleService) {
        LocalDateTime now = LocalDateTime.now();

        // Créneau occupé pour salle1
        LocalDateTime s1 = now.plusDays(1).withHour(9).withMinute(0).withSecond(0);
        LocalDateTime e1 = now.plusDays(1).withHour(11).withMinute(0).withSecond(0);
        afficherDisponibles(salleService, s1, e1, "salle A101 réservée → 4 salles attendues");

        // Créneau libre pour toutes les salles
        LocalDateTime s2 = now.plusDays(5).withHour(14).withMinute(0).withSecond(0);
        LocalDateTime e2 = now.plusDays(5).withHour(16).withMinute(0).withSecond(0);
        afficherDisponibles(salleService, s2, e2, "aucune réservation → 5 salles attendues");
    }

    private static void afficherDisponibles(SalleService svc,
                                            LocalDateTime start, LocalDateTime end,
                                            String commentaire) {
        System.out.println("\nCréneau " + start.format(FMT) + " → " + end.format(FMT));
        System.out.println("(" + commentaire + ")");
        List<Salle> result = svc.findAvailableRooms(start, end);
        if (result.isEmpty()) {
            System.out.println("  Aucune salle disponible.");
        } else {
            result.forEach(s -> System.out.printf(
                "  ✔ %-15s  capacité: %2d  bâtiment: %s%n",
                s.getNom(), s.getCapacite(), s.getBatiment()
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Test 2 – Multi-critères
    // ─────────────────────────────────────────────────────────────
    private static void testMultiCriteriaSearch(SalleService salleService) {

        // Capacité >= 30
        Map<String, Object> c1 = new HashMap<>();
        c1.put("capaciteMin", 30);
        afficherRecherche(salleService, c1, "Capacité >= 30");

        // Bâtiment A uniquement
        Map<String, Object> c2 = new HashMap<>();
        c2.put("batiment", "Bâtiment A");
        afficherRecherche(salleService, c2, "Bâtiment A");

        // Capacité entre 20 et 40, étage 2
        Map<String, Object> c3 = new HashMap<>();
        c3.put("capaciteMin", 20);
        c3.put("capaciteMax", 40);
        c3.put("etage", 2);
        afficherRecherche(salleService, c3, "Capacité 20-40 ET étage 2");

        // Recherche par nom partiel
        Map<String, Object> c4 = new HashMap<>();
        c4.put("nom", "B");
        afficherRecherche(salleService, c4, "Nom contient 'B'");
    }

    private static void afficherRecherche(SalleService svc,
                                          Map<String, Object> criteria,
                                          String label) {
        System.out.println("\nRecherche : " + label);
        List<Salle> result = svc.searchRooms(criteria);
        if (result.isEmpty()) {
            System.out.println("  Aucun résultat.");
        } else {
            result.forEach(s -> System.out.printf(
                "  → %-15s  cap: %2d  bât: %-12s  étage: %d%n",
                s.getNom(), s.getCapacite(), s.getBatiment(), s.getEtage()
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Test 3 – Pagination
    // ─────────────────────────────────────────────────────────────
    private static void testPagination(SalleService salleService) {
        int pageSize = 2;
        int totalPages = salleService.getTotalPages(pageSize);
        System.out.println("\nTaille de page : " + pageSize);
        System.out.println("Nombre total de pages : " + totalPages);

        for (int page = 1; page <= totalPages; page++) {
            System.out.println("\n--- Page " + page + "/" + totalPages + " ---");
            salleService.getPaginatedRooms(page, pageSize).forEach(s ->
                System.out.printf("  %-15s  cap: %2d  bât: %s%n",
                    s.getNom(), s.getCapacite(), s.getBatiment())
            );
        }

        // Objet PaginationResult sur la page 1
        long totalItems = salleService.getAllRooms().size();
        List<Salle> firstPage = salleService.getPaginatedRooms(1, pageSize);
        PaginationResult<Salle> pr = new PaginationResult<>(firstPage, 1, pageSize, totalItems);

        System.out.println("\nMétadonnées de pagination (page 1) :");
        System.out.println("  " + pr);
        System.out.println("  hasNext()     : " + pr.hasNext());
        System.out.println("  hasPrevious() : " + pr.hasPrevious());
    }
}
