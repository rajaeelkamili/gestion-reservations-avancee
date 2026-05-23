# TP5 – Requêtes avancées JPA/Hibernate : Gestion des Réservations

**ENSA El Jadida – 2025/2026 | Rajae Elkamili**

## Objectifs

- Implémenter une requête JPQL pour trouver les **salles disponibles** par créneau horaire
- Créer une **recherche multi-critères** via l'API Criteria de JPA
- Mettre en place un système de **pagination** des listings

## Technologies

| Composant | Version |
|-----------|---------|
| JDK | 8+ |
| Maven | 3.x |
| Hibernate | 5.6.5.Final |
| H2 Database | 2.1.214 (in-memory) |
| JPA API | 2.2 |

## Structure du projet

```
gestion-reservations-avancee/
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/
        │   ├── App.java                          # Point d'entrée
        │   ├── model/
        │   │   ├── Utilisateur.java
        │   │   ├── Salle.java
        │   │   ├── Reservation.java
        │   │   └── Equipement.java
        │   ├── repository/
        │   │   ├── SalleRepository.java          # Interface
        │   │   └── SalleRepositoryImpl.java      # Implémentation JPQL + Criteria
        │   ├── service/
        │   │   ├── SalleService.java             # Interface
        │   │   └── SalleServiceImpl.java         # Logique métier + transactions
        │   └── util/
        │       └── PaginationResult.java         # Wrapper pagination
        └── resources/
            └── META-INF/
                └── persistence.xml
```

## Lancer le projet

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.App"
```

## Concepts clés

### 1. Disponibilité par créneau (JPQL)

```java
SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN
  (SELECT r.salle.id FROM Reservation r
   WHERE r.dateDebut < :end AND r.dateFin > :start)
```

### 2. Recherche multi-critères (Criteria API)

Construction dynamique de prédicats selon les clés fournies :
`nom`, `capaciteMin`, `capaciteMax`, `batiment`, `etage`, `equipement`

### 3. Pagination (JPA)

```java
query.setFirstResult((page - 1) * size)
     .setMaxResults(size)
```
