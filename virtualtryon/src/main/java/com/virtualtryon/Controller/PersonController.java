package com.virtualtryon.Controller;

import com.virtualtryon.Dto.PersonResponse;
import com.virtualtryon.Dto.UploadPersonRequest;
import com.virtualtryon.Service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping("/upload")
    public ResponseEntity<PersonResponse> uploadPerson(@ModelAttribute UploadPersonRequest request){
        return ResponseEntity.ok(personService.uploadPerson(request));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> getAllPerson() {
        return ResponseEntity.ok(personService.getAllPerson());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(personService.getPersonById(id));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        personService.deletePerson(id);

        return ResponseEntity.ok("Person deleted successfully.");

    }
}
