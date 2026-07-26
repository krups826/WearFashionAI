package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.GenerateRequest;
import com.virtualtryon.Dto.PythonGenerateRequest;
import com.virtualtryon.Entity.Clothing;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Entity.Person;
import com.virtualtryon.Repository.ClothingRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Repository.PersonImageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeneratedServiceImplTest {

    @Test
    void generateStartsAsyncProcessingAndReturnsProcessingStatus() {
        GeneratedImageRepository generatedImageRepository = mock(GeneratedImageRepository.class);
        ClothingRepository clothingRepository = mock(ClothingRepository.class);
        PersonImageRepository personImageRepository = mock(PersonImageRepository.class);
        AsyncGenerationService asyncGenerationService = mock(AsyncGenerationService.class);

        GeneratedServiceImpl service = new GeneratedServiceImpl(
                generatedImageRepository,
                clothingRepository,
                personImageRepository,
                asyncGenerationService
        );

        Clothing clothing = new Clothing();
        clothing.setId(10L);
        clothing.setImagePath("uploads/clothes/jacket.png");
        clothing.setClothType("JACKET");
        clothing.setClothName("Denim Jacket");
        clothing.setColor("Blue");

        Person person = new Person();
        person.setId(20L);
        person.setImagePath("uploads/persons/model.png");

        when(clothingRepository.findById(10L)).thenReturn(Optional.of(clothing));
        when(personImageRepository.findById(20L)).thenReturn(Optional.of(person));
        when(generatedImageRepository.save(any(GeneratedImage.class))).thenAnswer(invocation -> {
            GeneratedImage generatedImage = invocation.getArgument(0);
            generatedImage.setId(99L);
            generatedImage.setGeneratedAt(LocalDateTime.now());
            return generatedImage;
        });
        doNothing().when(asyncGenerationService).processGeneration(any(), any());

        var response = service.generate(new GenerateRequest(10L, 20L));

        assertEquals(99L, response.id());
        assertEquals("PROCESSING", response.status());
        assertNull(response.outputImage());

        ArgumentCaptor<PythonGenerateRequest> captor =
                ArgumentCaptor.forClass(PythonGenerateRequest.class);
        verify(asyncGenerationService).processGeneration(
                org.mockito.ArgumentMatchers.eq(99L),
                captor.capture()
        );

        PythonGenerateRequest payload = captor.getValue();
        assertEquals("uploads/persons/model.png", payload.personPath());
        assertEquals(clothing.getImagePath(), payload.materialPath());
        assertEquals("JACKET", payload.clothType());
    }
}
