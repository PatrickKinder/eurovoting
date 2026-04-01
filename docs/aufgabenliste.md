# Eurovision Voting – Finale Aufgabenliste (alle Fixes eingebaut)

> Alle Änderungen aus der ChatGPT-Review eingearbeitet:  
> ✅ `fromCountryId` entfernt · ✅ NULL-Constraint-Warnung · ✅ Locale-Fix · ✅ DataInitializer ohne Votes

---

## Abhängigkeiten auf einen Blick

```
Person 3  →  muss ZUERST fertig sein
             ↓
Person 1  →  kann starten sobald Person 3 fertig ist
Person 2  →  kann starten sobald Person 3 fertig ist
Person 4  →  kann SOFORT starten (mit Mock)
Person 5  →  kann SOFORT starten (mit Mock)
```

---

## Person 3 – Data & Setup

> **Muss als Erstes fertig sein. Alle anderen warten auf diese Basis.**  
> Fertig = `mvn spring-boot:run` startet ohne Fehler, H2-Konsole zeigt Tabellen mit Daten.

---

### Schritt 1 – pom.xml anlegen

**Datei:** `pom.xml`  
**Inhalt: genau diese 4 Dependencies:**

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

- [ ] Java-Version auf 17 setzen: `<java.version>17</java.version>`
- [ ] Spring Boot Version: `3.2.x`

---

### Schritt 2 – application.properties anlegen

**Datei:** `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:eurovoting
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

server.port=8080
```

- [ ] Datei exakt so anlegen, keine Abweichungen

---

### Schritt 3 – EurovotingApplication.java

**Datei:** `src/main/java/com/eurovoting/EurovotingApplication.java`

```java
package com.eurovoting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EurovotingApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurovotingApplication.class, args);
    }
}
```

- [ ] Datei anlegen, Paket `com.eurovoting`

---

### Schritt 4 – VoterRole Enum

**Datei:** `src/main/java/com/eurovoting/model/VoterRole.java`

```java
package com.eurovoting.model;

public enum VoterRole {
    JURY,
    AUDIENCE
}
```

- [ ] Nur diese zwei Werte, exakt so geschrieben

---

### Schritt 5 – Country Entity

**Datei:** `src/main/java/com/eurovoting/model/Country.java`

```java
package com.eurovoting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "our_country", nullable = false)
    private boolean ourCountry = false;

    @Column(nullable = false)
    private boolean simulated = false;

    @OneToOne(mappedBy = "country", fetch = FetchType.LAZY)
    private Contestant contestant;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isOurCountry() { return ourCountry; }
    public void setOurCountry(boolean ourCountry) { this.ourCountry = ourCountry; }
    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean simulated) { this.simulated = simulated; }
    public Contestant getContestant() { return contestant; }
}
```

- [ ] Exakt diese Feldnamen verwenden
- [ ] `ourCountry` (camelCase) – wird in DB zu `our_country`
- [ ] Getter/Setter für alle Felder

---

### Schritt 6 – Contestant Entity

**Datei:** `src/main/java/com/eurovoting/model/Contestant.java`

```java
package com.eurovoting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contestant")
public class Contestant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "song_title", nullable = false)
    private String songTitle;

    @OneToOne
    @JoinColumn(name = "country_id", nullable = false, unique = true)
    private Country country;

    public Long getId() { return id; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
}
```

- [ ] Exakt diese Feldnamen verwenden
- [ ] `country_id` als FK-Name

---

### Schritt 7 – Vote Entity

**Datei:** `src/main/java/com/eurovoting/model/Vote.java`

```java
package com.eurovoting.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "vote",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_vote_voter_contestant",
            columnNames = {"from_country_id", "to_contestant_id", "voter_role", "jury_member_id"}
        )
    }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_country_id", nullable = false)
    private Country fromCountry;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_contestant_id", nullable = false)
    private Contestant toContestant;

    @Enumerated(EnumType.STRING)
    @Column(name = "voter_role", nullable = false)
    private VoterRole voterRole;

    @Column(nullable = false)
    private int points;

    // ⚠️ WICHTIG: DB-Unique-Constraint greift bei NULL nicht (SQL: NULL != NULL)
    // Duplikat-Prüfung für AUDIENCE läuft ausschließlich im VoteService!
    @Column(name = "jury_member_id")
    private Integer juryMemberId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Country getFromCountry() { return fromCountry; }
    public void setFromCountry(Country fromCountry) { this.fromCountry = fromCountry; }
    public Contestant getToContestant() { return toContestant; }
    public void setToContestant(Contestant toContestant) { this.toContestant = toContestant; }
    public VoterRole getVoterRole() { return voterRole; }
    public void setVoterRole(VoterRole voterRole) { this.voterRole = voterRole; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public Integer getJuryMemberId() { return juryMemberId; }
    public void setJuryMemberId(Integer juryMemberId) { this.juryMemberId = juryMemberId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] Kommentar zu NULL-Constraint muss drin bleiben (als Dokumentation für Person 1)
- [ ] `jury_member_id` (snake_case in DB, `juryMemberId` in Java)

---

### Schritt 8 – CountryRepository

**Datei:** `src/main/java/com/eurovoting/repository/CountryRepository.java`

```java
package com.eurovoting.repository;

import com.eurovoting.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByOurCountryTrue();
    List<Country> findBySimulatedTrue();
}
```

- [ ] `findByOurCountryTrue()` – liefert das eine eigene Land
- [ ] `findBySimulatedTrue()` – liefert alle simulierten Länder

---

### Schritt 9 – ContestantRepository

**Datei:** `src/main/java/com/eurovoting/repository/ContestantRepository.java`

```java
package com.eurovoting.repository;

import com.eurovoting.model.Contestant;
import com.eurovoting.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestantRepository extends JpaRepository<Contestant, Long> {
}
```

- [ ] Kein Custom Query nötig – `findAll()` reicht

---

### Schritt 10 – VoteRepository

**Datei:** `src/main/java/com/eurovoting/repository/VoteRepository.java`

```java
package com.eurovoting.repository;

import com.eurovoting.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Prüft ob dieser Voter den Punktwert schon vergeben hat
    boolean existsByFromCountryAndVoterRoleAndJuryMemberIdAndPoints(
        Country fromCountry,
        VoterRole voterRole,
        Integer juryMemberId,
        int points
    );

    // Prüft ob dieser Voter diesen Contestant schon bewertet hat
    boolean existsByFromCountryAndVoterRoleAndJuryMemberIdAndToContestant(
        Country fromCountry,
        VoterRole voterRole,
        Integer juryMemberId,
        Contestant toContestant
    );

    // Summiert alle Punkte für einen Contestant nach Rolle
    @Query("""
        SELECT COALESCE(SUM(v.points), 0)
        FROM Vote v
        WHERE v.toContestant = :contestant
          AND v.voterRole = :role
    """)
    int sumPointsByContestantAndRole(
        @Param("contestant") Contestant contestant,
        @Param("role") VoterRole role
    );

    // Zählt Votes eines Voters (für Status-Endpoint)
    @Query("""
        SELECT COUNT(v)
        FROM Vote v
        WHERE v.fromCountry = :country
          AND v.voterRole = :role
          AND v.juryMemberId = :memberId
    """)
    long countVotesByVoter(
        @Param("country") Country country,
        @Param("role") VoterRole role,
        @Param("memberId") Integer memberId
    );
}
```

- [ ] Alle 4 Methoden exakt so benennen – Person 1 referenziert diese Namen
- [ ] JPQL (keine native SQL Queries)

---

### Schritt 11 – ContestantResponse DTO

**Datei:** `src/main/java/com/eurovoting/dto/ContestantResponse.java`

```java
package com.eurovoting.dto;

public class ContestantResponse {
    private Long id;
    private String artistName;
    private String songTitle;
    private String countryName;
    private String countryCode;

    public ContestantResponse(Long id, String artistName, String songTitle,
                               String countryName, String countryCode) {
        this.id = id;
        this.artistName = artistName;
        this.songTitle = songTitle;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public Long getId() { return id; }
    public String getArtistName() { return artistName; }
    public String getSongTitle() { return songTitle; }
    public String getCountryName() { return countryName; }
    public String getCountryCode() { return countryCode; }
}
```

- [ ] Feldnamen exakt so (Frontend nutzt `artistName`, `songTitle`, `countryName`, `countryCode`)

---

### Schritt 12 – ContestantController

**Datei:** `src/main/java/com/eurovoting/controller/ContestantController.java`

```java
package com.eurovoting.controller;

import com.eurovoting.dto.ContestantResponse;
import com.eurovoting.repository.ContestantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contestants")
@CrossOrigin(origins = "*")
public class ContestantController {

    private final ContestantRepository contestantRepository;

    public ContestantController(ContestantRepository contestantRepository) {
        this.contestantRepository = contestantRepository;
    }

    @GetMapping
    public ResponseEntity<List<ContestantResponse>> getAllContestants() {
        List<ContestantResponse> response = contestantRepository.findAll().stream()
            .map(c -> new ContestantResponse(
                c.getId(),
                c.getArtistName(),
                c.getSongTitle(),
                c.getCountry().getName(),
                c.getCountry().getCode()
            ))
            .toList();
        return ResponseEntity.ok(response);
    }
}
```

- [ ] Endpoint: `GET /api/contestants`
- [ ] Liefert ALLE Contestants (inkl. eigenes Land – Frontend filtert selbst)
- [ ] `@CrossOrigin(origins = "*")` muss gesetzt sein

---

### Schritt 13 – DataInitializer

**Datei:** `src/main/java/com/eurovoting/config/DataInitializer.java`

```java
package com.eurovoting.config;

import com.eurovoting.model.*;
import com.eurovoting.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;
    private final ContestantRepository contestantRepository;

    public DataInitializer(CountryRepository countryRepository,
                           ContestantRepository contestantRepository) {
        this.countryRepository = countryRepository;
        this.contestantRepository = contestantRepository;
    }

    @Override
    public void run(String... args) {
        if (countryRepository.count() > 0) return; // Idempotent

        // ⚠️ KEIN VOTES hier anlegen – nur Länder + Contestants
        // Votes kommen über POST /api/votes (echte User) oder manuell

        // Unser Land (ourCountry = true, simulated = false)
        Country germany = createCountry("Germany", "DEU", true, false);
        createContestant("Max Mustermann", "Fire and Ice", germany);

        // Simulierte Länder (ourCountry = false, simulated = true)
        Country france = createCountry("France", "FRA", false, true);
        createContestant("Celine Marie", "Blue Horizon", france);

        Country sweden = createCountry("Sweden", "SWE", false, true);
        createContestant("Erik Lindqvist", "Northern Lights", sweden);

        Country italy = createCountry("Italy", "ITA", false, true);
        createContestant("Sofia Romano", "Bella Notte", italy);

        Country portugal = createCountry("Portugal", "PRT", false, true);
        createContestant("Ana Sousa", "Mar Salgado", portugal);

        Country spain = createCountry("Spain", "ESP", false, true);
        createContestant("Carlos Vega", "Fuego", spain);

        Country norway = createCountry("Norway", "NOR", false, true);
        createContestant("Ingrid Berg", "Aurora", norway);

        Country ukraine = createCountry("Ukraine", "UKR", false, true);
        createContestant("Olena Koval", "Zoria", ukraine);

        Country greece = createCountry("Greece", "GRC", false, true);
        createContestant("Nikos Papadopoulos", "Thalassa", greece);

        Country finland = createCountry("Finland", "FIN", false, true);
        createContestant("Aino Mäkinen", "Revontulet", finland);

        Country australia = createCountry("Australia", "AUS", false, true);
        createContestant("Lily Chen", "Southern Cross", australia);
    }

    private Country createCountry(String name, String code,
                                   boolean ourCountry, boolean simulated) {
        Country c = new Country();
        c.setName(name);
        c.setCode(code);
        c.setOurCountry(ourCountry);
        c.setSimulated(simulated);
        return countryRepository.save(c);
    }

    private void createContestant(String artist, String song, Country country) {
        Contestant c = new Contestant();
        c.setArtistName(artist);
        c.setSongTitle(song);
        c.setCountry(country);
        contestantRepository.save(c);
    }
}
```

- [ ] Exakt **ein Land** mit `ourCountry = true` (Germany)
- [ ] **Keine Votes** anlegen – die kommen über die API
- [ ] Mindestens 11 Länder (damit Voting-Set mit 10 Punkte möglich ist)
- [ ] `if (countryRepository.count() > 0) return;` – Idempotent

---

### Abnahme Person 3

```
[ ] mvn spring-boot:run → kein Fehler in der Konsole
[ ] http://localhost:8080/h2-console → Tabellen sichtbar
[ ] http://localhost:8080/api/contestants → JSON mit allen Contestants
[ ] JSON enthält Felder: id, artistName, songTitle, countryName, countryCode
[ ] Germany hat ourCountry=true in der DB
[ ] Kein einziger Vote in der DB
```

---

---

## Person 1 – Vote Backend

> **Startet nach Person 3.** Nutzt deren Entities + Repositories.

---

### Schritt 1 – Exceptions anlegen

**Datei:** `src/main/java/com/eurovoting/exception/VotingException.java`

```java
package com.eurovoting.exception;

import org.springframework.http.HttpStatus;

public class VotingException extends RuntimeException {

    private final HttpStatus status;

    // Für Business-Fehler (Duplikat, Self-Voting) → 409
    public VotingException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }

    // Für Validierungsfehler → 400
    public VotingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
```

**Datei:** `src/main/java/com/eurovoting/exception/NotFoundException.java`

```java
package com.eurovoting.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
```

- [ ] Beide Klassen anlegen bevor der Service gebaut wird

---

### Schritt 2 – GlobalExceptionHandler

**Datei:** `src/main/java/com/eurovoting/exception/GlobalExceptionHandler.java`

```java
package com.eurovoting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VotingException.class)
    public ResponseEntity<Map<String, Object>> handleVoting(VotingException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .findFirst().orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
```

- [ ] `@RestControllerAdvice` – fängt alle Exceptions aus allen Controllern
- [ ] Fehlerformat exakt: `{ status, error, message, timestamp }`

---

### Schritt 3 – DTOs anlegen

**Datei:** `src/main/java/com/eurovoting/dto/VoteRequest.java`  
⚠️ `fromCountryId` ist NICHT mehr dabei!

```java
package com.eurovoting.dto;

import com.eurovoting.model.VoterRole;
import jakarta.validation.constraints.NotNull;

public class VoteRequest {

    @NotNull(message = "toContestantId is required")
    private Long toContestantId;

    @NotNull(message = "voterRole is required")
    private VoterRole voterRole;

    @NotNull(message = "points is required")
    private Integer points;

    private Integer juryMemberId; // bei JURY: 1-3, bei AUDIENCE: muss null sein

    public Long getToContestantId() { return toContestantId; }
    public void setToContestantId(Long id) { this.toContestantId = id; }
    public VoterRole getVoterRole() { return voterRole; }
    public void setVoterRole(VoterRole r) { this.voterRole = r; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer p) { this.points = p; }
    public Integer getJuryMemberId() { return juryMemberId; }
    public void setJuryMemberId(Integer id) { this.juryMemberId = id; }
}
```

**Datei:** `src/main/java/com/eurovoting/dto/VoteResponse.java`

```java
package com.eurovoting.dto;

import com.eurovoting.model.VoterRole;
import java.time.LocalDateTime;

public class VoteResponse {
    private Long id;
    private String fromCountryName;
    private String toArtistName;
    private String toSongTitle;
    private String toCountryName;
    private VoterRole voterRole;
    private int points;
    private Integer juryMemberId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFromCountryName() { return fromCountryName; }
    public void setFromCountryName(String n) { this.fromCountryName = n; }
    public String getToArtistName() { return toArtistName; }
    public void setToArtistName(String n) { this.toArtistName = n; }
    public String getToSongTitle() { return toSongTitle; }
    public void setToSongTitle(String t) { this.toSongTitle = t; }
    public String getToCountryName() { return toCountryName; }
    public void setToCountryName(String n) { this.toCountryName = n; }
    public VoterRole getVoterRole() { return voterRole; }
    public void setVoterRole(VoterRole r) { this.voterRole = r; }
    public int getPoints() { return points; }
    public void setPoints(int p) { this.points = p; }
    public Integer getJuryMemberId() { return juryMemberId; }
    public void setJuryMemberId(Integer id) { this.juryMemberId = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}
```

**Datei:** `src/main/java/com/eurovoting/dto/VotingStatusResponse.java`

```java
package com.eurovoting.dto;

public class VotingStatusResponse {
    private Long ourCountryId;
    private String ourCountryName;
    private int totalRequiredPerSet;
    private long juryMember1VoteCount;
    private long juryMember2VoteCount;
    private long juryMember3VoteCount;
    private long audienceVoteCount;
    private boolean juryMember1Complete;
    private boolean juryMember2Complete;
    private boolean juryMember3Complete;
    private boolean audienceComplete;

    // Getter & Setter für alle Felder
    public Long getOurCountryId() { return ourCountryId; }
    public void setOurCountryId(Long id) { this.ourCountryId = id; }
    public String getOurCountryName() { return ourCountryName; }
    public void setOurCountryName(String n) { this.ourCountryName = n; }
    public int getTotalRequiredPerSet() { return totalRequiredPerSet; }
    public void setTotalRequiredPerSet(int t) { this.totalRequiredPerSet = t; }
    public long getJuryMember1VoteCount() { return juryMember1VoteCount; }
    public void setJuryMember1VoteCount(long c) { this.juryMember1VoteCount = c; }
    public long getJuryMember2VoteCount() { return juryMember2VoteCount; }
    public void setJuryMember2VoteCount(long c) { this.juryMember2VoteCount = c; }
    public long getJuryMember3VoteCount() { return juryMember3VoteCount; }
    public void setJuryMember3VoteCount(long c) { this.juryMember3VoteCount = c; }
    public long getAudienceVoteCount() { return audienceVoteCount; }
    public void setAudienceVoteCount(long c) { this.audienceVoteCount = c; }
    public boolean isJuryMember1Complete() { return juryMember1Complete; }
    public void setJuryMember1Complete(boolean b) { this.juryMember1Complete = b; }
    public boolean isJuryMember2Complete() { return juryMember2Complete; }
    public void setJuryMember2Complete(boolean b) { this.juryMember2Complete = b; }
    public boolean isJuryMember3Complete() { return juryMember3Complete; }
    public void setJuryMember3Complete(boolean b) { this.juryMember3Complete = b; }
    public boolean isAudienceComplete() { return audienceComplete; }
    public void setAudienceComplete(boolean b) { this.audienceComplete = b; }
}
```

- [ ] Alle 3 DTOs anlegen, Feldnamen exakt wie hier

---

### Schritt 4 – VoteService

**Datei:** `src/main/java/com/eurovoting/service/VoteService.java`

```java
package com.eurovoting.service;

import com.eurovoting.dto.VoteRequest;
import com.eurovoting.dto.VoteResponse;
import com.eurovoting.exception.NotFoundException;
import com.eurovoting.exception.VotingException;
import com.eurovoting.model.*;
import com.eurovoting.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class VoteService {

    private static final Set<Integer> VALID_POINTS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12);

    private final VoteRepository voteRepository;
    private final CountryRepository countryRepository;
    private final ContestantRepository contestantRepository;

    public VoteService(VoteRepository voteRepository,
                       CountryRepository countryRepository,
                       ContestantRepository contestantRepository) {
        this.voteRepository = voteRepository;
        this.countryRepository = countryRepository;
        this.contestantRepository = contestantRepository;
    }

    @Transactional
    public VoteResponse castVote(VoteRequest request) {
        // fromCountry wird automatisch ermittelt – nicht vom Frontend übergeben
        Country fromCountry = countryRepository.findByOurCountryTrue()
            .orElseThrow(() -> new VotingException(
                "Our country is not configured.", HttpStatus.INTERNAL_SERVER_ERROR));

        Contestant toContestant = contestantRepository.findById(request.getToContestantId())
            .orElseThrow(() -> new NotFoundException(
                "Contestant not found with id: " + request.getToContestantId()));

        // Validierungen – Reihenfolge einhalten
        validatePoints(request.getPoints());
        validateNoSelfVoting(fromCountry, toContestant);
        validateJuryMemberId(request);
        validateNoDuplicatePoints(request, fromCountry);
        validateNoDuplicateContestant(request, fromCountry, toContestant);

        Vote vote = new Vote();
        vote.setFromCountry(fromCountry);
        vote.setToContestant(toContestant);
        vote.setVoterRole(request.getVoterRole());
        vote.setPoints(request.getPoints());
        vote.setJuryMemberId(request.getJuryMemberId());

        Vote saved = voteRepository.save(vote);
        return toResponse(saved);
    }

    // --- Validierungen ---

    private void validatePoints(int points) {
        if (!VALID_POINTS.contains(points)) {
            throw new VotingException(
                "Invalid points: " + points + ". Allowed: " + VALID_POINTS,
                HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateNoSelfVoting(Country fromCountry, Contestant toContestant) {
        if (fromCountry.getId().equals(toContestant.getCountry().getId())) {
            throw new VotingException("A country cannot vote for its own contestant.");
        }
    }

    private void validateJuryMemberId(VoteRequest request) {
        if (request.getVoterRole() == VoterRole.JURY) {
            Integer id = request.getJuryMemberId();
            if (id == null || id < 1 || id > 3) {
                throw new VotingException(
                    "JURY votes require juryMemberId between 1 and 3.",
                    HttpStatus.BAD_REQUEST
                );
            }
        } else {
            if (request.getJuryMemberId() != null) {
                throw new VotingException(
                    "AUDIENCE votes must not include a juryMemberId.",
                    HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    // ⚠️ Diese Methode prüft auch AUDIENCE-Duplikate, weil DB-Constraint bei NULL nicht greift
    private void validateNoDuplicatePoints(VoteRequest request, Country fromCountry) {
        boolean exists = voteRepository.existsByFromCountryAndVoterRoleAndJuryMemberIdAndPoints(
            fromCountry,
            request.getVoterRole(),
            request.getJuryMemberId(),
            request.getPoints()
        );
        if (exists) {
            throw new VotingException(
                "Point value " + request.getPoints() + " already assigned by this voter."
            );
        }
    }

    // ⚠️ Diese Methode prüft auch AUDIENCE-Duplikate, weil DB-Constraint bei NULL nicht greift
    private void validateNoDuplicateContestant(VoteRequest request,
                                                Country fromCountry,
                                                Contestant toContestant) {
        boolean exists = voteRepository.existsByFromCountryAndVoterRoleAndJuryMemberIdAndToContestant(
            fromCountry,
            request.getVoterRole(),
            request.getJuryMemberId(),
            toContestant
        );
        if (exists) {
            throw new VotingException(
                "Contestant '" + toContestant.getArtistName() + "' already rated by this voter."
            );
        }
    }

    // --- Mapping ---

    private VoteResponse toResponse(Vote vote) {
        VoteResponse r = new VoteResponse();
        r.setId(vote.getId());
        r.setFromCountryName(vote.getFromCountry().getName());
        r.setToArtistName(vote.getToContestant().getArtistName());
        r.setToSongTitle(vote.getToContestant().getSongTitle());
        r.setToCountryName(vote.getToContestant().getCountry().getName());
        r.setVoterRole(vote.getVoterRole());
        r.setPoints(vote.getPoints());
        r.setJuryMemberId(vote.getJuryMemberId());
        r.setCreatedAt(vote.getCreatedAt());
        return r;
    }
}
```

- [ ] `findByOurCountryTrue()` – kein `fromCountryId` Parameter
- [ ] Validierungsreihenfolge einhalten (points → self → jury-id → duplikat-points → duplikat-contestant)
- [ ] Kommentare zu NULL-Constraint lassen stehen

---

### Schritt 5 – VotingStatusService

**Datei:** `src/main/java/com/eurovoting/service/VotingStatusService.java`

```java
package com.eurovoting.service;

import com.eurovoting.dto.VotingStatusResponse;
import com.eurovoting.exception.VotingException;
import com.eurovoting.model.VoterRole;
import com.eurovoting.repository.CountryRepository;
import com.eurovoting.repository.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingStatusService {

    private static final int REQUIRED_VOTES_PER_SET = 10;

    private final VoteRepository voteRepository;
    private final CountryRepository countryRepository;

    public VotingStatusService(VoteRepository voteRepository,
                                CountryRepository countryRepository) {
        this.voteRepository = voteRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public VotingStatusResponse getStatusForOurCountry() {
        var ourCountry = countryRepository.findByOurCountryTrue()
            .orElseThrow(() -> new VotingException(
                "Our country not configured.", HttpStatus.INTERNAL_SERVER_ERROR));

        long m1 = voteRepository.countVotesByVoter(ourCountry, VoterRole.JURY, 1);
        long m2 = voteRepository.countVotesByVoter(ourCountry, VoterRole.JURY, 2);
        long m3 = voteRepository.countVotesByVoter(ourCountry, VoterRole.JURY, 3);
        long audience = voteRepository.countVotesByVoter(ourCountry, VoterRole.AUDIENCE, null);

        VotingStatusResponse r = new VotingStatusResponse();
        r.setOurCountryId(ourCountry.getId());
        r.setOurCountryName(ourCountry.getName());
        r.setTotalRequiredPerSet(REQUIRED_VOTES_PER_SET);
        r.setJuryMember1VoteCount(m1);
        r.setJuryMember2VoteCount(m2);
        r.setJuryMember3VoteCount(m3);
        r.setAudienceVoteCount(audience);
        r.setJuryMember1Complete(m1 >= REQUIRED_VOTES_PER_SET);
        r.setJuryMember2Complete(m2 >= REQUIRED_VOTES_PER_SET);
        r.setJuryMember3Complete(m3 >= REQUIRED_VOTES_PER_SET);
        r.setAudienceComplete(audience >= REQUIRED_VOTES_PER_SET);
        return r;
    }
}
```

- [ ] `REQUIRED_VOTES_PER_SET = 10` als Konstante
- [ ] Berechnet `complete`-Flags selbst aus den Counts

---

### Schritt 6 – VoteController

**Datei:** `src/main/java/com/eurovoting/controller/VoteController.java`

```java
package com.eurovoting.controller;

import com.eurovoting.dto.VoteRequest;
import com.eurovoting.dto.VoteResponse;
import com.eurovoting.service.VoteService;
import com.eurovoting.service.VotingStatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
@CrossOrigin(origins = "*")
public class VoteController {

    private final VoteService voteService;
    private final VotingStatusService votingStatusService;

    public VoteController(VoteService voteService,
                          VotingStatusService votingStatusService) {
        this.voteService = voteService;
        this.votingStatusService = votingStatusService;
    }

    @PostMapping
    public ResponseEntity<VoteResponse> castVote(@Valid @RequestBody VoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voteService.castVote(request));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(votingStatusService.getStatusForOurCountry());
    }
}
```

- [ ] Controller hat KEINE Logik – nur Delegation an Services
- [ ] `@Valid` auf `castVote` – löst Bean-Validierung aus

---

### Abnahme Person 1

```
[ ] POST /api/votes mit gültigem JURY-Vote → 201 + VoteResponse JSON
[ ] POST /api/votes mit gültigem AUDIENCE-Vote (juryMemberId: null) → 201
[ ] POST /api/votes mit Punktwert 9 → 400 + Fehlermeldung
[ ] POST /api/votes für eigenen Contestant → 409 + "cannot vote for own"
[ ] POST /api/votes Punktwert nochmal → 409 + "already assigned"
[ ] POST /api/votes JURY ohne juryMemberId → 400
[ ] POST /api/votes AUDIENCE mit juryMemberId: 2 → 400
[ ] GET /api/votes/status → JSON mit allen Feldern korrekt
```

---

---

## Person 2 – Result Backend

> **Startet nach Person 3.** Nur `ResultService` + `ResultController` + 1 DTO.

---

### Schritt 1 – RankingEntry DTO

**Datei:** `src/main/java/com/eurovoting/dto/RankingEntry.java`

```java
package com.eurovoting.dto;

public class RankingEntry {
    private int rank;
    private Long contestantId;
    private String artistName;
    private String songTitle;
    private String countryName;
    private String countryCode;
    private int totalPoints;
    private int juryPoints;
    private int audiencePoints;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getContestantId() { return contestantId; }
    public void setContestantId(Long id) { this.contestantId = id; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String n) { this.artistName = n; }
    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String t) { this.songTitle = t; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String n) { this.countryName = n; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String c) { this.countryCode = c; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int p) { this.totalPoints = p; }
    public int getJuryPoints() { return juryPoints; }
    public void setJuryPoints(int p) { this.juryPoints = p; }
    public int getAudiencePoints() { return audiencePoints; }
    public void setAudiencePoints(int p) { this.audiencePoints = p; }
}
```

- [ ] Feldnamen exakt wie hier – Frontend referenziert `totalPoints`, `juryPoints`, `audiencePoints`

---

### Schritt 2 – ResultService

**Datei:** `src/main/java/com/eurovoting/service/ResultService.java`

```java
package com.eurovoting.service;

import com.eurovoting.dto.RankingEntry;
import com.eurovoting.model.Contestant;
import com.eurovoting.model.VoterRole;
import com.eurovoting.repository.ContestantRepository;
import com.eurovoting.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ResultService {

    private final VoteRepository voteRepository;
    private final ContestantRepository contestantRepository;

    public ResultService(VoteRepository voteRepository,
                         ContestantRepository contestantRepository) {
        this.voteRepository = voteRepository;
        this.contestantRepository = contestantRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingEntry> calculateRanking() {
        List<RankingEntry> ranking = contestantRepository.findAll().stream()
            .map(this::toRankingEntry)
            .sorted(
                Comparator.comparingInt(RankingEntry::getTotalPoints).reversed()
                    // ✅ Fix: Locale.ROOT verhindert Groß/Kleinschreibungs-Bugs
                    .thenComparing(e -> e.getCountryName().toLowerCase(Locale.ROOT))
            )
            .collect(Collectors.toList());

        // Rang zuweisen (1-basiert, keine Lücken)
        IntStream.range(0, ranking.size())
            .forEach(i -> ranking.get(i).setRank(i + 1));

        return ranking;
    }

    private RankingEntry toRankingEntry(Contestant contestant) {
        int jury = voteRepository.sumPointsByContestantAndRole(contestant, VoterRole.JURY);
        int audience = voteRepository.sumPointsByContestantAndRole(contestant, VoterRole.AUDIENCE);

        RankingEntry e = new RankingEntry();
        e.setContestantId(contestant.getId());
        e.setArtistName(contestant.getArtistName());
        e.setSongTitle(contestant.getSongTitle());
        e.setCountryName(contestant.getCountry().getName());
        e.setCountryCode(contestant.getCountry().getCode());
        e.setJuryPoints(jury);
        e.setAudiencePoints(audience);
        e.setTotalPoints(jury + audience); // immer: total = jury + audience
        return e;
    }
}
```

- [ ] `Locale.ROOT` beim Tie-Break (Fix aus ChatGPT-Review)
- [ ] `totalPoints = juryPoints + audiencePoints` immer berechnet, nie aus DB
- [ ] Rang erst nach dem Sortieren setzen

---

### Schritt 3 – ResultController

**Datei:** `src/main/java/com/eurovoting/controller/ResultController.java`

```java
package com.eurovoting.controller;

import com.eurovoting.dto.RankingEntry;
import com.eurovoting.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public ResponseEntity<List<RankingEntry>> getResults() {
        return ResponseEntity.ok(resultService.calculateRanking());
    }
}
```

- [ ] Nur 1 Endpoint, 1 Methode, keine Logik im Controller

---

### Abnahme Person 2

```
[ ] GET /api/results → JSON-Array mit allen Contestants
[ ] Alle Felder vorhanden: rank, contestantId, artistName, songTitle,
    countryName, countryCode, totalPoints, juryPoints, audiencePoints
[ ] totalPoints = juryPoints + audiencePoints bei jedem Eintrag
[ ] Sortiert nach totalPoints DESC
[ ] Bei 0 Votes: alle Contestants mit 0 Punkten, aber trotzdem sortiert
[ ] Germany (eigenes Land) ist auch im Ranking (mit 0 Punkten falls kein Vote)
```

---

---

## Person 4 – Frontend Voting

> **Kann sofort starten** – nutzt Mock-Daten. Schaltet auf echtes Backend sobald Person 1 fertig ist.

---

### Schritt 1 – Mock-Daten anlegen

**Datei:** `src/main/resources/static/js/mock-contestants.json`

```json
[
  { "id": 1, "artistName": "Max Mustermann", "songTitle": "Fire and Ice", "countryName": "Germany", "countryCode": "DEU" },
  { "id": 2, "artistName": "Celine Marie", "songTitle": "Blue Horizon", "countryName": "France", "countryCode": "FRA" },
  { "id": 3, "artistName": "Erik Lindqvist", "songTitle": "Northern Lights", "countryName": "Sweden", "countryCode": "SWE" },
  { "id": 4, "artistName": "Sofia Romano", "songTitle": "Bella Notte", "countryName": "Italy", "countryCode": "ITA" },
  { "id": 5, "artistName": "Ana Sousa", "songTitle": "Mar Salgado", "countryName": "Portugal", "countryCode": "PRT" },
  { "id": 6, "artistName": "Carlos Vega", "songTitle": "Fuego", "countryName": "Spain", "countryCode": "ESP" },
  { "id": 7, "artistName": "Ingrid Berg", "songTitle": "Aurora", "countryName": "Norway", "countryCode": "NOR" },
  { "id": 8, "artistName": "Olena Koval", "songTitle": "Zoria", "countryName": "Ukraine", "countryCode": "UKR" },
  { "id": 9, "artistName": "Nikos Papadopoulos", "songTitle": "Thalassa", "countryName": "Greece", "countryCode": "GRC" },
  { "id": 10, "artistName": "Aino Mäkinen", "songTitle": "Revontulet", "countryName": "Finland", "countryCode": "FIN" },
  { "id": 11, "artistName": "Lily Chen", "songTitle": "Southern Cross", "countryName": "Australia", "countryCode": "AUS" }
]
```

---

### Schritt 2 – api.js (gemeinsame Datei, Person 4 legt an)

**Datei:** `src/main/resources/static/js/api.js`

```javascript
// ← auf false setzen wenn Backend läuft
const USE_MOCK = true;

const BASE_URL = "http://localhost:8080/api";

// Lädt alle Contestants
async function getContestants() {
    if (USE_MOCK) {
        const res = await fetch("./js/mock-contestants.json");
        return res.json();
    }
    const res = await fetch(`${BASE_URL}/contestants`);
    if (!res.ok) throw new Error("Failed to load contestants");
    return res.json();
}

// Gibt ein Vote ab
// ⚠️ kein fromCountryId – Backend ermittelt das selbst
async function castVote(toContestantId, voterRole, points, juryMemberId) {
    // Helper: juryMemberId wird bei AUDIENCE automatisch null gesetzt
    const body = buildVoteRequest(toContestantId, voterRole, points, juryMemberId);

    if (USE_MOCK) {
        console.log("MOCK castVote:", body);
        return {
            id: Math.floor(Math.random() * 1000),
            fromCountryName: "Germany",
            toArtistName: "Mock Artist",
            toSongTitle: "Mock Song",
            toCountryName: "Mock Country",
            voterRole: voterRole,
            points: points,
            juryMemberId: body.juryMemberId,
            createdAt: new Date().toISOString().slice(0, 19)
        };
    }

    const res = await fetch(`${BASE_URL}/votes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message);
    }
    return res.json();
}

// Lädt das Ranking
async function getResults() {
    if (USE_MOCK) {
        const res = await fetch("./js/mock-results.json");
        return res.json();
    }
    const res = await fetch(`${BASE_URL}/results`);
    if (!res.ok) throw new Error("Failed to load results");
    return res.json();
}

// Lädt den Voting-Status
async function getVotingStatus() {
    if (USE_MOCK) {
        return {
            ourCountryId: 1, ourCountryName: "Germany",
            totalRequiredPerSet: 10,
            juryMember1VoteCount: 10, juryMember2VoteCount: 3, juryMember3VoteCount: 0,
            audienceVoteCount: 10,
            juryMember1Complete: true, juryMember2Complete: false,
            juryMember3Complete: false, audienceComplete: true
        };
    }
    const res = await fetch(`${BASE_URL}/votes/status`);
    if (!res.ok) throw new Error("Failed to load status");
    return res.json();
}

// ✅ Helper: stellt sicher dass juryMemberId bei AUDIENCE immer null ist
function buildVoteRequest(toContestantId, voterRole, points, juryMemberId) {
    return {
        toContestantId: toContestantId,
        voterRole: voterRole,
        points: points,
        juryMemberId: voterRole === "JURY" ? juryMemberId : null
    };
}
```

- [ ] `USE_MOCK = true` solange Backend nicht läuft
- [ ] `buildVoteRequest()` immer verwenden – nie manuell das Objekt bauen
- [ ] `castVote()` wirft `Error` mit Server-Fehlermeldung – muss in `voting.js` mit try/catch abgefangen werden

---

### Schritt 3 – index.html

**Datei:** `src/main/resources/static/index.html`

- [ ] Enthält `<script src="./js/api.js"></script>` **vor** `voting.js`
- [ ] Enthält `<script src="./js/voting.js"></script>`
- [ ] Enthält `<link rel="stylesheet" href="./css/style.css">`
- [ ] Formular mit: Dropdown für Contestants (wird per JS befüllt), Punkt-Auswahl (1-8, 10, 12), Rollen-Auswahl (JURY/AUDIENCE), Jury-Member-Auswahl (1-3, nur sichtbar bei JURY)
- [ ] Link zu `results.html`
- [ ] Contestants-Liste filtert `countryCode === "DEU"` (eigenes Land) aus der Auswahl heraus

---

### Schritt 4 – voting.js

**Datei:** `src/main/resources/static/js/voting.js`

- [ ] `getContestants()` beim Laden aufrufen → Dropdown befüllen
- [ ] Beim Submit: `castVote()` mit try/catch aufrufen
- [ ] Bei Erfolg (201): Erfolgsmeldung anzeigen + Formular zurücksetzen
- [ ] Bei Fehler: `error.message` anzeigen (das ist die Server-Fehlermeldung)
- [ ] JURY-Member-Feld ein-/ausblenden je nach gewählter Rolle
- [ ] `getVotingStatus()` aufrufen und Fortschritt anzeigen

---

### Abnahme Person 4

```
[ ] Seite lädt – Contestants erscheinen im Dropdown
[ ] Germany ist NICHT im Dropdown (gefiltert)
[ ] JURY wählen → Member-Feld erscheint
[ ] AUDIENCE wählen → Member-Feld verschwindet
[ ] Vote abschicken → Erfolgsmeldung erscheint
[ ] Ungültigen Punktwert (manuell im Code) → Fehler wird angezeigt
[ ] USE_MOCK = false + Backend läuft → alles funktioniert noch
```

---

---

## Person 5 – Frontend Results

> **Kann sofort starten** – nutzt Mock-Daten. Schaltet auf echtes Backend sobald Person 2 fertig ist.

---

### Schritt 1 – Mock-Daten anlegen

**Datei:** `src/main/resources/static/js/mock-results.json`

```json
[
  { "rank": 1, "contestantId": 2, "artistName": "Celine Marie", "songTitle": "Blue Horizon", "countryName": "France", "countryCode": "FRA", "totalPoints": 387, "juryPoints": 214, "audiencePoints": 173 },
  { "rank": 2, "contestantId": 3, "artistName": "Erik Lindqvist", "songTitle": "Northern Lights", "countryName": "Sweden", "countryCode": "SWE", "totalPoints": 340, "juryPoints": 190, "audiencePoints": 150 },
  { "rank": 3, "contestantId": 4, "artistName": "Sofia Romano", "songTitle": "Bella Notte", "countryName": "Italy", "countryCode": "ITA", "totalPoints": 298, "juryPoints": 165, "audiencePoints": 133 },
  { "rank": 4, "contestantId": 5, "artistName": "Ana Sousa", "songTitle": "Mar Salgado", "countryName": "Portugal", "countryCode": "PRT", "totalPoints": 201, "juryPoints": 120, "audiencePoints": 81 },
  { "rank": 5, "contestantId": 6, "artistName": "Carlos Vega", "songTitle": "Fuego", "countryName": "Spain", "countryCode": "ESP", "totalPoints": 180, "juryPoints": 100, "audiencePoints": 80 },
  { "rank": 6, "contestantId": 7, "artistName": "Ingrid Berg", "songTitle": "Aurora", "countryName": "Norway", "countryCode": "NOR", "totalPoints": 155, "juryPoints": 90, "audiencePoints": 65 },
  { "rank": 7, "contestantId": 8, "artistName": "Olena Koval", "songTitle": "Zoria", "countryName": "Ukraine", "countryCode": "UKR", "totalPoints": 134, "juryPoints": 80, "audiencePoints": 54 },
  { "rank": 8, "contestantId": 9, "artistName": "Nikos Papadopoulos", "songTitle": "Thalassa", "countryName": "Greece", "countryCode": "GRC", "totalPoints": 98, "juryPoints": 55, "audiencePoints": 43 },
  { "rank": 9, "contestantId": 10, "artistName": "Aino Mäkinen", "songTitle": "Revontulet", "countryName": "Finland", "countryCode": "FIN", "totalPoints": 67, "juryPoints": 40, "audiencePoints": 27 },
  { "rank": 10, "contestantId": 11, "artistName": "Lily Chen", "songTitle": "Southern Cross", "countryName": "Australia", "countryCode": "AUS", "totalPoints": 42, "juryPoints": 25, "audiencePoints": 17 },
  { "rank": 11, "contestantId": 1, "artistName": "Max Mustermann", "songTitle": "Fire and Ice", "countryName": "Germany", "countryCode": "DEU", "totalPoints": 0, "juryPoints": 0, "audiencePoints": 0 }
]
```

---

### Schritt 2 – results.html

**Datei:** `src/main/resources/static/results.html`

- [ ] Enthält `<script src="./js/api.js"></script>` **vor** `results.js`
- [ ] Enthält `<script src="./js/results.js"></script>`
- [ ] Enthält `<link rel="stylesheet" href="./css/style.css">`
- [ ] Tabelle mit Spalten: Rang, Land (Code), Künstler, Song, Jury-Punkte, Publikum-Punkte, Gesamt
- [ ] Link zurück zu `index.html`

---

### Schritt 3 – results.js

**Datei:** `src/main/resources/static/js/results.js`

- [ ] `getResults()` beim Laden aufrufen
- [ ] Für jeden Eintrag: `rank`, `countryCode`, `artistName`, `songTitle`, `juryPoints`, `audiencePoints`, `totalPoints` anzeigen
- [ ] Zeile für Platz 1 optisch hervorheben (z.B. CSS-Klasse `winner`)
- [ ] Bei Fehler: Fehlermeldung auf der Seite anzeigen

---

### Abnahme Person 5

```
[ ] Seite lädt – Ranking erscheint mit allen 11 Einträgen
[ ] Platz 1 ist optisch hervorgehoben
[ ] Germany ist im Ranking (letzter Platz mit 0 Punkten im Mock)
[ ] USE_MOCK = false + Backend läuft → alles funktioniert noch
[ ] totalPoints stimmt mit juryPoints + audiencePoints überein (visuell prüfen)
```

---

---

## Gesamtliste – alle Tasks zusammengefasst

### Person 3 (zuerst!) – 13 Dateien

- [ ] `pom.xml`
- [ ] `src/main/resources/application.properties`
- [ ] `EurovotingApplication.java`
- [ ] `model/VoterRole.java`
- [ ] `model/Country.java`
- [ ] `model/Contestant.java`
- [ ] `model/Vote.java`
- [ ] `repository/CountryRepository.java`
- [ ] `repository/ContestantRepository.java`
- [ ] `repository/VoteRepository.java`
- [ ] `dto/ContestantResponse.java`
- [ ] `controller/ContestantController.java`
- [ ] `config/DataInitializer.java`

### Person 1 – 9 Dateien

- [ ] `exception/VotingException.java`
- [ ] `exception/NotFoundException.java`
- [ ] `exception/GlobalExceptionHandler.java`
- [ ] `dto/VoteRequest.java` ← kein fromCountryId!
- [ ] `dto/VoteResponse.java`
- [ ] `dto/VotingStatusResponse.java`
- [ ] `service/VoteService.java`
- [ ] `service/VotingStatusService.java`
- [ ] `controller/VoteController.java`

### Person 2 – 3 Dateien

- [ ] `dto/RankingEntry.java`
- [ ] `service/ResultService.java`
- [ ] `controller/ResultController.java`

### Person 4 – 4 Dateien

- [ ] `static/js/mock-contestants.json`
- [ ] `static/js/api.js`
- [ ] `static/index.html`
- [ ] `static/js/voting.js`

### Person 5 – 3 Dateien

- [ ] `static/js/mock-results.json`
- [ ] `static/results.html`
- [ ] `static/js/results.js`

### Shared – 1 Datei

- [ ] `static/css/style.css` ← Person 4 legt an, Person 5 ergänzt

---

## Finale Ordnerstruktur

```
eurovoting/
│
├── pom.xml                                               [XML]
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── eurovoting/
        │           │
        │           ├── EurovotingApplication.java        [Java]
        │           │
        │           ├── config/
        │           │   └── DataInitializer.java          [Java]
        │           │
        │           ├── controller/
        │           │   ├── ContestantController.java     [Java]  ← Person 3
        │           │   ├── VoteController.java           [Java]  ← Person 1
        │           │   └── ResultController.java         [Java]  ← Person 2
        │           │
        │           ├── service/
        │           │   ├── VoteService.java              [Java]  ← Person 1
        │           │   ├── VotingStatusService.java      [Java]  ← Person 1
        │           │   └── ResultService.java            [Java]  ← Person 2
        │           │
        │           ├── repository/
        │           │   ├── CountryRepository.java        [Java]  ← Person 3
        │           │   ├── ContestantRepository.java     [Java]  ← Person 3
        │           │   └── VoteRepository.java           [Java]  ← Person 3
        │           │
        │           ├── model/
        │           │   ├── Country.java                  [Java]  ← Person 3
        │           │   ├── Contestant.java               [Java]  ← Person 3
        │           │   ├── Vote.java                     [Java]  ← Person 3
        │           │   └── VoterRole.java                [Java]  ← Person 3
        │           │
        │           ├── dto/
        │           │   ├── ContestantResponse.java       [Java]  ← Person 3
        │           │   ├── VoteRequest.java              [Java]  ← Person 1
        │           │   ├── VoteResponse.java             [Java]  ← Person 1
        │           │   ├── VotingStatusResponse.java     [Java]  ← Person 1
        │           │   └── RankingEntry.java             [Java]  ← Person 2
        │           │
        │           └── exception/
        │               ├── VotingException.java          [Java]  ← Person 1
        │               ├── NotFoundException.java        [Java]  ← Person 1
        │               └── GlobalExceptionHandler.java   [Java]  ← Person 1
        │
        └── resources/
            │
            ├── application.properties                    [Properties]  ← Person 3
            │
            └── static/                                   ← Frontend (von Spring Boot serviert)
                │
                ├── index.html                            [HTML]   ← Person 4
                ├── results.html                          [HTML]   ← Person 5
                │
                ├── css/
                │   └── style.css                         [CSS]    ← Person 4+5
                │
                └── js/
                    ├── api.js                            [JS]     ← Person 4
                    ├── voting.js                         [JS]     ← Person 4
                    ├── results.js                        [JS]     ← Person 5
                    ├── mock-contestants.json             [JSON]   ← Person 4
                    └── mock-results.json                 [JSON]   ← Person 5
```

**Gesamt: 29 Dateien · 5 Personen · ~1 Woche**

