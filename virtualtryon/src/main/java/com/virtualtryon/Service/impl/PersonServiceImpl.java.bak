package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.PersonResponse;
import com.virtualtryon.Dto.UploadPersonRequest;
import com.virtualtryon.Entity.Person;
import com.virtualtryon.Repository.PersonImageRepository;
import com.virtualtryon.Service.PersonService;
import com.virtualtryon.upload.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

        private final PersonImageRepository personImageRepository;
        private final FileStorageService fileStorageService;

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
    public void deletePerson(Long id) {
        Person person = personImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        fileStorageService.deleteFile(person.getImagePath());

        personImageRepository.delete(person);

    }
}
