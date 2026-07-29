package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.PersonResponse;
import com.virtualtryon.Dto.UploadPersonRequest;
import com.virtualtryon.Entity.Person;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Entity.History;
import com.virtualtryon.Entity.StyleRecommendation;
import com.virtualtryon.Repository.PersonImageRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Repository.HistoryRepository;
import com.virtualtryon.Service.PersonService;
import com.virtualtryon.upload.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

        private final PersonImageRepository personImageRepository;
        private final FileStorageService fileStorageService;
        private final GeneratedImageRepository generatedImageRepository;
        private final HistoryRepository historyRepository;
        private final EntityManager entityManager;

    @Override
    public PersonResponse uploadPerson(UploadPersonRequest request){
        Person person = new Person();
        person.setPersonName(request.getPersonName());
        person.setAge(request.getAge());
        person.setGender(request.getGender());
        person.setUploadedAt(LocalDateTime.now());
        String imagePath = fileStorageService.saveFile(request.getImage());
        person.setImagePath(imagePath);

        person = personImageRepository.save(person);
        return new PersonResponse(
                person.getId(),
                person.getPersonName(),
                person.getAge(),
                person.getGender(),
                person.getImagePath()
        );
    }

    @Override
    public List<PersonResponse> getAllPerson(){
        return personImageRepository.findAll()
                .stream()
                .map(person -> new PersonResponse(
                        person.getId(),
                        person.getPersonName(),
                        person.getAge(),
                        person.getGender(),
                        person.getImagePath()
                ))
                .toList();
    }

    @Override
    public PersonResponse getPersonById(Long id) {

        Person person = personImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        return new PersonResponse(
                person.getId(),
                person.getPersonName(),
                person.getAge(),
                person.getGender(),
                person.getImagePath()
        );
    }



    @Override
    @Transactional
    public void deletePerson(Long id) {
        Person person = personImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        // Clean up dependent resources in GeneratedImage, History, StyleRecommendation, Favorite, AIReport
        List<GeneratedImage> dependentImages = generatedImageRepository.findAll().stream()
                .filter(img -> img.getPerson() != null && img.getPerson().getId().equals(id))
                .toList();

        for (GeneratedImage img : dependentImages) {
            // Delete history entries for this generated image
            List<History> historyEntries = historyRepository.findAll().stream()
                    .filter(h -> h.getGeneratedImage() != null && h.getGeneratedImage().getId() == img.getId())
                    .toList();
            historyRepository.deleteAll(historyEntries);

            // Delete style recommendations for this generated image using EntityManager
            entityManager.createQuery("DELETE FROM StyleRecommendation r WHERE r.generatedImage = :img")
                    .setParameter("img", img)
                    .executeUpdate();

            // Delete the generated image file from disk if it exists
            if (img.getOutputImage() != null) {
                try {
                    fileStorageService.deleteFile(img.getOutputImage());
                } catch (Exception ignored) {}
            }

            // Finally delete the generated image (AIReport and Favorite are cascade deleted)
            generatedImageRepository.delete(img);
        }

        if (person.getImagePath() != null) {
            try {
                fileStorageService.deleteFile(person.getImagePath());
            } catch (Exception ignored) {}
        }

        personImageRepository.delete(person);

    }
}
