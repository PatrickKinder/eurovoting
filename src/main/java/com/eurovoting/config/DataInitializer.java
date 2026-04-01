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
        if (countryRepository.count() > 0) return;

        // ⚠️ KEIN VOTES hier anlegen – nur Länder + Contestants
        Country germany = createCountry("Germany", "DEU", true, false);
        createContestant("Max Mustermann", "Fire and Ice", germany);

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