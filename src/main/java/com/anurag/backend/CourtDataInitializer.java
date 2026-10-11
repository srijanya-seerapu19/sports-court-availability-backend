package com.anurag.backend;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CourtDataInitializer implements CommandLineRunner {

    private final CourtRepository courtRepository;

    public CourtDataInitializer(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @Override
    public void run(String... args) {
        List<Court> starterCourts = List.of(
            new Court("Badminton Court 1", "Badminton", "Sports Complex"),
            new Court("Badminton Court 2", "Badminton", "Sports Complex"),
            new Court("Basketball Court", "Basketball", "Outdoor Court"),
            new Court("Cricket Ground", "Cricket", "Main Ground"),
            new Court("Football Ground", "Football", "Main Ground"),
            new Court("Tennis Court", "Tennis", "Sports Complex"),
            new Court("Volleyball Court", "Volleyball", "Sports Complex"),
            new Court("Table Tennis Court", "Table Tennis", "Indoor Hall"),

            new Court("Carrom Board 1", "Carrom", "Indoor Recreation Room"),
            new Court("Carrom Board 2", "Carrom", "Indoor Recreation Room"),
            new Court("Carrom Board 3", "Carrom", "Indoor Recreation Room"),
            new Court("Carrom Board 4", "Carrom", "Indoor Recreation Room"),
            new Court("Carrom Board 5", "Carrom", "Indoor Recreation Room"),

            new Court("Chess Board 1", "Chess", "Indoor Recreation Room"),
            new Court("Chess Board 2", "Chess", "Indoor Recreation Room"),
            new Court("Chess Board 3", "Chess", "Indoor Recreation Room"),
            new Court("Chess Board 4", "Chess", "Indoor Recreation Room"),
            new Court("Chess Board 5", "Chess", "Indoor Recreation Room")
        );

        List<String> existingNames = courtRepository.findAll()
            .stream()
            .map(Court::getName)
            .toList();

        List<Court> missingCourts = starterCourts.stream()
            .filter(court -> !existingNames.contains(court.getName()))
            .toList();

        courtRepository.saveAll(missingCourts);
    }
}